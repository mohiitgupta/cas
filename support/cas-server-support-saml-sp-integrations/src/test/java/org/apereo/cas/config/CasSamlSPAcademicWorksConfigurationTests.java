package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPAcademicWorksConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLServiceProvider")
@TestPropertySource(properties = {
    "cas.saml-sp.academic-works.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.academic-works.name-id-attribute=cn",
    "cas.saml-sp.academic-works.name-id-format=transient"
})
class CasSamlSPAcademicWorksConfigurationTests extends BaseCasSamlSPConfigurationTests {

}
