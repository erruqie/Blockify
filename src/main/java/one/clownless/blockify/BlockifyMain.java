package one.clownless.blockify;


import one.clownless.blockify.util.SpotifyUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;



public class BlockifyMain implements ModInitializer
{
    public static final String MOD_ID = "blockify";
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

    public static final Logger LOGGER = LogManager.getLogger("Blockify");

    @Override
    public void onInitialize()
    {
        LOGGER.info("[Blockify] Successfully loaded");
        BlockifyConfig.init("blockify", BlockifyConfig.class);
        requestThread = new Thread()
        {
            public void run()
            {
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
                                    LOGGER.info("Reset condition, maintaining HUD until reset");
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
        prevKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "blockify.key.prev",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_KP_4,
                        "Blockify"
                )
        );

        playKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "blockify.key.play",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_KP_5,
                        "Blockify"
                )
        );

        nextKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "blockify.key.next",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_KP_6,
                        "Blockify"
                )
        );

        forceKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "blockify.key.force",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_KP_8,
                        "Blockify"
                )
        );

        hideKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "blockify.key.hide",
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

        ClientCommandManager.DISPATCHER.register(ClientCommandManager.literal("sharetrack").executes(context -> {
            var player = MinecraftClient.getInstance().player;
            if (player == null) { return 0; }
            player.sendChatMessage(BlockifyHUD.hudInfo[5]);
            return 1;
        }));
    }



    public void playKeyHandler(boolean currPressState)
    {
        try
        {
            if (currPressState && !playKeyPrevState)
            {

                if (SpotifyUtil.isAuthorized())
                {
                    LOGGER.info("Authorized!");
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
            LOGGER.info("Next Key Pressed");
            SpotifyUtil.nextSong();
        }
        nextKeyPrevState = currPressState;
    }

    public void prevKeyHandler(boolean currPressState)
    {
        if (currPressState && !prevKeyPrevState)
        {
            LOGGER.info("Previous Key Pressed");
            SpotifyUtil.prevSong();
        }
        prevKeyPrevState = currPressState;
    }

    public void forceKeyHandler(boolean currPressState)
    {
        if (currPressState && !forceKeyPrevState)
        {
            LOGGER.info("Force Key Pressed");
            BlockifyHUD.setDuration(-2000);
        }
        forceKeyPrevState = currPressState;
    }

    public void hideKeyHandler(boolean currPressState)
    {
        if (currPressState && !hideKeyPrevState)
        {
            LOGGER.info("Hide Key Pressed");
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