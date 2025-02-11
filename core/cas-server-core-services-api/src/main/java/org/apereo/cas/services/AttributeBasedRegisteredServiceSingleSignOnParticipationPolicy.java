package org.apereo.cas.services;

import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.util.RegexUtils;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import java.io.Serial;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link AttributeBasedRegisteredServiceSingleSignOnParticipationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode
@Accessors(chain = true)
@RequiredArgsConstructor
@Slf4j
public class AttributeBasedRegisteredServiceSingleSignOnParticipationPolicy implements RegisteredServiceSingleSignOnParticipationPolicy {
    @Serial
    private static final long serialVersionUID = -1223946898337761319L;

    private Map<String, List<String>> attributes = new HashMap<>();

    private boolean requireAllAttributes;

    @Override
    public boolean shouldParticipateInSso(final RegisteredService registeredService, final AuthenticationAwareTicket ticketState) {
        val authentication = ticketState.getAuthentication();
        val participation = doPrincipalAttributesAllowParticipationInSso(authentication.getPrincipal().getAttributes())
            || doPrincipalAttributesAllowParticipationInSso(authentication.getAttributes());

        if (!participation) {
            LOGGER.debug("""
                No SSO participation is allowed since none of the defined attributes [{}] match authentication/principal attributes
                """.stripIndent(), attributes);
            return false;
        }
        return true;
    }

    protected boolean doPrincipalAttributesAllowParticipationInSso(
        final Map<String, List<Object>> givenAttributes) {
        LOGGER.debug("Attributes examined for SSO participation are [{}]", givenAttributes);
        if (requireAllAttributes) {
            return attributes.entrySet().stream().allMatch(entry -> examineAttributeValues(givenAttributes, entry));
        }
        return attributes.entrySet().stream().anyMatch(entry -> examineAttributeValues(givenAttributes, entry));
    }

    private static boolean examineAttributeValues(final Map<String, List<Object>> givenAttributes,
                                                  final Map.Entry<String, List<String>> entry) {
        val attributeValues = givenAttributes.getOrDefault(entry.getKey(), List.of());
        return entry
            .getValue()
            .stream()
            .anyMatch(pattern -> attributeValues.stream().anyMatch(attrValue -> {
                val attributeValue = attrValue.toString();
                LOGGER.trace("Comparing [{}] against pattern [{}]", attributeValue, pattern);
                return RegexUtils.find(pattern, attributeValue);
            }));
    }
}
