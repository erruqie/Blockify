package one.clownless.blockify;

import net.minecraft.client.renderer.RenderPipelines;
import eu.midnightdust.lib.util.MidnightColorUtil;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.util.FormattedCharSequence;
import one.clownless.blockify.util.SpotifyUtil;
import one.clownless.blockify.util.URLImage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix3x2fStack;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlockifyHUD implements HudElement {
    private static URLImage albumImage;
    public static String[] hudInfo;
    private static String prevImage;
    private static int progressMS;
    private static int durationMS;
    private static int prevVolume;
    private static int newVolume;
    public static boolean isHidden = false;
    public static final Logger LOGGER = LogManager.getLogger("Blockify");
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    public BlockifyHUD() {
        albumImage = new URLImage(300, 300);
        hudInfo = new String[8];
        prevImage = "empty";
        progressMS = 0;
        durationMS = -1;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, DeltaTracker tickDelta) {
        if (hudInfo[1] == null || isHidden) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        Font font = client.font;
        int scaledWidth = client.getWindow().getGuiScaledWidth();
        int scaledHeight = client.getWindow().getGuiScaledHeight();

        double percentProgress = (double) progressMS / (double) durationMS;
        if (percentProgress < 0) {
            percentProgress = 0;
        }

        Matrix3x2fStack pose = context.pose();
        pose.pushMatrix();
        pose.translate(
                (BlockifyConfig.anchor == BlockifyConfig.Anchor.TOP_LEFT || BlockifyConfig.anchor == BlockifyConfig.Anchor.BOTTOM_LEFT) ? BlockifyConfig.posX : scaledWidth - 185 - BlockifyConfig.posX,
                (BlockifyConfig.anchor == BlockifyConfig.Anchor.TOP_LEFT || BlockifyConfig.anchor == BlockifyConfig.Anchor.TOP_RIGHT) ? BlockifyConfig.posY : scaledHeight - 55 - BlockifyConfig.posY);
        pose.scale((float) BlockifyConfig.scale, (float) BlockifyConfig.scale);

        int textOffset = 55;
        if ((BlockifyConfig.drawCover) && hudInfo[4] != null && (!prevImage.equals(hudInfo[4]) && !hudInfo[4].equals(""))) {
            LOGGER.info("Drawing new album cover.");
            albumImage.setImage(hudInfo[4]);
            prevImage = hudInfo[4];
        }
        if (hudInfo[4] != null && (BlockifyConfig.drawCover)) {
            int size = (int) (albumImage.getWidth() * 0.15F);
            context.blit(RenderPipelines.GUI_TEXTURED, albumImage.getIdentifier(), 5, 5, 0F, 0F, size, size, size, size);
            textOffset = 0;
        }

        Color bgColor = MidnightColorUtil.hex2Rgb(BlockifyConfig.backgroundColor);
        int bgArgb = new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), BlockifyConfig.backgroundTransparency).getRGB();
        context.fill(0, 0, 185 - textOffset, 55, bgArgb);
        context.fill(60 - textOffset, 48, 180 - textOffset, 50, MidnightColorUtil.hex2Rgb(BlockifyConfig.barColor).darker().darker().getRGB());
        context.fill(60 - textOffset, 48, (int) (60 + (120 * percentProgress)) - textOffset, 50, MidnightColorUtil.hex2Rgb(BlockifyConfig.barColor).getRGB());

        int titleColor = MidnightColorUtil.hex2Rgb(BlockifyConfig.titleColor).getRGB();
        List<FormattedCharSequence> nameWrap = font.split(FormattedText.of(hudInfo[0]), 125);
        int yOffset = 0;
        if (nameWrap.size() > 1) {
            context.text(font, nameWrap.get(0), 60 - textOffset, 5, titleColor, true);
            context.text(font, nameWrap.get(1), 60 - textOffset, 18, titleColor, true);
            yOffset = 15;
        } else {
            context.text(font, nameWrap.get(0), 60 - textOffset, 5, titleColor, true);
        }

        pose.scale(0.5F, 0.5F);

        int artistColor = MidnightColorUtil.hex2Rgb(BlockifyConfig.artistColor).getRGB();
        List<FormattedCharSequence> artistWrap = font.split(FormattedText.of(hudInfo[1]), 140);
        if (artistWrap.size() > 1) {
            context.text(font, artistWrap.get(0), 120 - (textOffset * 2), 44 + yOffset, artistColor, true);
            context.text(font, artistWrap.get(1), 120 - (textOffset * 2), 57 + yOffset, artistColor, true);
        } else {
            context.text(font, artistWrap.get(0), 120 - (textOffset * 2), 44 + yOffset, artistColor, true);
        }

        String volumeText = hudInfo[6] == null ? "" : hudInfo[6];
        String progressText = (progressMS / (1000 * 60)) + ":" + String.format("%02d", (progressMS / 1000 % 60));
        String durationText = (durationMS / (1000 * 60)) + ":" + String.format("%02d ", (durationMS / 1000 % 60)) + I18n.get("blockify.hud.volume") + ": " + volumeText;

        int timeColor = MidnightColorUtil.hex2Rgb(BlockifyConfig.timeColor).getRGB();
        context.text(font, progressText, 120 - (textOffset * 2), 85, timeColor, true);
        context.text(font, durationText, 360 - font.width(durationText) - (textOffset * 2), 85, timeColor, true);

        pose.popMatrix();
    }

    public static void updateData(String[] data) {
        hudInfo = data;
        progressMS = hudInfo[2] == null ? 0 : (Integer.parseInt(hudInfo[2]) - 1000);
        durationMS = hudInfo[3] == null ? -1 : Integer.parseInt(hudInfo[3]);
    }

    public static int getProgress() {
        return progressMS;
    }

    public static int getDuration() {
        return durationMS;
    }

    public static void setProgress(int progress) {
        progressMS = progress;
    }

    public static void setDuration(int duration) {
        durationMS = duration;
    }

    public static void increaseVolume() {
        EXECUTOR_SERVICE.execute(() -> {
            if (hudInfo[6] == null) return;
            prevVolume = Integer.parseInt(hudInfo[6]);
            LOGGER.info("Prev volume: " + prevVolume);
            newVolume = prevVolume + BlockifyConfig.volumeStep;
            if (prevVolume == 100) {
                return;
            } else if (newVolume > 100) {
                newVolume = 100;
            }
            SpotifyUtil.putRequest("volume?volume_percent=" + newVolume);
            LOGGER.info("Set new volume: " + newVolume);
            hudInfo[6] = String.valueOf(newVolume);
        });
    }

    public static void decreaseVolume() {
        EXECUTOR_SERVICE.execute(() -> {
            if (hudInfo[6] == null) return;
            prevVolume = Integer.parseInt(hudInfo[6]);
            LOGGER.info("Prev volume: " + prevVolume);
            newVolume = prevVolume - BlockifyConfig.volumeStep;
            if (prevVolume == 0) {
                return;
            } else if (newVolume < 0) {
                newVolume = 0;
            }
            SpotifyUtil.putRequest("volume?volume_percent=" + newVolume);
            LOGGER.info("Set new volume: " + newVolume);
            hudInfo[6] = String.valueOf(newVolume);
        });
    }
}
