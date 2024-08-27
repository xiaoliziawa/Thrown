package com.tyxcnjiu.main.thrown.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SpawnEggThrower {


    public static boolean trySpawnMob(ItemStack itemStack, Level level, Vec3 position) {
        if (level instanceof ServerLevel serverLevel && itemStack.getItem() instanceof SpawnEggItem spawnEggItem) {
            EntityType<?> entityType = spawnEggItem.getType(itemStack.getTag());
            BlockPos blockPos = new BlockPos((int)position.x, (int)position.y, (int)position.z);

            Mob mob = (Mob) entityType.spawn(
                    serverLevel,
                    itemStack,
                    null,
                    blockPos,
                    MobSpawnType.SPAWN_EGG,
                    true,
                    false
            );

            if (mob != null) {
                mob.moveTo(position.x, position.y, position.z, 0, 0);
                mob.spawnAnim();
                return false;
            }
        }
        return true;
    }

}