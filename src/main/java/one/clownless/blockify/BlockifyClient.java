package one.clownless.blockify;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import one.clownless.blockify.util.SpotifyUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.net.URI;

public class BlockifyClient implements ClientModInitializer {
    private static KeyMapping playKey;
    private static KeyMapping nextKey;
    private static KeyMapping prevKey;
    private static KeyMapping forceKey;
    private static KeyMapping addTrackKey;
    private static KeyMapping hideKey;
    private static KeyMapping increaseVolumeKey;
    private static KeyMapping decreaseVolumeKey;
    private static KeyMapping toggleInGameMusicKey;
    private boolean playKeyPrevState = false;
    private boolean nextKeyPrevState = false;
    private boolean prevKeyPrevState = false;
    private boolean forceKeyPrevState = false;
    private boolean addTrackKeyPrevState = false;
    private boolean hideKeyPrevState = false;
    private boolean increaseVolumeKeyPrevState = false;
    private boolean decreaseVolumeKeyPrevState = false;
    private boolean toggleInGameMusicKeyPrevState = false;
    private static Thread requestThread;

    public static final Logger LOGGER = LogManager.getLogger("Blockify");

    private static final KeyMapping.Category BLOCKIFY_CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(BlockifyMain.MOD_ID, "blockify"));

    @Override
    public void onInitializeClient() {
        LOGGER.info("[Blockify] Successfully loaded");
        BlockifyConfig.init(BlockifyMain.MOD_ID, BlockifyConfig.class);

        BlockifyHUD hud = new BlockifyHUD();
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(BlockifyMain.MOD_ID, "spotify_hud"),
                hud);

        requestThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    if (Minecraft.getInstance().level != null) {
                        if (BlockifyHUD.getDuration() < BlockifyHUD.getProgress()) {
                            Thread.sleep(1000);
                            String[] data = SpotifyUtil.getPlaybackInfo();
                            if (data[0] != null && data[0].equals("Status Code: 204")) {
                                SpotifyUtil.refreshActiveSession();
                            } else if (data[0] != null && data[0].equals("Status Code: 429")) {
                                Thread.sleep(3000);
                            } else if (data[0] != null && data[0].equals("Reset")) {
                                LOGGER.info("Reset condition, maintaining HUD until reset");
                            } else {
                                BlockifyHUD.updateData(data);
                            }
                        } else if (SpotifyUtil.isPlaying()) {
                            BlockifyHUD.setProgress(BlockifyHUD.getProgress() + 1000);
                        }
                    } else {
                        BlockifyHUD.setProgress(0);
                        BlockifyHUD.setDuration(-1);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        requestThread.setName("Spotify Thread");
        requestThread.start();

        SpotifyUtil.initialize();

        prevKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "blockify.key.prev", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_4, BLOCKIFY_CATEGORY));
        playKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "blockify.key.play", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_5, BLOCKIFY_CATEGORY));
        nextKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "blockify.key.next", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_6, BLOCKIFY_CATEGORY));
        addTrackKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "blockify.key.addTrack", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_L, BLOCKIFY_CATEGORY));
        forceKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "blockify.key.force", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_8, BLOCKIFY_CATEGORY));
        hideKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "blockify.key.hide", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_9, BLOCKIFY_CATEGORY));
        increaseVolumeKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "blockify.key.increaseVolume", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_ADD, BLOCKIFY_CATEGORY));
        decreaseVolumeKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "blockify.key.decreaseVolume", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_SUBTRACT, BLOCKIFY_CATEGORY));
        toggleInGameMusicKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "blockify.key.toggleInGameMusic", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_1, BLOCKIFY_CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            try {
                playKeyHandler(playKey.isDown());
                nextKeyHandler(nextKey.isDown());
                prevKeyHandler(prevKey.isDown());
                forceKeyHandler(forceKey.isDown());
                addTrackKeyHandler(addTrackKey.isDown());
                hideKeyHandler(hideKey.isDown());
                increaseVolumeKeyHandler(increaseVolumeKey.isDown());
                decreaseVolumeKeyHandler(decreaseVolumeKey.isDown());
                toggleInGameMusicKeyHandler(toggleInGameMusicKey.isDown());
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    ClientCommands.literal("sharetrack").executes(context -> {
                        var player = Minecraft.getInstance().player;
                        if (player == null) {
                            return 0;
                        }
                        if (BlockifyHUD.hudInfo[5] != null) {
                            player.sendSystemMessage(Component.literal(BlockifyHUD.hudInfo[5]));
                        }
                        return 0;
                    })
            );
        });
    }

    public void playKeyHandler(boolean currPressState) {
        try {
            if (currPressState && !playKeyPrevState) {
                if (SpotifyUtil.isAuthorized()) {
                    LOGGER.info("Authorized!");
                    SpotifyUtil.playPause();
                } else {
                    Util.getPlatform().openUri(URI.create(SpotifyUtil.authorize()));
                }
            }
            playKeyPrevState = currPressState;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void nextKeyHandler(boolean currPressState) {
        if (currPressState && !nextKeyPrevState) {
            LOGGER.info("Next Key Pressed");
            SpotifyUtil.nextSong();
        }
        nextKeyPrevState = currPressState;
    }

    public void prevKeyHandler(boolean currPressState) {
        if (currPressState && !prevKeyPrevState) {
            LOGGER.info("Previous Key Pressed");
            SpotifyUtil.prevSong();
        }
        prevKeyPrevState = currPressState;
    }

    public void addTrackKeyHandler(boolean currPressState) {
        if (currPressState && !addTrackKeyPrevState) {
            LOGGER.info("Add Track Key Pressed");
            SpotifyUtil.addTrack();
        }
        addTrackKeyPrevState = currPressState;
    }

    public void forceKeyHandler(boolean currPressState) {
        if (currPressState && !forceKeyPrevState) {
            LOGGER.info("Force Key Pressed");
            BlockifyHUD.setDuration(-2000);
        }
        forceKeyPrevState = currPressState;
    }

    public void hideKeyHandler(boolean currPressState) {
        if (currPressState && !hideKeyPrevState) {
            LOGGER.info("Hide Key Pressed");
            BlockifyHUD.isHidden = !BlockifyHUD.isHidden;
        }
        hideKeyPrevState = currPressState;
    }

    public void increaseVolumeKeyHandler(boolean currPressState) {
        if (currPressState && !increaseVolumeKeyPrevState) {
            LOGGER.info("Increase Volume Key Pressed");
            BlockifyHUD.increaseVolume();
        }
        increaseVolumeKeyPrevState = currPressState;
    }

    public void decreaseVolumeKeyHandler(boolean currPressState) {
        if (currPressState && !decreaseVolumeKeyPrevState) {
            LOGGER.info("Decrease Volume Key Pressed");
            BlockifyHUD.decreaseVolume();
        }
        decreaseVolumeKeyPrevState = currPressState;
    }

    public void toggleInGameMusicKeyHandler(boolean currPressState) {
        if (currPressState && !toggleInGameMusicKeyPrevState) {
            LOGGER.info("Toggle In Game Music Key Pressed");
            SpotifyUtil.toggleInGameMusic();
        }
        toggleInGameMusicKeyPrevState = currPressState;
    }
}
