package com.tyxcnjiu.main.thrown.client;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;
import com.tyxcnjiu.main.thrown.entity.ThrownItemEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public class ReturningItemRenderer extends ThrownItemRenderer {

    public ReturningItemRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ThrownItemEntity entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        if (entity.isReturning()) {
            matrixStack.mulPose(new Quaternionf().rotationY((float) Math.toRadians(entity.tickCount * 20)));
        }
        super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownItemEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
