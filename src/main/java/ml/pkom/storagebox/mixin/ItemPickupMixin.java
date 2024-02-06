package ml.pkom.storagebox.mixin;

import ml.pkom.storagebox.ModConfig;
import ml.pkom.storagebox.StorageBoxItem;
import ml.pkom.storagebox.StorageBoxSlot;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ml.pkom.storagebox.StorageBoxItem.*;

@Mixin(ItemEntity.class)
public class ItemPickupMixin {
    @Unique
    private static boolean process(ItemStack stack, ItemStack pickupStack) {
        // ストレージボックス
        if (stack.getItem() instanceof StorageBoxItem) {
            ItemStack storageBoxStack = stack;
            if (!StorageBoxItem.isAutoCollect(storageBoxStack)) return false;
            ItemStack stackInNbt = getStackInStorageBox(storageBoxStack);
            if (stackInNbt == null) return false;
            if (stackInNbt.getItem() == pickupStack.getItem()) {
                if (!StorageBoxSlot.canInsertStack(pickupStack)) return false;
                setItemStackSize(storageBoxStack, getItemDataAsInt(storageBoxStack, KEY_SIZE) + pickupStack.getCount());
                return true;
            }
        }

        Boolean supportSimpleBackpack = ModConfig.getBoolean("SupportSimpleBackpack");
        if (supportSimpleBackpack == null) supportSimpleBackpack = true;
        // SimpleBackpackのサポート
        if (supportSimpleBackpack && Registry.ITEM.getId(stack.getItem()).equals(new Identifier("simple_backpack", "backpack"))) {
            NbtCompound nbt = stack.getNbt();
            if (nbt.contains("backpack")) {
                nbt = nbt.getCompound("backpack");
                DefaultedList<ItemStack> items = DefaultedList.ofSize(54, ItemStack.EMPTY);
                Inventories.readNbt(nbt, items);
                for (ItemStack inStack : items) {
                    if (process(inStack, pickupStack)) {
                        return true;
                    }
                }
            }
        }

        Boolean supportShulkerBox = ModConfig.getBoolean("SupportShulkerBox");
        if (supportShulkerBox == null) supportShulkerBox = true;
        // シュルカーボックスのサポート
        if (supportShulkerBox && stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof ShulkerBoxBlock) {
            NbtCompound nbt = stack.getNbt();
            if (nbt.contains("BlockEntityTag")) {
                nbt = nbt.getCompound("BlockEntityTag");
                DefaultedList<ItemStack> items = DefaultedList.ofSize(27, ItemStack.EMPTY);
                Inventories.readNbt(nbt, items);
                for (ItemStack inStack : items) {
                    if (process(inStack, pickupStack)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Inject(method = "onPlayerCollision", at = @At(value = "HEAD"), cancellable = true)
    private void onPickup(PlayerEntity player, CallbackInfo ci) {
        ItemEntity itemEntity = (ItemEntity) (Object) this;

        Boolean supportEnderChest = ModConfig.getBoolean("SupportEnderChest");
        if (supportEnderChest == null) supportEnderChest = true;

        if (!itemEntity.world.isClient) {
            ItemStack itemStack = itemEntity.getStack();
            Item item = itemStack.getItem();
            int count = itemStack.getCount();
            if (((ItemEntityAccessor)itemEntity).getPickupDelay() == 0 && (((ItemEntityAccessor)itemEntity).getOwner() == null || ((ItemEntityAccessor)itemEntity).getOwner().equals(player.getUuid()))) {

                boolean insertedBox = false;
                boolean checkedEnderChest = false;
                // インベントリ
                for (ItemStack inStack : player.getInventory().main) {
                    // エンダーチェストが含まれていたらエンダーチェストもループ処理
                    if (supportEnderChest && inStack.getItem() == Items.ENDER_CHEST && !checkedEnderChest) {
                        for (ItemStack enderChestStack : player.getEnderChestInventory().stacks) {
                            if (enderChestStack.hasNbt()) {
                                if (process(enderChestStack, itemStack)) {
                                    insertedBox = true;
                                    itemStack = ItemStack.EMPTY;
                                    checkedEnderChest = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (inStack.hasNbt()) {
                        if (process(inStack, itemStack)) {
                            insertedBox = true;
                            itemStack = ItemStack.EMPTY;
                            break;
                        }
                    }
                }

                if (!insertedBox) {
                    // オフハンド
                    if (player.getOffHandStack().hasNbt()) {
                        if (process(player.getOffHandStack(), itemStack)) {
                            insertedBox = true;
                            itemStack = ItemStack.EMPTY;
                        }
                    }
                }

                if (insertedBox) {
                    player.sendPickup(itemEntity, count);
                    if (itemStack.isEmpty()) {
                        itemEntity.discard();
                        itemStack.setCount(count);
                    }

                    player.increaseStat(Stats.PICKED_UP.getOrCreateStat(item), count);
                    player.triggerItemPickedUpByEntityCriteria(itemEntity);
                    ci.cancel();
                }
            }
        }
    }
}