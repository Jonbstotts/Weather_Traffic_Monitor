package com.wtm.net;

import com.wtm.usage.ApiUsageTracker;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;

/** Centralized HTTP client with timeouts and a descriptive User-Agent. */
public final class HttpService {
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).followRedirects(HttpClient.Redirect.NORMAL).build();
    private static final String UA = "WeatherTrafficMonitor/1.0 (workplace-display; contact=local-admin)";

    public String getText(String url) throws IOException, InterruptedException {
        return getText(url, UA);
    }

    /** Allows providers such as NWS to use a site-configurable identifying User-Agent. */
    public String getText(String url, String userAgent) throws IOException, InterruptedException {
        String ua=(userAgent==null||userAgent.isBlank())?UA:userAgent;
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(12)).header("User-Agent", ua).header("Accept", "application/geo+json, application/json, text/plain, */*").GET().build();
        ApiUsageTracker.get().record(url);
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() < 200 || res.statusCode() >= 300) throw new IOException("HTTP " + res.statusCode() + " for " + url);
        return res.body();
    }
    /** Performs a JSON/text GET with one provider-specific request header. */
    public String getTextWithHeader(String url, String headerName, String headerValue)
            throws IOException, InterruptedException {
        HttpRequest.Builder b=HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(12))
                .header("User-Agent",UA)
                .header("Accept","application/json, text/plain, */*")
                .GET();
        if(headerName!=null && !headerName.isBlank() && headerValue!=null && !headerValue.isBlank())
            b.header(headerName,headerValue);
        ApiUsageTracker.get().record(url);
        HttpResponse<String> res=client.send(b.build(),HttpResponse.BodyHandlers.ofString());
        if(res.statusCode()<200 || res.statusCode()>=300)
            throw new IOException("HTTP "+res.statusCode()+" for "+url);
        return res.body();
    }

    public byte[] getBytes(String url) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(12)).header("User-Agent", UA).GET().build();
        ApiUsageTracker.get().record(url);
        HttpResponse<byte[]> res = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
        if (res.statusCode() < 200 || res.statusCode() >= 300) throw new IOException("HTTP " + res.statusCode() + " for " + url);
        return res.body();
    }
}
