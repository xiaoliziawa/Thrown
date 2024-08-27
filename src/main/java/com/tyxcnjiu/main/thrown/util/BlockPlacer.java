package com.tyxcnjiu.main.thrown.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class BlockPlacer {
    public static boolean placeBlock(ItemStack itemStack, Level level, BlockHitResult hitResult, Direction playerFacing, Vec3 throwDirection) {
        if (itemStack.getItem() instanceof BlockItem blockItem) {
            BlockPos pos = hitResult.getBlockPos().relative(hitResult.getDirection());
            BlockState state = blockItem.getBlock().defaultBlockState();

            Direction facingDirection = getFacingDirection(throwDirection, playerFacing);

            if (state.hasProperty(BlockStateProperties.FACING)) {
                state = state.setValue(BlockStateProperties.FACING, facingDirection);
            } else if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, facingDirection.getAxis().isHorizontal() ? facingDirection : playerFacing.getOpposite());
            }

            if (level.setBlock(pos, state, 3)) {
                if (itemStack.hasTag()) {
                    BlockEntity blockEntity = level.getBlockEntity(pos);
                    if (blockEntity != null) {
                        CompoundTag nbt = itemStack.getTag().copy();
                        nbt.remove("x");
                        nbt.remove("y");
                        nbt.remove("z");

                        if (nbt.contains("BlockEntityTag")) {
                            CompoundTag blockEntityTag = nbt.getCompound("BlockEntityTag");
                            if (blockEntityTag.contains("Items")) {
                                blockEntity.load(blockEntityTag);
                            }
                        } else {
                            blockEntity.load(nbt);
                        }
                        blockEntity.setChanged();
                    }
                }
                return true;
            }
        }
        return false;    }

    private static Direction getFacingDirection(Vec3 throwDirection, Direction playerFacing) {
        if (Math.abs(throwDirection.y) > Math.abs(throwDirection.x) && Math.abs(throwDirection.y) > Math.abs(throwDirection.z)) {
            return throwDirection.y > 0 ? Direction.DOWN : Direction.UP;
        }
        return playerFacing.getOpposite();
    }
}
