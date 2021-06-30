package com.github.buffmage;

import com.github.buffmage.util.RenderUtil;
import com.github.buffmage.util.SpotifyUtil;
import com.github.buffmage.util.URLImage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;

public class BlockifyHUD
{
    private static MinecraftClient client;
    private static MatrixStack matrixStack;
    private static int scaledWidth;
    private static int scaledHeight;
    private static URLImage albumImage;
    private static TextRenderer fontRenderer;
    private static String [] hudInfo;
    private static int ticks;
    private static String prevImage;

    public BlockifyHUD(MinecraftClient client)
    {
        this.client = client;
        scaledWidth = client.getWindow().getScaledWidth();
        scaledHeight = client.getWindow().getScaledHeight();
        albumImage = new URLImage(300, 300);
        fontRenderer = client.textRenderer;
        ticks = 0;
        hudInfo = new String[5];
        prevImage = "";
    }

    public static void draw(MatrixStack matrixStack)
    {
        if (hudInfo[1] == null)
        {
            return;
        }

        int progressMS = Integer.parseInt(hudInfo[2]);
        int durationMS = Integer.parseInt(hudInfo[3]);
        double percentProgress = (double) progressMS / (double) durationMS;

        BlockifyHUD.matrixStack = matrixStack;
        scaledWidth = client.getWindow().getScaledWidth();
        scaledHeight = client.getWindow().getScaledHeight();
        drawRectangle(0, 0, 185, 55, new Color(0, 0, 0, 100));
        drawRectangle(60, 30, 180, 32, new Color(100, 100, 100, 255));
        drawRectangle(60, 30, (float)(60 + (120 * percentProgress)), 32, new Color(230, 230, 230, 255));

        if (!prevImage.equals(hudInfo[4]) && !hudInfo[4].equals(""))
        {
            System.out.println("Drawing!");
            albumImage.setImage(hudInfo[4]);
            prevImage = hudInfo[4];
        }

        RenderUtil.drawTexture(matrixStack, albumImage, 5, 5, .15F);

        String nameText = fontRenderer.trimToWidth(hudInfo[0], 125);
        fontRenderer.drawWithShadow(matrixStack, nameText, 60, 10, new Color(255, 255, 255, 255).getRGB());
        matrixStack.scale(.5F, .5F, .5F);
        fontRenderer.drawWithShadow(matrixStack, hudInfo[1], 120, 45, new Color(255, 255, 255, 255).getRGB());

        String progressText = (progressMS / (1000 * 60)) + ":" + String.format("%02d", (progressMS / 1000 % 60));
        String durationText = (durationMS / (1000 * 60)) + ":" + String.format("%02d", (durationMS / 1000 % 60));
        fontRenderer.drawWithShadow(matrixStack, progressText, 120, 67, new Color(255, 255, 255, 255).getRGB());
        fontRenderer.drawWithShadow(matrixStack, durationText, 360 - (fontRenderer.getWidth(durationText)), 67, new Color(255, 255, 255, 255).getRGB());
        matrixStack.scale(2F, 2F, 2F);
    }

    public static void drawRectangle(float x1, float y1, float x2, float y2, Color color)
    {
        InGameHud.fill(matrixStack, (int)(x1), (int)(y1), (int)(x2), (int)(y2), color.getRGB());
    }

    public static void updateData(String [] data)
    {
        hudInfo = data;
    }

}