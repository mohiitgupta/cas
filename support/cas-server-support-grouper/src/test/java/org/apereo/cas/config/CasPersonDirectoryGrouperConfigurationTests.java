package org.apereo.cas.config;

import org.apereo.cas.BaseGrouperConfigurationTests;
import org.apereo.cas.util.spring.beans.BeanContainer;

import org.apereo.services.persondir.IPersonAttributeDao;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasPersonDirectoryGrouperConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Grouper")
@SpringBootTest(classes = BaseGrouperConfigurationTests.SharedTestConfiguration.class,
    properties = "cas.authn.attribute-repository.grouper.state=ACTIVE")
class CasPersonDirectoryGrouperConfigurationTests {
    @Autowired
    @Qualifier("grouperAttributeRepositories")
    private BeanContainer<IPersonAttributeDao> grouperAttributeRepositories;

    @Test
    void verifyOperation() {
        assertNotNull(grouperAttributeRepositories);
    }

}
