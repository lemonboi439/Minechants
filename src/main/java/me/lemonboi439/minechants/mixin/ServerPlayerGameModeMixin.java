package me.lemonboi439.minechants.mixin;

import me.lemonboi439.minechants.Minechants;
import me.lemonboi439.minechants.config.MinechantsConfig;
import me.lemonboi439.minechants.enchantment.ModEnchantments;
import me.lemonboi439.minechants.util.FeatureToggleState;
import me.lemonboi439.minechants.util.MiningContext;
import me.lemonboi439.minechants.util.ToggleFeature;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
    private static final TagKey<net.minecraft.world.level.block.Block> MINECHANTS_THREE_BY_THREE_BLACKLIST = TagKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(Minechants.MOD_ID, "3x3_miner_blacklist")
    );
    @Unique
    private static final ExplosionDamageCalculator minechants$visualExplosionDamageCalculator = new ExplosionDamageCalculator() {
        @Override
        public boolean shouldBlockExplode(Explosion explosion, net.minecraft.world.level.BlockGetter world, BlockPos pos, BlockState state, float power) {
            return false;
        }
    };

    @Shadow @Final protected ServerPlayer player;

    @Unique
    private boolean minechants$processingExplosiveBreak;
    @Unique
    private boolean minechants$processingMinerBreak;

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void minechants$pushMiningContext(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        MiningContext.push(this.player, this.player.getMainHandItem());
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void minechants$clearMiningContext(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        MiningContext.clear();
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void minechants$breakExplosiveBurst(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() || this.minechants$processingExplosiveBreak) {
            return;
        }

        ItemStack tool = this.player.getMainHandItem();
        if (tool.isEmpty()) {
            return;
        }

        var enchantmentLookup = this.player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var explosiveEntry = enchantmentLookup.getOrThrow(ModEnchantments.EXPLOSIVE);
        int level = EnchantmentHelper.getItemEnchantmentLevel(explosiveEntry, tool);
        if (level <= 0 || !MinechantsConfig.explosiveEnabled || !FeatureToggleState.isEnabled(this.player, ToggleFeature.EXPLOSIVE)) {
            this.minechants$scheduleMinerBurst(pos, tool, enchantmentLookup);
            return;
        }

        if (!this.player.level().isClientSide() && MinechantsConfig.disableExplosiveOnServer) {
            this.minechants$scheduleMinerBurst(pos, tool, enchantmentLookup);
            return;
        }

        BlockPos direction = this.minechants$getForwardStep(this.player.getLookAngle());
        if (direction.equals(BlockPos.ZERO)) {
            return;
        }

        this.minechants$processingExplosiveBreak = true;
        try {
            ServerPlayerGameMode manager = (ServerPlayerGameMode) (Object) this;
            int explosivePower = Mth.clamp(MinechantsConfig.explosivePower, 1, 5);
            double radius = 0.85D + (explosivePower * 0.5D);
            Vec3 center = Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(direction).scale(0.8D + (0.2D * explosivePower)));
            this.player.level().explode(
                    this.player,
                    this.player.level().damageSources().explosion(this.player, this.player),
                    minechants$visualExplosionDamageCalculator,
                    center,
                    0.75F + (explosivePower * 0.25F),
                    false,
                    Level.ExplosionInteraction.TNT
            );
            this.minechants$breakBlastArea(manager, center, radius, pos);
        } finally {
            this.minechants$processingExplosiveBreak = false;
        }
    }

    @Unique
    private void minechants$scheduleMinerBurst(BlockPos pos, ItemStack tool, net.minecraft.core.HolderLookup.RegistryLookup<Enchantment> enchantmentLookup) {
        if (this.minechants$processingMinerBreak) {
            return;
        }

        var minerEntry = enchantmentLookup.getOrThrow(ModEnchantments.THREE_BY_THREE_MINER);
        if (EnchantmentHelper.getItemEnchantmentLevel(minerEntry, tool) <= 0
                || !MinechantsConfig.threeByThreeEnabled
                || !FeatureToggleState.isEnabled(this.player, ToggleFeature.THREE_BY_THREE_MINER)) {
            return;
        }

        BlockPos forward = this.minechants$getForwardStep(this.player.getLookAngle());
        if (forward.equals(BlockPos.ZERO)) {
            return;
        }

        this.minechants$processingMinerBreak = true;
        try {
            ServerPlayerGameMode manager = (ServerPlayerGameMode) (Object) this;
            for (BlockPos offset : this.minechants$getMinerOffsets(forward)) {
                BlockPos target = pos.offset(offset);
                if (this.minechants$canThreeByThreeMine(target)) {
                    manager.destroyBlock(target);
                }
            }
        } finally {
            this.minechants$processingMinerBreak = false;
        }
    }

    @Unique
    private BlockPos minechants$getForwardStep(Vec3 rotation) {
        double x = Math.abs(rotation.x);
        double y = Math.abs(rotation.y);
        double z = Math.abs(rotation.z);

        if (x >= y && x >= z) {
            return BlockPos.containing(Math.signum(rotation.x), 0, 0);
        }

        if (z >= x && z >= y) {
            return BlockPos.containing(0, 0, Math.signum(rotation.z));
        }

        return BlockPos.containing(0, Math.signum(rotation.y), 0);
    }

    @Unique
    private void minechants$breakBlastArea(ServerPlayerGameMode manager, Vec3 center, double radius, BlockPos origin) {
        int blockRadius = (int) Math.ceil(radius);
        BlockPos centerPos = BlockPos.containing(center);
        for (BlockPos target : BlockPos.betweenClosed(centerPos.offset(-blockRadius, -blockRadius, -blockRadius), centerPos.offset(blockRadius, blockRadius, blockRadius))) {
            if (target.equals(origin)) {
                continue;
            }

            if (Vec3.atCenterOf(target).distanceToSqr(center) > radius * radius) {
                continue;
            }

            manager.destroyBlock(target.immutable());
        }
    }

    @Unique
    private BlockPos[] minechants$getMinerOffsets(BlockPos forward) {
        List<BlockPos> offsets = new ArrayList<>(8);
        BlockPos axisA = this.minechants$getPrimaryPerpendicularAxis(forward);
        BlockPos axisB = this.minechants$getSecondaryPerpendicularAxis(forward);

        for (int depth = 0; depth <= 2; depth++) {
            for (int a = -1; a <= 1; a++) {
                for (int b = -1; b <= 1; b++) {
                    if (depth == 0 && a == 0 && b == 0) {
                        continue;
                    }

                    offsets.add(forward.multiply(depth).offset(axisA.multiply(a)).offset(axisB.multiply(b)));
                }
            }
        }

        offsets.sort(
                Comparator
                        .comparingInt((BlockPos offset) -> this.minechants$getMinerSquaredDistance(offset, forward, axisA, axisB))
                        .thenComparingInt(offset -> this.minechants$getForwardDepth(offset, forward))
                        .thenComparingInt(offset -> this.minechants$getPlaneManhattan(offset, axisA, axisB))
        );

        return offsets.toArray(BlockPos[]::new);
    }

    @Unique
    private BlockPos minechants$getPrimaryPerpendicularAxis(BlockPos forward) {
        if (forward.getX() != 0) {
            return new BlockPos(0, 1, 0);
        }

        if (forward.getY() != 0) {
            return new BlockPos(1, 0, 0);
        }

        return new BlockPos(0, 1, 0);
    }

    @Unique
    private BlockPos minechants$getSecondaryPerpendicularAxis(BlockPos forward) {
        if (forward.getX() != 0) {
            return new BlockPos(0, 0, 1);
        }

        if (forward.getY() != 0) {
            return new BlockPos(0, 0, 1);
        }

        return new BlockPos(1, 0, 0);
    }

    @Unique
    private int minechants$getMinerSquaredDistance(BlockPos offset, BlockPos forward, BlockPos axisA, BlockPos axisB) {
        int depth = this.minechants$getForwardDepth(offset, forward);
        int a = this.minechants$getAxisComponent(offset, axisA);
        int b = this.minechants$getAxisComponent(offset, axisB);
        return (depth * depth) + (a * a) + (b * b);
    }

    @Unique
    private int minechants$getForwardDepth(BlockPos offset, BlockPos forward) {
        return Math.abs(this.minechants$getAxisComponent(offset, forward));
    }

    @Unique
    private int minechants$getPlaneManhattan(BlockPos offset, BlockPos axisA, BlockPos axisB) {
        return Math.abs(this.minechants$getAxisComponent(offset, axisA))
                + Math.abs(this.minechants$getAxisComponent(offset, axisB));
    }

    @Unique
    private int minechants$getAxisComponent(BlockPos offset, BlockPos axis) {
        return (offset.getX() * axis.getX()) + (offset.getY() * axis.getY()) + (offset.getZ() * axis.getZ());
    }

    @Unique
    private boolean minechants$canThreeByThreeMine(BlockPos pos) {
        BlockState state = this.player.level().getBlockState(pos);
        if (state.isAir() || state.hasBlockEntity()) {
            return false;
        }

        if (state.is(MINECHANTS_THREE_BY_THREE_BLACKLIST)) {
            return false;
        }

        if (!state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            return false;
        }

        return this.player.getMainHandItem().isCorrectToolForDrops(state);
    }
}
