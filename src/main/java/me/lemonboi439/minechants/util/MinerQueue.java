package me.lemonboi439.minechants.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public final class MinerQueue {
    private static final List<QueuedMinerBreak> QUEUED_BREAKS = new ArrayList<>();

    private MinerQueue() {
    }

    public static void schedule(MinecraftServer server, int tick, QueuedMinerBreaker manager, BlockPos pos) {
        QUEUED_BREAKS.add(new QueuedMinerBreak(server, tick, manager, pos.immutable()));
    }

    public static void tick(MinecraftServer server) {
        for (int index = QUEUED_BREAKS.size() - 1; index >= 0; index--) {
            QueuedMinerBreak queuedBreak = QUEUED_BREAKS.get(index);
            if (queuedBreak.server() != server || queuedBreak.tick() > server.getTickCount()) {
                continue;
            }

            queuedBreak.manager().minechants$runQueuedMinerBreak(queuedBreak.pos());
            QUEUED_BREAKS.remove(index);
        }
    }

    private record QueuedMinerBreak(
            MinecraftServer server,
            int tick,
            QueuedMinerBreaker manager,
            BlockPos pos
    ) {}
}
