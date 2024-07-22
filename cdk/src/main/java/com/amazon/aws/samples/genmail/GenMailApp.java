package com.amazon.aws.samples.genmail;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.util.Map;

public class GenMailApp {
    static Environment DEV = Environment.builder()
            .region("eu-central-1")
            .account("414184173441").build();

    static Environment PROD = Environment.builder()
            .region("us-east-1")
            .account("414184173441").build();

    public static void main(final String[] args) {
        App app = new App();

        var devStackProps = StackProps.builder()
                .env(DEV)
                .stackName("JavaMagazinGenMailDevEnvironment")
                .description("The Stack for the DEV environment of GenMail. env=DEV,account=" + DEV.getAccount() + ",region=" + DEV.getRegion() + ",uuid=(uksb-bks3lhbujl) (tag: dev)")
                .tags(Map.of(
                        "project", "GenMail",
                        "source", "https://github.com/aws-samples/gen-ai-email-assistant-java-magazin",
                        "env", "DEV"
                ))
                .build();

        var prodStackProps = StackProps.builder()
                .env(PROD)
                .stackName("JavaMagazinGenMailProdEnvironment")
                .description("The Stack for the DEV environment of GenMail. env=PROD,account=" + PROD.getAccount() + ",region=" + PROD.getRegion() + ",uuid=(uksb-bks3lhbujl) (tag: prod)")
                .tags(Map.of(
                        "project", "GenMail",
                        "source", "https://github.com/aws-samples/gen-ai-email-assistant-java-magazin",
                        "env", "PROD"
                ))
                .build();

        new GenMailStack(app, "JavaMagazinGenMailDev", devStackProps, true);
        new GenMailStack(app, "JavaMagazinGenMailProd", prodStackProps, false);

        app.synth();
    }
}
