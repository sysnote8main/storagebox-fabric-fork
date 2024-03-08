package ml.pkom.storagebox;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public class StorageBoxServer {

    public static void init() {
        PayloadTypeRegistry.playC2S().register(KeyPayload.ID, KeyPayload.CODEC);

        ServerPlayConnectionEvents.INIT.register((handler, server) -> {
            ServerPlayNetworking.registerReceiver(handler, KeyPayload.ID, ((payload, context) -> {
                String str = payload.data();
                ServerPlayerEntity player = context.player();
                player.server.execute(() -> {
                            if (context.player().getMainHandStack().getItem() instanceof StorageBoxItem) {
                                ItemStack itemStack = player.getMainHandStack();
                                switch (str) {
                                    case StorageBoxNetworkKey.PUT_OUT:
                                        StorageBoxItem.keyboardEvent(0, player, itemStack);
                                        break;
                                    case StorageBoxNetworkKey.PUT_OUT_AND_THROW:
                                        StorageBoxItem.keyboardEvent(1, player, itemStack);
                                        break;
                                    case StorageBoxNetworkKey.PUT_IN:
                                        StorageBoxItem.keyboardEvent(2, player, itemStack);
                                        break;
                                    case StorageBoxNetworkKey.AUTO_COLLECT:
                                        StorageBoxItem.keyboardEvent(3, player, itemStack);
                                        break;
                                }
                            }
                        }
                );

            }));
        });
    }
}
