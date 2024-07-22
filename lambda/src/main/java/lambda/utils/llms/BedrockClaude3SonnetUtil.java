package lambda.utils.llms;

import dev.langchain4j.model.input.PromptTemplate;
import lambda.utils.BedrockUtil;
import lambda.utils.prompts.Claude3Prompts;
import org.json.JSONObject;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

public class BedrockClaude3SonnetUtil extends BedrockUtil {
    private static final String MODEL_ID = "anthropic.claude-3-sonnet-20240229-v1:0";
    private static final String ANTHROPIC_VERSION = "bedrock-2023-05-31";

    private static final int MAX_TOKENS_TO_SAMPLE = 2048;
    private static final double TEMPERATURE = 0.2;
    private static final int TOP_K = 50;
    private static final String SYSTEM_PROMPT = """
                You are a chat bot that assists business users in replying to emails.
                Your task is to be friendly, concise and factually correct.
                When drafting the response make sure to adhere to the instructions given in the <instructions> tag.
                Write the mail using proper html syntax.
                Answer in the same language as the email in the <email> tag.
                If the email is on a first name basis answer on a first name basis and in the same tone.
                Put my name to the kind regards at the end of my email.
                Answer immediately and skip the preamble.
                """;

    public BedrockClaude3SonnetUtil() {
        super();
        promptTemplate = PromptTemplate.from(Claude3Prompts.SONNET);
    }

    public String generateEmailReply(String mail, String instructions) {
        String prompt = generatePrompt(mail,instructions);

        var payload = new JSONObject()
                .put("anthropic_version", ANTHROPIC_VERSION)
                .put("max_tokens", MAX_TOKENS_TO_SAMPLE)
                .put("top_k", TOP_K)
                .put("temperature", TEMPERATURE)
                .put("system", SYSTEM_PROMPT)
                .append("messages", new JSONObject()
                        .put("role", "user")
                        .append("content", new JSONObject()
                                .put("type", "text")
                                .put("text", prompt)
                        ));

        InvokeModelRequest request = InvokeModelRequest.builder()
                .body(SdkBytes.fromUtf8String(payload.toString()))
                .modelId(MODEL_ID)
                .contentType("application/json")
                .accept("application/json")
                .build();

        InvokeModelResponse response = client.invokeModel(request);
        JSONObject responseBody = new JSONObject(response.body().asUtf8String());

        return responseBody
                .getJSONArray("content")
                .getJSONObject(0)
                .getString("text");
    }
}
