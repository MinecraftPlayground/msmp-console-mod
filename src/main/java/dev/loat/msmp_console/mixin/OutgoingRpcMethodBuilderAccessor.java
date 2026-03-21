package dev.loat.msmp_console.mixin;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.jsonrpc.OutgoingRpcMethod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;


@Mixin(OutgoingRpcMethod.OutgoingRpcMethodBuilder.class)
public interface OutgoingRpcMethodBuilderAccessor<Params, Result> {
    @Invoker("register")
    Holder.Reference<OutgoingRpcMethod<Params, Result>> invokeRegister(Identifier id);
}
