package com.github.buffmage;


import com.github.buffmage.util.SpotifyUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

public class BlockifyMain implements ModInitializer
{
    public static final String MOD_ID = "blockifybuffmage";
    private static KeyBinding playKey;
    private boolean playKeyPrevState = false;
    private static Thread requestThread;

    @Override
    public void onInitialize()
    {
        requestThread = new Thread()
        {
            public void run()
            {
                System.out.println("Hello again!");
                while (true)
                {
                    try
                    {
                        Thread.sleep(1000);
                        if (MinecraftClient.getInstance().world != null)
                        {
                            String [] data = SpotifyUtil.getPlaybackInfo();
                            if (data[0] != null && data[0].equals("Status Code: 204"))
                            {
                                SpotifyUtil.refreshActiveSession();
                            }
                            else
                            {
                                BlockifyHUD.updateData(data);
                            }
                        }
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }

        };
        requestThread.setName("Spotify Thread");
        requestThread.start();
        SpotifyUtil.initialize();
        playKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "Play/Pause",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_KP_5,
                        "Blockify"
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(
                client ->
                {
                    try
                    {
                        playKeyHandler(playKey.isPressed());
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
        );
    }



    public void playKeyHandler(boolean currPressState)
    {
        try
        {
            if (currPressState && !playKeyPrevState)
            {

                //MinecraftClient.getInstance().player.sendMessage(new LiteralText("Key Pad 5 was pressed!"), false);
                if (SpotifyUtil.isAuthorized())
                {
                    System.out.println("Authorized!");
                    SpotifyUtil.playPause();
                }
                else
                {
                    Util.getOperatingSystem().open(SpotifyUtil.authorize());
                }
            }
            playKeyPrevState = currPressState;
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}