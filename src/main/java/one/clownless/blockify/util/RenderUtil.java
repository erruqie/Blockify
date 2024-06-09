package one.clownless.blockify.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
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
        tex.bindTexture(texture);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        Supplier<ShaderProgram> texProgram = GameRenderer::getPositionTexProgram;
        RenderSystem.setShader(texProgram);
        RenderSystem.setShaderTexture(0, texture);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

        RenderSystem.enableDepthTest();

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        Matrix4f matrices = matrixStack.peek().getPositionMatrix();

        bufferBuilder.vertex(matrices, x, y + (height * scale), 0).texture(0, 1).next();
        bufferBuilder.vertex(matrices, x + (width * scale), y + (height * scale), 0).texture(1, 1).next();
        bufferBuilder.vertex(matrices, x + (width * scale), y, 0).texture(1, 0).next();
        bufferBuilder.vertex(matrices, x, y, 0).texture(0, 0).next();

        tessellator.draw();

        RenderSystem.disableBlend();
    }
}
