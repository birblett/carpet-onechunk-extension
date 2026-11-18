package com.birblett.util;

import carpet.CarpetSettings;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WebhookUtils {

    public static void sendDiscordMessage(String url, String message) {
        String payload = "{\"content\": \"" + message + "\"}";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
        try {
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        System.out.println("Status Code: " + response.statusCode());
                        return response.body();
                    });
        } catch (Exception e) {
            CarpetSettings.LOG.warn("Failed to send to webhook with url " + url + ": " + e);
        }
    }

}