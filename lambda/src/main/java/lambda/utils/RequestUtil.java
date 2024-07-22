package lambda.utils;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.google.gson.Gson;
import com.google.gson.JsonObject;



public class RequestUtil {
    public static final Map<String, String> CORS_HEADERS = Map.of(
            "Content-Type", "application/json",
            "Access-Control-Allow-Origin", "*",
            "Access-Control-Allow-Methods", "OPTIONS,GET,POST"
    );

    private final APIGatewayProxyRequestEvent event;
    private final String instructions;
    private final String mail;

    private RequestUtil(APIGatewayProxyRequestEvent proxyEvent) {
        event = proxyEvent;

        JsonObject body = new Gson().fromJson(event.getBody(), JsonObject.class);

        if(!body.get("mail").isJsonPrimitive() || !body.get("instructions").isJsonPrimitive()) {
            throw new RuntimeException("Malformed body");
        }

        mail = body.get("mail").getAsString();
        instructions = body.get("instructions").getAsString();
    }

    public static RequestUtil fromEvent(APIGatewayProxyRequestEvent proxyEvent) {
        return new RequestUtil(proxyEvent);
    }

    public String getInstructions() {
        return instructions;
    }

    public String getMail() {
        return mail;
    }
}

