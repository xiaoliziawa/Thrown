package com.tyxcnjiu.main.thrown.client;

import com.tyxcnjiu.main.thrown.Thrown;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = Thrown.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeyBindings {
    public static final KeyMapping THROW_ITEM_KEY = new KeyMapping("key.throwitem", GLFW.GLFW_KEY_G, "key.categories." + Thrown.MOD_ID);
    public static final KeyMapping PLACE_BLOCK_KEY = new KeyMapping("key.placeblock", GLFW.GLFW_KEY_B, "key.categories." + Thrown.MOD_ID);

    @SubscribeEvent
    public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
        event.register(THROW_ITEM_KEY);
        event.register(PLACE_BLOCK_KEY);
    }
}
