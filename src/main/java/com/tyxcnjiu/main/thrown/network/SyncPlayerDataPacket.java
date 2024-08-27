package com.tyxcnjiu.main.thrown.network;

import com.tyxcnjiu.main.thrown.player.PlayerDataProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.world.entity.player.Player;

import java.util.function.Supplier;

public class SyncPlayerDataPacket {
    private final boolean throwMode;
    private final boolean placeBlockMode;

    public SyncPlayerDataPacket(boolean throwMode, boolean placeBlockMode) {
        this.throwMode = throwMode;
        this.placeBlockMode = placeBlockMode;
    }

    public static void encode(SyncPlayerDataPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.throwMode);
        buf.writeBoolean(msg.placeBlockMode);
    }

    public static SyncPlayerDataPacket decode(FriendlyByteBuf buf) {
        return new SyncPlayerDataPacket(buf.readBoolean(), buf.readBoolean());
    }

    public static void handle(SyncPlayerDataPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = ctx.get().getSender();
            if (player != null) {
                player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(playerData -> {
                    playerData.setThrowModeEnabled(msg.throwMode);
                    playerData.setPlaceBlockModeEnabled(msg.placeBlockMode);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
