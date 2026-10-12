package com.wdiscute.bym.registry;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.wdiscute.bym.BuildYourMind;
import com.wdiscute.bym.MaybeStack;
import com.wdiscute.bym.MindData;
import com.wdiscute.bym.StoredBuild;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public interface BYMCommands
{
    DynamicCommandExceptionType ALREADY_INSIDE_MIND = new DynamicCommandExceptionType(
            o -> Component.literal("Already inside the mind")
    );

    DynamicCommandExceptionType NOT_INSIDE_MIND = new DynamicCommandExceptionType(
            o -> Component.literal("Not inside the mind")
    );

    DynamicCommandExceptionType INVALID_ITEM_TO_COPY = new DynamicCommandExceptionType(
            o -> Component.literal("You must hold the paper in your off-hand, and the item to apply on your main hand!")
    );

    DynamicCommandExceptionType INVALID_ITEM = new DynamicCommandExceptionType(
            o -> Component.literal("You must hold the item with the model in your main-hand!")
    );

    static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context)
    {
        dispatcher.register(Commands.literal("woo")
                        .requires(sourceStack -> sourceStack.hasPermission(2))

                        .then(Commands.literal("enter")
                                .executes(c ->
                                        enterMind(
                                                c.getSource().getPlayerOrException()
                                        )
                                )
                        )

                        .then(Commands.literal("exit")
                                .executes(c ->
                                        exitMind(
                                                c.getSource().getPlayerOrException()
                                        )
                                )
                        )

                        .then(Commands.literal("set_first_person_scale")
                                .then(Commands.argument("scale", FloatArgumentType.floatArg())
                                        .executes(c ->
                                                setFirstPersonScale(
                                                        c.getSource().getPlayerOrException(),
                                                        FloatArgumentType.getFloat(c, "scale")
                                                )
                                        )
                                )
                        )

                        .then(Commands.literal("set_rotation_y")
                                .then(Commands.argument("rotation_y", FloatArgumentType.floatArg())
                                        .executes(c ->
                                                setRotationY(
                                                        c.getSource().getPlayerOrException(),
                                                        FloatArgumentType.getFloat(c, "rotation_y")
                                                )
                                        )
                                )
                        )

                        .then(Commands.literal("set_rotation_x")
                                .then(Commands.argument("rotation_x", FloatArgumentType.floatArg())
                                        .executes(c ->
                                                setRotationX(
                                                        c.getSource().getPlayerOrException(),
                                                        FloatArgumentType.getFloat(c, "rotation_x")
                                                )
                                        )
                                )
                        )
        );


        dispatcher.register(Commands.literal("build_your_mind")
                        .requires(sourceStack -> sourceStack.hasPermission(2))

                        .then(Commands.literal("set_result")
                                .executes(c ->
                                        setResultItem(
                                                c.getSource().getPlayerOrException()
                                        )
                                )
                        )

                        .then(Commands.literal("set_name")
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .executes(c ->
                                                setName(
                                                        c.getSource().getPlayerOrException(),
                                                        StringArgumentType.getString(c, "name")
                                                )
                                        )
                                )
                        )
        );
    }

    private static int setResultItem(ServerPlayer player)
    {
        player.displayClientMessage(Component.literal("Set the result item to ").append(player.getMainHandItem().getDisplayName()), true);
        player.setData(BYMDataAttachments.ITEM, new MaybeStack(player.getMainHandItem()));
        return 1;
    }

    private static int setName(ServerPlayer player, String name)
    {
        player.displayClientMessage(Component.literal("Set the name to " + name), true);
        player.setData(BYMDataAttachments.ITEM_NAME, name);
        return 1;
    }

    private static int setFirstPersonScale(ServerPlayer player, float scale) throws CommandSyntaxException
    {
        ItemStack mainhand = player.getMainHandItem();

        StoredBuild build = mainhand.get(BYMDataComponents.STORED_BUILD.get());
        if (build == null)
            throw INVALID_ITEM.create(null);

        mainhand.set(BYMDataComponents.STORED_BUILD.get(), mainhand.getOrDefault(BYMDataComponents.STORED_BUILD.get(), StoredBuild.EMPTY).setFirstPersonScale(scale));
        return 1;
    }

    private static int setRotationY(ServerPlayer player, float scale) throws CommandSyntaxException
    {
        ItemStack mainhand = player.getMainHandItem();

        StoredBuild build = mainhand.get(BYMDataComponents.STORED_BUILD.get());
        if (build == null)
            throw INVALID_ITEM.create(null);

        mainhand.set(BYMDataComponents.STORED_BUILD.get(), mainhand.getOrDefault(BYMDataComponents.STORED_BUILD.get(), StoredBuild.EMPTY).setRotationY(scale));
        return 1;
    }

    private static int setRotationX(ServerPlayer player, float scale) throws CommandSyntaxException
    {
        ItemStack mainhand = player.getMainHandItem();

        StoredBuild build = mainhand.get(BYMDataComponents.STORED_BUILD.get());
        if (build == null)
            throw INVALID_ITEM.create(null);

        mainhand.set(BYMDataComponents.STORED_BUILD.get(), mainhand.getOrDefault(BYMDataComponents.STORED_BUILD.get(), StoredBuild.EMPTY).setRotationX(scale));
        return 1;
    }

    private static int enterMind(ServerPlayer player) throws CommandSyntaxException
    {
        if (player.level().dimension().equals(BuildYourMind.MIND))
            throw ALREADY_INSIDE_MIND.create(null);

        MindData.addPlayer(player);
        return 1;
    }

    private static int exitMind(ServerPlayer player) throws CommandSyntaxException
    {
        if (!player.level().dimension().equals(BuildYourMind.MIND))
            throw NOT_INSIDE_MIND.create(null);

        MindData.removePlayer(player);
        return 1;
    }
}
