package io.stockman.exchange.moex.iss;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Tests for {@link MoexIssClient}.
 *
 * Created by maksim.alekseev on 18/09/2018
 */
public class MoexIssClientTest {

    private final MoexIssClient issClient = new MoexIssClient();

    @Test
    public void listSecurities() {
        AtomicInteger bondsCount = new AtomicInteger();
        issClient.listSecurities("stock", "bonds", s -> bondsCount.incrementAndGet());
        System.out.println("Bonds read: " + bondsCount.get());
    }

}