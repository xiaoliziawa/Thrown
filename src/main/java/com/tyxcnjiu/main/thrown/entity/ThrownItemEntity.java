package com.tyxcnjiu.main.thrown.entity;

import com.tyxcnjiu.main.thrown.init.ModEntities;
import com.tyxcnjiu.main.thrown.util.SpawnEggThrower;
import com.tyxcnjiu.main.thrown.util.BlockPlacer;
import com.tyxcnjiu.main.thrown.util.SpecialThrowEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraft.world.entity.item.*;
import net.minecraft.core.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.*;
import net.minecraft.nbt.CompoundTag;

import java.lang.reflect.Method;
import java.util.Random;

public class ThrownItemEntity extends ThrowableItemProjectile {
    private static final Random RANDOM = new Random();
    private static final int MAX_RETURN_TICKS = 100;
    private boolean placeBlockMode, hitEntity, isReturning, shouldExplode;
    private int returnTicks;

    public ThrownItemEntity(EntityType<? extends ThrownItemEntity> entityType, Level level) { super(entityType, level); }
    public ThrownItemEntity(Level level, LivingEntity entity) { super(ModEntities.THROWN_ITEM.get(), entity, level); }
    public ThrownItemEntity(Level level, double x, double y, double z) { super(ModEntities.THROWN_ITEM.get(), x, y, z, level); }

    public void setPlaceBlockMode(boolean mode) { this.placeBlockMode = mode; }
    public void setShouldExplode(boolean explode) { this.shouldExplode = explode; }
    public boolean isReturning() { return isReturning; }

    @Override protected Item getDefaultItem() { return Items.SNOWBALL; }

    @Override
    public void tick() {
        super.tick();
        if (isReturning && (++returnTicks > MAX_RETURN_TICKS || this.getOwner() == null || this.distanceToSqr(this.getOwner()) < 1.0)) {
            if (this.getOwner() instanceof Player player) player.getInventory().add(this.getItem());
            this.discard();
        } else if (isReturning) {
            this.setDeltaMovement(this.getOwner().position().subtract(this.position()).normalize().scale(0.5));
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level().isClientSide()) return;

        ItemStack itemStack = this.getItem();
        Item item = itemStack.getItem();

        if (item instanceof SwordItem && hitEntity) {
            isReturning = true;
            return;
        }

        if (item instanceof BucketItem && result instanceof BlockHitResult) {
            handleBucketHit((BlockHitResult) result, (BucketItem) item);
        } else if (item == Items.WITHER_SKELETON_SKULL) {
            spawnWitherSkull();
        } else if (placeBlockMode && result instanceof BlockHitResult && item instanceof BlockItem) {
            handleBlockPlacement(itemStack, (BlockHitResult) result);
        } else if (SpecialThrowEffects.handleSpecialItem(itemStack, this.level(), this.position(), this.getDeltaMovement())) {
            this.discard();
        } else if (item instanceof PickaxeItem && result instanceof BlockHitResult && mineBlock(itemStack, (BlockHitResult) result)) {
            this.discard();
        } else {
            handleRegularItemHit(item, itemStack);
        }
    }

    private void handleBucketHit(BlockHitResult result, BucketItem bucketItem) {
        BlockPos placePos = result.getBlockPos().relative(result.getDirection());
        Fluid fluid = bucketItem.getFluid();
        if (fluid != Fluids.EMPTY) {
            BlockState fluidState = fluid.defaultFluidState().createLegacyBlock();
            if (fluidState != null) {
                this.level().setBlock(placePos, fluidState, 3);
                spawnItemDrop(new ItemStack(Items.BUCKET));
                if (bucketItem instanceof MobBucketItem) handleMobBucket(bucketItem, placePos);
            } else {
                spawnItemDrop(this.getItem());
            }
        } else {
            spawnItemDrop(this.getItem());
        }
        this.discard();
    }

    private void handleMobBucket(BucketItem bucketItem, BlockPos placePos) {
        try {
            Method getFishTypeMethod = MobBucketItem.class.getDeclaredMethod("getFishType");
            getFishTypeMethod.setAccessible(true);
            EntityType<?> entityType = (EntityType<?>) getFishTypeMethod.invoke(bucketItem);
            Entity entity = entityType.create(this.level());
            if (entity != null) {
                CompoundTag bucketTag = this.getItem().getTag();
                if (bucketTag != null) {
                    if (bucketTag.contains("Variant")) entity.load(bucketTag);
                    else if (bucketTag.contains("BucketVariantTag")) entity.load(bucketTag.getCompound("BucketVariantTag"));
                    else if (bucketTag.contains("EntityTag")) entity.load(bucketTag.getCompound("EntityTag"));
                }
                entity.moveTo(placePos.getX() + 0.5, placePos.getY(), placePos.getZ() + 0.5);
                this.level().addFreshEntity(entity);
            }
        } catch (Exception e) {
            //
        }
    }

    private void spawnWitherSkull() {
        WitherSkull witherSkull = EntityType.WITHER_SKULL.create(level());
        if (witherSkull != null) {
            witherSkull.setPos(this.position());
            witherSkull.setDeltaMovement(this.getDeltaMovement());
            if (RANDOM.nextFloat() < 0.3f) witherSkull.setDangerous(true);
            level().addFreshEntity(witherSkull);
        }
        this.discard();
    }

    private void handleBlockPlacement(ItemStack itemStack, BlockHitResult blockHitResult) {
        Entity owner = this.getOwner();
        Direction playerFacing = (owner instanceof Player) ? ((Player) owner).getDirection() : Direction.NORTH;
        if (BlockPlacer.placeBlock(itemStack, this.level(), blockHitResult, playerFacing, this.getDeltaMovement().normalize())) {
            this.discard();
        }
    }

    private boolean mineBlock(ItemStack itemStack, BlockHitResult blockHitResult) {
        BlockPos blockPos = blockHitResult.getBlockPos();
        BlockState blockState = this.level().getBlockState(blockPos);
        if (blockState.getDestroySpeed(this.level(), blockPos) >= 0) {
            this.level().destroyBlock(blockPos, true, this);
            itemStack.hurtAndBreak(1, (LivingEntity) this.getOwner(), (entity) -> {});
            if (itemStack.getDamageValue() >= itemStack.getMaxDamage()) itemStack.shrink(1);
            if (!itemStack.isEmpty()) spawnItemDrop(itemStack);
            return true;
        }
        return false;
    }

    private void handleRegularItemHit(Item item, ItemStack itemStack) {
        if (item == Items.TNT) {
            if (shouldExplode) {
                PrimedTnt primedTnt = new PrimedTnt(this.level(), this.getX(), this.getY(), this.getZ(), this.getOwner() instanceof LivingEntity ? (LivingEntity) this.getOwner() : null);
                primedTnt.setFuse(0);
                this.level().addFreshEntity(primedTnt);
            } else {
                spawnItemDrop(new ItemStack(Items.TNT));
            }
        } else if (item == Items.BLAZE_POWDER) {
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), 2.0F, Level.ExplosionInteraction.NONE);
        } else if (item instanceof SpawnEggItem) {
            Vec3 spawnPos = this.position();
            if (SpawnEggThrower.trySpawnMob(itemStack, this.level(), spawnPos)) {
                spawnItemDrop(itemStack);
            }
        } else {
            spawnItemDrop(itemStack);
        }
        this.discard();
    }

    private void spawnItemDrop(ItemStack itemStack) {
        this.level().addFreshEntity(new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), itemStack));
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide()) return;

        Entity entity = result.getEntity();
        ItemStack itemStack = this.getItem();
        Item item = itemStack.getItem();

        if (item instanceof SwordItem) {
            hitEntity = true;
            itemStack.hurtAndBreak(1, (LivingEntity) this.getOwner(), (player) -> {});
        }

        float damage = itemStack.getHoverName().getString().length();
        if (item == Items.WITHER_SKELETON_SKULL && entity instanceof LivingEntity) {
            ((LivingEntity) entity).hurt(this.damageSources().magic(), 80f);
        } else {
            entity.hurt(this.damageSources().thrown(this, this.getOwner()), damage);
            if (item == Items.BLAZE_POWDER) {
                this.level().explode(this, this.getX(), this.getY(), this.getZ(), 2.0F, Level.ExplosionInteraction.NONE);
            }
        }

        if (item instanceof SpawnEggItem) {
            Vec3 spawnPos = this.position();
            SpawnEggThrower.trySpawnMob(itemStack, this.level(), spawnPos);
        }
    }

    @Override
    public ItemStack getItem() {
        ItemStack itemstack = this.getItemRaw();
        return itemstack.isEmpty() ? new ItemStack(this.getDefaultItem()) : itemstack;
    }
}
