package com.practice.ts.PracticeTwo.services;

import com.google.gson.GsonBuilder;
import com.practice.ts.PracticeTwo.models.StoryItemDto;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.net.http.HttpResponse.BodyHandlers.ofString;

@Service
public class HackerNewsService {
    private final String baseurl = "https://hacker-news.firebaseio.com/v0/";
    private final int TOP_LIMIT = 10;


    public List<StoryItemDto> getWithGson() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        var idReq = HttpRequest.newBuilder()
                .uri(new URI(baseurl + "topstories.json"))
                .build();
        var idResp = client.send(idReq, ofString());
        var uris = Arrays.stream(new GsonBuilder().create().fromJson(idResp.body(), Integer[].class))
                .limit(TOP_LIMIT)
                .map(this::buildUri)
                .toList();

        List<HttpRequest> requests = uris.stream()
                .map(HttpRequest::newBuilder)
                .map(HttpRequest.Builder::build)
                .toList();

        List<StoryItemDto> storyNewsItems = requests.parallelStream()
                .map(req -> makeRequest(req, client))
                .filter(Objects::nonNull)
                .map(item -> new GsonBuilder().create().fromJson(item.body(), StoryItemDto.class))
                .toList();

        return storyNewsItems;
    }


    private URI buildUri(Integer id) {
        try {
            return new URI(baseurl + "item/" + id + ".json");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpResponse<String> makeRequest(HttpRequest request, HttpClient client) {
        try {
            return client.send(request, ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}


