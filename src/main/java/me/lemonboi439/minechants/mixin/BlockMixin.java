package me.lemonboi439.minechants.mixin;

import me.lemonboi439.minechants.config.MinechantsConfig;
import me.lemonboi439.minechants.enchantment.ModEnchantments;
import me.lemonboi439.minechants.util.FeatureToggleState;
import me.lemonboi439.minechants.util.MiningContext;
import me.lemonboi439.minechants.util.ToggleFeature;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.ItemInstance;

@Mixin(Block.class)
public abstract class BlockMixin {
    private static final double MINECHANTS_MAGNETIC_SPEED_MULTIPLIER = 1.3D;

    @Inject(method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemInstance;)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private static void getDroppedStacks(BlockState state, ServerLevel world, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemInstance tool, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (!(entity instanceof net.minecraft.server.level.ServerPlayer player)
                || !MinechantsConfig.smeltingEnabled
                || !FeatureToggleState.isEnabled(player, ToggleFeature.SMELTING)) {
            return;
        }

        var enchantmentLookup = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var smeltingEntry = enchantmentLookup.getOrThrow(ModEnchantments.SMELTING);
        if (EnchantmentHelper.getItemEnchantmentLevel(smeltingEntry, tool) > 0) {
            var silkTouchEntry = enchantmentLookup.getOrThrow(Enchantments.SILK_TOUCH);
            if (EnchantmentHelper.getItemEnchantmentLevel(silkTouchEntry, tool) > 0) {
                return;
            }

            var input = new SingleRecipeInput(new ItemStack(state.getBlock().asItem()));
            Optional<RecipeHolder<SmeltingRecipe>> recipe = world.recipeAccess()
                    .getRecipeFor(RecipeType.SMELTING, input, world);

            if (recipe.isPresent()) {
                ItemStack result = recipe.get().value().assemble(input);
                if (!result.isEmpty()) {
                    cir.setReturnValue(Collections.singletonList(result));
                }
            }
        }
    }

    @Inject(method = "popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
    private static void minechants$pullDropsMagnetically(net.minecraft.world.level.Level world, BlockPos pos, ItemStack stack, CallbackInfo ci) {
        if (!(world instanceof ServerLevel serverWorld) || stack.isEmpty()) {
            return;
        }

        MiningContext.Context context = MiningContext.getCurrent();
        if (context == null || context.player() == null || context.tool().isEmpty()) {
            return;
        }

        if (context.player().level() != serverWorld
                || !MinechantsConfig.magneticEnabled
                || !FeatureToggleState.isEnabled(context.player(), ToggleFeature.MAGNETIC)) {
            return;
        }

        var enchantmentLookup = serverWorld.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var magneticEntry = enchantmentLookup.getOrThrow(ModEnchantments.MAGNETIC);
        if (EnchantmentHelper.getItemEnchantmentLevel(magneticEntry, context.tool()) <= 0) {
            return;
        }

        ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack);

        Vec3 playerCenter = new Vec3(context.player().getX(), context.player().getY() + 0.75D, context.player().getZ());
        Vec3 delta = playerCenter.subtract(itemEntity.getX(), itemEntity.getY(), itemEntity.getZ());
        if (delta.lengthSqr() > 1.0E-6D) {
            Vec3 velocity = delta.normalize() .scale(
                    Math.min(0.65D, 0.22D + (delta.length() * 0.05D)) * MINECHANTS_MAGNETIC_SPEED_MULTIPLIER
            );
            itemEntity.setDeltaMovement(velocity);
        }

        ci.cancel();
        world.addFreshEntity(itemEntity);
    }
}
