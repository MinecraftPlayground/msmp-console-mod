package dev.loat.msmp_console.mixin;

import com.google.gson.JsonArray;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;


@Mixin(targets = "net.minecraft.server.management.ManagementServer")
public interface ManagementServerAccessor {
    @Invoker("broadcastNotification")
    void invokeBroadcastNotification(String method, JsonArray params);
}
