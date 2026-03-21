package dev.loat.msmp_console;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;


public class MSMPConsole implements ModInitializer {

    @SuppressWarnings("unchecked")
    public static final Holder.Reference<OutgoingRpcMethod<ConsoleLogPayload, Void>> CONSOLE_MESSAGE =
        ((OutgoingRpcMethodBuilderAccessor<ConsoleLogPayload, Void>)
            OutgoingRpcMethod.<ConsoleLogPayload>notificationWithParams()
                .description("A server console log message")
                .param("message", ConsoleLogPayload.SCHEMA)
        ).invokeRegister(Identifier.fromNamespaceAndPath("console", "notification/message"));

    private static ManagementServer managementServer;

    @Override
    public void onInitialize() {
        registerLogAppender();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            managementServer = getManagementServer(server);
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            managementServer = null;
        });
    }

    private void registerLogAppender() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Logger root = ctx.getRootLogger();
        ConsoleNotificationAppender appender =
            ConsoleNotificationAppender.createAppender("ConsoleNotificationAppender");
        appender.start();
        root.addAppender(appender);
    }

    private static ManagementServer getManagementServer(MinecraftServer server) {
        Class<?> clazz = server.getClass();
        while (clazz != null) {
            for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
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
