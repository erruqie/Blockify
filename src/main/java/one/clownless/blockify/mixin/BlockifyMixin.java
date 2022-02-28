package one.clownless.blockify.mixin;

import one.clownless.blockify.BlockifyHUD;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Environment(EnvType.CLIENT)
@Mixin(value = InGameHud.class)
public abstract class BlockifyMixin {

	private BlockifyHUD blockifyHUD;

	@Shadow
	@Final
	private MinecraftClient client;

	@Inject(method = "<init>(Lnet/minecraft/client/MinecraftClient;)V", at = @At(value = "RETURN"))
	private void onInit(MinecraftClient client, CallbackInfo ci) throws IOException
	{
		this.blockifyHUD = new BlockifyHUD(client);
	}

	@Inject(method = "render", at = @At("HEAD"))
	private void onDraw(MatrixStack matrixStack, float esp, CallbackInfo ci)
	{
		this.blockifyHUD.draw(matrixStack);
	}
}


