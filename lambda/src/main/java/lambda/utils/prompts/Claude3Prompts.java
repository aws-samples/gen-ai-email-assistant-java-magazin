package lambda.utils.prompts;

public class Claude3Prompts {
    public static final String SONNET = """
        <xml>
            Here is an example:
        
            <example>
                <email>Hi Johannes, when is our meeting scheduled on Friday? Best regards, Martin</email>
                <instructions> Tell him the meeting starts ar 10 a.m. CET</instructions>
                <response>
                    <html><body>
                        <p>Hello Martin,</p>
                        <p>thanks for reaching out. the meeting starts at 10 a.m. CET on Friday. Looking forward to talking to you!</p>
        
                        <p>Best regards,<br>Johannes</p>
                    </body></html>
                </response>
            </example>
        
            Here is the email:
        
            <email>
                {{mail}}
            </email>
            <instructions>
                {{instructions}}
            </instructions>
        </xml>
        """;
}