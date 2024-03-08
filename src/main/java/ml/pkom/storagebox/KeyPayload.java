package ml.pkom.storagebox;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record KeyPayload(String data) implements CustomPayload {
    public static final CustomPayload.Id<KeyPayload> ID = new Id<>(StorageBoxMod.id("key"));
    public static final PacketCodec<PacketByteBuf, KeyPayload> CODEC = PacketCodecs.STRING.xmap(KeyPayload::new, KeyPayload::data).cast();

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
