package me.lemonboi439.minechants.mixin;

import me.lemonboi439.minechants.enchantment.ModEnchantments;
import me.lemonboi439.minechants.util.FeatureToggleState;
import me.lemonboi439.minechants.util.MiningContext;
import me.lemonboi439.minechants.util.ToggleFeature;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Mixin(Block.class)
public abstract class BlockMixin {
    private static final double MINECHANTS_MAGNETIC_SPEED_MULTIPLIER = 1.3D;

    @Inject(method = "getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private static void getDroppedStacks(BlockState state, ServerWorld world, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack tool, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (!(entity instanceof net.minecraft.server.network.ServerPlayerEntity player) || !FeatureToggleState.isEnabled(player, ToggleFeature.SMELTING)) {
            return;
        }

        var enchantmentLookup = world.getRegistryManager().get(RegistryKeys.ENCHANTMENT);
        var smeltingEntry = enchantmentLookup.get(ModEnchantments.SMELTING);
        if (EnchantmentHelper.getLevel(smeltingEntry, tool) <= 0) {
            return;
        }

        var silkTouchEntry = Enchantments.SILK_TOUCH;
        if (EnchantmentHelper.getLevel(silkTouchEntry, tool) > 0) {
            return;
        }

        var inventory = new net.minecraft.inventory.SimpleInventory(new ItemStack(state.getBlock().asItem()));
        Optional<SmeltingRecipe> recipe = world.getRecipeManager()
                .getFirstMatch(RecipeType.SMELTING, inventory, world);

        if (recipe.isPresent()) {
            ItemStack result = recipe.get().craft(inventory, world.getRegistryManager());
            if (!result.isEmpty()) {
                cir.setReturnValue(Collections.singletonList(result));
            }
        }
    }

    @Inject(method = "dropStack(Lnet/minecraft/world/World;Ljava/util/function/Supplier;Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
    private static void minechants$pullDropsMagnetically(net.minecraft.world.World world, Supplier<ItemEntity> itemEntitySupplier, ItemStack stack, CallbackInfo ci) {
        if (!(world instanceof ServerWorld serverWorld) || stack.isEmpty()) {
            return;
        }

        MiningContext.Context context = MiningContext.getCurrent();
        if (context == null || context.player() == null || context.tool().isEmpty()) {
            return;
        }

        if (context.player().getEntityWorld() != serverWorld || !FeatureToggleState.isEnabled(context.player(), ToggleFeature.MAGNETIC)) {
            return;
        }

        var enchantmentLookup = serverWorld.getRegistryManager().get(RegistryKeys.ENCHANTMENT);
        var magneticEntry = enchantmentLookup.get(ModEnchantments.MAGNETIC);
        if (EnchantmentHelper.getLevel(magneticEntry, context.tool()) <= 0) {
            return;
        }

        ItemEntity itemEntity = itemEntitySupplier.get();
        if (itemEntity == null) {
            return;
        }

        Vec3d playerCenter = new Vec3d(context.player().getX(), context.player().getY() + 0.75D, context.player().getZ());
        Vec3d delta = playerCenter.subtract(itemEntity.getX(), itemEntity.getY(), itemEntity.getZ());
        if (delta.lengthSquared() > 1.0E-6D) {
            Vec3d velocity = delta.normalize().multiply(
                    Math.min(0.65D, 0.22D + (delta.length() * 0.05D)) * MINECHANTS_MAGNETIC_SPEED_MULTIPLIER
            );
            itemEntity.setVelocity(velocity);
        }

        ci.cancel();
        world.spawnEntity(itemEntity);
    }
}
