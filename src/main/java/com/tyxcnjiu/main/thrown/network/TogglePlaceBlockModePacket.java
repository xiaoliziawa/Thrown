package com.tyxcnjiu.main.thrown.network;

import com.tyxcnjiu.main.thrown.player.PlayerDataProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TogglePlaceBlockModePacket {
    private final boolean placeBlockMode;

    public TogglePlaceBlockModePacket(boolean placeBlockMode) {
        this.placeBlockMode = placeBlockMode;
    }

    public static void encode(TogglePlaceBlockModePacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.placeBlockMode);
    }

    public static TogglePlaceBlockModePacket decode(FriendlyByteBuf buf) {
        return new TogglePlaceBlockModePacket(buf.readBoolean());
    }

    public static void handle(TogglePlaceBlockModePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ctx.get().getSender().getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(playerData -> {
                playerData.setPlaceBlockModeEnabled(msg.placeBlockMode);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
