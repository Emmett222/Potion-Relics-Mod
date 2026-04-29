package net.emmett222.potionrelicsmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.emmett222.potionrelicsmod.blockentities.RelicShrineBlockEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RelicShrineBlockEntityRenderer implements BlockEntityRenderer<RelicShrineBlockEntity> {
    private final ItemRenderer itemRenderer;

    public RelicShrineBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(RelicShrineBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemStack relic = blockEntity.getRelic();
        Level level = blockEntity.getLevel();
        if (relic.isEmpty() || level == null) {
            return;
        }

        float time = level.getGameTime() + partialTick;
        float bobOffset = Mth.sin(time * 0.08f) * 0.04f;
        float spin = (time * 1.5f) % 360.0f;

        poseStack.pushPose();
        poseStack.translate(0.5d, 0.9d + bobOffset, 0.5d);
        poseStack.mulPose(Axis.YP.rotationDegrees(spin));
        poseStack.scale(0.8f, 0.8f, 0.8f);
        itemRenderer.renderStatic(relic, ItemDisplayContext.GROUND, LightTexture.FULL_BRIGHT, packedOverlay,
                poseStack, buffer, level, blockEntity.getBlockPos().hashCode());
        poseStack.popPose();
    }
}
