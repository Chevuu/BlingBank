package sirs.com.controller;

import sirs.com.Main;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;

public class SharedController {

    public static HttpRequest buildPostRequest(String url, String jsonText) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonText))
                .build();
    }

    public static HttpRequest buildGetRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .build();
    }

    public static HttpRequest buildDeleteRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .DELETE()
                .build();
    }

    public static boolean setNewScreen(String scene) {
        try {
            Main.setRoot("/screens/" + scene + ".fxml");
        } catch (IOException e) {
            System.out.println("Wrong FXML path.");
            return false;
        }
        return true;
    }
}
