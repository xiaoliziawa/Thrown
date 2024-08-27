package com.tyxcnjiu.main.thrown;

import com.tyxcnjiu.main.thrown.client.ReturningItemRenderer;
import com.tyxcnjiu.main.thrown.init.ModEntities;
import com.tyxcnjiu.main.thrown.player.PlayerData;
import com.tyxcnjiu.main.thrown.player.PlayerDataProvider;
import com.tyxcnjiu.main.thrown.network.NetworkHandler;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Thrown.MOD_ID)
public class Thrown {
    public static final String MOD_ID = "thrown";

    public Thrown() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModEntities.ENTITY_TYPES.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerCapabilities);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, this::attachCapabilities);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::init);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        EntityRenderers.register(ModEntities.THROWN_ITEM.get(), ReturningItemRenderer::new);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(PlayerData.class);
    }

    private void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(PlayerDataProvider.PLAYER_DATA).isPresent()) {
                event.addCapability(new ResourceLocation(MOD_ID, "player_data"), new PlayerDataProvider());
            }
        }
    }
}
