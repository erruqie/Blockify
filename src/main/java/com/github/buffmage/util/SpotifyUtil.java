package com.github.buffmage.util;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class SpotifyUtil
{
    private static String client_id = "bd91ca99ddc2470aabf25c025107ceea";
    private static String challenge;
    private static String verifier;
    private static String authCode;
    private static String accessToken;
    private static String refreshToken;
    private static String apiAddress = "https://accounts.spotify.com/api/";
    private static HttpClient client;
    private static HttpServer authServer;
    private static ThreadPoolExecutor threadPoolExecutor;
    private static HttpRequest playbackRequest;
    private static HttpResponse<String> playbackResponse;
    private static File authFile;
    private static boolean isAuthorized = false;


    public static void initialize()
    {
        authFile = new File(System.getProperty("user.dir") + File.separator +
                "mods" + File.separator + "blockifyTokens.json");
        try
        {
            if (!authFile.exists())
            {
                authFile.createNewFile();
                System.out.println("Created new token file at: " + authFile.getAbsolutePath());
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
        }
        catch(IOException e)
        {
            e.printStackTrace();
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
        }
        catch(Exception e)
        {
            e.printStackTrace();
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
                    new URI(apiAddress + "token"))
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
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void refreshAuthToken()
    {

    }

    public static void refreshActiveSession()
    {

    }

    public static void putRequest(String type)
    {

    }

    public static void postRequest(String type)
    {

    }

    public static void nextSong()
    {

    }

    public static void prevSong()
    {

    }

    public static void playSong()
    {

    }

    public static void pauseSong()
    {

    }

    public static String [] getPlaybackInfo()
    {
        String [] results = new String[5];
        try
        {
            playbackResponse = client.send(playbackRequest, HttpResponse.BodyHandlers.ofString());
            if (playbackResponse.statusCode() != 200)
            {
                results[0] = "Status Code: " + playbackResponse.statusCode();
                return results;
            }
            JsonObject json = (JsonObject) new JsonParser().parse(playbackResponse.body());

            results[0] = json.get("item").getAsJsonObject().get("name").getAsString();
            results[1] = json.get("item").getAsJsonObject().get("artists").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString();
            results[2] = json.get("progress_ms").getAsString();
            results[3] = json.get("item").getAsJsonObject().get("duration_ms").getAsString();
            results[4] = json.get("item").getAsJsonObject().get("album").getAsJsonObject().get("images")
                    .getAsJsonArray().get(1).getAsJsonObject().get("url").getAsString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    public static void updatePlaybackRequest()
    {
        playbackRequest = HttpRequest.newBuilder(
                URI.create("https://api.spotify.com/v1/me/player"))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json").build();
    }

    public static boolean isAuthorized()
    {
        return isAuthorized;
    }

}
