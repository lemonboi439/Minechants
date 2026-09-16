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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
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
    @Unique private static final TagKey<net.minecraft.world.level.block.Block> MINECHANTS_BLACKLIST = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Minechants.MOD_ID, "3x3_miner_blacklist"));
    @Shadow @Final protected ServerPlayer player;
    @Unique private boolean minechants$processing;

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void minechants$mineAdjacent(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() || minechants$processing || !MinechantsConfig.threeByThreeEnabled
                || !FeatureToggleState.isEnabled(player, ToggleFeature.THREE_BY_THREE_MINER)) return;
        ItemStack tool = player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND);
        var lookup = player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        if (lookup.get(ModEnchantments.THREE_BY_THREE_MINER).map(e -> net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(e, tool) > 0).orElse(false) == false) return;
        Vec3 look = player.getLookAngle();
        BlockPos axis = Math.abs(look.x) > Math.abs(look.y) && Math.abs(look.x) > Math.abs(look.z) ? new BlockPos(0, 1, 0) : (Math.abs(look.z) > Math.abs(look.y) ? new BlockPos(0, 1, 0) : new BlockPos(1, 0, 0));
        BlockPos other = axis.getY() != 0 ? new BlockPos(look.x > look.z ? 0 : 0, 0, 1) : new BlockPos(0, 1, 0);
        minechants$processing = true;
        try {
            for (int a = -1; a <= 1; a++) for (int b = -1; b <= 1; b++) {
                if (a == 0 && b == 0) continue;
                BlockPos target = pos.offset(axis.getX() * a + other.getX() * b, axis.getY() * a + other.getY() * b, axis.getZ() * a + other.getZ() * b);
                BlockState state = player.level().getBlockState(target);
                if (!state.isAir() && !state.hasBlockEntity() && state.is(BlockTags.MINEABLE_WITH_PICKAXE) && !state.is(MINECHANTS_BLACKLIST)) {
                    ((ServerPlayerGameMode)(Object)this).destroyBlock(target);
                }
            }
        } finally { minechants$processing = false; }
    }
}


