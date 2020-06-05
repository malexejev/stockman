package io.stockman.math;

import io.stockman.domain.Transaction;
import org.javamoney.moneta.FastMoney;
import org.junit.Test;

import javax.money.MonetaryAmount;
import javax.money.NumberSupplier;
import javax.money.NumberValue;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static java.lang.Math.pow;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Temporary test to manually compute YTM of selected bonds
 *
 * Created by maksim.alekseev on 28/06/2018
 */
public class BondYtmTest {

    private static final double BC = 0.03776 / 100; // broker commission
    private static final double EC = 0.01 / 100;    // exchange commission
    private static final double TAX = 0.13;         // profit tax rate

    private static final DateTimeFormatter formatter = ofPattern("dd.MM.uuuu");

    @Test
    public void afkSistemaCloseOfferNow() {
        var transactions = new ArrayList<Transaction>(){{
            // purchase
            add(new Transaction(rub(-1202160.00), dateFrom("24.10.2018")));   // -1200*1001.80
            add(new Transaction(rub(-26424.00), dateFrom("24.10.2018")));
            add(new Transaction(rub(-453.94), dateFrom("24.10.2018"))); // -0.03776%*1200*1001.80
            add(new Transaction(rub(-120.22), dateFrom("24.10.2018")));    // -0.01%*1200*1001.80
//            add(new Transaction(rub(-150.00), dateFrom("24.10.2018")));

            // coupon
            add(new Transaction(rub(58644.00), dateFrom("01.02.2019")));  // 1200*48.87

            // offer
            add(new Transaction(rub(-453.12), dateFrom("01.02.2019")));    // -0.03776%*1200*1000
            add(new Transaction(rub(-120.00), dateFrom("01.02.2019")));   // -0.01%*1200*1000
//            add(new Transaction(rub(-150.00), dateFrom("01.02.2019")));
            add(new Transaction(rub(1200000.00), dateFrom("06.02.2019")));   // 1200*1000
        }};

        // 8.437%
        double ytm = new IRR(0.1, 0.00001, 50).compute(transactions).orElse(-1d);
        System.out.println("YTM to offer: " + ytm * 100 + "%");
    }

    @Test
    public void afkSistemaCloseOfferNext() {
        var transactions = new ArrayList<Transaction>(){{
            // purchase
            add(new Transaction(rub(-1202160.00), dateFrom("24.10.2018")));  // -1200*1001.80
            add(new Transaction(rub(-26424.00), dateFrom("24.10.2018")));
            add(new Transaction(rub(-453.94), dateFrom("24.10.2018")));  // -0.03776%*1200*1001.80
            add(new Transaction(rub(-120.22), dateFrom("24.10.2018")));  // -0.01%*1200*1001.80
//            add(new Transaction(rub(-150.00), dateFrom("24.10.2018")));

            // coupons
            add(new Transaction(rub(58644.00), dateFrom("01.02.2019")));  // 1200*48.87
            add(new Transaction(rub(59832.00), dateFrom("02.08.2019")));  // 1200*49.86
            add(new Transaction(rub(59832.00), dateFrom("31.01.2020")));  // 1200*49.86
            add(new Transaction(rub(59832.00), dateFrom("31.07.2020")));  // 1200*49.86
            add(new Transaction(rub(59832.00), dateFrom("29.01.2021")));  // 1200*49.86

            // offer
            add(new Transaction(rub(-453.12), dateFrom("01.02.2021")));  // -0.03776%*1200*1000
            add(new Transaction(rub(-120.00), dateFrom("01.02.2021")));  // -0.01%*1200*1000
//            add(new Transaction(rub(-150.00), dateFrom("01.02.2021")));
            add(new Transaction(rub(1200000.00), dateFrom("03.02.2021")));  // 1200*1000
        }};

        // 10.011%
        double ytm = new IRR(0.1, 0.00001, 50).compute(transactions).orElse(-1d);
        System.out.println("YTM to offer: " + ytm * 100 + "%");
    }

    @Test
    public void buyRU000A0ZYLD2() {
        // СофтЛайн Трейд-001Р-01
        double nominal = 1000;
        double coupon = 54.85;
        double price = 97.00 / 100d;
        double nkd = 31.95;
        int quantity = 1000;

        var transactions = new ArrayList<Transaction>(){{
            // purchase
            add(new Transaction(rub(-quantity * nominal * price), dateFrom("05.04.2019")));
            add(new Transaction(rub(-quantity * nkd), dateFrom("05.04.2019")));
            add(new Transaction(rub(-quantity * nominal * price * BC), dateFrom("05.04.2019")));
            add(new Transaction(rub(-quantity * nominal * price * EC), dateFrom("05.04.2019")));
//            add(new Transaction(rub(-depositary), dateFrom("05.04.2019")));

            // coupons
            add(new Transaction(rub(quantity * coupon), dateFrom("20.06.2019")));
            add(new Transaction(rub(quantity * coupon), dateFrom("19.12.2019")));
            add(new Transaction(rub(quantity * coupon), dateFrom("18.06.2020")));
            add(new Transaction(rub(quantity * nominal * 0.5), dateFrom("18.06.2020"))); // amortisation 50%
            add(new Transaction(rub(quantity * 27.42), dateFrom("17.12.2020")));

            // maturity 50%
            add(new Transaction(rub(quantity * nominal * 0.5), dateFrom("17.12.2020")));

            if (price < 1d) {
                // price discount tax
                add(new Transaction(rub(-quantity * nominal * (1 - price) * TAX), dateFrom("01.01.2021")));
            }
        }};

        // 13.4477%
        double ytm = new IRR(0.1, 0.00001, 50).compute(transactions).orElse(-1d);
        System.out.println("YTM to maturity: " + ytm * 100 + "%");
    }

    @Test
    public void buyRU000A0ZZZU3() {
        // СофтЛайн Трейд-001Р-02

        double nominal = 1000;
        double coupon = 54.85;
        double price = 96.24 / 100d;
        double nkd = 30.14;
        int quantity = 500;

        var transactions = new ArrayList<Transaction>(){{
            // purchase
            add(new Transaction(rub(-quantity * nominal * price), dateFrom("05.04.2019")));
            add(new Transaction(rub(-quantity * nkd), dateFrom("05.04.2019")));
            add(new Transaction(rub(-quantity * nominal * price * BC), dateFrom("05.04.2019")));
            add(new Transaction(rub(-quantity * nominal * price * EC), dateFrom("05.04.2019")));
//            add(new Transaction(rub(-depositary), dateFrom("05.04.2019")));

            // coupons
            add(new Transaction(rub(quantity * coupon), dateFrom("26.06.2019")));
            add(new Transaction(rub(quantity * coupon), dateFrom("25.12.2019")));
            add(new Transaction(rub(quantity * coupon), dateFrom("24.06.2020")));
            add(new Transaction(rub(quantity * coupon), dateFrom("23.12.2020")));
            add(new Transaction(rub(quantity * coupon), dateFrom("23.06.2021")));
            add(new Transaction(rub(quantity * coupon), dateFrom("22.12.2021")));

            add(new Transaction(rub(quantity * nominal), dateFrom("22.12.2021")));

            if (price < 1d) {
                // price discount tax
                add(new Transaction(rub(-quantity * nominal * (1 - price) * TAX), dateFrom("01.01.2022")));
            }
        }};

        // 12.86919%
        double ytm = new IRR(0.1, 0.00001, 50).compute(transactions).orElse(-1d);
        System.out.println("YTM to maturity: " + ytm * 100 + "%");
    }

    @Test
    public void buyVTB_2DaysBond() {
        // ВТБКС3-217 (RU000A100FT3.PSAU)

        double nominal = 1000;
        int lot = 10;
        double coupon = 0;
        double price = 99.961 / 100d;
        double nkd = 0;
        int quantity = 39 * lot;

        var transactions = new ArrayList<Transaction>(){{
            // purchase
            add(new Transaction(rub(-quantity * nominal * price), dateFrom("11.06.2019")));
            add(new Transaction(rub(-quantity * nkd), dateFrom("11.06.2019")));
            add(new Transaction(rub(-0.16), dateFrom("11.06.2019")));
            add(new Transaction(rub(-13.33), dateFrom("11.06.2019")));
//            add(new Transaction(rub(-quantity * nominal * price * BC), dateFrom("11.06.2019")));
//            add(new Transaction(rub(-quantity * nominal * price * EC), dateFrom("11.06.2019")));
//            add(new Transaction(rub(-depositary), dateFrom("05.04.2019")));

            // payout
            add(new Transaction(rub(quantity * nominal), dateFrom("13.06.2019")));

            if (price < 1d) {
                // price discount tax
                add(new Transaction(rub(-18.02), dateFrom("01.01.2020")));
//                add(new Transaction(rub(-quantity * nominal * (1 - price) * TAX), dateFrom("01.01.2020")));
            }
        }};

        // 12.86919%
        double ytm = new IRR(0.1, 0.00001, 50).compute(transactions).orElse(-1d);
        System.out.println("YTM to maturity: " + ytm * 100 + "%");
    }

    @Test
    public void buyRU000A100ER0() {
        // РОСНАНО-БО-002Р-01

        double nominal = 1000;
        double coupon = 45.38;
        double price = 100.64 / 100d;
        double nkd = 3.5;
        int quantity = 400;

        var transactions = new ArrayList<Transaction>(){{
            // purchase
            add(new Transaction(rub(-quantity * nominal * price), dateFrom("18.06.2019")));
            add(new Transaction(rub(-quantity * nkd), dateFrom("18.06.2019")));
            add(new Transaction(rub(-quantity * nominal * price * BC), dateFrom("18.06.2019")));
            add(new Transaction(rub(-quantity * nominal * price * EC), dateFrom("18.06.2019")));
//            add(new Transaction(rub(-depositary), dateFrom("05.04.2019")));

            // coupons
            add(new Transaction(rub(quantity * coupon), dateFrom("04.12.2019")));
            add(new Transaction(rub(quantity * coupon), dateFrom("03.06.2020")));
            add(new Transaction(rub(quantity * coupon), dateFrom("02.12.2020")));
            add(new Transaction(rub(quantity * coupon), dateFrom("02.06.2021")));
            add(new Transaction(rub(quantity * coupon), dateFrom("01.12.2021")));

            add(new Transaction(rub(quantity * nominal), dateFrom("01.12.2021")));

            if (price < 1d) {
                // price discount tax
                add(new Transaction(rub(-quantity * nominal * (1 - price) * TAX), dateFrom("01.01.2022")));
            }
        }};

        // 8.9328%
        double ytm = new IRR(0.1, 0.00001, 50).compute(transactions).orElse(-1d);
        System.out.println("YTM to maturity: " + ytm * 100 + "%");
    }

    @Test
    public void resellVTBAfterInitialOffering() {
        // Банк ВТБ-Б-1-27

        double nominal = 1000;
        double coupon = 39.89;
        double buyPrice = 100.00 / 100d;
        double sellPrice = 100.20 / 100d;
        int quantity = 1000;

        var transactions = new ArrayList<Transaction>(){{
            // purchase
            add(new Transaction(rub(-quantity * nominal * buyPrice), dateFrom("12.04.2019")));
//            add(new Transaction(rub(-quantity * nkd), dateFrom("12.04.2019"))); FIXME MA
//            add(new Transaction(rub(-quantity * nominal * price * BC), dateFrom("18.06.2019"))); FIXME MA
//            add(new Transaction(rub(-quantity * nominal * price * EC), dateFrom("18.06.2019"))); FIXME MA

            // sell after few days on public market
            add(new Transaction(rub(quantity * nominal * sellPrice), dateFrom("30.04.2019")));
            add(new Transaction(rub(quantity * ((31 + 6) / 182d * coupon)), dateFrom("30.04.2019")));
            add(new Transaction(rub(-quantity * nominal * sellPrice * BC), dateFrom("30.04.2019")));
            add(new Transaction(rub(-quantity * nominal * sellPrice * EC), dateFrom("30.04.2019")));

            // nkd tax (corporate bond issued after 2017)
            add(new Transaction(rub(-quantity * ((31 + 6) / 182d * coupon) * TAX), dateFrom("01.01.2020")));

            // price discount tax
            if (sellPrice > buyPrice) {
                add(new Transaction(rub(-quantity * nominal * (sellPrice - buyPrice) * TAX), dateFrom("01.01.2020")));
            }
        }};

        // FIXME MA: looks this one is wrong, NKD is likely payed when buying on initial offering
        // TODO MA: check what happens with price after offering is finished (coupon was very low)

        // 8.9328%
        double ytm = new IRR(0.1, 0.00001, 50).compute(transactions).orElse(-1d);
        System.out.println("YTM to sell: " + ytm * 100 + "%");
    }

    @Test
    public void buyRU000A1005Y9() {
        // Рольф-001Р-01

        double nominal = 1000;
        double coupon = 26.05;
        double price = 101.26 / 100d;
        double nkd = 19.75;
        int quantity = 263;

        var transactions = new ArrayList<Transaction>(){{
            // purchase
            add(new Transaction(rub(-quantity * nominal * price), dateFrom("18.11.2019")));
            add(new Transaction(rub(-quantity * nkd), dateFrom("18.11.2019")));
            add(new Transaction(rub(-quantity * nominal * price * BC), dateFrom("18.11.2019")));
            add(new Transaction(rub(-quantity * nominal * price * EC), dateFrom("18.11.2019")));

            // coupons
//            add(new Transaction(rub(quantity * coupon), dateFrom("10.09.2019")));
            add(new Transaction(rub(quantity * coupon), dateFrom("10.12.2019")));
            add(new Transaction(rub(quantity * coupon), dateFrom("10.03.2020")));
            add(new Transaction(rub(quantity * coupon), dateFrom("09.06.2020")));
            add(new Transaction(rub(quantity * coupon), dateFrom("08.09.2020")));
            add(new Transaction(rub(quantity * coupon), dateFrom("08.12.2020")));
            add(new Transaction(rub(quantity * coupon), dateFrom("09.03.2021")));
            add(new Transaction(rub(quantity * coupon), dateFrom("08.06.2021")));
            add(new Transaction(rub(quantity * coupon), dateFrom("07.09.2021")));
            add(new Transaction(rub(quantity * coupon), dateFrom("07.12.2021")));
            add(new Transaction(rub(quantity * coupon), dateFrom("08.03.2022")));

            // maturity
            add(new Transaction(rub(quantity * nominal), dateFrom("08.03.2022")));

            if (price < 1d) {
                // price discount tax
                add(new Transaction(rub(-quantity * nominal * (1 - price) * TAX), dateFrom("01.01.2023")));
            }
        }};

        // 12.1789% (360) + 11.9682% (140) + ... + 10.1714% (263)
        double ytm = new IRR(0.1, 0.00001, 50).compute(transactions).orElse(-1d);
        System.out.println("YTM to maturity: " + ytm * 100 + "%");

        // (1000*100.65%*(1+0.03776%)*(1+0.01%)+3.5)*400
    }

    @Test
    public void buyRU000A0ZZYL5() {
        // Калсб 1P01

        double nominal = 1000;
        double coupon = 64.82;
        double taxFactor1 = (12.0 + 1.0 * (1 - 0.35)) / 13.0; // 7.0% + 5.0% overflow
        double taxFactor2 = 0.87;
        double price = 99.48 / 100d;
        double nkd = 53.78;
        int quantity = 100;

        var transactions = new ArrayList<Transaction>(){{
            // purchase
            add(new Transaction(rub(-quantity * nominal * price), dateFrom("15.05.2020")));
            add(new Transaction(rub(-quantity * nkd), dateFrom("15.05.2020")));
            add(new Transaction(rub(-quantity * nominal * price * BC), dateFrom("15.05.2020")));
            add(new Transaction(rub(-quantity * nominal * price * EC), dateFrom("15.05.2020")));

            // coupons
            add(new Transaction(rub(quantity * coupon * taxFactor1), dateFrom("15.06.2020")));
            add(new Transaction(rub(quantity * coupon * taxFactor1), dateFrom("14.12.2020")));
            add(new Transaction(rub(quantity * coupon * taxFactor2), dateFrom("14.06.2021")));
            add(new Transaction(rub(quantity * coupon * taxFactor2), dateFrom("13.12.2021")));

            // offer
            add(new Transaction(rub(quantity * nominal), dateFrom("20.12.2021")));

            if (price < 1d) {
                // price discount tax
                add(new Transaction(rub(-quantity * nominal * (1 - price) * TAX), dateFrom("01.01.2022")));
            }
        }};

        // 12.7642%
        double ytm = new IRR(0.1, 0.00001, 50).compute(transactions).orElse(-1d);
        System.out.println("YTM to maturity: " + ytm * 100 + "%");
    }

    @Test
    public void buyRU000A1009R5() {
        // ФЭС-АгроБ1

        double nominal = 1000;
        double price = 101.95 / 100d;
        double taxFactor = (12.0 + 2.0 * (1 - 0.35)) / 14.0; // 7.0% + 5.0% overflow
        double nkd = 12.66;
        int quantity = 102;

        var transactions = new ArrayList<Transaction>(){{
            // purchase
            add(new Transaction(rub(-quantity * nominal * price), dateFrom("19.11.2019")));
            add(new Transaction(rub(-quantity * nkd), dateFrom("19.11.2019")));
            add(new Transaction(rub(-quantity * nominal * price * BC), dateFrom("19.11.2019")));
            add(new Transaction(rub(-quantity * nominal * price * EC), dateFrom("19.11.2019")));

            // coupons
            add(new Transaction(rub(quantity * 34.90 * taxFactor), dateFrom("16.01.2020")));
            add(new Transaction(rub(quantity * 34.90 * taxFactor), dateFrom("16.04.2020")));
            add(new Transaction(rub(quantity * 34.90 * taxFactor), dateFrom("16.07.2020")));
            add(new Transaction(rub(quantity * 26.18 * taxFactor), dateFrom("15.10.2020")));
            add(new Transaction(rub(quantity * 17.45 * taxFactor), dateFrom("14.01.2021")));
            add(new Transaction(rub(quantity * 8.73 * taxFactor), dateFrom("15.04.2021")));

            // amortisation
            add(new Transaction(rub(quantity * nominal / 4), dateFrom("16.07.2020")));
            add(new Transaction(rub(quantity * nominal / 4), dateFrom("15.10.2020")));
            add(new Transaction(rub(quantity * nominal / 4), dateFrom("14.01.2021")));
            add(new Transaction(rub(quantity * nominal / 4), dateFrom("15.04.2021")));

            if (price < 1d) {
                // price discount tax
                add(new Transaction(rub(-quantity * nominal * (1 - price) * TAX), dateFrom("01.01.2022")));
            }
        }};

        // 11.6101%
        double ytm = new IRR(0.1, 0.00001, 50).compute(transactions).orElse(-1d);
        System.out.println("YTM to maturity: " + ytm * 100 + "%");
    }

    @Test
    public void buyRU000A1008B1() {
        // Тинькофф Банк-001P-02R

        double nominal = 1000;
        double coupon = 46.12;
        double price = 101.68 / 100d;
        double nkd = 26.36;
        int quantity = 100;

        var transactions = new ArrayList<Transaction>(){{
            // purchase
            add(new Transaction(rub(-quantity * nominal * price), dateFrom("17.07.2019")));
            add(new Transaction(rub(-quantity * nkd), dateFrom("17.07.2019")));
            add(new Transaction(rub(-quantity * nominal * price * BC), dateFrom("17.07.2019")));
            add(new Transaction(rub(-quantity * nominal * price * EC), dateFrom("17.07.2019")));

            // coupons
            add(new Transaction(rub(quantity * coupon), dateFrom("02.10.2019")));
            add(new Transaction(rub(quantity * coupon), dateFrom("01.04.2020")));
            add(new Transaction(rub(quantity * coupon), dateFrom("30.09.2020")));
            add(new Transaction(rub(quantity * coupon), dateFrom("31.03.2021")));
            add(new Transaction(rub(quantity * coupon), dateFrom("29.09.2021")));
            add(new Transaction(rub(quantity * coupon), dateFrom("30.03.2022")));

            // offer
            add(new Transaction(rub(quantity * nominal), dateFrom("30.03.2022")));
        }};

        // 8.7028%
        double ytm = new IRR(0.1, 0.00001, 50).compute(transactions).orElse(-1d);
        System.out.println("YTM to maturity: " + ytm * 100 + "%");
    }

    @Test
    public void buyRU000A0JV8G3() {
        // ВЭБ-лизинг-5-боб

        double nominal = 1000;
        double coupon = 40.34; // coupon is variable for this bond
        double price = 98.07 / 100d;
        double nkd = 34.13;
        int quantity = 100;

        var transactions = new ArrayList<Transaction>(){{
            // purchase
            add(new Transaction(rub(-quantity * nominal * price), dateFrom("17.07.2019")));
            add(new Transaction(rub(-quantity * nkd), dateFrom("17.07.2019")));
            add(new Transaction(rub(-quantity * nominal * price * BC), dateFrom("17.07.2019")));
            add(new Transaction(rub(-quantity * nominal * price * EC), dateFrom("17.07.2019")));

            // coupons
            add(new Transaction(rub(quantity * coupon * (1 - TAX)), dateFrom("13.08.2019")));
            add(new Transaction(rub(quantity * coupon * (1 - TAX)), dateFrom("11.02.2020")));

            // offer
            add(new Transaction(rub(quantity * nominal), dateFrom("11.02.2020")));

            if (price < 1d) {
                // price discount tax
                add(new Transaction(rub(-quantity * nominal * (1 - price) * TAX), dateFrom("01.01.2021")));
            }
        }};

        // 9.5087% (8.4% for RU000A0JV8D0 at 98.72 price)
        double ytm = new IRR(0.1, 0.00001, 50).compute(transactions).orElse(-1d);
        System.out.println("YTM to maturity: " + ytm * 100 + "%");
    }

    @Test
    public void buyRU000A0ZYQY7() {
        // СистемаАФК-1Р-07-боб

        double nominal = 1000;
        double coupon = 49.86;
        double price = 101.21 / 100d;
        double nkd = 0.82;
        int quantity = 49;

        var transactions = new ArrayList<Transaction>(){{
            // purchase
            add(new Transaction(rub(-quantity * nominal * price), dateFrom("05.08.2019")));
            add(new Transaction(rub(-quantity * nkd), dateFrom("05.08.2019")));
            add(new Transaction(rub(-quantity * nominal * price * BC), dateFrom("05.08.2019")));
            add(new Transaction(rub(-quantity * nominal * price * EC), dateFrom("05.08.2019")));

            // coupons
            add(new Transaction(rub(quantity * coupon), dateFrom("31.01.2020")));
            add(new Transaction(rub(quantity * coupon), dateFrom("31.07.2020")));
            add(new Transaction(rub(quantity * coupon), dateFrom("29.01.2021")));

            // maturity
            add(new Transaction(rub(quantity * nominal), dateFrom("29.01.2021")));

            if (price < 1d) {
                // price discount tax
                add(new Transaction(rub(-quantity * nominal * (1 - price) * TAX), dateFrom("01.01.2022")));
            }
        }};

        // 9.2451%, 9.2815%
        double ytm = new IRR(0.1, 0.00001, 50).compute(transactions).orElse(-1d);
        System.out.println("YTM to maturity: " + ytm * 100 + "%");
    }

    @Test
    public void buyRU000A0ZZXT0() {
        // ВСЕИНСТРУМЕНТЫ.РУ-БО-01

        double nominal = 1000;
        double coupon = 28.67;
        double price = 102.18 / 100d;
        double taxFactor = (11.25 + 0.25 * (1 - 0.35)) / 11.5; // 6.25% + 5.0% overflow
        double nkd = 12.60;
        int quantity = 200;

        var transactions = new ArrayList<Transaction>(){{
            // purchase
            add(new Transaction(rub(-quantity * nominal * price), dateFrom("22.01.2020")));
            add(new Transaction(rub(-quantity * nkd), dateFrom("22.01.2020")));
            add(new Transaction(rub(-quantity * nominal * price * BC), dateFrom("22.01.2020")));
            add(new Transaction(rub(-quantity * nominal * price * EC), dateFrom("22.01.2020")));

            // coupons
            add(new Transaction(rub(quantity * coupon * taxFactor), dateFrom("12.03.2020")));
            add(new Transaction(rub(quantity * coupon * taxFactor), dateFrom("11.06.2020")));
            add(new Transaction(rub(quantity * coupon * taxFactor), dateFrom("10.09.2020")));
            add(new Transaction(rub(quantity * coupon * taxFactor), dateFrom("10.12.2020")));

            // offer
            add(new Transaction(rub(quantity * nominal), dateFrom("10.12.2020")));

            if (price < 1d) {
                // price discount tax
                add(new Transaction(rub(-quantity * nominal * (1 - price) * TAX), dateFrom("01.01.2021")));
            }
        }};

        // 9.0776%
        double ytm = new IRR(0.1, 0.00001, 50).compute(transactions).orElse(-1d);
        System.out.println("YTM to maturity: " + ytm * 100 + "%");
    }

    @Test
    public void buyRU000A101970() {
        // ВСЕИНСТРУМЕНТЫ.РУ-БО-02

        double nominal = 1000;
        double coupon = 28.67;
        double price = 101.77 / 100d;
        double taxFactor = (11.25 + 0.25 * (1 - 0.35)) / 11.5; // 6.25% + 5.0% overflow
        double nkd = 7.56;
        int quantity = 200;

        var transactions = new ArrayList<Transaction>(){{
            // purchase
            add(new Transaction(rub(-quantity * nominal * price), dateFrom("22.01.2020")));
            add(new Transaction(rub(-quantity * nkd), dateFrom("22.01.2020")));
            add(new Transaction(rub(-quantity * nominal * price * BC), dateFrom("22.01.2020")));
            add(new Transaction(rub(-quantity * nominal * price * EC), dateFrom("22.01.2020")));

            // coupons
            add(new Transaction(rub(quantity * coupon * taxFactor), dateFrom("27.03.2020")));
            add(new Transaction(rub(quantity * coupon * taxFactor), dateFrom("26.06.2020")));
            add(new Transaction(rub(quantity * coupon * taxFactor), dateFrom("25.09.2020")));
            add(new Transaction(rub(quantity * coupon * taxFactor), dateFrom("25.12.2020")));
            add(new Transaction(rub(quantity * coupon * taxFactor), dateFrom("26.03.2021")));
            add(new Transaction(rub(quantity * coupon * taxFactor), dateFrom("25.06.2021")));
            add(new Transaction(rub(quantity * coupon * taxFactor), dateFrom("24.09.2021")));
            add(new Transaction(rub(quantity * coupon * taxFactor), dateFrom("24.12.2021")));

            // offer
            add(new Transaction(rub(quantity * nominal), dateFrom("24.12.2021")));

            if (price < 1d) {
                // price discount tax
                add(new Transaction(rub(-quantity * nominal * (1 - price) * TAX), dateFrom("01.01.2022")));
            }
        }};

        // 10.7973%
        double ytm = new IRR(0.1, 0.00001, 50).compute(transactions).orElse(-1d);
        System.out.println("YTM to maturity: " + ytm * 100 + "%");
    }

    @Test
    public void buyRU000A0JVC59() {
        // БанкФК Открытие-БО-ПО1

        double nominal = 1000;
        double coupon = 32.26;
        double price = 85.13 / 100d;
        double taxFactor = 0.87; //(11.25 + 0.25 * (1 - 0.35)) / 11.5; // 6.25% + 5.0% overflow
        double nkd = 6.14;
        int quantity = 200;

        var transactions = new ArrayList<Transaction>(){{
            // purchase
            add(new Transaction(rub(-quantity * nominal * price), dateFrom("12.05.2020")));
            add(new Transaction(rub(-quantity * nkd), dateFrom("12.05.2020")));
            add(new Transaction(rub(-quantity * nominal * price * BC), dateFrom("12.05.2020")));
            add(new Transaction(rub(-quantity * nominal * price * EC), dateFrom("12.05.2020")));

            // coupons
            add(new Transaction(rub(quantity * coupon * taxFactor), dateFrom("08.10.2020")));
            add(new Transaction(rub(quantity * coupon * taxFactor), dateFrom("10.04.2021")));
            add(new Transaction(rub(quantity * coupon * taxFactor), dateFrom("11.10.2021")));
            add(new Transaction(rub(quantity * coupon * taxFactor), dateFrom("13.04.2022")));
            add(new Transaction(rub(quantity * coupon * taxFactor), dateFrom("14.10.2022")));
            add(new Transaction(rub(quantity * coupon * taxFactor), dateFrom("16.04.2023")));

            // maturity
            add(new Transaction(rub(quantity * nominal), dateFrom("16.04.2023")));

            if (price < 1d) {
                // price discount tax
                add(new Transaction(rub(-quantity * nominal * (1 - price) * TAX), dateFrom("01.01.2024")));
            }
        }};

        // 9.07% no tax, 8.13% with tax
        double ytm = new IRR(0.1, 0.00001, 50).compute(transactions).orElse(-1d);
        System.out.println("YTM to maturity: " + ytm * 100 + "%");
    }

    @Test
    public void buyRU000A0JWWG0() {
        // ПервоеКоллекторБюро-1-боб

        double nominal = 600;
        double price = 101.21 / 100d;
        double taxFactor = 0.87;
        double nkd = 6.16;
        int quantity = 200;

        var transactions = new ArrayList<Transaction>(){{
            // purchase
            add(new Transaction(rub(-quantity * nominal * price), dateFrom("13.05.2020")));
            add(new Transaction(rub(-quantity * nkd), dateFrom("13.05.2020")));
            add(new Transaction(rub(-quantity * nominal * price * BC), dateFrom("13.05.2020")));
            add(new Transaction(rub(-quantity * nominal * price * EC), dateFrom("13.05.2020")));

            // coupons and amortisations
            add(new Transaction(rub(quantity * 22.44 * taxFactor), dateFrom("17.07.2020")));
            add(new Transaction(rub(quantity * 100), dateFrom("17.07.2020")));

            add(new Transaction(rub(quantity * 18.70 * taxFactor), dateFrom("16.10.2020")));
            add(new Transaction(rub(quantity * 100), dateFrom("16.10.2020")));

            add(new Transaction(rub(quantity * 14.96 * taxFactor), dateFrom("15.01.2021")));
            add(new Transaction(rub(quantity * 100), dateFrom("15.01.2021")));

            add(new Transaction(rub(quantity * 11.22 * taxFactor), dateFrom("16.04.2021")));
            add(new Transaction(rub(quantity * 100), dateFrom("16.04.2021")));

            add(new Transaction(rub(quantity * 7.48 * taxFactor), dateFrom("16.07.2021")));
            add(new Transaction(rub(quantity * 100), dateFrom("16.07.2021")));

            add(new Transaction(rub(quantity * 3.74 * taxFactor), dateFrom("15.10.2021")));
            add(new Transaction(rub(quantity * 100), dateFrom("15.10.2021")));

//            if (price < 1d) {
//                // price discount tax
//                add(new Transaction(rub(-quantity * nominal * (1 - price) * TAX), dateFrom("01.01.2024")));
//            }
        }};

        // 12.28% with tax
        double ytm = new IRR(0.1, 0.00001, 50).compute(transactions).orElse(-1d);
        System.out.println("YTM to maturity: " + ytm * 100 + "%");
    }

    @Test
    public void nkdAnalysis_BuyBeforeCoupon() {
        // СистемаАФК-1Р-07-боб

        double budget = 1_000_000d;
        double nominal = 1000;
        double coupon = 49.86;
        double price = 101.32 / 100d;
        double nkd = 49.59;

        // (1000 * 101.32% * (1 + 0.03776%) * (1 + 0.01%) + 45.21) * Q <= 1000000
        // FIXME MA: broker and exchange commissions must be rounded separately (find examples on smartlab)
        int quantity = (int) Math.round(Math.floor(budget / (nkd + nominal * price * (1 + BC + EC))));

        var transactions = new ArrayList<Transaction>(){{
            // purchase
            add(new Transaction(rub(-quantity * nominal * price), dateFrom("01.08.2019")));
            add(new Transaction(rub(-quantity * nkd), dateFrom("01.08.2019")));
            add(new Transaction(rub(-quantity * nominal * price * BC), dateFrom("01.08.2019")));
            add(new Transaction(rub(-quantity * nominal * price * EC), dateFrom("01.08.2019")));

            // coupons
            add(new Transaction(rub(quantity * coupon), dateFrom("02.08.2019")));
            add(new Transaction(rub(quantity * coupon), dateFrom("31.01.2020")));
            add(new Transaction(rub(quantity * coupon), dateFrom("31.07.2020")));
            add(new Transaction(rub(quantity * coupon), dateFrom("29.01.2021")));

            // maturity
            add(new Transaction(rub(quantity * nominal), dateFrom("29.01.2021")));

            if (price < 1d) {
                // price discount tax
                add(new Transaction(rub(-quantity * nominal * (1 - price) * TAX), dateFrom("01.01.2022")));
            }
        }};

        double ytm = new IRR(0.1, 0.00001, 50).compute(transactions).orElse(-1d);
        double profits = transactions.stream()
                .map(Transaction::getAmount)
                .map(MonetaryAmount::getNumber)
                .map(NumberValue::doubleValue)
                .collect(Collectors.summingDouble(Double::doubleValue));
        double presentProfits = transactions.stream()
                .map(tx -> new SimpleEntry<>(tx.getAmount().getNumber().doubleValueExact(),
                        DAYS.between(dateFrom("01.08.2019"), tx.getDate())))
                .filter(e -> e.getKey() > 0d)
                .map(e -> e.getKey() / pow(1d + ytm, e.getValue() / 365d))
                .collect(Collectors.summingDouble(Double::doubleValue));

        System.out.println("YTM to maturity: " + ytm * 100 + "%");
        System.out.println("Bonds bought: " + quantity);
        System.out.println("Net profits: " + profits + " RUB");
        System.out.println("Net Present profits: " + presentProfits + " RUB");
    }

    @Test
    public void nkdAnalysis_BuyAfterCoupon() {
        // СистемаАФК-1Р-07-боб

        double budget = 1_000_000d;
        double nominal = 1000;
        double coupon = 49.86;
        double price = 101.32 / 100d;
        double nkd = 0;

        // (1000 * 101.32% * (1 + 0.03776%) * (1 + 0.01%) + 45.21) * Q <= 1000000
        // FIXME MA: broker and exchange commissions must be rounded separately (find examples on smartlab)
        int quantity = (int) Math.round(Math.floor(budget / (nkd + nominal * price * (1 + BC + EC))));

        var transactions = new ArrayList<Transaction>(){{
            // purchase
            add(new Transaction(rub(-quantity * nominal * price), dateFrom("02.08.2019")));
            add(new Transaction(rub(-quantity * nkd), dateFrom("02.08.2019")));
            add(new Transaction(rub(-quantity * nominal * price * BC), dateFrom("02.08.2019")));
            add(new Transaction(rub(-quantity * nominal * price * EC), dateFrom("02.08.2019")));

            // coupons
            add(new Transaction(rub(quantity * coupon), dateFrom("31.01.2020")));
            add(new Transaction(rub(quantity * coupon), dateFrom("31.07.2020")));
            add(new Transaction(rub(quantity * coupon), dateFrom("29.01.2021")));

            // maturity
            add(new Transaction(rub(quantity * nominal), dateFrom("29.01.2021")));

            if (price < 1d) {
                // price discount tax
                add(new Transaction(rub(-quantity * nominal * (1 - price) * TAX), dateFrom("01.01.2022")));
            }
        }};

        double ytm = new IRR(0.1, 0.00001, 50).compute(transactions).orElse(-1d);
        double profits = transactions.stream()
                .map(Transaction::getAmount)
                .map(MonetaryAmount::getNumber)
                .map(NumberValue::doubleValue)
                .collect(Collectors.summingDouble(Double::doubleValue));
        double presentProfits = transactions.stream()
                .map(tx -> new SimpleEntry<>(tx.getAmount().getNumber().doubleValueExact(),
                        DAYS.between(dateFrom("01.08.2019"), tx.getDate())))
                .filter(e -> e.getKey() > 0d)
                .map(e -> e.getKey() / pow(1d + ytm, e.getValue() / 365d))
                .collect(Collectors.summingDouble(Double::doubleValue));

        System.out.println("YTM to maturity: " + ytm * 100 + "%");
        System.out.println("Bonds bought: " + quantity);
        System.out.println("Net profits: " + profits + " RUB");
        System.out.println("Net Present profits: " + presentProfits + " RUB");
    }

    protected FastMoney rub(double preciseAmount) {
        BigDecimal roundedAmount = new BigDecimal(preciseAmount).setScale(2, RoundingMode.HALF_UP);
        FastMoney rub = FastMoney.of(roundedAmount.doubleValue(), "RUB");
        return rub;
    }

    protected LocalDate dateFrom(String dateString) {
        LocalDate date = LocalDate.parse(dateString, formatter);
        return date;
    }

}
