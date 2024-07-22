package com.amazon.aws.samples.genmail;

import com.amazon.aws.samples.genmail.constructs.Auth;
import software.amazon.awscdk.*;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.apigateway.Resource;
import software.amazon.awscdk.services.cloudfront.BehaviorOptions;
import software.amazon.awscdk.services.cloudfront.Distribution;
import software.amazon.awscdk.services.cloudfront.origins.S3Origin;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.s3.BlockPublicAccess;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketEncryption;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

public class GenMailStack extends Stack {
    public GenMailStack(Construct scope, String id, StackProps props, boolean isDev) {
        super(scope, id, props);

        if (!isDev) {
            Bucket pluginWebBundleBucket = Bucket.Builder.create(this, "Bucket")
                    .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
                    .encryption(BucketEncryption.S3_MANAGED)
                    .versioned(true)
                    .removalPolicy(RemovalPolicy.DESTROY)
                    .autoDeleteObjects(true)
                    .bucketName(this.getAccount() + "." + this.getRegion() + "." + "genmail.web-dist")
                    .build();

            Distribution dist = Distribution.Builder.create(this, "Distribution")
                    .defaultBehavior(BehaviorOptions.builder()
                            .origin(new S3Origin(pluginWebBundleBucket))
                            .build())
                    .build();

            CfnOutput.Builder
                    .create(this, "CdnUrl")
                    .key("CdnUrl")
                    .value("https://" + dist.getDomainName())
                    .build();

            CfnOutput.Builder
                    .create(this, "BucketName")
                    .key("BucketName")
                    .value(pluginWebBundleBucket.getBucketName())
                    .build();
        }

        CorsOptions corsOptions = CorsOptions.builder()
                .allowOrigins(Cors.ALL_ORIGINS)
                .allowMethods(Cors.ALL_METHODS)
                .allowHeaders(List.of("Content-Type", "User-Agent", "Authorization"))
                .build();

        RestApi api = RestApi.Builder
                .create(this, "ApiGateway")
                .restApiName("GenMailApi")
                .description("The API used by the GenMail Outlook Add-In")
                .defaultCorsPreflightOptions(corsOptions)
                .build();

        var corsFor401 = GatewayResponseOptions.builder()
                .responseHeaders(Map.of("Access-Control-Allow-Origin", "'*'")).type(ResponseType.UNAUTHORIZED)
                .templates(Map.of("application/json", "{}"))
                .build();

        api.addGatewayResponse("CorsFor401", corsFor401);

        Function handlerFn = Function.Builder.create(this, "BedrockRequestHandlerFn")
                .code(Code.fromAsset("../lambda/target/latest.jar"))
                .runtime(Runtime.JAVA_17)
                .functionName("GenMailHandler")
                .description("Asks Bedrock for a reply to an email")
                .timeout(Duration.minutes(5))
                .memorySize(5000)
                .handler("lambda.LambdaHandler")
                .logRetention(RetentionDays.ONE_MONTH)
                .build();

        Auth authStack = new Auth(
                this,
                "Authentication", api.getUrl(), isDev);


        CognitoUserPoolsAuthorizer authorizer = CognitoUserPoolsAuthorizer.Builder
                .create(this, "Authorizer")
                .cognitoUserPools(List.of(authStack.userPool))
                .build();

        Resource response = api
                .getRoot()
                .addResource("response");

        // For debugging purposes, an unauthenticated endpoint is created in the DEV environment
        if (isDev) {
            response
                    .addResource("noauth")
                    .addMethod("POST", new LambdaIntegration(handlerFn));
        }

        response
            .addMethod(
                "POST",
                    new LambdaIntegration(handlerFn),
                    MethodOptions
                        .builder()
                        .authorizer(authorizer)
                        .authorizationType(AuthorizationType.COGNITO)
                        .build()
            );

        handlerFn.addToRolePolicy(PolicyStatement.Builder.create()
                .actions(List.of("bedrock:InvokeModel", "bedrock:InvokeModelWithResponseStream"))
                .resources(
                        List.of("arn:aws:bedrock:*::foundation-model/anthropic.*")
                )
                .effect(Effect.ALLOW)
                .build());

        CfnOutput.Builder
                .create(this, "ApiEndpoint")
                .key("ApiEndpoint")
                .value(api.urlForPath("/response"))
                .build();
    }
}
