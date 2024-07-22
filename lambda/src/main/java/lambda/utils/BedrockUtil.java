package lambda.utils;

import dev.langchain4j.model.input.PromptTemplate;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

import java.util.Map;

public abstract class BedrockUtil {
    protected final BedrockRuntimeClient client;
    protected PromptTemplate promptTemplate;

    public BedrockUtil() {
        client = BedrockRuntimeClient.builder()
                .region(Region.US_EAST_1)
                .build();
    }

    public abstract String generateEmailReply(String mail, String instructions);

    protected String generatePrompt(String mail, String instructions) {
        Map<String, Object> variables = Map.of(
                "mail", mail,
                "instructions", instructions
        );

        return promptTemplate.apply(variables).text();
    }
}