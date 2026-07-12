package dev.loat.msmp_console.msmp.endpoints.send;

import dev.loat.msmp.MSMPNamespace;


public class Send {
    
    private Send() {}

    public static void register(MSMPNamespace namespace) {
        namespace.method("send")
            .description("Send a command to the server console")
            .requestSchema(SendRequest.SCHEMA)
            .responseSchema(SendResponse.SCHEMA)
            .register((server, client, params) -> {
                String command = params.command();

                // execute command on server, evaluate if successful and return response
                
                return new SendResponse(command, "", false);
            });
    }
}
