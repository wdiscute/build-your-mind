package com.wdiscute.bym;

import com.wdiscute.bym.registry.BYMDataAttachments;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class MindGuiLayer implements LayeredDraw.Layer
{
    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker)
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        if (Minecraft.getInstance().options.hideGui) return;

        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        Font font = Minecraft.getInstance().font;

        MindData timelessData = player.getData(BYMDataAttachments.MIND_DATA);

        //display time remaining if not Long.MAX_VALUE (isHub)
        if (timelessData.timeToExit() != Long.MAX_VALUE)
        {
            long ticksRemaining = timelessData.timeToExit() - player.level().getGameTime();

            if (ticksRemaining < 0) return;

            long seconds = ticksRemaining / 20;
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;

            String time = String.format("%02d:%02d", minutes, remainingSeconds);
            centeredText(guiGraphics, font, Component.literal(time), width / 2, 10, 0xffffffff, true);
        }
    }


    public static void centeredText(GuiGraphics guiGraphics, Font font, Component text, int x, int y, int color, boolean shadow)
    {
        FormattedCharSequence formattedcharsequence = text.getVisualOrderText();
        guiGraphics.drawString(font, formattedcharsequence, x - font.width(formattedcharsequence) / 2, y, color, shadow);
    }
}
