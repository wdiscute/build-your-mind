package com.wdiscute.bym.registry;

import com.mojang.serialization.Codec;
import com.wdiscute.bym.BuildYourMind;
import com.wdiscute.bym.MaybeStack;
import com.wdiscute.bym.MindData;
import net.minecraft.network.codec.ByteBufCodecs;
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

    Supplier<AttachmentType<MaybeStack>> ITEM = ATTACHMENT_TYPES.register(
            "item", () -> AttachmentType.builder(() -> MaybeStack.EMPTY)
                    .serialize(MaybeStack.CODEC)
                    .sync(MaybeStack.STREAM_CODEC)
                    .build()
    );

    Supplier<AttachmentType<String>> ITEM_NAME = ATTACHMENT_TYPES.register(
            "item_name", () -> AttachmentType.builder(() -> "")
                    .serialize(Codec.STRING)
                    .sync(ByteBufCodecs.STRING_UTF8)
                    .build()
    );

    static void register(IEventBus eventBus)
    {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
