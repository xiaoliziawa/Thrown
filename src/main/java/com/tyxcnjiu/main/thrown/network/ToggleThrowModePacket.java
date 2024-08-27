package com.tyxcnjiu.main.thrown.network;

import com.tyxcnjiu.main.thrown.player.PlayerDataProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ToggleThrowModePacket {
    private final boolean throwMode;

    public ToggleThrowModePacket(boolean throwMode) {
        this.throwMode = throwMode;
    }

    public static void encode(ToggleThrowModePacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.throwMode);
    }

    public static ToggleThrowModePacket decode(FriendlyByteBuf buf) {
        return new ToggleThrowModePacket(buf.readBoolean());
    }

    public static void handle(ToggleThrowModePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ctx.get().getSender().getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(playerData -> {
                playerData.setThrowModeEnabled(msg.throwMode);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
