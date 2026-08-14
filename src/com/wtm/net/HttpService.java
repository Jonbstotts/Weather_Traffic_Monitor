package com.wtm.net;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;

/** Centralized HTTP client with timeouts and a descriptive User-Agent. */
public final class HttpService {
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).followRedirects(HttpClient.Redirect.NORMAL).build();
    private static final String UA = "WeatherTrafficMonitor/1.0 (workplace-display; contact=local-admin)";

    public String getText(String url) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(12)).header("User-Agent", UA).header("Accept", "application/geo+json, application/json, text/plain, */*").GET().build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() < 200 || res.statusCode() >= 300) throw new IOException("HTTP " + res.statusCode() + " for " + url);
        return res.body();
    }
    public byte[] getBytes(String url) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(12)).header("User-Agent", UA).GET().build();
        HttpResponse<byte[]> res = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
        if (res.statusCode() < 200 || res.statusCode() >= 300) throw new IOException("HTTP " + res.statusCode() + " for " + url);
        return res.body();
    }
}
