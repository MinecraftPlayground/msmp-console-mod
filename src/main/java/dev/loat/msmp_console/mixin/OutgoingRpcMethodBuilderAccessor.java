package dev.loat.msmp_console.mixin;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.jsonrpc.OutgoingRpcMethod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Mixin accessor for the private
 * {@code OutgoingRpcMethod.OutgoingRpcMethodBuilder#register(Identifier)} method.
 *
 * <p>The public {@code register(String)} overload always prepends {@code minecraft:notification/}
 * as the namespace via {@link Identifier#withDefaultNamespace(String)}, making it impossible
 * to register notifications under a custom namespace. This accessor exposes the private
 * {@code register(Identifier)} overload directly, allowing a fully custom
 * {@link Identifier} (e.g. {@code console:notification/message}) to be used.</p>
 *
 * <p>Usage:</p>
 * <pre>{@code
 * ((OutgoingRpcMethodBuilderAccessor<MyParams, Void>)
 *     OutgoingRpcMethod.<MyParams>notificationWithParams()
 *         .description("...")
 *         .param("param", MY_SCHEMA)
 * ).invokeRegister(Identifier.fromNamespaceAndPath("mynamespace", "notification/myevent"));
 * }</pre>
 *
 * @param <Params> the type of the notification parameters
 * @param <Result> the type of the result (typically {@link Void})
 */
@Mixin(OutgoingRpcMethod.OutgoingRpcMethodBuilder.class)
public interface OutgoingRpcMethodBuilderAccessor<Params, Result> {

    /**
     * Invokes the private {@code register(Identifier)} method on
     * {@link OutgoingRpcMethod.OutgoingRpcMethodBuilder}, registering the built
     * {@link OutgoingRpcMethod} under the given fully qualified {@link Identifier}
     * without any namespace defaulting.
     *
     * @param id the fully qualified identifier for the notification method,
     *           e.g. {@code Identifier.fromNamespaceAndPath("console", "notification/message")}
     * @return a {@link Holder.Reference} to the registered {@link OutgoingRpcMethod}
     */
    @Invoker("register")
    Holder.Reference<OutgoingRpcMethod<Params, Result>> invokeRegister(Identifier id);
}
