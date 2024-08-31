package com.tyxcnjiu.main.thrown.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class ThrownBucketEntity extends ThrowableItemProjectile {

    public ThrownBucketEntity(EntityType<? extends ThrownBucketEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.BUCKET;
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (!this.level().isClientSide) {
            Item item = this.getItem().getItem();

            if (item instanceof BucketItem) {
                BucketItem bucketItem = (BucketItem) item;
                Fluid fluid = bucketItem.getFluid();

                if (fluid != Fluids.EMPTY) {
                    if (fluid.defaultFluidState().createLegacyBlock() != null) {
                        this.level().setBlock(result.getBlockPos().relative(result.getDirection()),
                                fluid.defaultFluidState().createLegacyBlock(), 3);
                        this.discard();
                    } else {
                        this.spawnAtLocation(this.getItem());
                    }
                } else {
                    if (this.level().getBlockState(result.getBlockPos()).getBlock() instanceof LiquidBlock) {
                        LiquidBlock liquidBlock = (LiquidBlock) this.level().getBlockState(result.getBlockPos()).getBlock();
                        Fluid pickedFluid = liquidBlock.getFluid();

                        if (pickedFluid != Fluids.EMPTY) {
                            this.level().setBlock(result.getBlockPos(), Blocks.AIR.defaultBlockState(), 3);
                            ItemStack filledBucket = new ItemStack(pickedFluid.getBucket());
                            ItemEntity itemEntity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), filledBucket);
                            this.level().addFreshEntity(itemEntity);
                            this.discard();
                        }
                    } else {
                        this.spawnAtLocation(this.getItem());
                    }
                }
            }
        }
    }
}
