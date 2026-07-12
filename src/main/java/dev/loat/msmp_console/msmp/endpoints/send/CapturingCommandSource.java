package dev.loat.msmp_console.msmp.endpoints.send;

import net.minecraft.commands.CommandSource;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;


/**
 * A {@link CommandSource} that captures all feedback and error messages produced by a
 * command into an in-memory list instead of forwarding them anywhere (e.g. the real
 * server console), so they can be returned as the {@code result} of {@code console:send}.
 *
 * <p>Both success and failure feedback are captured ({@link #acceptsSuccess()} and
 * {@link #acceptsFailure()} both return {@code true}), and nothing is broadcast to
 * operators ({@link #shouldInformAdmins()} returns {@code false}), since the caller
 * already receives the full output directly in the RPC response.</p>
 */
final class CapturingCommandSource implements CommandSource {

    private final List<String> messages = new ArrayList<>();

    @Override
    public void sendSystemMessage(Component message) {
        messages.add(message.getString());
    }

    @Override
    public boolean acceptsSuccess() {
        return true;
    }

    @Override
    public boolean acceptsFailure() {
        return true;
    }

    @Override
    public boolean shouldInformAdmins() {
        return false;
    }

    /**
     * @return All captured feedback/error lines, joined by newlines, in the order they were produced
     */
    String getOutput() {
        return String.join("\n", messages);
    }
}
