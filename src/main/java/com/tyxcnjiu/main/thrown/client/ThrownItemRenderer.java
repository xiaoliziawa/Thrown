package com.tyxcnjiu.main.thrown.client;

import com.tyxcnjiu.main.thrown.entity.ThrownItemEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ThrownItemRenderer extends EntityRenderer<ThrownItemEntity> {
    private final ItemRenderer itemRenderer;

    public ThrownItemRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.75F;
    }

    @Override
    public void render(ThrownItemEntity entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        matrixStack.pushPose();
        matrixStack.translate(0.0D, 0.15D, 0.0D);
        matrixStack.mulPose(new Quaternionf().rotationY(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
        matrixStack.mulPose(new Quaternionf().rotationZ(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
        matrixStack.mulPose(new Quaternionf().rotationX(45.0F));
        matrixStack.scale(0.5F, 0.5F, 0.5F);
        matrixStack.translate(0.0D, -0.3D, 0.0D);

        ItemStack itemstack = entity.getItem();
        BakedModel bakedmodel = this.itemRenderer.getModel(itemstack, entity.level(), null, entity.getId());
        this.itemRenderer.render(itemstack, ItemDisplayContext.GROUND, false, matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, bakedmodel);

        matrixStack.popPose();
        super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownItemEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
