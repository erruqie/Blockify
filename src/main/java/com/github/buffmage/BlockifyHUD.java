package com.github.buffmage;

import com.github.buffmage.util.RenderUtil;
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

    public BlockifyHUD(MinecraftClient client)
    {
        this.client = client;
        scaledWidth = client.getWindow().getScaledWidth();
        scaledHeight = client.getWindow().getScaledHeight();
        albumImage = new URLImage(300, 300);
        albumImage.setImage("https://i.scdn.co/image/ab67616d00001e0233ea9fb3fd69bca55a015229");
        fontRenderer = client.textRenderer;
    }

    public void draw(MatrixStack matrixStack)
    {
        this.matrixStack = matrixStack;
        scaledWidth = client.getWindow().getScaledWidth();
        scaledHeight = client.getWindow().getScaledHeight();
        drawRectangle(0, 0, 185, 55, new Color(0, 0, 0, 100));
        drawRectangle(60, 30, 180, 32, new Color(100, 100, 100, 255));
        drawRectangle(60, 30, 120, 32, new Color(230, 230, 230, 255));
        RenderUtil.drawTexture(matrixStack, albumImage, 5, 5, .15F);

        fontRenderer.drawWithShadow(matrixStack, "Merry-Go-Round", 60, 10, new Color(255, 255, 255, 255).getRGB());
        matrixStack.scale(.5F, .5F, .5F);
        fontRenderer.drawWithShadow(matrixStack, "Joe Hisaishi", 120, 45, new Color(255, 255, 255, 255).getRGB());
        fontRenderer.drawWithShadow(matrixStack, "2:10", 120, 65, new Color(255, 255, 255, 255).getRGB());
        fontRenderer.drawWithShadow(matrixStack, "4:20", 360 - (fontRenderer.getWidth("4:23")), 65, new Color(255, 255, 255, 255).getRGB());
        matrixStack.scale(2F, 2F, 2F);
    }

    public void drawRectangle(float x1, float y1, float x2, float y2, Color color)
    {
        InGameHud.fill(matrixStack, (int)(x1), (int)(y1), (int)(x2), (int)(y2), color.getRGB());
    }
}