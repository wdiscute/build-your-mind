package com.wdiscute.bym;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import com.wdiscute.bym.registry.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.DecimalFormat;
import java.util.List;

@Mod(BuildYourMind.MOD_ID)
public class BuildYourMind
{
    public static final String MOD_ID = "bym";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceLocation MISSINGNO = rl("missingno");
    public static final DecimalFormat FORMAT = new DecimalFormat("#.##");
    public static final ResourceKey<Level> MIND = ResourceKey.create(Registries.DIMENSION, rl("mind"));

    public static ResourceLocation rl(String s)
    {
        return ResourceLocation.fromNamespaceAndPath(BuildYourMind.MOD_ID, s);
    }

    public BuildYourMind(IEventBus modEventBus, ModContainer modContainer)
    {
        SCCreativeModeTabs.register(modEventBus);
        BYMItems.register(modEventBus);
        BYMDataComponents.register(modEventBus);
        BYMDataAttachments.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.CLIENT, BYMConfig.Client.SPEC_CLIENT);
        modContainer.registerConfig(ModConfig.Type.SERVER, BYMConfig.Server.SPEC_SERVER);
    }

    @Mod(value = BuildYourMind.MOD_ID, dist = Dist.CLIENT)
    public static class Client
    {
        public Client(ModContainer modContainer)
        {
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }

        public static Level getLevel()
        {
            return Minecraft.getInstance().level;
        }
    }

    public static void renderNonMixin(ItemStack stack, ItemDisplayContext displayContext, boolean leftHand, PoseStack poseStack,
                                      MultiBufferSource bufferSource, int combinedLight, int combinedOverlay, BakedModel p_model,
                                      CallbackInfo ci)
    {
        if(!stack.has(BYMDataComponents.STORED_BUILD))
            return;

        StoredBuild build = stack.get(BYMDataComponents.STORED_BUILD);
        if (build == null || build.blocks().isEmpty())
            return;

        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        List<StoredBuild.Duo<BlockPos, BlockState>> blocks = build.blocks();

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        for (StoredBuild.Duo<BlockPos, BlockState> entry : blocks)
        {
            BlockPos pos = entry.first();
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
            minY = Math.min(minY, pos.getY());
            maxY = Math.max(maxY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        int sizeX = maxX - minX + 1;
        int sizeY = maxY - minY + 1;
        int sizeZ = maxZ - minZ + 1;
        int largestSize = Math.max(sizeX, Math.max(sizeY, sizeZ));
        float scale = 1.0f / largestSize;


        poseStack.pushPose();

        // Center in the item's unit cube, then shrink everything to fit

        if(displayContext.equals(ItemDisplayContext.GUI))
        {
            poseStack.scale(scale, scale, scale);
            poseStack.scale(0.6f, 0.6f, 0.6f);
            poseStack.mulPose(Axis.XP.rotationDegrees(30));
            poseStack.mulPose(Axis.YP.rotationDegrees(45));
        }
        else
        {
            poseStack.scale(0.1f, 0.1f, 0.1f);
            poseStack.mulPose(Axis.YP.rotationDegrees(build.rotationY()));
            poseStack.mulPose(Axis.XP.rotationDegrees(build.rotationX()));
            poseStack.translate(0, 2, 0);
            float scaleFP = build.fpScale();
            poseStack.scale(scaleFP, scaleFP, scaleFP);
        }

        poseStack.translate(-0.5, -0.5, -0.5);

        for (StoredBuild.Duo<BlockPos, BlockState> entry : blocks)
        {
            BlockPos pos = entry.first();
            BlockState state = entry.second();
            if (state.isAir()) continue;

            poseStack.pushPose();

            float relX = (pos.getX() - minX) - (sizeX - 1) / 2f;
            float relY = (pos.getY() - minY) - (sizeY - 1) / 2f;
            float relZ = (pos.getZ() - minZ) - (sizeZ - 1) / 2f;
            poseStack.translate(relX, relY, relZ);

            blockRenderer.renderSingleBlock(state, poseStack, bufferSource, combinedLight, combinedOverlay);

            poseStack.popPose();
        }

        poseStack.popPose();
        ci.cancel();
    }
}
