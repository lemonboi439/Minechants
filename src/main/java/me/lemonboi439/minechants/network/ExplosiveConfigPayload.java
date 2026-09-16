package me.lemonboi439.minechants.network;

import me.lemonboi439.minechants.Minechants;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ExplosiveConfigPayload(boolean disableExplosiveOnServer) implements CustomPayload {
    public static final CustomPayload.Id<ExplosiveConfigPayload> ID =
            new CustomPayload.Id<>(Identifier.of(Minechants.MOD_ID, "explosive_config"));
    public static final PacketCodec<RegistryByteBuf, ExplosiveConfigPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOLEAN,
            ExplosiveConfigPayload::disableExplosiveOnServer,
            ExplosiveConfigPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
