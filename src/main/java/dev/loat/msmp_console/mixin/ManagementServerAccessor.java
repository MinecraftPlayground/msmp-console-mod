package dev.loat.msmp_console.mixin;

import net.minecraft.server.jsonrpc.Connection;
import net.minecraft.server.jsonrpc.ManagementServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import java.util.function.Consumer;


@Mixin(ManagementServer.class)
public interface ManagementServerAccessor {
    @Invoker("forEachConnection")
    void invokeForEachConnection(Consumer<Connection> consumer);
}
