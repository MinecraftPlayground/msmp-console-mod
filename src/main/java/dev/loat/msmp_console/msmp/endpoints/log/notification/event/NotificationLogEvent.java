package dev.loat.msmp_console.msmp.endpoints.log.notification.event;

import java.util.function.Supplier;

import dev.loat.msmp.MSMPNamespace;
import dev.loat.msmp.MSMPNotification;
import dev.loat.msmp.MSMPServer;
import dev.loat.msmp_console.logging.ConsoleNotificationAppender;


public class NotificationLogEvent {

    private NotificationLogEvent() {}

    public static void register(MSMPNamespace namespace, Supplier<MSMPServer> msmpServer) {
        MSMPNotification<NotificationLogEventPayload> notification = namespace.notification("log/event")
            .description("A server console log message")
            .responseSchema(NotificationLogEventPayload.SCHEMA)
            .register();
        
        ConsoleNotificationAppender.register(payload -> {
            MSMPServer server = msmpServer.get();
            if (server == null) return;

            server.send(notification, new NotificationLogEventPayload(
                payload.timestamp(),
                payload.level(),
                payload.thread(),
                payload.logger(),
                payload.message(),
                payload.throwable()
            ));
        });
    }
}


