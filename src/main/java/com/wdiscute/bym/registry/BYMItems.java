package com.wdiscute.bym.registry;

import com.wdiscute.bym.BuildYourMind;
import com.wdiscute.bym.PaperItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface BYMItems
{
    DeferredRegister.Items ITEMS = DeferredRegister.createItems(BuildYourMind.MOD_ID);

    DeferredItem<Item> PAPER = ITEMS.registerItem("paper", PaperItem::new);

    static void register(IEventBus modEventBus)
    {
        ITEMS.register(modEventBus);
    }
}
