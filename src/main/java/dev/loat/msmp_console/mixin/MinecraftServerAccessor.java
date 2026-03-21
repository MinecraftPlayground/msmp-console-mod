package dev.loat.msmp_console.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.Optional;


@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor {
    @Accessor("managementServer")
    Optional<?> getManagementServer();
}
