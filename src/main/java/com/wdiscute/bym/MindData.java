package com.wdiscute.bym;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wdiscute.bym.registry.BYMDataAttachments;
import com.wdiscute.bym.registry.BYMItems;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public record MindData(long timeToExit, List<MaybeStack> inventory, BlockPos posToExit, ResourceLocation dimToExit,
                       BlockPos buildOrigin)
{
    public static final MindData EMPTY = new MindData(Long.MAX_VALUE, List.of(), BlockPos.ZERO, Level.OVERWORLD.location(), BlockPos.ZERO);

    public static final Codec<MindData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.LONG.fieldOf("time_to_exit").forGetter(t -> t.timeToExit),
                    MaybeStack.CODEC.listOf().fieldOf("inventory").forGetter(t -> t.inventory),
                    BlockPos.CODEC.fieldOf("pos_to_exit").forGetter(t -> t.posToExit),
                    ResourceLocation.CODEC.fieldOf("dim_to_exit").forGetter(t -> t.dimToExit),
                    BlockPos.CODEC.fieldOf("pos_to_exit").forGetter(t -> t.buildOrigin)
            ).apply(instance, MindData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, MindData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, data -> data.timeToExit,
            MaybeStack.STREAM_CODEC.apply(ByteBufCodecs.list()), data -> data.inventory,
            BlockPos.STREAM_CODEC, data -> data.posToExit,
            ResourceLocation.STREAM_CODEC, data -> data.dimToExit,
            BlockPos.STREAM_CODEC, data -> data.buildOrigin,
            MindData::new
    );

    private MindData withTimeToExit(long timeToExit)
    {
        return new MindData(timeToExit, inventory, posToExit, dimToExit, buildOrigin);
    }

    private MindData withInventory(List<MaybeStack> inventory)
    {
        return new MindData(timeToExit, inventory, posToExit, dimToExit, buildOrigin);
    }

    private MindData withPosToExit(BlockPos posToExit)
    {
        return new MindData(timeToExit, inventory, posToExit, dimToExit, buildOrigin);
    }

    private MindData withDimToExit(ResourceLocation dimToExit)
    {
        return new MindData(timeToExit, inventory, posToExit, dimToExit, buildOrigin);
    }

    private MindData withBuildOrigin(BlockPos buildOrigin)
    {
        return new MindData(timeToExit, inventory, posToExit, dimToExit, buildOrigin);
    }

    //inventory
    public static void setInventory(Player player, List<MaybeStack> inventory)
    {
        MindData data = player.getData(BYMDataAttachments.MIND_DATA);
        player.setData(BYMDataAttachments.MIND_DATA, data.withInventory(inventory));
    }

    public static void setTimeToExit(Player player, long timeToExit)
    {
        MindData data = player.getData(BYMDataAttachments.MIND_DATA);
        player.setData(BYMDataAttachments.MIND_DATA, data.withTimeToExit(timeToExit));
    }

    public static void setPosToExit(Player player, BlockPos posToExit)
    {
        MindData data = player.getData(BYMDataAttachments.MIND_DATA);
        player.setData(BYMDataAttachments.MIND_DATA, data.withPosToExit(posToExit));
    }

    public static void setDimToExit(Player player, ResourceLocation dimToExit)
    {
        MindData data = player.getData(BYMDataAttachments.MIND_DATA);
        player.setData(BYMDataAttachments.MIND_DATA, data.withDimToExit(dimToExit));
    }

    public static void setBuildOrigin(Player player, BlockPos buildOrigin)
    {
        MindData data = player.getData(BYMDataAttachments.MIND_DATA);
        player.setData(BYMDataAttachments.MIND_DATA, data.withBuildOrigin(buildOrigin));
    }

    public static void swapInventory(Player player)
    {
        //save overworld inventory and swap to timeless inventory
        List<MaybeStack> inventory = player.getData(BYMDataAttachments.MIND_DATA).inventory();
        List<MaybeStack> list = new ArrayList<>();
        for (int i = 0; i < 100; i++)
        {
            list.add(new MaybeStack(player.getInventory().getItem(i)));

            if (inventory.size() > i)
                player.getInventory().setItem(i, inventory.get(i).toStack());
            else
                player.getInventory().setItem(i, ItemStack.EMPTY);
        }

        //store overworld inventory
        MindData.setInventory(player, list);
    }

    public static void addPlayer(ServerPlayer player)
    {
        //get timeless server level
        ServerLevel mindLevel = player.level().getServer().getLevel(BuildYourMind.MIND);

        //swap inventory
        swapInventory(player);

        //set last hotbar slot to paper
        player.getInventory().setItem(8, BYMItems.PAPER.get().getDefaultInstance());

        //set to creative
        player.setGameMode(GameType.CREATIVE);

        //set time to exit
        MindData.setTimeToExit(player, mindLevel.getGameTime() + BYMConfig.Server.TIME_INSIDE_MIND.getAsInt());

        //set posToExist
        MindData.setPosToExit(player, player.blockPosition());

        //set dimToExit
        MindData.setDimToExit(player, player.level().dimension().location());

        //pick origin
        int x = mindLevel.getRandom().nextInt(1000000);
        int z = mindLevel.getRandom().nextInt(1000000);

        //set dimToExit
        MindData.setBuildOrigin(player, new BlockPos(x - 10, 100, z - 10));

        //teleport player
        player.teleportTo(mindLevel, x, 102, z, Set.of(), 0, 0);

        //build platform
        for (int i = x - 10; i < x + 10; i++)
            for (int j = z - 10; j < z + 10; j++)
                mindLevel.setBlockAndUpdate(new BlockPos(i, 100, j), Blocks.DIAMOND_BLOCK.defaultBlockState());
    }

    public static void removePlayer(ServerPlayer player)
    {
        //get data
        MindData data = player.getData(BYMDataAttachments.MIND_DATA);

        //get paper
        ItemStack item = player.getInventory().getItem(8);

        //swap inventory
        swapInventory(player);

        //add paper
        player.addItem(item);

        //get timeless server level
        ServerLevel levelToReturn = player.level().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, data.dimToExit));

        if (levelToReturn == null)
            player.level().getServer().getLevel(Level.OVERWORLD);

        //teleport player to mind
        Vec3 center = data.posToExit.getCenter();
        player.teleportTo(levelToReturn, center.x, center.y, center.z, Set.of(), 0, 0);

        //reset data
        player.setData(BYMDataAttachments.MIND_DATA, MindData.EMPTY);

        //set to survival
        if (player.isCreative())
            player.setGameMode(GameType.SURVIVAL);
    }
}
