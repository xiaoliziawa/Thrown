package com.tyxcnjiu.main.thrown.event;

import com.tyxcnjiu.main.thrown.Thrown;
import com.tyxcnjiu.main.thrown.entity.ThrownItemEntity;
import com.tyxcnjiu.main.thrown.client.KeyBindings;
import com.tyxcnjiu.main.thrown.player.PlayerDataProvider;
import com.tyxcnjiu.main.thrown.network.NetworkHandler;
import com.tyxcnjiu.main.thrown.network.ToggleThrowModePacket;
import com.tyxcnjiu.main.thrown.network.TogglePlaceBlockModePacket;
import com.tyxcnjiu.main.thrown.network.SyncPlayerDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraft.server.level.ServerPlayer;

@Mod.EventBusSubscriber(modid = Thrown.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerEventHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.side.isClient()) {
            Player player = event.player;
            player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(playerData -> {
                if (KeyBindings.THROW_ITEM_KEY.consumeClick()) {
                    boolean newThrowMode = !playerData.isThrowModeEnabled();
                    playerData.setThrowModeEnabled(newThrowMode);
                    player.displayClientMessage(Component.literal("Throw mode: " + (newThrowMode ? "Enabled" : "Disabled")), true);
                    NetworkHandler.INSTANCE.sendToServer(new ToggleThrowModePacket(newThrowMode));
                }
                if (KeyBindings.PLACE_BLOCK_KEY.consumeClick()) {
                    boolean newPlaceBlockMode = !playerData.isPlaceBlockModeEnabled();
                    playerData.setPlaceBlockModeEnabled(newPlaceBlockMode);
                    player.displayClientMessage(Component.literal("Place block mode: " + (newPlaceBlockMode ? "Enabled" : "Disabled")), true);
                    NetworkHandler.INSTANCE.sendToServer(new TogglePlaceBlockModePacket(newPlaceBlockMode));
                }
            });
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack itemStack = event.getItemStack();

        if (itemStack.getItem() instanceof ArmorItem) {
            return;
        }

        player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(playerData -> {
            if (playerData.isThrowModeEnabled()) {
                Level level = player.level();

                if (!itemStack.isEmpty() && !level.isClientSide()) {
                    ThrownItemEntity thrownItem = new ThrownItemEntity(level, player);
                    thrownItem.setItem(itemStack.copy());
                    thrownItem.setPlaceBlockMode(playerData.isPlaceBlockModeEnabled());
                    thrownItem.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
                    level.addFreshEntity(thrownItem);
                    player.swing(InteractionHand.MAIN_HAND, true);
                    if (!player.getAbilities().instabuild) {
                        itemStack.shrink(1);
                    }
                    event.setCanceled(true);
                }
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getOriginal().getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(oldStore -> {
                event.getEntity().getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(newStore -> {
                    newStore.setThrowModeEnabled(oldStore.isThrowModeEnabled());
                    newStore.setPlaceBlockModeEnabled(oldStore.isPlaceBlockModeEnabled());
                });
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(playerData -> {
            playerData.resetState();
            if (player instanceof ServerPlayer serverPlayer) {
                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new SyncPlayerDataPacket(playerData.isThrowModeEnabled(), playerData.isPlaceBlockModeEnabled()));
            }
        });
    }
}
