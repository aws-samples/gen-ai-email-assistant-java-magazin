package lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import lambda.utils.llms.BedrockClaude3SonnetUtil;
import lambda.utils.BedrockUtil;
import lambda.utils.RequestUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.lambda.powertools.logging.Logging;

import static lambda.utils.LambdaUtil.*;

public class LambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final BedrockUtil llmUtil;

    public LambdaHandler() {
        llmUtil = new BedrockClaude3SonnetUtil();
    }

    Logger log = LogManager.getLogger(LambdaHandler.class);

    @Logging(logEvent = true)
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
            log.info("Request retrieved by handler");
            var requestUtil = RequestUtil.fromEvent(event);

            var mail = requestUtil.getMail();
            var instructions = requestUtil.getInstructions();
            var llmResponse = llmUtil.generateEmailReply(mail, instructions);
            log.info("Response generated");
            return makeApiGatewayProxyResponse(200, makeBodyForSuccessfulRequest(llmResponse));
        } catch (Exception e) {
            log.error(e);
            return makeApiGatewayProxyResponse(400, makeErrorMessageJson(e.getMessage()));
        }
    }
}

