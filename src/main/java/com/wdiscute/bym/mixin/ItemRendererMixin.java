package com.wdiscute.bym.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wdiscute.bym.BuildYourMind;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin
{
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void bym$render(ItemStack itemStack,
                            ItemDisplayContext displayContext,
                            boolean leftHand,
                            PoseStack poseStack,
                            MultiBufferSource bufferSource,
                            int combinedLight,
                            int combinedOverlay,
                            BakedModel p_model,
                            CallbackInfo ci)
    {
        BuildYourMind.renderNonMixin(
                itemStack,
                displayContext,
                leftHand,
                poseStack,
                bufferSource,
                combinedLight,
                combinedOverlay,
                p_model,
                ci
        );


    }
}
