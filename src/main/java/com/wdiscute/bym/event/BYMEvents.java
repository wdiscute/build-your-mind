package com.wdiscute.bym.event;

import com.wdiscute.bym.BuildYourMind;
import com.wdiscute.bym.MindData;
import com.wdiscute.bym.StoredBuild;
import com.wdiscute.bym.registry.BYMCommands;
import com.wdiscute.bym.registry.BYMDataAttachments;
import com.wdiscute.bym.registry.BYMDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;

@EventBusSubscriber(modid = BuildYourMind.MOD_ID)
public class BYMEvents
{
    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event)
    {
        LivingEntity livingEntity = event.getEntity();
        if (event.getItem().is(Items.PAPER))
        {
            if (!(livingEntity instanceof ServerPlayer player))
                return;

            //if player is not inside mind, swap inventory
            if (!player.level().dimension().equals(BuildYourMind.MIND))
                MindData.addPlayer(player);
            else
                MindData.removePlayer(player);
        }
    }

    @SubscribeEvent
    public static void modifyVanillaItems(ModifyDefaultComponentsEvent event)
    {
        event.modify(Items.PAPER, builder -> builder
                .set(DataComponents.FOOD, new FoodProperties.Builder()
                        .nutrition(1)
                        .saturationModifier(0.1f)
                        .alwaysEdible()
                        .build()
                )
        );
    }

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

        ItemStack stack = Items.PAPER.getDefaultInstance();
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
        ItemStack paper = player.getInventory().getItem(8);
        int index = 8;
        for (int i = 0; i < 40; i++)
        {
            ItemStack item = player.getInventory().getItem(i);
            if (item.has(BYMDataComponents.STORED_BUILD))
            {
                paper = item;
                index = i;
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }

        StoredBuild build = paper.getOrDefault(BYMDataComponents.STORED_BUILD.get(), StoredBuild.EMPTY);

        Set<StoredBuild.Duo<BlockPos, BlockState>> storedBSs = new HashSet<>(build.blocks());

        BlockPos bpToStore = event.getPos().offset(-data.buildOrigin().getX(), -data.buildOrigin().getY(), -data.buildOrigin().getZ());

        storedBSs.removeIf(o -> o.first().equals(bpToStore));

        paper.set(BYMDataComponents.STORED_BUILD, new StoredBuild(storedBSs.stream().toList(), build.fpScale(), build.tpScale(), build.rotationY(), build.rotationX()));
        player.getInventory().setItem(index, paper);
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
