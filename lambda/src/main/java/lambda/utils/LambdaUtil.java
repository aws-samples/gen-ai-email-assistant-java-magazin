package lambda.utils;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class LambdaUtil {

    public static String makeErrorMessageJson(String errorMessage) {
        try {
            return new ObjectMapper().writeValueAsString(Map.of("error", errorMessage));
        } catch (JsonProcessingException e) {
            return "Unknown error";
        }
    }


    public static String makeBodyForSuccessfulRequest(String llmResponse) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(Map.of("response", llmResponse));
    }


    public static APIGatewayProxyResponseEvent makeApiGatewayProxyResponse(int statusCode, String body) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(RequestUtil.CORS_HEADERS)
                .withBody(body);
    }
}
