package me.lemonboi439.minechants.network;

import me.lemonboi439.minechants.Minechants;
import me.lemonboi439.minechants.util.ToggleFeature;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ToggleFeaturePayload(ToggleFeature feature) implements CustomPacketPayload {
    public static final Type<ToggleFeaturePayload> ID = new Type<>(Identifier.fromNamespaceAndPath(Minechants.MOD_ID, "toggle_feature"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleFeaturePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT.map(index -> ToggleFeature.values()[index], ToggleFeature::ordinal),
            ToggleFeaturePayload::feature, ToggleFeaturePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return ID; }
}
