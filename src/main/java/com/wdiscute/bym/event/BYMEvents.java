package com.wdiscute.bym.event;

import com.wdiscute.bym.BuildYourMind;
import com.wdiscute.bym.MindData;
import com.wdiscute.bym.StoredBuild;
import com.wdiscute.bym.registry.BYMCommands;
import com.wdiscute.bym.registry.BYMDataAttachments;
import com.wdiscute.bym.registry.BYMDataComponents;
import com.wdiscute.bym.registry.BYMItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;

@EventBusSubscriber(modid = BuildYourMind.MOD_ID)
public class BYMEvents
{
    @SubscribeEvent
    public static void blockPlaceEvent(BlockEvent.EntityPlaceEvent event)
    {
        //ignore if client
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        //ignore if not inside mind
        if (!player.level().dimension().equals(BuildYourMind.MIND))
            return;

        //get data
        MindData data = player.getData(BYMDataAttachments.MIND_DATA);

        //store blockstates
        StoredBuild build = player.getInventory().getItem(8).getOrDefault(BYMDataComponents.STORED_BUILD.get(), StoredBuild.EMPTY);
        Set<StoredBuild.Duo<BlockPos, BlockState>> storedBSs = new HashSet<>(build.blocks());

        BlockState placedBS = event.getPlacedBlock();
        BlockPos bpToStore = event.getPos().offset(-data.buildOrigin().getX(), -data.buildOrigin().getY(), -data.buildOrigin().getZ());

        if (placedBS.isEmpty())
            storedBSs.removeIf(o -> o.first().equals(bpToStore));
        else
            storedBSs.add(new StoredBuild.Duo<>(bpToStore, placedBS));

        ItemStack stack = BYMItems.PAPER.get().getDefaultInstance();
        stack.set(BYMDataComponents.STORED_BUILD, new StoredBuild(storedBSs.stream().toList(), build.fpScale(), build.tpScale(), build.rotationY(), build.rotationX()));
        player.getInventory().setItem(8, stack);
    }

    @SubscribeEvent
    public static void blockBreakEvent(BlockEvent.BreakEvent event)
    {
        //ignore if client
        if (!(event.getPlayer() instanceof ServerPlayer player))
            return;

        //ignore if not inside mind
        if (!player.level().dimension().equals(BuildYourMind.MIND))
            return;

        //get data
        MindData data = player.getData(BYMDataAttachments.MIND_DATA);

        //store blockstates
        StoredBuild build = player.getInventory().getItem(8).getOrDefault(BYMDataComponents.STORED_BUILD.get(), StoredBuild.EMPTY);

        Set<StoredBuild.Duo<BlockPos, BlockState>> storedBSs = new HashSet<>(build.blocks());

        BlockPos bpToStore = event.getPos().offset(-data.buildOrigin().getX(), -data.buildOrigin().getY(), -data.buildOrigin().getZ());

        storedBSs.removeIf(o -> o.first().equals(bpToStore));

        ItemStack stack = BYMItems.PAPER.get().getDefaultInstance();
        stack.set(BYMDataComponents.STORED_BUILD, new StoredBuild(storedBSs.stream().toList(), build.fpScale(), build.tpScale(), build.rotationY(), build.rotationX()));
        player.getInventory().setItem(8, stack);
    }

    @SubscribeEvent
    public static void playerTick(PlayerTickEvent.Post event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        MindData data = player.getData(BYMDataAttachments.MIND_DATA);

        if (!player.level().dimension().equals(BuildYourMind.MIND))
            return;

        //if inside mind
        if (data.timeToExit() <= player.level().getGameTime())
            MindData.removePlayer(player);
    }

    @SubscribeEvent
    public static void addCommand(RegisterCommandsEvent event)
    {
        BYMCommands.register(event.getDispatcher(), event.getBuildContext());
    }
}
