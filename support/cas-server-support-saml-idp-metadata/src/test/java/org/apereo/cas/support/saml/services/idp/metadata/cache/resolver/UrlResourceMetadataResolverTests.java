package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.services.BaseSamlIdPServicesTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.MockWebServer;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UrlResourceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("SAMLMetadata")
@TestPropertySource(properties = "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/saml")
class UrlResourceMetadataResolverTests extends BaseSamlIdPServicesTests {
    public static final String MDQ_URL = "https://mdq.incommon.org/entities/{0}";
    
    
    @Test
    void verifyResolverSupports() throws Exception {
        try (val webServer = new MockWebServer(9155, new ClassPathResource("sample-metadata.xml"), HttpStatus.OK)) {
            webServer.start();
            val props = new SamlIdPProperties();
            props.getMetadata().getFileSystem().setLocation(new FileSystemResource(FileUtils.getTempDirectory()).getFile().getCanonicalPath());
            val resolver = new UrlResourceMetadataResolver(httpClient, props, openSamlConfigBean);
            val service = new SamlRegisteredService();
            service.setMetadataLocation("http://localhost:9155");
            assertTrue(resolver.supports(service));
            service.setMetadataLocation("classpath:sample-sp.xml");
            assertFalse(resolver.supports(service));
            service.setMetadataLocation(MDQ_URL);
            assertFalse(resolver.supports(service));
        }
    }

    @Test
    void verifyResolverFromBackup() throws Exception {
        val service = new SamlRegisteredService();
        service.setName("TestShib");
        service.setId(1000);
        service.setMetadataLocation("http://localhost:9155");

        try (val webServer = new MockWebServer(9155, new ClassPathResource("sample-metadata.xml"), HttpStatus.OK)) {
            webServer.start();
            val props = new SamlIdPProperties();
            props.getMetadata().getFileSystem().setLocation(new FileSystemResource(FileUtils.getTempDirectory()).getFile().getCanonicalPath());
            props.getMetadata().getHttp().setForceMetadataRefresh(true);
            val resolver = new UrlResourceMetadataResolver(httpClient, props, openSamlConfigBean);
            val results = resolver.resolve(service);
            assertFalse(results.isEmpty());
        }

        val props = new SamlIdPProperties();
        props.getMetadata().getFileSystem().setLocation(new FileSystemResource(FileUtils.getTempDirectory()).getFile().getCanonicalPath());
        props.getMetadata().getHttp().setForceMetadataRefresh(false);
        val resolver = new UrlResourceMetadataResolver(httpClient, props, openSamlConfigBean);
        val results = resolver.resolve(service);
        assertFalse(results.isEmpty());

        val backupFile = resolver.getMetadataBackupFile(new UrlResource(service.getMetadataLocation()), service);
        FileUtils.writeByteArrayToFile(backupFile, UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        try (val webServer = new MockWebServer(9155, new ClassPathResource("sample-metadata.xml"), HttpStatus.OK)) {
            webServer.start();
            val finalResults = resolver.resolve(service);
            assertFalse(finalResults.isEmpty());
        }

        FileUtils.writeByteArrayToFile(backupFile, new ClassPathResource("metadata-invalid.xml").getInputStream().readAllBytes());
        try (val webServer = new MockWebServer(9155, new ClassPathResource("sample-metadata.xml"), HttpStatus.OK)) {
            webServer.start();
            val finalResults = resolver.resolve(service);
            assertFalse(finalResults.isEmpty());
        }
    }

    @Test
    void verifyResolverResolves() throws Exception {
        try (val webServer = new MockWebServer(9155, new ClassPathResource("sample-metadata.xml"), HttpStatus.OK)) {
            webServer.start();
            val props = new SamlIdPProperties();
            props.getMetadata().getFileSystem().setLocation(new FileSystemResource(FileUtils.getTempDirectory()).getFile().getCanonicalPath());
            val service = new SamlRegisteredService();
            val resolver = new UrlResourceMetadataResolver(httpClient, props, openSamlConfigBean);
            service.setName("TestShib");
            service.setId(1000);
            service.setMetadataLocation("http://localhost:9155");
            val results = resolver.resolve(service);
            assertFalse(results.isEmpty());
            assertTrue(resolver.isAvailable(service));
            assertFalse(resolver.supports(null));
        }
    }

    @Test
    void verifyResolverResolvesFailsAccess() throws Exception {
        try (val webServer = new MockWebServer(9155, new ClassPathResource("sample-metadata.xml"), HttpStatus.OK)) {
            webServer.start();
            val props = new SamlIdPProperties();
            props.getMetadata().getFileSystem().setLocation(new FileSystemResource(FileUtils.getTempDirectory()).getFile().getCanonicalPath());
            val service = new SamlRegisteredService();
            service.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(false, false));
            val resolver = new UrlResourceMetadataResolver(httpClient, props, openSamlConfigBean);
            service.setName("TestShib");
            service.setId(1000);
            service.setMetadataLocation("http://localhost:9155");
            assertThrows(SamlException.class, () -> resolver.resolve(service));
        }
    }

    @Test
    void verifyResolverUnknownUrl() throws Exception {
        val props = new SamlIdPProperties();
        props.getMetadata().getFileSystem().setLocation(new FileSystemResource(FileUtils.getTempDirectory()).getFile().getCanonicalPath());
        val service = new SamlRegisteredService();
        val resolver = new UrlResourceMetadataResolver(httpClient, props, openSamlConfigBean);
        service.setName("TestShib");
        service.setId(1000);
        service.setMetadataLocation("https://localhost:9999");
        assertTrue(resolver.resolve(service).isEmpty());
    }

    @Test
    void verifyResolverWithProtocol() throws Exception {
        try (val webServer = new MockWebServer(9155, new ClassPathResource("sample-metadata.xml"), HttpStatus.OK)) {
            webServer.start();
            val props = new SamlIdPProperties();
            props.getMetadata().getFileSystem().setLocation("file:/" + FileUtils.getTempDirectory());
            val service = new SamlRegisteredService();
            val resolver = new UrlResourceMetadataResolver(httpClient, props, openSamlConfigBean);
            service.setName("TestShib");
            service.setId(1000);
            service.setMetadataLocation("http://localhost:9155");
            val results = resolver.resolve(service);
            assertFalse(results.isEmpty());
        }
    }
}
