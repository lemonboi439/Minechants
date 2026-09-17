package me.lemonboi439.minechants.network;

import me.lemonboi439.minechants.Minechants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ExplosiveConfigPayload(boolean disableExplosiveOnServer) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ExplosiveConfigPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Minechants.MOD_ID, "explosive_config"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ExplosiveConfigPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            ExplosiveConfigPayload::disableExplosiveOnServer,
            ExplosiveConfigPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}








