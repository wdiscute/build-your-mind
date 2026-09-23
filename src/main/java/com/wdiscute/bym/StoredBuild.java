package com.wdiscute.bym;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public record StoredBuild(List<Duo<BlockPos, BlockState>> blocks, float fpScale, float tpScale, float rotationY, float rotationX)
{
    public static final StoredBuild EMPTY = new StoredBuild(List.of(), 1, 1, 0, 0);

    public static final Codec<StoredBuild> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Duo.codec(BlockPos.CODEC, BlockState.CODEC).listOf().fieldOf("blocks").forGetter(StoredBuild::blocks),
                    Codec.FLOAT.fieldOf("first_person_scale").forGetter(StoredBuild::fpScale),
                    Codec.FLOAT.fieldOf("third_person_scale").forGetter(StoredBuild::tpScale),
                    Codec.FLOAT.fieldOf("rotation_y").forGetter(StoredBuild::rotationY),
                    Codec.FLOAT.fieldOf("rotation_x").forGetter(StoredBuild::rotationX)
            ).apply(instance, StoredBuild::new)
    );

    public static final StreamCodec<ByteBuf, BlockState> BLOCK_STATE_STREAM_CODEC =
            ByteBufCodecs.idMapper(
                    Block::stateById,
                    Block::getId
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, StoredBuild> STREAM_CODEC =
            StreamCodec.composite(
                    Duo.streamCodec(BlockPos.STREAM_CODEC, BLOCK_STATE_STREAM_CODEC).apply(ByteBufCodecs.list()), StoredBuild::blocks,
                    ByteBufCodecs.FLOAT, StoredBuild::fpScale,
                    ByteBufCodecs.FLOAT, StoredBuild::tpScale,
                    ByteBufCodecs.FLOAT, StoredBuild::rotationY,
                    ByteBufCodecs.FLOAT, StoredBuild::rotationX,
                    StoredBuild::new
            );

    public StoredBuild setFirstPersonScale(float scale)
    {
        return new StoredBuild(blocks, scale, tpScale, rotationY, rotationX);
    }

    public StoredBuild setThirdPersonScale(float scale)
    {
        return new StoredBuild(blocks, fpScale, scale, rotationY, rotationX);
    }

    public StoredBuild setRotationY(float rotation)
    {
        return new StoredBuild(blocks, fpScale, tpScale, rotation, rotationX);
    }

    public StoredBuild setRotationX(float rotationX)
    {
        return new StoredBuild(blocks, fpScale, tpScale, rotationY, rotationX);
    }

    public record Duo<F, S>(F first, S second)
    {
        public static <F, S> Codec<Duo<F, S>> codec(
                Codec<F> firstCodec,
                Codec<S> secondCodec
        )
        {
            return RecordCodecBuilder.create(instance -> instance.group(
                    firstCodec.fieldOf("first").forGetter(Duo::first),
                    secondCodec.fieldOf("second").forGetter(Duo::second)
            ).apply(instance, Duo::new));
        }

        public static <B, F, S> StreamCodec<B, Duo<F, S>> streamCodec(
                StreamCodec<? super B, F> firstCodec,
                StreamCodec<? super B, S> secondCodec
        )
        {
            return StreamCodec.composite(
                    firstCodec, Duo::first,
                    secondCodec, Duo::second,
                    Duo::new
            );
        }
    }
}
