package com.saucedemo.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Price {

    private static final Pattern AMOUNT = Pattern.compile("\\$(\\d+(\\.\\d+)?)");

    private Price() {
    }

    // 49.99 -> "$49.99"
    public static String format(BigDecimal amount) {
        return "$" + amount.setScale(2, RoundingMode.HALF_UP);
    }

    // "Tax: $4.64" -> 4.64
    public static BigDecimal parse(String text) {
        Matcher matcher = AMOUNT.matcher(text);
        if (!matcher.find()) {
            throw new IllegalArgumentException("No price found in \"" + text + "\"");
        }
        return new BigDecimal(matcher.group(1));
    }
}
