package io.stockman.exchange.moex.iss;

import io.stockman.domain.exchange.moex.Security;

import java.util.function.Consumer;

/**
 * Created by maksim.alekseev on 18/09/2018
 */
public interface MoexClient {

    /**
     * Iterates over a list of securities in specific engine and market
     *
     * @param engine engine name, like 'stock'
     * @param market market name, like 'shares' or 'bonds'
     * @param callback callback function
     */
    void listSecurities(String engine, String market, Consumer<Security> callback);

}
