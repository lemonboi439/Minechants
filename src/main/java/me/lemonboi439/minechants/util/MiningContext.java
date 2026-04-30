package me.lemonboi439.minechants.util;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public final class MiningContext {
    private static final ThreadLocal<Context> CURRENT = new ThreadLocal<>();

    private MiningContext() {
    }

    public static void push(ServerPlayerEntity player, ItemStack tool) {
        CURRENT.set(new Context(player, tool));
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static Context getCurrent() {
        return CURRENT.get();
    }

    public record Context(ServerPlayerEntity player, ItemStack tool) {
    }
}
