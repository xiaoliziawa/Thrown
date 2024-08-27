package com.tyxcnjiu.main.thrown.init;

import com.tyxcnjiu.main.thrown.Thrown;
import com.tyxcnjiu.main.thrown.entity.ThrownItemEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Thrown.MOD_ID);

    public static final RegistryObject<EntityType<ThrownItemEntity>> THROWN_ITEM = ENTITY_TYPES.register("thrown_item",
            () -> EntityType.Builder.<ThrownItemEntity>of(ThrownItemEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("thrown_item"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
