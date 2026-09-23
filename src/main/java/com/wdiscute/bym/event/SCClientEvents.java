package com.wdiscute.bym.event;

import com.wdiscute.bym.BuildYourMind;
import com.wdiscute.bym.MindGuiLayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

@EventBusSubscriber(modid = BuildYourMind.MOD_ID, value = Dist.CLIENT)
public class SCClientEvents
{
    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event)
    {
        event.registerAboveAll(BuildYourMind.rl("mind_layer"), new MindGuiLayer());
    }
}
