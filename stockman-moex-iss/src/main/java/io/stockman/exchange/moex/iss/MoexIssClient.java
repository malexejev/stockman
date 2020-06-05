package io.stockman.exchange.moex.iss;

import io.stockman.domain.exchange.moex.Security;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

/**
 * Created by maksim.alekseev on 18/09/2018
 */
@Service
public class MoexIssClient implements MoexClient {

    private static final Logger LOG = LoggerFactory.getLogger(MoexIssClient.class);

    private static final ApiTransport TRANSPORT = ApiTransport.CSV;
    private static final String SEC_LIST_URL = "https://iss.moex.com/iss/engines/%s/markets/%s/securities.%1s";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final ConcurrentMap<String, HttpRequest> reqCache = new ConcurrentHashMap<>();

    @Override
    public void listSecurities(String engine, String market, Consumer<Security> callback) {
        String key = engine + '/' + market;
        HttpRequest request = reqCache.computeIfAbsent(key, k -> {
            String url = String.format(SEC_LIST_URL, engine, market, TRANSPORT.name().toLowerCase());
            HttpRequest newRequest = HttpRequest.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create(url))
//                .timeout(Duration.ofMillis(500))
                    .build();
            return newRequest;
        });

//        fetchSync(callback);
        fetchAsync(request, callback);
//        fetchReactive(callback);
    }

    private void fetchSync(HttpRequest req, Consumer<Security> callback) {
        try {
            // blocks here
            var res = httpClient.send(req, HttpResponse.BodyHandlers.ofLines());
            if (res.statusCode() == 200) {
                res.body().map(this::parseSecurity).forEach(callback);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void fetchAsync(HttpRequest req, Consumer<Security> callback) {
        // FIXME MA: check response status code before reading body
        long start = System.nanoTime();
        LOG.info("Start: {}", System.nanoTime() - start);
        httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofLines())
//                .thenApply(res -> {
//                    LOG.info("Response received: {}", System.nanoTime() - start);
//                    if (res.statusCode() != 200) { throw new IllegalStateException(); }
//                    return res;
//                })
                .thenApply(HttpResponse::body)
//                .exceptionally(e -> {
//                    LOG.info("Failed to query MOEX API", e);
//                    return null;
//                })
                .thenAccept(lines -> {
                    LOG.info("After .body(): {}", System.nanoTime() - start);
                    lines.forEach(line -> {
                        LOG.info("After .body(): {}", System.nanoTime() - start);
                        Security security = parseSecurity(line);
                        // TODO MA: exceptions?
                        callback.accept(security);
                    });
                })
                .join(); // dont need join, just for testing
        LOG.info("Done: {}", System.nanoTime() - start);
    }

    private void fetchReactive(HttpRequest req, Consumer<Security> callback) {

    }

    protected Security parseSecurity(String line) {
        return null;
//        return new Security();
    }

}
