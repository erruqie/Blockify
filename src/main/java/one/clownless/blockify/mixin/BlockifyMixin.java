package one.clownless.blockify.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import one.clownless.blockify.BlockifyHUD;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.client.gl.GlDebug;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Environment(EnvType.CLIENT)
@Mixin(value = InGameHud.class)
public abstract class BlockifyMixin {

	private BlockifyHUD blockifyHUD;

	@Shadow
	@Final
	private MinecraftClient client;

	@Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/client/MinecraftClient;)V")
	private void onInit(MinecraftClient client, CallbackInfo ci) throws IOException
	{
		this.blockifyHUD = new BlockifyHUD(client);
	}

	@Inject(method = "render", at = @At("HEAD"))
	private void onDraw(DrawContext context, RenderTickCounter tickDelta, CallbackInfo ci)
	{
		if (!GlDebug.isDebugMessageEnabled())
			BlockifyHUD.draw(context);
	}
}


