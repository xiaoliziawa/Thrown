package com.tyxcnjiu.main.thrown.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.LightningBolt;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Random;

public class SpecialThrowEffects {

    private static final Random RANDOM = new Random();

    public static boolean handleSpecialItem(ItemStack itemStack, Level level, Vec3 pos, Vec3 motion) {
        if (itemStack.getItem() == Items.NETHER_STAR) {
            spawnWither(level, pos);
            return true;
        } else if (itemStack.getItem() == Items.DRAGON_EGG) {
            spawnEnderDragon(level, pos);
            return true;
        } else if (itemStack.getItem() == Items.ICE) {
            freezeNearbyEntities(level, pos);
            return true;
        } else if (itemStack.getItem() == Items.LIGHTNING_ROD) {
            summonLightning(level, pos);
            return true;
        } else if (itemStack.getItem() == Items.WITHER_SKELETON_SKULL) {
            shootWitherSkull(level, pos, motion);
            return true;
        } else if (itemStack.getItem() == Items.FLINT_AND_STEEL) {
            igniteArea(level, pos);
            return true;
        } else if (itemStack.getItem() == Items.END_CRYSTAL) {
            spawnEndCrystal(level, pos);
            return true;
        } else if (itemStack.getItem() == ForgeRegistries.ITEMS.getValue(new ResourceLocation("thrown", "kzzyc"))) {
            scheduleNullPointerException(level, pos);
            return true;
        }
        return false;
    }

    private static void igniteArea(Level level, Vec3 pos) {
        if (!level.isClientSide()) {
            BlockPos center = new BlockPos((int)pos.x, (int)pos.y, (int)pos.z);
            int radius = 5;

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos targetPos = center.offset(x, 0, z);
                        BlockState targetState = level.getBlockState(targetPos);

                        if (targetState.isAir() || targetState.canBeReplaced()) {
                            level.setBlockAndUpdate(targetPos, Blocks.FIRE.defaultBlockState());
                        }
                    }
                }
            }
        }
    }

    private static void spawnWither(Level level, Vec3 pos) {
        if (level instanceof ServerLevel) {
            WitherBoss wither = EntityType.WITHER.create(level);
            if (wither != null) {
                wither.setPos(pos.x, pos.y, pos.z);
                level.addFreshEntity(wither);
            }
        }
    }

    private static void spawnEnderDragon(Level level, Vec3 pos) {
        if (level instanceof ServerLevel) {
            EnderDragon dragon = EntityType.ENDER_DRAGON.create(level);
            if (dragon != null) {
                dragon.setPos(pos.x, pos.y, pos.z);
                level.addFreshEntity(dragon);
            }
        }
    }

    private static void freezeNearbyEntities(Level level, Vec3 pos) {
        AABB box = new AABB(pos.x - 10, pos.y - 10, pos.z - 10, pos.x + 10, pos.y + 10, pos.z + 10);
        List<Entity> entities = level.getEntities(null, box);

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                LivingEntity livingEntity = (LivingEntity) entity;
                livingEntity.setDeltaMovement(Vec3.ZERO);
                livingEntity.setNoGravity(true);
                livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 255, false, false));
                BlockPos entityPos = entity.blockPosition();
                for (int x = -1; x <= 1; x++) {
                    for (int y = 0; y <= 2; y++) {
                        for (int z = -1; z <= 1; z++) {
                            BlockPos icePos = entityPos.offset(x, y, z);
                            if (level.getBlockState(icePos).isAir()) {
                                level.setBlock(icePos, Blocks.ICE.defaultBlockState(), 3);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void summonLightning(Level level, Vec3 pos) {
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel) level;
            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
            if (lightning != null) {
                lightning.setPos(pos.x, pos.y, pos.z);
                serverLevel.addFreshEntity(lightning);

                AABB box = new AABB(pos.x - 5, pos.y - 5, pos.z - 5, pos.x + 5, pos.y + 5, pos.z + 5);
                List<Entity> entities = level.getEntities(null, box);

                for (Entity entity : entities) {
                    if (entity instanceof LivingEntity) {
                        LivingEntity livingEntity = (LivingEntity) entity;
                        if (livingEntity instanceof Player) {
                            livingEntity.hurt(level.damageSources().lightningBolt(), 500);
                        } else {
                            livingEntity.kill();
                        }
                    }
                }
            }
        }
    }

    private static void shootWitherSkull(Level level, Vec3 pos, Vec3 motion) {
        if (!level.isClientSide()) {
            WitherSkull witherSkull = EntityType.WITHER_SKULL.create(level);
            if (witherSkull != null) {
                witherSkull.setPos(pos.x, pos.y, pos.z);

                witherSkull.setDeltaMovement(motion);

                if (RANDOM.nextFloat() < 0.3f) {
                    witherSkull.setDangerous(true);
                }

                witherSkull.setTicksFrozen(200);

                level.addFreshEntity(witherSkull);
            }
        }
    }

    private static void spawnEndCrystal(Level level, Vec3 pos) {
        if (level instanceof ServerLevel serverLevel) {
            BlockPos blockPos = new BlockPos((int)pos.x, (int)pos.y, (int)pos.z);
            EndCrystal endCrystal = EntityType.END_CRYSTAL.create(serverLevel);
            if (endCrystal != null) {
                endCrystal.moveTo(blockPos, 0.0F, 0.0F);
                serverLevel.addFreshEntity(endCrystal);
            }
        }
    }

    private static void scheduleNullPointerException(Level level, Vec3 pos) {
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel) level;
            serverLevel.getServer().tell(new TickTask(0, () -> {
                throw new Error("Critical error caused by java.lang.NullPointerException item");
            }));
        }
    }
}
