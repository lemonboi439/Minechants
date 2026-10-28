package me.lemonboi439.minechants.mixin;

import me.lemonboi439.minechants.Minechants;
import me.lemonboi439.minechants.config.MinechantsConfig;
import me.lemonboi439.minechants.enchantment.ModEnchantments;
import me.lemonboi439.minechants.util.FeatureToggleState;
import me.lemonboi439.minechants.util.ToggleFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {
    @Unique
    private static final TagKey<Block> MINECHANTS_BLACKLIST = TagKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(Minechants.MOD_ID, "3x3_miner_blacklist")
    );

    @Unique
    private static final double MINECHANTS_MAGNET_RADIUS = 8.0D;

    @Shadow @Final protected ServerPlayer player;
    @Shadow protected ServerLevel level;

    @Unique private boolean minechants$processingExplosiveBreak;
    @Unique private boolean minechants$processingMinerBreak;
    @Unique private Set<UUID> minechants$dropsBeforeBreak;
    @Unique private boolean minechants$smeltPending;
    @Unique private boolean minechants$magnetPending;

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void minechants$captureBreakContext(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        this.minechants$dropsBeforeBreak = null;
        this.minechants$smeltPending = false;
        this.minechants$magnetPending = false;

        ItemStack tool = this.player.getItemInHand(InteractionHand.MAIN_HAND);
        var enchantments = this.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

        this.minechants$smeltPending = FeatureToggleState.isEnabled(this.player, ToggleFeature.SMELTING)
                && EnchantmentHelper.getItemEnchantmentLevel(
                        enchantments.getOrThrow(ModEnchantments.SMELTING), tool) > 0;
        this.minechants$magnetPending = FeatureToggleState.isEnabled(this.player, ToggleFeature.MAGNETIC)
                && EnchantmentHelper.getItemEnchantmentLevel(
                        enchantments.getOrThrow(ModEnchantments.MAGNETIC), tool) > 0;

        // The smelt pass has to tell this break's drops apart from items already lying in the same
        // block space, and the block is air by the time destroyBlock returns.
        if (this.minechants$smeltPending) {
            this.minechants$dropsBeforeBreak = this.minechants$itemsInBlockSpace(pos).stream()
                    .map(ItemEntity::getUUID)
                    .collect(Collectors.toSet());
        }
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void minechants$breakAdditionalBlocks(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Set<UUID> dropsBeforeBreak = this.minechants$dropsBeforeBreak;
        boolean smelt = this.minechants$smeltPending;
        boolean magnet = this.minechants$magnetPending;
        this.minechants$dropsBeforeBreak = null;
        this.minechants$smeltPending = false;
        this.minechants$magnetPending = false;

        if (!cir.getReturnValue()) {
            return;
        }

        ItemStack tool = this.player.getItemInHand(InteractionHand.MAIN_HAND);
        var enchantments = this.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

        // Runs before the chain-mining passes below so those nested destroyBlock calls can capture
        // their own drop context without clobbering this one.
        if (smelt) {
            this.minechants$smeltDrops(pos, dropsBeforeBreak, tool, enchantments);
        }
        if (magnet) {
            this.minechants$magnetizeDrops(pos);
        }

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
                    this.level.explode(
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
    private List<ItemEntity> minechants$itemsInBlockSpace(BlockPos pos) {
        return this.level.getEntitiesOfClass(
                ItemEntity.class,
                new AABB(pos).inflate(0.1D),
                entity -> entity.isAlive()
        );
    }

    @Unique
    private void minechants$smeltDrops(
            BlockPos pos,
            Set<UUID> dropsBeforeBreak,
            ItemStack tool,
            HolderLookup.RegistryLookup<Enchantment> enchantments
    ) {
        // Vanilla lets silk touch and smelting coexist only via commands, and every ore block item has
        // a furnace recipe, so smelting a silk-touched drop would silently eat the ore block.
        if (EnchantmentHelper.getItemEnchantmentLevel(
                enchantments.getOrThrow(Enchantments.SILK_TOUCH), tool) > 0) {
            return;
        }

        var recipeManager = this.level.recipeAccess();
        float experience = 0.0F;

        for (ItemEntity entity : this.minechants$itemsInBlockSpace(pos)) {
            if (dropsBeforeBreak != null && dropsBeforeBreak.contains(entity.getUUID())) {
                continue;
            }

            ItemStack drop = entity.getItem();
            if (drop.isEmpty()) {
                continue;
            }

            SingleRecipeInput input = new SingleRecipeInput(drop);
            Optional<RecipeHolder<SmeltingRecipe>> recipe =
                    recipeManager.getRecipeFor(RecipeType.SMELTING, input, this.level);
            if (recipe.isEmpty()) {
                continue;
            }

            // Scaling by the drop count keeps Fortune yields intact, and leaves ores whose drop is
            // already the smelted result (coal, diamond, redstone) untouched.
            ItemStack smelted = recipe.get().value().assemble(input);
            if (smelted.isEmpty()) {
                continue;
            }

            int count = drop.getCount();
            smelted.setCount(smelted.getCount() * count);
            entity.setItem(smelted);
            experience += recipe.get().value().experience() * count;
        }

        int orbs = Mth.floor(experience);
        if (this.level.getRandom().nextFloat() < experience - orbs) {
            orbs++;
        }
        if (orbs > 0) {
            ExperienceOrb.award(this.level, Vec3.atCenterOf(pos), orbs);
        }
    }

    @Unique
    private void minechants$magnetizeDrops(BlockPos pos) {
        Vec3 center = Vec3.atCenterOf(pos);
        Vec3 target = this.player.position().add(0.0D, this.player.getBbHeight() * 0.5D, 0.0D);
        AABB area = new AABB(pos).inflate(MINECHANTS_MAGNET_RADIUS);

        for (ItemEntity entity : this.level.getEntitiesOfClass(ItemEntity.class, area, ItemEntity::isAlive)) {
            Vec3 toTarget = target.subtract(entity.position());
            double distance = toTarget.length();
            if (distance < 1.0E-4D) {
                continue;
            }

            // Lift matters as much as speed: ground friction is ~0.6 per tick, so an item that stays
            // on the floor only slides a couple of blocks no matter how hard it is launched.
            Vec3 impulse = toTarget.scale(1.0D / distance)
                    .scale(Mth.clamp(distance * 0.18D, 0.25D, 0.85D))
                    .add(0.0D, 0.25D + distance * 0.02D, 0.0D);
            entity.setDeltaMovement(impulse);
            entity.setPickUpDelay(0);
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
        BlockState state = this.level.getBlockState(pos);
        return !state.isAir() && !state.hasBlockEntity()
                && state.is(BlockTags.MINEABLE_WITH_PICKAXE)
                && !state.is(MINECHANTS_BLACKLIST)
                && tool.canDestroyBlock(state, this.level, pos, this.player);
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
