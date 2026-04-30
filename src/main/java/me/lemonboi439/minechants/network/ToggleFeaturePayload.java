package me.lemonboi439.minechants.network;

import me.lemonboi439.minechants.Minechants;
import me.lemonboi439.minechants.util.ToggleFeature;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ToggleFeaturePayload(ToggleFeature feature) implements CustomPayload {
    public static final CustomPayload.Id<ToggleFeaturePayload> ID =
            new CustomPayload.Id<>(Identifier.of(Minechants.MOD_ID, "toggle_feature"));
    public static final PacketCodec<RegistryByteBuf, ToggleFeaturePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.indexed(index -> ToggleFeature.values()[index], ToggleFeature::ordinal),
            ToggleFeaturePayload::feature,
            ToggleFeaturePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
