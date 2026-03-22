package dev.loat.msmp_console;

import dev.loat.msmp_console.config.Config;
import dev.loat.msmp_console.config.ConfigManager;
import dev.loat.msmp_console.config.files.MSMPConsoleConfigFile;
import dev.loat.msmp_console.logging.ConsoleNotificationAppender;
import dev.loat.msmp_console.mixin.ManagementServerAccessor;
import dev.loat.msmp_console.mixin.OutgoingRpcMethodBuilderAccessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.jsonrpc.ManagementServer;
import net.minecraft.server.jsonrpc.OutgoingRpcMethod;

import java.lang.reflect.Field;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;


/**
 * Main entrypoint of the MSMP Console mod.
 *
 * <p>This mod extends the Minecraft Server Management Protocol (MSMP) by forwarding
 * every server console log event to all connected WebSocket clients as a JSON-RPC
 * notification under the method {@code console:notification/message}.</p>
 *
 * <p>On initialization the mod:</p>
 * <ol>
 *   <li>Registers a custom {@link OutgoingRpcMethod} under {@code console:notification/message}
 *       via {@link OutgoingRpcMethodBuilderAccessor} to bypass the default {@code minecraft:}
 *       namespace.</li>
 *   <li>Attaches a {@link ConsoleNotificationAppender} to the root Log4j2 logger to
 *       intercept all log events.</li>
 *   <li>Caches the {@link ManagementServer} instance on {@code SERVER_STARTED} and
 *       clears it on {@code SERVER_STOPPED}.</li>
 * </ol>
 */
public class MSMPConsole implements ModInitializer {

    /**
     * The registered JSON-RPC notification method for console log messages.
     *
     * <p>Registered under {@code console:notification/message} using
     * {@link OutgoingRpcMethodBuilderAccessor#invokeRegister(Identifier)} to bypass
     * the default {@code minecraft:notification/} namespace that the public
     * {@code register(String)} overload would apply.</p>
     *
     * <p>The {@code @SuppressWarnings("unchecked")} is required because the cast from
     * {@link OutgoingRpcMethod.OutgoingRpcMethodBuilder} to
     * {@link OutgoingRpcMethodBuilderAccessor} is a generic unchecked cast that is
     * safe at runtime due to Mixin's bytecode transformation.</p>
     */
    @SuppressWarnings("unchecked")
    public static final Holder.Reference<OutgoingRpcMethod<ConsoleLogPayload, Void>> CONSOLE_MESSAGE =
        ((OutgoingRpcMethodBuilderAccessor<ConsoleLogPayload, Void>)
            OutgoingRpcMethod.<ConsoleLogPayload>notificationWithParams()
                .description("A server console log message")
                .param("message", ConsoleLogPayload.SCHEMA)
        ).invokeRegister(Identifier.fromNamespaceAndPath("console", "notification/message"));

    /**
     * The cached {@link ManagementServer} instance, set on {@code SERVER_STARTED}
     * and cleared on {@code SERVER_STOPPED}. {@code null} if the server is not running.
     */
    private static ManagementServer managementServer;

    /**
     * Called by Fabric when the mod is initialized.
     *
     * <p>Registers the Log4j2 appender and the server lifecycle listeners.</p>
     */
    @Override
    public void onInitialize() {
        dev.loat.msmp_console.logging.Logger.setLoggerClass(MSMPConsole.class);

        ConfigManager.addConfig(new Config<>(
            ConfigManager.resolve("config.yml"),
            MSMPConsoleConfigFile.class
        ));
    
        registerLogAppender();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            managementServer = getManagementServer(server);
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            managementServer = null;
        });

        dev.loat.msmp_console.logging.Logger.info("MSMP Console initialized.");
    }

    /**
     * Attaches a {@link ConsoleNotificationAppender} to the root Log4j2 logger.
     *
     * <p>The appender is created programmatically so no {@code log4j2.xml}
     * configuration is required.</p>
     */
    private void registerLogAppender() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Logger root = ctx.getRootLogger();
        ConsoleNotificationAppender appender =
            ConsoleNotificationAppender.createAppender("ConsoleNotificationAppender");
        appender.start();
        root.addAppender(appender);
    }

    /**
     * Finds the {@link ManagementServer} instance held by the given {@link MinecraftServer}
     * by traversing its class hierarchy via reflection.
     *
     * <p>The field ({@code jsonRpcServer} in {@code DedicatedServer}) is not publicly
     * accessible, so reflection is used to locate the first field of type
     * {@link ManagementServer} in the class hierarchy.</p>
     *
     * @param server the running {@link MinecraftServer} instance
     * @return the {@link ManagementServer} instance, or {@code null} if not found
     */
    private static ManagementServer getManagementServer(MinecraftServer server) {
        Class<?> clazz = server.getClass();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getType() == ManagementServer.class) {
                    try {
                        field.setAccessible(true);
                        ManagementServer ms = (ManagementServer) field.get(server);
                        if (ms != null) return ms;
                    } catch (Exception ignored) {}
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    /**
     * Sends a console log notification to all connected MSMP clients.
     *
     * <p>Called by {@link ConsoleNotificationAppender} for every intercepted log event.
     * Does nothing if the {@link ManagementServer} is not yet available (i.e. before
     * {@code SERVER_STARTED} or after {@code SERVER_STOPPED}).</p>
     *
     * <p>The {@link ConsoleNotificationAppender.LogPayload} is mapped to a
     * {@link ConsoleLogPayload} with {@code null} throwables replaced by empty strings
     * to satisfy the non-null codec contract.</p>
     *
     * @param payload the log payload captured by {@link ConsoleNotificationAppender}
     */
    public static void sendConsoleNotification(ConsoleNotificationAppender.LogPayload payload) {
        if (managementServer == null) return;

        ConsoleLogPayload rpcPayload = new ConsoleLogPayload(
            payload.timestamp(),
            payload.level(),
            payload.thread(),
            payload.logger(),
            payload.message(),
            payload.throwable() != null ? payload.throwable() : ""
        );

        ((ManagementServerAccessor) managementServer)
            .invokeForEachConnection(conn ->
                conn.sendNotification(CONSOLE_MESSAGE, rpcPayload)
            );
    }
}
