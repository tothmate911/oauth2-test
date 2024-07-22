package com.reg.sun.oauth2_test.test;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TokenGetter {

    private final ConfidentialClientApplication application;
    private final ClientCredentialParameters parameters;

    public TokenGetter(
            @Value("${tenant.id}") String tenantId,
            @Value("${client.id}") String clientId,
            @Value("${client.secret}") String clientSecret,
            @Value("${authorization.scope}") String scope
    ) throws MalformedURLException {
        var oauth2TokenEndpoint = "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token";
        var clientCredential = ClientCredentialFactory.createFromSecret(clientSecret);
        application = ConfidentialClientApplication.builder(clientId, clientCredential)
                .authority(oauth2TokenEndpoint)
                .build();

        parameters = ClientCredentialParameters.builder(Collections.singleton(scope)).build();
    }

    public Optional<String> getToken() {
        try {
            // In case of client credential flow, MSAL takes care of token caching, so the token shouldn't be acquired silently
            var authResultFuture = application.acquireToken(parameters);
            var authResult = authResultFuture.get();
            return Optional.of(authResult.accessToken());
        } catch (ExecutionException | InterruptedException e) {
            log.error("Exception while acquiring token", e);
            return Optional.empty();
        }
    }

}
