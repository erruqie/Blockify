package com.clownless.blockify.util;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


public class AuthServerHandler implements HttpHandler
{

    @Override
    public void handle(HttpExchange httpExchange) throws IOException
    {
        String requestParamValue = null;
        if ("GET".equals(httpExchange.getRequestMethod()))
        {
            requestParamValue = handleGetRequest(httpExchange);
        }
        try
        {
            handleResponse(httpExchange, requestParamValue);
        } catch (URISyntaxException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }


    private String handleGetRequest(HttpExchange httpExchange)
    {
        return httpExchange
                .getRequestURI()
                .toString()
                .split("\\?")[1]
                .split("=")[1];
    }

    private void handleResponse (HttpExchange httpExchange, String requestParamValue) throws IOException, URISyntaxException, InterruptedException
    {
        OutputStream outputStream = httpExchange.getResponseBody();
        StringBuilder htmlBuilder = new StringBuilder();

        htmlBuilder.append("<html>")
                .append("<body>")
                .append("<h1>")
                .append("Success!")
                .append("</h1>")
                .append("</body?")
                .append("</html>");

        String htmlResponse = htmlBuilder.toString();
        httpExchange.sendResponseHeaders(200, htmlResponse.length());
        outputStream.write(htmlResponse.getBytes());
        outputStream.flush();
        outputStream.close();
        SpotifyUtil.authorize(requestParamValue);
    }

}