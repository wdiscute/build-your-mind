package com.wdiscute.bym;

import com.wdiscute.bym.registry.BYMDataAttachments;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PaperItem extends Item
{
    public PaperItem(Properties properties)
    {
        super(properties.food(
                new FoodProperties(
                        1,
                        1,
                        true,
                        6,
                        Optional.empty(), List.of()
                )));
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration)
    {

        level.addParticle(
                ParticleTypes.PORTAL,
                livingEntity.getRandomX(0.5),
                livingEntity.getRandomY() - 0.25,
                livingEntity.getRandomZ(0.5),

                (livingEntity.getRandom().nextDouble() - 0.5) * 2.0,
                -livingEntity.getRandom().nextDouble(),
                (livingEntity.getRandom().nextDouble() - 0.5) * 2.0
        );

        super.onUseTick(level, livingEntity, stack, remainingUseDuration);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity)
    {
        if (!(livingEntity instanceof ServerPlayer player))
            return stack;

        ItemStack toReturn = super.finishUsingItem(stack, level, player);

        //if player is not inside mind, swap inventory
        if (!player.level().dimension().equals(BuildYourMind.MIND))
            MindData.addPlayer(player);
        else
            MindData.removePlayer(player);

        return toReturn;
    }
}
