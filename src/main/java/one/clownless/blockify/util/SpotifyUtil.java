package one.clownless.blockify.util;


import one.clownless.blockify.BlockifyConfig;
import one.clownless.blockify.BlockifyHUD;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.client.option.GameOptions;
import net.minecraft.sound.SoundCategory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class SpotifyUtil
{
    private static String client_id = "d34978659f8940e9bfce52d124539feb";
    private static String challenge;
    private static String verifier;
    private static String authCode;
    private static String accessToken;
    private static String refreshToken;
    private static String tokenAddress = "https://accounts.spotify.com/api/token";
    private static String playerAddress = "https://api.spotify.com/v1/me/player/";
    private static HttpClient client;
    private static HttpServer authServer;
    private static ThreadPoolExecutor threadPoolExecutor;
    private static HttpRequest playbackRequest;
    private static HttpResponse<String> playbackResponse;
    private static File authFile;
    private static boolean isAuthorized = false;
    private static boolean isPlaying = false;

    public static final Logger LOGGER = LogManager.getLogger("Blockify");
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    public static void initialize()
    {
        authFile = new File(System.getProperty("user.dir") + File.separator +
                "config" + File.separator + "blockifyTokens.json");
        try
        {
            if (!authFile.exists())
            {
                authFile.createNewFile();
                LOGGER.info("Created new token file at: " + authFile.getAbsolutePath());
                accessToken = "";
                refreshToken = "";
                isAuthorized = false;
            }
            else
            {
                Scanner scan = new Scanner(authFile);
                JsonObject authJson = null;
                if (scan.hasNextLine())
                {
                    authJson = new JsonParser().parse(scan.nextLine()).getAsJsonObject();
                    accessToken = authJson.get("access_token").getAsString();
                    refreshToken = authJson.get("refresh_token").getAsString();
                    isAuthorized = true;
                }
                else
                {
                    accessToken = "";
                    refreshToken = "";
                    isAuthorized = false;
                }
                scan.close();
            }
        } catch (IOException e)
        {
            LOGGER.error(e.getMessage());
        }
        client = HttpClient.newHttpClient();
        updatePlaybackRequest();
    }

    public static String authorize()
    {
        StringBuilder authURI = null;
        try
        {
            authURI = new StringBuilder();
            authURI.append("https://accounts.spotify.com/authorize");
            authURI.append("?client_id=" + client_id);
            authURI.append("&response_type=code");
            authURI.append("&redirect_uri=http%3A%2F%2Flocalhost%3A8001%2Fcallback");
            authURI.append("&scope=user-read-playback-state%20user-read-currently-playing");
            authURI.append("%20user-modify-playback-state");
            authURI.append("&code_challenge_method=S256");
            verifier = PKCEUtil.generateCodeVerifier();
            challenge = PKCEUtil.generateCodeChallenge(verifier);
            authURI.append("&code_challenge=" + challenge);
            authServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 8001), 0);
            threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
            authServer.setExecutor(threadPoolExecutor);
            authServer.createContext("/callback", new AuthServerHandler());
            authServer.start();
        } catch (Exception e)
        {
            LOGGER.error(e.getMessage());
        }
        if (authURI == null)
        {
            return "https://www.google.com";
        }

        return authURI.toString();
    }

    public static void authorize(String authCode)
    {
        SpotifyUtil.authCode = authCode;
        authServer.stop(0);
        threadPoolExecutor.shutdown();
        requestAccessToken();
    }

    private static void requestAccessToken()
    {

        try
        {
            StringBuilder accessBody = new StringBuilder();
            accessBody.append("grant_type=authorization_code");
            accessBody.append("&code=" + authCode);
            accessBody.append("&redirect_uri=http%3A%2F%2Flocalhost%3A8001%2Fcallback");
            accessBody.append("&client_id=" + client_id);
            accessBody.append("&code_verifier=" + verifier);
            HttpRequest accessRequest = HttpRequest.newBuilder(
                    new URI(tokenAddress))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(accessBody.toString()))
                    .build();
            HttpResponse<String> accessResponse = client.send(accessRequest, HttpResponse.BodyHandlers.ofString());
            JsonObject accessJson = new JsonParser().parse(accessResponse.body()).getAsJsonObject();
            accessToken = accessJson.get("access_token").getAsString();
            refreshToken = accessJson.get("refresh_token").getAsString();
            updatePlaybackRequest();
            updateJson();
            isAuthorized = true;
        } catch (Exception e)
        {
            LOGGER.error(e.getMessage());
        }
    }

    public static boolean refreshAccessToken()
    {
        try
        {
            StringBuilder refreshRequestBody = new StringBuilder();
            refreshRequestBody.append("grant_type=refresh_token");
            refreshRequestBody.append("&refresh_token=" + refreshToken);
            refreshRequestBody.append("&client_id=" + client_id);

            HttpRequest refreshRequest = HttpRequest.newBuilder(
                    new URI(tokenAddress))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(refreshRequestBody.toString()))
                    .build();

            HttpResponse<String> refreshResponse = client.send(refreshRequest, HttpResponse.BodyHandlers.ofString());
            if (refreshResponse.statusCode() == 200)
            {
                JsonObject refreshJson = new JsonParser().parse(refreshResponse.body()).getAsJsonObject();
                accessToken = refreshJson.get("access_token").getAsString();
                refreshToken = refreshJson.get("refresh_token").getAsString();
                updateJson();
                updatePlaybackRequest();
                return true;
            }
        } catch (Exception e)
        {
            LOGGER.error(e.getMessage());
        }
        return false;
    }

    public static void refreshActiveSession()
    {
        try
        {
            HttpRequest getDevices = HttpRequest.newBuilder(
                    new URI(playerAddress + "devices"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> devices = client.send(getDevices, HttpResponse.BodyHandlers.ofString());
            JsonArray devicesJson = new JsonParser().parse(devices.body()).getAsJsonObject().get("devices").getAsJsonArray();
            JsonObject currDevice;
            String computerName = InetAddress.getLocalHost().getHostName();
            String thisDeviceID = "";
            if (devicesJson.size() == 0)
            {
                BlockifyHUD.setDuration(1);
                BlockifyHUD.setProgress(0);
                isPlaying = false;
                return;
            }
            for (int i = 0; i < devicesJson.size(); i++)
            {
                currDevice = devicesJson.get(i).getAsJsonObject();
                if (currDevice.get("name").getAsString().equals(computerName))
                {
                    thisDeviceID = currDevice.get("id").getAsString();
                    break;
                }
            }
            String deviceIDBody = "{\"device_ids\" : [\"" + thisDeviceID + "\"]}";
            HttpRequest setActive = HttpRequest.newBuilder(
                    new URI(playerAddress))
                    .header("Authorization", "Bearer " + accessToken)
                    .PUT(HttpRequest.BodyPublishers.ofString(deviceIDBody))
                    .build();
            LOGGER.info("Responded with :" + client.send(setActive, HttpResponse.BodyHandlers.ofString()).statusCode());

        } catch (Exception e)
        {
            LOGGER.error(e.getMessage());
        }
        LOGGER.info("Successfully refreshed active session");
    }

    public static void putRequest(String type)
    {
        try
        {
            HttpRequest putReq = HttpRequest.newBuilder(new URI("https://api.spotify.com/v1/me/player/" + type))
                    .PUT(HttpRequest.BodyPublishers.ofString(""))
                    .header("Authorization", "Bearer " + accessToken).build();
            HttpResponse<String> putRes = client.send(putReq, HttpResponse.BodyHandlers.ofString());
            LOGGER.info("PUT Request (" + type + "): " + putRes.statusCode());
            if (putRes.statusCode() == 404)
            {
                refreshActiveSession();
                LOGGER.info("Retrying put request...");
                putRes = client.send(putReq, HttpResponse.BodyHandlers.ofString());
                LOGGER.info("PUT Request (" + type + "): " + putRes.statusCode());
            }
            else if (putRes.statusCode() == 403)
            {
                MinecraftClient.getInstance().player.sendMessage(Text.of("Spotify Premium is required for this feature."));
            }
            else if (putRes.statusCode() == 401)
            {
                if (refreshAccessToken())
                {
                    putRequest(type);
                }
                else
                {
                    isAuthorized = false;
                }
            }
        } catch (IOException | InterruptedException | URISyntaxException e)
        {
            if (e instanceof IOException && e.getMessage().equals("Connection reset"))
            {
                LOGGER.info("Attempting to retry put request...");
                putRequest(type);
                LOGGER.info("Successfully sent put request");
            }
            else
            {
                LOGGER.error(e.getMessage());
            }
        }

    }

    public static void postRequest(String type)
    {
        try
        {
            HttpRequest postReq = HttpRequest.newBuilder(new URI("https://api.spotify.com/v1/me/player/" + type))
                    .POST(HttpRequest.BodyPublishers.ofString(""))
                    .header("Authorization", "Bearer " + accessToken).build();
            HttpResponse<String> postRes = client.send(postReq, HttpResponse.BodyHandlers.ofString());
            LOGGER.info("POST Request (" + type + "): " + postRes.statusCode());
            if (postRes.statusCode() == 404)
            {
                refreshActiveSession();
                LOGGER.info("Retrying post request...");
                postRes = client.send(postReq, HttpResponse.BodyHandlers.ofString());
                LOGGER.info("POST Request (" + type + "): " + postRes.statusCode());
            }
            else if (postRes.statusCode() == 403)
            {
                MinecraftClient.getInstance().player.sendMessage(Text.of("Spotify Premium is required for this feature."));
            }
            else if (postRes.statusCode() == 401)
            {
                if (refreshAccessToken())
                {
                    postRequest(type);
                }
                else
                {
                    isAuthorized = false;
                }
            }
        } catch (IOException | InterruptedException | URISyntaxException e)
        {
            if (e instanceof IOException && e.getMessage().equals("Connection reset"))
            {
                LOGGER.info("Attempting to retry post request...");
                postRequest(type);
                LOGGER.info("Successfully sent post request");
            }
            else
            {
                LOGGER.error(e.getMessage());
            }
        }
    }

    public static void nextSong()
    {
        EXECUTOR_SERVICE.execute(() -> {
            postRequest("next");
            BlockifyHUD.setDuration(-2000);
        });
    }

    public static void prevSong()
    {
        EXECUTOR_SERVICE.execute(() -> {
            postRequest("previous");
            BlockifyHUD.setDuration(-2000);
        });
    }

    public static void playSong()
    {
        EXECUTOR_SERVICE.execute(() -> {
            putRequest("play");
        });
    }

    public static void pauseSong()
    {
        EXECUTOR_SERVICE.execute(() -> {
            putRequest("pause");
        });
    }

    public static void playPause()
    {
        if (isPlaying)
        {
            pauseSong();
            isPlaying = false;
        }
        else
        {
            playSong();
            isPlaying = true;
        }
    }

    public static String[] getPlaybackInfo()
    {
        String[] results = new String[7];
        try
        {
            playbackResponse = client.send(playbackRequest, HttpResponse.BodyHandlers.ofString());
            if (playbackResponse.statusCode() == 429)
            {
                results[0] = "Status Code: " + playbackResponse.statusCode();
                return results;
            }
            if (playbackResponse.statusCode() == 200)
            {
                JsonObject json = (JsonObject) new JsonParser().parse(playbackResponse.body());
                if (json.get("currently_playing_type").getAsString().equals("episode"))
                {
                    results[0] = json.get("item").getAsJsonObject().get("name").getAsString();
                    results[1] = json.get("item").getAsJsonObject().get("show").getAsJsonObject().get("name").getAsString();
                    results[2] = json.get("progress_ms").getAsString();
                    results[3] = json.get("item").getAsJsonObject().get("duration_ms").getAsString();
                    results[4] = json.get("item").getAsJsonObject().get("images").getAsJsonArray().get(1).getAsJsonObject().get("url").getAsString();
                    return results;
                }
                results[0] = json.get("item").getAsJsonObject().get("name").getAsString();
                JsonArray artistArray = json.get("item").getAsJsonObject().get("artists").getAsJsonArray();
                StringBuilder artistString = new StringBuilder();
                for (int i = 0; i < artistArray.size(); i++)
                {
                    if (i == artistArray.size() - 1)
                    {
                        artistString.append(artistArray.get(i).getAsJsonObject().get("name").getAsString());
                    }
                    else
                    {
                        artistString.append(artistArray.get(i).getAsJsonObject().get("name").getAsString());
                        artistString.append(", ");
                    }
                }
                results[1] = artistString.toString();
                results[2] = json.get("progress_ms").getAsString();
                results[3] = json.get("item").getAsJsonObject().get("duration_ms").getAsString();
                JsonArray imageArray = json.get("item").getAsJsonObject().get("album").getAsJsonObject().get("images")
                        .getAsJsonArray();
                if (imageArray.size() > 1)
                {
                    results[4] = imageArray.get(1).getAsJsonObject().get("url").getAsString();
                }
                else
                {
                    results[4] = null;
                }
                results[5] = json.get("item").getAsJsonObject().get("external_urls").getAsJsonObject().get("spotify").getAsString();
                results[6] = json.get("device").getAsJsonObject().get("volume_percent").getAsString();
                isPlaying = json.get("is_playing").getAsBoolean();
            }
            else if (playbackResponse.statusCode() == 401)
            {
                if (!refreshAccessToken())
                {
                    isAuthorized = false;
                }
            }
            else
            {
                results[0] = "Status Code: " + playbackResponse.statusCode();
                return results;
            }
        } catch (Exception e)
        {
            if (e instanceof IOException && e.getMessage().equals("Connection reset"))
            {
                LOGGER.info("Resetting connection and retrying info get...");
                results[0] = "Reset";
            }
            else
            {
                LOGGER.error(e.getMessage());
            }
        }
        return results;
    }

    public static void updateJson()
    {
        try
        {
            FileWriter jsonWriter = new FileWriter(authFile);
            jsonWriter.write("{" + "\"access_token\" : \"" + accessToken + "\", \"refresh_token\" : \"" + refreshToken + "\" }");
            jsonWriter.flush();
            jsonWriter.close();
        } catch (IOException e)
        {
            LOGGER.error(e.getMessage());
        }
    }

    public static void updatePlaybackRequest()
    {
        playbackRequest = HttpRequest.newBuilder(
                URI.create("https://api.spotify.com/v1/me/player?additional_types=episode"))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json").build();
    }

    public static boolean isAuthorized()
    {
        return isAuthorized;
    }

    public static boolean isPlaying()
    {
        return isPlaying;
    }

    public static void toggleInGameMusic()
    {
        GameOptions options = MinecraftClient.getInstance().options;
        if (!isPlaying()) {
            options.getSoundVolumeOption(SoundCategory.MUSIC).setValue(BlockifyConfig.inGameMusicVolume);
            MinecraftClient.getInstance().player.sendMessage(Text.of("In-game music is now enabled"));
        } else {
            options.getSoundVolumeOption(SoundCategory.MUSIC).setValue(0.0d);
            MinecraftClient.getInstance().player.sendMessage(Text.of("In-game music is now disabled"));
        }
        options.write();
    }
}
