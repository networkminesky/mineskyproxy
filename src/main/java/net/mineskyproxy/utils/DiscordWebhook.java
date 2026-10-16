package net.mineskyproxy.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class DiscordWebhook {

    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private final String url;
    private String username;
    private String avatarUrl;
    private String content;

    public DiscordWebhook(String url) {
        this.url = url;
    }

    public DiscordWebhook setUsername(String username) {
        this.username = username;
        return this;
    }

    public DiscordWebhook setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        return this;
    }

    public DiscordWebhook setContent(String content) {
        this.content = content;
        return this;
    }

    public CompletableFuture<Boolean> sendAsync(Logger logger) {
        if (this.url == null || this.url.isBlank()) {
            return CompletableFuture.completedFuture(false);
        }

        JsonObject json = new JsonObject();
        if (this.content != null && !this.content.isBlank()) {
            json.addProperty("content", this.content);
        }
        if (this.username != null) {
            json.addProperty("username", this.username);
        }
        if (this.avatarUrl != null) {
            json.addProperty("avatar_url", this.avatarUrl);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.url))
                .header("Content-Type", "application/json")
                .header("User-Agent", "MineSkyProxy-DiscordWebhook/1.0")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(json)))
                .build();

        return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .thenApply(response -> {
                    int status = response.statusCode();
                    return status >= 200 && status < 300;
                })
                .exceptionally(ex -> {
                    if (logger != null) {
                        logger.warn("[DiscordWebhook] Erro ao enviar webhook para o Discord: {}", ex.getMessage());
                    }
                    return false;
                });
    }
}