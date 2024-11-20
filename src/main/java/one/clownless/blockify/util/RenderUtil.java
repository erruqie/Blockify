package one.clownless.blockify.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import java.util.function.Supplier;


public class RenderUtil
{


    public static void drawTexture(MatrixStack matrixStack, URLImage image, float x, float y, float scale)
    {
        Identifier texture = image.getIdentifier();
        int width = image.getWidth();
        int height = image.getHeight();

        TextureManager tex = MinecraftClient.getInstance().getTextureManager();

        Tessellator tessellator = Tessellator.getInstance();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX);
        RenderSystem.setShaderTexture(0, texture);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

        RenderSystem.enableDepthTest();

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        Matrix4f matrices = matrixStack.peek().getPositionMatrix();

        bufferBuilder.vertex(matrices, x, y + (height * scale), 0).texture(0, 1);
        bufferBuilder.vertex(matrices, x + (width * scale), y + (height * scale), 0).texture(1, 1);
        bufferBuilder.vertex(matrices, x + (width * scale), y, 0).texture(1, 0);
        bufferBuilder.vertex(matrices, x, y, 0).texture(0, 0);

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        RenderSystem.disableBlend();
    }
}
