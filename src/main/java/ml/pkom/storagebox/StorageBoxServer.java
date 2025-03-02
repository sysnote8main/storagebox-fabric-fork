package ml.pkom.storagebox;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;

public class StorageBoxServer {

    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(StorageBoxMod.id("key"), (server, player, handler, buf, responseSender) -> {
            String str = buf.readNbt().getString("type");
            if (str.equals("put_out"))
                server.execute(() -> {
                    if (player.getMainHandStack().getItem() instanceof StorageBoxItem) {
                        ItemStack itemStack = player.getMainHandStack();
                        StorageBoxItem item = (StorageBoxItem) itemStack.getItem();
                        item.keyboardEvent(0, player, itemStack);
                    }
                });
            if (str.equals("put_out_and_throw"))
                server.execute(() -> {
                    if (player.getMainHandStack().getItem() instanceof StorageBoxItem) {
                        ItemStack itemStack = player.getMainHandStack();
                        StorageBoxItem item = (StorageBoxItem) itemStack.getItem();
                        item.keyboardEvent(1, player, itemStack);
                    }
                });
            if (str.equals("put_in"))
                server.execute(() -> {
                    if (player.getMainHandStack().getItem() instanceof StorageBoxItem) {
                        ItemStack itemStack = player.getMainHandStack();
                        StorageBoxItem item = (StorageBoxItem) itemStack.getItem();
                        item.keyboardEvent(2, player, itemStack);
                    }
                });
            if (str.equals("auto_collect"))
                server.execute(() -> {
                    if (player.getMainHandStack().getItem() instanceof StorageBoxItem) {
                        ItemStack itemStack = player.getMainHandStack();
                        StorageBoxItem item = (StorageBoxItem) itemStack.getItem();
                        item.keyboardEvent(3, player, itemStack);
                    }
                });
        });
    }
}
