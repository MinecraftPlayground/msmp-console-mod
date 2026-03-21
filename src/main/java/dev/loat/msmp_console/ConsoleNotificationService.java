package dev.loat.msmp_console;

import net.minecraft.core.Holder;
import net.minecraft.server.jsonrpc.OutgoingRpcMethod;
import net.minecraft.server.notifications.NotificationService;


public class ConsoleNotificationService implements NotificationService {

    // Notification einmalig beim Classloading registrieren
    public static final Holder.Reference<OutgoingRpcMethod<ConsoleLogPayload, Void>> CONSOLE_MESSAGE =
        OutgoingRpcMethod.<ConsoleLogPayload>notificationWithParams()
            .description("A server console log message")
            .param("message", ConsoleLogPayload.SCHEMA)
            .register("console:notification/message");

    private final net.minecraft.server.jsonrpc.JsonRpcNotificationService rpcService;

    public ConsoleNotificationService(net.minecraft.server.jsonrpc.JsonRpcNotificationService rpcService) {
        this.rpcService = rpcService;
    }

    public void sendConsoleNotification(ConsoleLogPayload payload) {
        // broadcastNotification ist private in JsonRpcNotificationService,
        // wir brauchen dafür einen Accessor – oder wir rufen es über
        // forEachConnection direkt auf (siehe MSMPConsole.java unten)
    }

    // NotificationService-Methoden müssen implementiert werden,
    // aber wir brauchen nur sendConsoleNotification – der Rest ist no-op
    @Override public void playerJoined(net.minecraft.server.level.ServerPlayer p) {}
    @Override public void playerLeft(net.minecraft.server.level.ServerPlayer p) {}
    @Override public void serverStarted() {}
    @Override public void serverShuttingDown() {}
    @Override public void serverSaveStarted() {}
    @Override public void serverSaveCompleted() {}
    @Override public void serverActivityOccured() {}
    @Override public void playerOped(net.minecraft.server.players.ServerOpListEntry e) {}
    @Override public void playerDeoped(net.minecraft.server.players.ServerOpListEntry e) {}
    @Override public void playerAddedToAllowlist(net.minecraft.server.players.NameAndId n) {}
    @Override public void playerRemovedFromAllowlist(net.minecraft.server.players.NameAndId n) {}
    @Override public void ipBanned(net.minecraft.server.players.IpBanListEntry e) {}
    @Override public void ipUnbanned(String s) {}
    @Override public void playerBanned(net.minecraft.server.players.UserBanListEntry e) {}
    @Override public void playerUnbanned(net.minecraft.server.players.NameAndId n) {}
    @Override public <T> void onGameRuleChanged(net.minecraft.world.level.gamerules.GameRule<T> r, T v) {}
    @Override public void statusHeartbeat() {}
}
