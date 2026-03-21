package dev.loat.msmp_console;

import dev.loat.msmp_console.logging.ConsoleNotificationAppender;
import dev.loat.msmp_console.mixin.ManagementServerAccessor;
import dev.loat.msmp_console.mixin.MinecraftServerAccessor;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;


public class MSMPConsole implements ModInitializer {

    private static MinecraftServer serverInstance;

    @Override
    public void onInitialize() {
        registerLogAppender();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            serverInstance = server;
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            serverInstance = null;
        });
    }

    private void registerLogAppender() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Logger root = ctx.getRootLogger();

        ConsoleNotificationAppender appender = ConsoleNotificationAppender.createAppender("ConsoleNotificationAppender");
        appender.start();
        root.addAppender(appender);
    }

    public static void sendConsoleNotification(ConsoleNotificationAppender.LogPayload payload) {
        if (serverInstance == null) return;

        ((MinecraftServerAccessor) serverInstance)
            .getManagementServer()
            .ifPresent(management -> {
                JsonObject obj = new JsonObject();
                obj.addProperty("timestamp", payload.timestamp());
                obj.addProperty("level",     payload.level());
                obj.addProperty("thread",    payload.thread());
                obj.addProperty("logger",    payload.logger());
                obj.addProperty("message",   payload.message());
                if (payload.throwable() != null)
                    obj.addProperty("throwable", payload.throwable());

                JsonArray params = new JsonArray();
                params.add(obj);

                ((ManagementServerAccessor) management)
                    .invokeBroadcastNotification("console:notification/message", params);
            });
    }
}
