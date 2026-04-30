package me.lemonboi439.minechants.mixin;

import me.lemonboi439.minechants.util.MinerQueue;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void minechants$tickQueuedMinerBreaks(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        MinerQueue.tick((MinecraftServer) (Object) this);
    }
}
