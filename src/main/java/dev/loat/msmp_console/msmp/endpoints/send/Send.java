package dev.loat.msmp_console.msmp.endpoints.send;


import dev.loat.msmp.MSMPNamespace;
import dev.loat.msmp_console.config.Config;
import dev.loat.msmp_console.logging.Logger;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.PermissionSet;


/**
 * Registers the {@code console:send} MSMP method.
 *
 * <p>Executes an arbitrary command as if typed by an operator (permission level 4) and
 * returns its textual feedback/error output together with a success indicator.</p>
 *
 * <p>The command is executed via {@link net.minecraft.commands.Commands#performPrefixedCommand},
 * which strips a leading {@code /} if present - so {@code command} may be sent either with
 * or without the prefix.</p>
 */
public class Send {
    
    private Send() {}

    public static void register(MSMPNamespace namespace) {
        namespace.method("send")
            .description("Send a command to the server console")
            .requestSchema(SendRequest.SCHEMA)
            .responseSchema(SendResponse.SCHEMA)
            .register((server, client, params) -> {
                String command = params.command();

                CapturingCommandSource capturingSource = new CapturingCommandSource();

                boolean[] successHolder = {false};

                CommandSourceStack source = server.createCommandSourceStack()
                    .withSource(capturingSource)
                    .withPermission(PermissionSet.ALL_PERMISSIONS)
                    .withCallback((success, result) -> successHolder[0] = success);

                try {
                    server.getCommands().performPrefixedCommand(source, command);
                } catch (Exception e) {
                    Logger.warning("console:send - failed to execute '%s': %s".formatted(command, e.getMessage()));
                    return new SendResponse(command, e.getMessage() != null ? e.getMessage() : "", false);
                }

                String result = capturingSource.getOutput();
                boolean success = successHolder[0];

                if (Config.getConfig().send.logCommandExecution) {
                    Logger.info("console:send - executed '%s'".formatted(command));
                }

                return new SendResponse(command, result, success);
            });
    }
}
