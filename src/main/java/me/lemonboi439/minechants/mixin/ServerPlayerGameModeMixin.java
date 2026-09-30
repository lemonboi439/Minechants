package me.lemonboi439.minechants.mixin;

import me.lemonboi439.minechants.Minechants;
import me.lemonboi439.minechants.config.MinechantsConfig;
import me.lemonboi439.minechants.enchantment.ModEnchantments;
import me.lemonboi439.minechants.util.FeatureToggleState;
import me.lemonboi439.minechants.util.ToggleFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {
    @Unique
    private static final TagKey<Block> MINECHANTS_BLACKLIST = TagKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(Minechants.MOD_ID, "3x3_miner_blacklist")
    );

    @Shadow @Final protected ServerPlayer player;
    @Unique private boolean minechants$processingExplosiveBreak;
    @Unique private boolean minechants$processingMinerBreak;

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void minechants$breakAdditionalBlocks(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            return;
        }

        ItemStack tool = this.player.getItemInHand(InteractionHand.MAIN_HAND);
        var enchantments = this.player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        int explosiveLevel = EnchantmentHelper.getItemEnchantmentLevel(
                enchantments.getOrThrow(ModEnchantments.EXPLOSIVE), tool
        );

        if (explosiveLevel > 0 && MinechantsConfig.explosiveEnabled
                && FeatureToggleState.isEnabled(this.player, ToggleFeature.EXPLOSIVE)
                && !MinechantsConfig.disableExplosiveOnServer
                && !this.minechants$processingExplosiveBreak) {
            this.minechants$processingExplosiveBreak = true;
            try {
                BlockPos forward = this.minechants$getForwardStep(this.player.getLookAngle());
                if (!forward.equals(BlockPos.ZERO)) {
                    int power = Math.max(1, Math.min(5, MinechantsConfig.explosivePower));
                    double radius = 0.85D + power * 0.5D;
                    Vec3 center = Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(forward).scale(0.8D + power * 0.2D));
                    this.player.level().explode(
                            this.player, center.x, center.y, center.z,
                            0.75F + power * 0.25F, false, Level.ExplosionInteraction.NONE
                    );
                    this.minechants$breakBlastArea(pos, center, radius);
                }
            } finally {
                this.minechants$processingExplosiveBreak = false;
            }
        }

        if (MinechantsConfig.threeByThreeEnabled
                && FeatureToggleState.isEnabled(this.player, ToggleFeature.THREE_BY_THREE_MINER)
                && EnchantmentHelper.getItemEnchantmentLevel(
                        enchantments.getOrThrow(ModEnchantments.THREE_BY_THREE_MINER), tool
                ) > 0
                && !this.minechants$processingMinerBreak) {
            this.minechants$processingMinerBreak = true;
            try {
                BlockPos forward = this.minechants$getForwardStep(this.player.getLookAngle());
                BlockPos axisA = this.minechants$getPrimaryPerpendicularAxis(forward);
                BlockPos axisB = this.minechants$getSecondaryPerpendicularAxis(forward);
                for (int depth = 0; depth <= 2; depth++) {
                    for (int a = -1; a <= 1; a++) {
                        for (int b = -1; b <= 1; b++) {
                            if (depth == 0 && a == 0 && b == 0) continue;
                            BlockPos target = pos.offset(
                                    forward.getX() * depth + axisA.getX() * a + axisB.getX() * b,
                                    forward.getY() * depth + axisA.getY() * a + axisB.getY() * b,
                                    forward.getZ() * depth + axisA.getZ() * a + axisB.getZ() * b);
                            if (this.minechants$canMine(target, tool)) {
                                ((ServerPlayerGameMode) (Object) this).destroyBlock(target);
                            }
                        }
                    }
                }
            } finally {
                this.minechants$processingMinerBreak = false;
            }
        }
    }

    @Unique
    private void minechants$breakBlastArea(BlockPos origin, Vec3 center, double radius) {
        int blockRadius = (int) Math.ceil(radius);
        BlockPos centerPos = BlockPos.containing(center);
        for (BlockPos target : BlockPos.betweenClosed(centerPos.offset(-blockRadius, -blockRadius, -blockRadius),
                centerPos.offset(blockRadius, blockRadius, blockRadius))) {
            if (!target.equals(origin)
                    && Vec3.atCenterOf(target).distanceToSqr(center) <= radius * radius
                    && this.minechants$canMine(target, this.player.getItemInHand(InteractionHand.MAIN_HAND))) {
                ((ServerPlayerGameMode) (Object) this).destroyBlock(target);
            }
        }
    }

    @Unique
    private boolean minechants$canMine(BlockPos pos, ItemStack tool) {
        BlockState state = this.player.level().getBlockState(pos);
        return !state.isAir() && !state.hasBlockEntity()
                && state.is(BlockTags.MINEABLE_WITH_PICKAXE)
                && !state.is(MINECHANTS_BLACKLIST)
                && tool.canDestroyBlock(state, this.player.level(), pos, this.player);
    }

    @Unique
    private BlockPos minechants$getForwardStep(Vec3 look) {
        if (Math.abs(look.x) >= Math.abs(look.y) && Math.abs(look.x) >= Math.abs(look.z)) {
            return new BlockPos((int) Math.signum(look.x), 0, 0);
        }
        if (Math.abs(look.z) >= Math.abs(look.x) && Math.abs(look.z) >= Math.abs(look.y)) {
            return new BlockPos(0, 0, (int) Math.signum(look.z));
        }
        return new BlockPos(0, (int) Math.signum(look.y), 0);
    }

    @Unique
    private BlockPos minechants$getPrimaryPerpendicularAxis(BlockPos forward) {
        if (forward.getX() != 0) return new BlockPos(0, 1, 0);
        if (forward.getY() != 0) return new BlockPos(1, 0, 0);
        return new BlockPos(0, 1, 0);
    }

    @Unique
    private BlockPos minechants$getSecondaryPerpendicularAxis(BlockPos forward) {
        if (forward.getX() != 0) return new BlockPos(0, 0, 1);
        if (forward.getY() != 0) return new BlockPos(0, 0, 1);
        return new BlockPos(1, 0, 0);
    }
}
