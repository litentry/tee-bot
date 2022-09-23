package com.litentry.litbot.TEEBot.utils;

import com.litentry.litbot.TEEBot.config.Constants;

import java.awt.*;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

public class MiscUtils {
    public static Color hex2Rgb(String colorStr) {
        return new Color(
            Integer.valueOf(colorStr.substring(1, 3), 16),
            Integer.valueOf(colorStr.substring(3, 5), 16),
            Integer.valueOf(colorStr.substring(5, 7), 16)
        );
    }

    public static String formatTime(long seconds) {
        int day = (int) TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) - (day * 24L);
        //long minute = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds) * 60);
        //long second = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) * 60);

        String dayStr = day > 1 ? " Days " : " Day ";
        String hourStr = hours > 1 ? " Hours " : " Hour ";
        //        String minStr = minute > 1 ? " Minutes " : " Minute ";
        //        String secStr = second > 1 ? " Seconds" : " Second";

        String str = "";
        if (day > 0) str += day + dayStr;
        if (hours > 0) str += hours + hourStr;
        //        if (minute > 0) str += minute + minStr;
        //        if (second > 0) str += second + secStr;

        return str;
    }

    public static String formatTimeSimple(long seconds) {
        int day = (int) TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) - (day * 24L);
        long minute = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds) * 60);
        long second = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) * 60);

        String str = "";

        if (day > 0) str += day + ":";
        if (hours > 0) str += hours + ":";
        if (minute > 0) str += minute + ":";
        if (second > 0) str += second + ":";

        return str;
    }

    public static long getMaxRewardInDecimal(long max) {
        BigDecimal decimal = (new BigDecimal(10)).pow(Constants.CHAIN_LITENTRY_STAGING);
        return (new BigDecimal(max)).multiply(decimal).longValue();
    }

    //convert to LIT decimals
    public static long parseLITNumberInDecimal(String content) {
        long n = 0L;
        String reg = "^(\\-|\\+)?\\d+(\\.\\d+)?$";
        if (content != null && content.length() > 0 && !content.startsWith(".") && content.matches(reg)) {
            try {
                BigDecimal decimal = (new BigDecimal(10)).pow(Constants.CHAIN_LITENTRY_STAGING);
                BigDecimal rewardByDecimals = (new BigDecimal(content)).multiply(decimal);
                n = rewardByDecimals.longValue();
            } catch (Exception e) {
                //log.error("parsed invalid content {}", content);
            }
        }
        return n;
    }
}
