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
    private static KeyBinding nextKey;
    private static KeyBinding prevKey;
    private static KeyBinding forceKey;
    private static KeyBinding hideKey;
    private boolean playKeyPrevState = false;
    private boolean nextKeyPrevState = false;
    private boolean prevKeyPrevState = false;
    private boolean forceKeyPrevState = false;
    private boolean hideKeyPrevState = false;
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
                            if (BlockifyHUD.getDuration() < BlockifyHUD.getProgress())
                            {
                                String [] data = SpotifyUtil.getPlaybackInfo();
                                if (data[0] != null && data[0].equals("Status Code: 204"))
                                {
                                    SpotifyUtil.refreshActiveSession();
                                }
                                else if (data[0] != null && data[0].equals("Status Code: 429"))
                                {
                                    Thread.sleep(3000);
                                }
                                else if (data[0] != null && data[0].equals("Reset"))
                                {
                                    System.out.println("Reset condition, maintaining HUD until reset");
                                }
                                else
                                {
                                    BlockifyHUD.updateData(data);
                                }
                            }
                            else if (SpotifyUtil.isPlaying())
                            {
                                BlockifyHUD.setProgress(BlockifyHUD.getProgress() + 1000);
                            }
                        }
                        else
                        {
                            BlockifyHUD.setProgress(0);
                            BlockifyHUD.setDuration(-1);
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

        nextKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "Next Song",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_KP_6,
                        "Blockify"
                )
        );

        prevKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "Previous Song",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_KP_4,
                        "Blockify"
                )
        );

        forceKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "Force Update",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_KP_8,
                        "Blockify"
                )
        );
        hideKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "Hide Spotify",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_KP_9,
                        "Blockify"
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(
                client ->
                {
                    try
                    {
                        playKeyHandler(playKey.isPressed());
                        nextKeyHandler(nextKey.isPressed());
                        prevKeyHandler(prevKey.isPressed());
                        forceKeyHandler(forceKey.isPressed());
                        hideKeyHandler(hideKey.isPressed());

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

    public void nextKeyHandler(boolean currPressState)
    {
        if (currPressState && !nextKeyPrevState)
        {
            System.out.println("Next Key Pressed");
            SpotifyUtil.nextSong();
        }
        nextKeyPrevState = currPressState;
    }

    public void prevKeyHandler(boolean currPressState)
    {
        if (currPressState && !prevKeyPrevState)
        {
            System.out.println("Previous Key Pressed");
            SpotifyUtil.prevSong();
        }
        prevKeyPrevState = currPressState;
    }

    public void forceKeyHandler(boolean currPressState)
    {
        if (currPressState && !forceKeyPrevState)
        {
            System.out.println("Force Key Pressed");
            BlockifyHUD.setDuration(-2000);
        }
        forceKeyPrevState = currPressState;
    }

    public void hideKeyHandler(boolean currPressState)
    {
        if (currPressState && !hideKeyPrevState)
        {
            System.out.println("Hide Key Pressed");
            if (BlockifyHUD.isHidden)
            {
                BlockifyHUD.isHidden = false;
            }
            else
            {
                BlockifyHUD.isHidden = true;
            }
        }
        hideKeyPrevState = currPressState;
    }
}