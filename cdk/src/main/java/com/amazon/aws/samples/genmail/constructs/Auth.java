package com.amazon.aws.samples.genmail.constructs;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Fn;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.cognito.*;
import software.constructs.Construct;

import java.util.List;

public class Auth extends Construct {
    public UserPool userPool;
    public UserPoolDomain userPoolDomain;

    public Auth(final Construct scope, final String id, final String apiDomain, final boolean isDev) {
        super(scope, id);

        userPool = UserPool.Builder.create(this, "UserPool")
                .selfSignUpEnabled(true)
                .signInCaseSensitive(false)
                .userPoolName("JavaMagazinGenMailUserpool")
                .signInAliases(SignInAliases.builder()
                        .username(false)
                        .email(true)
                        .build())
                .advancedSecurityMode(AdvancedSecurityMode.ENFORCED)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();


        var cognitoDomainPrefix = (isDev ? "genmail-test-" : "genmail-") + Stack.of(this).getAccount();
        this.userPoolDomain = userPool.addDomain("CognitoCustomDomain",
                UserPoolDomainOptions.builder()
                        .cognitoDomain(CognitoDomainOptions.builder().domainPrefix(cognitoDomainPrefix).build())
                        .build());


        var authRedirectUri = isDev ? "https://localhost:3000/auth.html" : "https://" + apiDomain + "auth.html";

        var client = userPool.addClient("JavaMagazinGenMailClient", UserPoolClientOptions.builder()
                .authFlows(AuthFlow.builder().userSrp(true).build())
                .userPoolClientName("JavaMagazinGenMailClient")
                .oAuth(OAuthSettings.builder()
                        .callbackUrls(List.of(authRedirectUri))
                        .logoutUrls(List.of(authRedirectUri))
                        .flows(OAuthFlows.builder()
                                .authorizationCodeGrant(true)
                                .build())
                        .build())
                .build());

        CfnOutput.Builder
                .create(this, "CognitoBaseUrl")
                .key("CognitoBaseUrl")
                .value(userPoolDomain.baseUrl())
                .build();

        CfnOutput.Builder
                .create(this, "CognitoClientId")
                .key("CognitoClientId")
                .value(client.getUserPoolClientId())
                .build();


        CfnOutput.Builder
                .create(this, "CognitoSignInUrl")
                .key("CognitoSignInUrl")
                .value(userPoolDomain.signInUrl(client,
                        SignInUrlOptions
                                .builder()
                                .redirectUri(authRedirectUri)
                                .build())
                )
                .build();
    }
}