package com.tyxcnjiu.main.thrown.entity;

import com.tyxcnjiu.main.thrown.init.ModEntities;
import com.tyxcnjiu.main.thrown.util.SpawnEggThrower;
import com.tyxcnjiu.main.thrown.util.BlockPlacer;
import com.tyxcnjiu.main.thrown.util.SpecialThrowEffects;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;


public class ThrownItemEntity extends ThrowableItemProjectile {

    private static final Random RANDOM = new Random();

    private boolean placeBlockMode = false;
    private boolean hitEntity = false;
    private boolean isReturning = false;
    private int returnTicks = 0;
    private static final int MAX_RETURN_TICKS = 100;

    public ThrownItemEntity(EntityType<? extends ThrownItemEntity> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownItemEntity(Level level, LivingEntity entity) {
        super(ModEntities.THROWN_ITEM.get(), entity, level);
    }

    public ThrownItemEntity(Level level, double x, double y, double z) {
        super(ModEntities.THROWN_ITEM.get(), x, y, z, level);
    }

    public void setPlaceBlockMode(boolean placeBlockMode) {
        this.placeBlockMode = placeBlockMode;
    }

    public boolean isReturning() {
        return isReturning;
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SNOWBALL;
    }

    @Override
    public void tick() {
        super.tick();
        if (isReturning) {
            returnTicks++;
            if (returnTicks > MAX_RETURN_TICKS || this.getOwner() == null) {
                this.discard();
                return;
            }
            Vec3 returnVector = this.getOwner().position().subtract(this.position()).normalize();
            this.setDeltaMovement(returnVector.scale(0.5));
            if (this.distanceToSqr(this.getOwner()) < 1.0) {
                returnToOwner(this.getItem());
                this.discard();
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            ItemStack itemStack = this.getItem();
            Item item = itemStack.getItem();

            if (item instanceof SwordItem && hitEntity) {
                isReturning = true;
                return;
            }

            if (item == Items.WITHER_SKELETON_SKULL) {
                WitherSkull witherSkull = EntityType.WITHER_SKULL.create(level());
                if (witherSkull != null) {
                    witherSkull.setPos(this.getX(), this.getY(), this.getZ());
                    witherSkull.setDeltaMovement(this.getDeltaMovement());
                    if (RANDOM.nextFloat() < 0.3f) {
                        witherSkull.setDangerous(true);
                    }
                    level().addFreshEntity(witherSkull);
                }
                this.discard();
                return;
            }


            if (placeBlockMode && result instanceof BlockHitResult blockHitResult) {
                if (item instanceof BlockItem) {
                    Entity owner = this.getOwner();
                    Direction playerFacing = (owner instanceof Player) ? ((Player) owner).getDirection() : Direction.NORTH;
                    Vec3 throwDirection = this.getDeltaMovement().normalize();
                    if (BlockPlacer.placeBlock(itemStack, this.level(), blockHitResult, playerFacing, throwDirection)) {
                        this.discard();
                        return;
                    }
                }
            }

            Vec3 motion = this.getDeltaMovement();
            if (SpecialThrowEffects.handleSpecialItem(itemStack, this.level(), this.position(), motion)) {
                this.discard();
                return;
            }

            if (item instanceof PickaxeItem && result instanceof BlockHitResult blockHitResult) {
                if (mineBlock(itemStack, blockHitResult)) {
                    this.discard();
                    return;
                }
            }

            if (item == Items.TNT) {
                PrimedTnt primedTnt = new PrimedTnt(this.level(), this.getX(), this.getY(), this.getZ(), this.getOwner() instanceof LivingEntity ? (LivingEntity) this.getOwner() : null);
                primedTnt.setFuse(80);
                this.level().addFreshEntity(primedTnt);
            } else if (item == Items.BLAZE_POWDER) {
                this.level().explode(this, this.getX(), this.getY(), this.getZ(), 2.0F, Level.ExplosionInteraction.NONE);
            } else if (item instanceof SpawnEggItem) {
                Vec3 spawnPos = new Vec3(this.getX(), this.getY(), this.getZ());
                if (SpawnEggThrower.trySpawnMob(itemStack, this.level(), spawnPos)) {
                    this.level().addFreshEntity(new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), itemStack));
                }
            } else {
                this.level().addFreshEntity(new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), itemStack));
            }

            this.discard();
        }
    }

    public void returnToOwner(ItemStack itemStack) {
        Entity owner = this.getOwner();
        if (owner instanceof Player player) {
            player.getInventory().add(itemStack);
        }
    }

    public boolean mineBlock(ItemStack itemStack, BlockHitResult blockHitResult) {
        if (itemStack.getItem() instanceof PickaxeItem) {
            BlockPos blockPos = blockHitResult.getBlockPos();
            BlockState blockState = this.level().getBlockState(blockPos);

            if (blockState.getDestroySpeed(this.level(), blockPos) >= 0) {
                this.level().destroyBlock(blockPos, true, this);
                itemStack.hurtAndBreak(1, (LivingEntity) this.getOwner(), (entity) -> {});

                if (itemStack.getDamageValue() >= itemStack.getMaxDamage()) {
                    itemStack.shrink(1);
                }

                if (!itemStack.isEmpty()) {
                    this.level().addFreshEntity(new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), itemStack));
                }

                return true;
            }
        }
        return false;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide()) {
            Entity entity = result.getEntity();
            ItemStack itemStack = this.getItem();

            if (itemStack.getItem() instanceof SwordItem) {
                hitEntity = true;
                itemStack.hurtAndBreak(1, (LivingEntity) this.getOwner(), (player) -> {});
            }

            if (itemStack.getItem() == Items.WITHER_SKELETON_SKULL) {
                if (entity instanceof LivingEntity) {
                    ((LivingEntity) entity).hurt(this.damageSources().magic(), 80f);
                }
            } else if (itemStack.getItem() instanceof SpawnEggItem) {
                Vec3 spawnPos = new Vec3(this.getX(), this.getY(), this.getZ());
                if (SpawnEggThrower.trySpawnMob(itemStack, this.level(), spawnPos)) {
                    String itemNameString = itemStack.getHoverName().getString();
                    float damage = itemNameString.length();
                    entity.hurt(this.damageSources().thrown(this, this.getOwner()), damage);
                }
            } else {
                String itemNameString = itemStack.getHoverName().getString();
                float damage = itemNameString.length();
                entity.hurt(this.damageSources().thrown(this, this.getOwner()), damage);

                if (itemStack.getItem() == Items.BLAZE_POWDER) {
                    this.level().explode(this, this.getX(), this.getY(), this.getZ(), 2.0F, Level.ExplosionInteraction.NONE);
                }
            }
        }
    }


    @Override
    public ItemStack getItem() {
        ItemStack itemstack = this.getItemRaw();
        return itemstack.isEmpty() ? new ItemStack(this.getDefaultItem()) : itemstack;
    }
}

