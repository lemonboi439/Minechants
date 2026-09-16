package me.lemonboi439.minechants.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;

public final class MiningContext {
    private static final ThreadLocal<Context> CURRENT = new ThreadLocal<>();

    private MiningContext() {
    }

    public static void push(ServerPlayer player, ItemStack tool) {
        CURRENT.set(new Context(player, tool));
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static Context getCurrent() {
        return CURRENT.get();
    }

    public record Context(ServerPlayer player, ItemStack tool) {
    }
}
