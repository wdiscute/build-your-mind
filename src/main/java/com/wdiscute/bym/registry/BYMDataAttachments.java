package com.wdiscute.bym.registry;

import com.wdiscute.bym.BuildYourMind;
import com.wdiscute.bym.MindData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public interface BYMDataAttachments
{
    DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(
            NeoForgeRegistries.ATTACHMENT_TYPES, BuildYourMind.MOD_ID);

    Supplier<AttachmentType<MindData>> MIND_DATA = ATTACHMENT_TYPES.register(
            "mind_data", () -> AttachmentType.builder(() -> MindData.EMPTY)
                    .serialize(MindData.CODEC)
                    .sync(MindData.STREAM_CODEC)
                    .build()
    );

    static void register(IEventBus eventBus)
    {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
