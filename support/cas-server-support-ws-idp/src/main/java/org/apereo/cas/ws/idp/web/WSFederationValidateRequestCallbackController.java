package org.apereo.cas.ws.idp.web;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.wsfed.WsFederationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ws.idp.WSFederationConstants;
import org.apereo.cas.ws.idp.IdentityProviderConfigurationService;
import org.apereo.cas.ws.idp.RealmAwareIdentityProvider;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas30ServiceTicketValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link WSFederationValidateRequestCallbackController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class WSFederationValidateRequestCallbackController extends BaseWSFederationRequestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(WSFederationValidateRequestCallbackController.class);

    public WSFederationValidateRequestCallbackController(final IdentityProviderConfigurationService identityProviderConfigurationService,
                                                         final ServicesManager servicesManager,
                                                         final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
                                                         final CasConfigurationProperties casProperties) {
        super(identityProviderConfigurationService, servicesManager,
                webApplicationServiceFactory, casProperties);
    }

    /**
     * Handle federation request.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @GetMapping(path = WSFederationConstants.ENDPOINT_FEDERATION_REQUEST_CALLBACK)
    protected void handleFederationRequest(final HttpServletResponse response, final HttpServletRequest request) throws Exception {
        final WSFederationRequest fedRequest = WSFederationRequest.of(request);
        final WsFederationProperties wsfed = casProperties.getAuthn().getWsfedIdP();
        final RealmAwareIdentityProvider idp = this.identityProviderConfigurationService.getIdentityProvider(wsfed.getIdp().getRealm());

        LOGGER.info("Received callback profile request [{}]", request.getRequestURI());
        final String ticket = CommonUtils.safeGetParameter(request, CasProtocolConstants.PARAMETER_TICKET);
        if (StringUtils.isBlank(ticket)) {
            LOGGER.error("Can not validate the request because no [{}] is provided via the request", CasProtocolConstants.PARAMETER_TICKET);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        final Assertion assertion = validateRequestAndBuildCasAssertion(response, request, fedRequest);
    }
    
    private Assertion validateRequestAndBuildCasAssertion(final HttpServletResponse response,
                                                          final HttpServletRequest request,
                                                          final WSFederationRequest fedRequest) throws Exception {
        final String ticket = CommonUtils.safeGetParameter(request, CasProtocolConstants.PARAMETER_TICKET);
        final Cas30ServiceTicketValidator validator = new Cas30ServiceTicketValidator(casProperties.getServer().getPrefix());
        final String serviceUrl = constructServiceUrl(request, response, fedRequest);
        LOGGER.debug("Created service url for validation: [{}]", serviceUrl);
        final Assertion assertion = validator.validate(ticket, serviceUrl);
        LOGGER.debug("Located CAS assertion [{}]", assertion);
        return assertion;
    }

}
