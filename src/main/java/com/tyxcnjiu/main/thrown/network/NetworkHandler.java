package com.tyxcnjiu.main.thrown.network;

import com.tyxcnjiu.main.thrown.Thrown;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Thrown.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void init() {
        int id = 0;
        INSTANCE.registerMessage(id++, ToggleThrowModePacket.class, ToggleThrowModePacket::encode, ToggleThrowModePacket::decode, ToggleThrowModePacket::handle);
        INSTANCE.registerMessage(id++, TogglePlaceBlockModePacket.class, TogglePlaceBlockModePacket::encode, TogglePlaceBlockModePacket::decode, TogglePlaceBlockModePacket::handle);
        INSTANCE.registerMessage(id++, SyncPlayerDataPacket.class, SyncPlayerDataPacket::encode, SyncPlayerDataPacket::decode, SyncPlayerDataPacket::handle);
    }
}
