package org.apereo.cas.integration.pac4j.authentication.handler.support;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.ClientCustomPropertyConstants;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.jee.context.session.JEESessionStore;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.security.auth.login.FailedLoginException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link UsernamePasswordWrapperAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("AuthenticationHandler")
class UsernamePasswordWrapperAuthenticationHandlerTests {

    @Test
    void verifyTypes() {
        val handler = new UsernamePasswordWrapperAuthenticationHandler("Handler1", mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), 0, JEESessionStore.INSTANCE);
        assertNotNull(handler.getCasCredentialsType());
        assertNotNull(handler.supports(UsernamePasswordCredential.class));
    }

    @Test
    void verifyAuthWithNoContext() {
        val handler = new UsernamePasswordWrapperAuthenticationHandler("Handler1", mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), 0, JEESessionStore.INSTANCE);
        assertThrows(FailedLoginException.class,
            () -> handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"), mock(Service.class)));
    }

    @Test
    void verifyAuthWithNoPrincipalAttr() throws Exception {
        val request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));
        val handler = new UsernamePasswordWrapperAuthenticationHandler("Handler1", mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), 0, JEESessionStore.INSTANCE);
        handler.setPrincipalAttributeId("givenName");
        val result = handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"), mock(Service.class));
        assertEquals("casuser", result.getPrincipal().getId());
    }

    @Test
    void verifyAuthWithWithPrincipalAttr() throws Exception {
        val request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));
        val handler = new UsernamePasswordWrapperAuthenticationHandler("Handler1", mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), 0, JEESessionStore.INSTANCE);
        handler.setProfileCreator((callContext, credentials) -> {
            val profile = credentials.getUserProfile();
            profile.addAttribute("givenName", List.of("cas-person"));
            return Optional.of(profile);
        });
        handler.setPrincipalAttributeId("givenName");
        val result = handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"), mock(Service.class));
        assertEquals("cas-person", result.getPrincipal().getId());
    }

    @Test
    void verifyAuthWithWithPrincipalAttrTyped() throws Exception {
        val request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));
        val handler = new UsernamePasswordWrapperAuthenticationHandler("Handler1", mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), 0, JEESessionStore.INSTANCE);
        handler.setProfileCreator((callContext, credentials) -> {
            val profile = credentials.getUserProfile();
            profile.addAttribute("givenName", List.of("cas-person"));
            return Optional.of(profile);
        });
        handler.setTypedIdUsed(true);
        handler.setPrincipalAttributeId("givenName");
        val result = handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"), mock(Service.class));
        assertEquals("org.pac4j.core.profile.CommonProfile#cas-person", result.getPrincipal().getId());
    }

    @Test
    void verifyAuthWithWithClientPrincipalAttrTyped() throws Exception {
        val request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));
        val handler = new UsernamePasswordWrapperAuthenticationHandler("Handler1", mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), 0, JEESessionStore.INSTANCE) {
            @Override
            protected String determinePrincipalIdFrom(final UserProfile profile, final BaseClient client) {
                val mockClient = mock(BaseClient.class);
                when(mockClient.getCustomProperties()).thenReturn(
                    Map.of(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_PRINCIPAL_ATTRIBUTE_ID, "givenName"));
                return super.determinePrincipalIdFrom(profile, mockClient);
            }
        };
        handler.setProfileCreator((callContext, credentials) -> {
            val profile = credentials.getUserProfile();
            profile.addAttribute("givenName", List.of("cas-person"));
            return Optional.of(profile);
        });
        handler.setTypedIdUsed(true);

        val result = handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"), mock(Service.class));
        assertEquals("org.pac4j.core.profile.CommonProfile#cas-person", result.getPrincipal().getId());
    }

}
