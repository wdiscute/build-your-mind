package com.wdiscute.bym.registry;

import com.wdiscute.bym.BuildYourMind;
import com.wdiscute.bym.StoredBuild;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

public interface BYMDataComponents
{
    DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, BuildYourMind.MOD_ID);

    DeferredHolder<DataComponentType<?>, DataComponentType<StoredBuild>> STORED_BUILD = register(
            "stored_build",
            builder -> builder
                    .persistent(StoredBuild.CODEC)
                    .networkSynchronized(StoredBuild.STREAM_CODEC)
    );

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name,
                                                                                           UnaryOperator<DataComponentType.Builder<T>> builderOperator)
    {
        return DATA_COMPONENT_TYPES.register(name, () -> builderOperator.apply(DataComponentType.builder()).build());
    }

    static void register(IEventBus eventBus)
    {
        DATA_COMPONENT_TYPES.register(eventBus);
    }

}
