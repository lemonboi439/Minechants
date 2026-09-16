package me.lemonboi439.minechants.mixin;

import me.lemonboi439.minechants.Minechants;
import me.lemonboi439.minechants.enchantment.ModEnchantments;
import me.lemonboi439.minechants.util.FeatureToggleState;
import me.lemonboi439.minechants.util.MiningContext;
import me.lemonboi439.minechants.util.ToggleFeature;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;

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

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin {
    @Unique
    private static final TagKey<net.minecraft.block.Block> MINECHANTS_THREE_BY_THREE_BLACKLIST = TagKey.of(
            RegistryKeys.BLOCK,
            Identifier.of(Minechants.MOD_ID, "3x3_miner_blacklist")
    );

    @Unique
    private static final double[] minechants$explosiveRadii = {1.35D, 1.85D, 2.35D};
    @Unique
    private static final ExplosionBehavior minechants$visualExplosionBehavior = new ExplosionBehavior() {
        @Override
        public boolean canDestroyBlock(Explosion explosion, net.minecraft.world.BlockView world, BlockPos pos, BlockState state, float power) {
            return false;
        }
    };

    @Shadow @Final protected ServerPlayerEntity player;

    @Unique
    private boolean minechants$processingExplosiveBreak;
    @Unique
    private boolean minechants$processingMinerBreak;

    @Inject(method = "tryBreakBlock", at = @At("HEAD"))
    private void minechants$pushMiningContext(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        MiningContext.push(this.player, this.player.getMainHandStack());
    }

    @Inject(method = "tryBreakBlock", at = @At("RETURN"))
    private void minechants$clearMiningContext(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        MiningContext.clear();
    }

    @Inject(method = "tryBreakBlock", at = @At("RETURN"))
    private void minechants$breakExplosiveBurst(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() || this.minechants$processingExplosiveBreak) {
            return;
        }

        ItemStack tool = this.player.getMainHandStack();
        if (tool.isEmpty()) {
            return;
        }

        var enchantmentLookup = this.player.getEntityWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT);
        var explosiveEntry = enchantmentLookup.getEntry(ModEnchantments.EXPLOSIVE).orElseThrow();
        int level = EnchantmentHelper.getLevel(explosiveEntry, tool);
        if (level <= 0 || !FeatureToggleState.isEnabled(this.player, ToggleFeature.EXPLOSIVE)) {
            this.minechants$scheduleMinerBurst(pos, tool);
            return;
        }

        BlockPos direction = this.minechants$getForwardStep(this.player.getRotationVec(1.0F));
        if (direction.equals(BlockPos.ORIGIN)) {
            return;
        }

        this.minechants$processingExplosiveBreak = true;
        try {
            ServerPlayerInteractionManager manager = (ServerPlayerInteractionManager) (Object) this;
            double radius = minechants$explosiveRadii[Math.min(level, minechants$explosiveRadii.length) - 1];
            Vec3d center = Vec3d.ofCenter(pos).add(Vec3d.of(direction).multiply(0.8D + (0.35D * level)));
            this.player.getEntityWorld().createExplosion(
                    this.player,
                    this.player.getEntityWorld().getDamageSources().explosion(this.player, this.player),
                    minechants$visualExplosionBehavior,
                    center.x,
                    center.y,
                    center.z,
                    1.0F + (level * 0.35F),
                    false,
                    World.ExplosionSourceType.TNT
            );
            this.minechants$breakBlastArea(manager, center, radius, pos);
        } finally {
            this.minechants$processingExplosiveBreak = false;
        }
    }

    @Unique
    private void minechants$scheduleMinerBurst(BlockPos pos, ItemStack tool) {
        if (this.minechants$processingMinerBreak) {
            return;
        }

        var enchantmentLookup = this.player.getEntityWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT);
        var minerEntry = enchantmentLookup.getEntry(ModEnchantments.THREE_BY_THREE_MINER).orElseThrow();
        if (EnchantmentHelper.getLevel(minerEntry, tool) <= 0 || !FeatureToggleState.isEnabled(this.player, ToggleFeature.THREE_BY_THREE_MINER)) {
            return;
        }

        BlockPos forward = this.minechants$getForwardStep(this.player.getRotationVec(1.0F));
        if (forward.equals(BlockPos.ORIGIN)) {
            return;
        }

        this.minechants$processingMinerBreak = true;
        try {
            ServerPlayerInteractionManager manager = (ServerPlayerInteractionManager) (Object) this;
            for (BlockPos offset : this.minechants$getMinerOffsets(forward)) {
                BlockPos target = pos.add(offset);
                if (this.minechants$canThreeByThreeMine(target)) {
                    manager.tryBreakBlock(target);
                }
            }
        } finally {
            this.minechants$processingMinerBreak = false;
        }
    }

    @Unique
    private BlockPos minechants$getForwardStep(Vec3d rotation) {
        double x = Math.abs(rotation.x);
        double y = Math.abs(rotation.y);
        double z = Math.abs(rotation.z);

        if (x >= y && x >= z) {
            return BlockPos.ofFloored(Math.signum(rotation.x), 0, 0);
        }

        if (z >= x && z >= y) {
            return BlockPos.ofFloored(0, 0, Math.signum(rotation.z));
        }

        return BlockPos.ofFloored(0, Math.signum(rotation.y), 0);
    }

    @Unique
    private void minechants$breakBlastArea(ServerPlayerInteractionManager manager, Vec3d center, double radius, BlockPos origin) {
        int blockRadius = (int) Math.ceil(radius);
        BlockPos centerPos = BlockPos.ofFloored(center);
        for (BlockPos target : BlockPos.iterate(centerPos.add(-blockRadius, -blockRadius, -blockRadius), centerPos.add(blockRadius, blockRadius, blockRadius))) {
            if (target.equals(origin)) {
                continue;
            }

            if (Vec3d.ofCenter(target).squaredDistanceTo(center) > radius * radius) {
                continue;
            }

            manager.tryBreakBlock(target.toImmutable());
        }
    }

    @Unique
    private BlockPos[] minechants$getMinerOffsets(BlockPos forward) {
        List<BlockPos> offsets = new ArrayList<>(8);
        BlockPos axisA = this.minechants$getPrimaryPerpendicularAxis(forward);
        BlockPos axisB = this.minechants$getSecondaryPerpendicularAxis(forward);

        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                if (a == 0 && b == 0) {
                    continue;
                }

                offsets.add(axisA.multiply(a).add(axisB.multiply(b)));
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
        BlockState state = this.player.getEntityWorld().getBlockState(pos);
        if (state.isAir() || state.hasBlockEntity()) {
            return false;
        }

        if (state.isIn(MINECHANTS_THREE_BY_THREE_BLACKLIST)) {
            return false;
        }

        if (!state.isIn(BlockTags.PICKAXE_MINEABLE)) {
            return false;
        }

        return true;
    }
}
