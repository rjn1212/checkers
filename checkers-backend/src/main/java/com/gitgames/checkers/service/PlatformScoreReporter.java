package com.gitgames.checkers.service;

import com.gitgames.checkers.model.Game;
import com.gitgames.checkers.model.Player;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class PlatformScoreReporter {

    private static final Logger log = Logger.getLogger(PlatformScoreReporter.class.getName());

    @Value("${platform.api.url:}")
    private String platformApiUrl;

    @Value("${platform.api.key:}")
    private String platformApiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void reportGameResult(Game game) {
        if (platformApiUrl.isEmpty() || platformApiKey.isEmpty()) return;

        String userId = game.getPlatformUserIds().get(game.getHumanSide());
        if (userId == null) return;

        boolean won = game.getWinner() == game.getHumanSide();
        int captured = game.getCapturedCount().getOrDefault(game.getHumanSide(), 0);
        int lost = game.getCapturedCount().getOrDefault(game.getAiSide(), 0);

        String body = String.format(
                "{\"userId\":\"%s\",\"game\":\"checkers\",\"result\":\"%s\",\"score\":%d,\"metadata\":{\"difficulty\":\"%s\",\"captured\":%d,\"lost\":%d,\"moves\":%d}}",
                userId, won ? "win" : "loss", won ? 1 : 0,
                game.getAiDifficulty().name(), captured, lost, game.getMoveHistory().size()
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(platformApiUrl + "/api/scores"))
                .header("Content-Type", "application/json")
                .header("X-API-Key", platformApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .exceptionally(e -> {
                    log.log(Level.WARNING, "Failed to report score to platform", e);
                    return null;
                });
    }
}
