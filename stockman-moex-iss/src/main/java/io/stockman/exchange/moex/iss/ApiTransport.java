package io.stockman.exchange.moex.iss;

import java.nio.charset.Charset;

/**
 * MOEX ISS API supports getting all results in one of 4 forms for every API method.
 * If affects actual body contents and also content encoding.
 *
 * Created by maksim.alekseev on 15/06/2018
 */
public enum ApiTransport {

    CSV("Windows-1251"),
    JSON("UTF-8"),
    XML("UTF-8"),
    HTML("UTF-8");

    private final Charset charset;

    ApiTransport(String charset) {
        this.charset = Charset.forName(charset);
    }

    public Charset getCharset() {
        return charset;
    }

}
