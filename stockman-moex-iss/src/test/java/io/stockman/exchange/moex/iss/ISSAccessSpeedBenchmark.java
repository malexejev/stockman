package io.stockman.exchange.moex.iss;

import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;
import org.junit.Assert;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.Charset;

import static io.stockman.exchange.moex.iss.ApiTransport.*;
import static jdk.incubator.http.HttpResponse.BodyHandler.asString;

/**
 * Tests different HTTP and API params to find the most effective way to query MOEX ISS API.
 * Results available at https://github.com/malexejev/stockman/wiki/ISS-API-Client
 *
 * Created by maksim.alekseev on 15/06/2018
 */
@BenchmarkMode(Mode.AverageTime)
@State(Scope.Benchmark)
@Warmup(iterations = 0)
@Measurement(iterations = 3)
public class ISSAccessSpeedBenchmark {

    private static final Logger LOG = LoggerFactory.getLogger(ISSAccessSpeedBenchmark.class);

    private static final boolean ASYNC = false;
    private static final String INDEX_URL = "https://iss.moex.com/iss/index.%1s";
    private static final String BONDS_LIST_URL = "https://iss.moex.com/iss/engines/stock/markets/bonds/securities.%1s";

    private final HttpClient httpClient;

    public ISSAccessSpeedBenchmark() {
        httpClient = HttpClient.newHttpClient();
    }

    @Test
    public void debugHttpCall() {
        for (int i = 0; i < 100; i++) {
            long start = System.currentTimeMillis();
            try {
                makeHttpCall(INDEX_URL, CSV);
            } finally {
                long elapsed = System.currentTimeMillis() - start;
                LOG.info("Elapsed: {} ms", elapsed);
            }
        }
    }

    @Benchmark
    public void testIndexCsv() {
        makeHttpCall(INDEX_URL, CSV);
    }

    @Benchmark
    public void testIndexJson() {
        makeHttpCall(INDEX_URL, JSON);
    }

    @Benchmark
    public void testIndexXml() {
        makeHttpCall(INDEX_URL, XML);
    }

    @Benchmark
    public void testIndexHtml() {
        makeHttpCall(INDEX_URL, HTML);
    }

    @Benchmark
    public void testBondsListCsv() {
        makeHttpCall(BONDS_LIST_URL, CSV);
    }

    @Benchmark
    public void testBondsListJson() {
        makeHttpCall(BONDS_LIST_URL, JSON);
    }

    @Benchmark
    public void testBondsListXml() {
        makeHttpCall(BONDS_LIST_URL, XML);
    }

    @Benchmark
    public void testBondsListHtml() {
        makeHttpCall(BONDS_LIST_URL, HTML);
    }

    protected String makeHttpCall(String urlTemplate, ApiTransport transport) {
        String body = ASYNC ? makeAsyncHttpCall(urlTemplate, transport) : makeSyncHttpCall(urlTemplate, transport);
        Assert.assertTrue(body.length() > 1024);
        return body;
    }

    /**
     * Simple synchronous blocking request
     */
    protected String makeSyncHttpCall(String urlTemplate, ApiTransport transport) {
        String url = String.format(urlTemplate, transport.name().toLowerCase());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        try {
            Charset charset = transport.getCharset();
            String responseBody = httpClient.send(request, asString(charset)).body();
            return responseBody;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Async request that we manually block on, just to see if there is any difference
     */
    protected String makeAsyncHttpCall(String urlTemplate, ApiTransport transport) {
        String url = String.format(urlTemplate, transport.name().toLowerCase());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        Charset charset = transport.getCharset();
        String responseBody = httpClient.sendAsync(request, asString(charset))
                .thenApply(HttpResponse::body)
                .join();
        return responseBody;
    }

}
