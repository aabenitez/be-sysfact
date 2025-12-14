package com.marithe.sysfact.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    public static String defaultFormat(String date) {
        return DateUtils.format("dd-MM-yyyy", "yyyy-MM-dd", date);
    }

    public static String format(String fromFormat, String toFormat, String date) {
        try {
            SimpleDateFormat fromFormatter = new SimpleDateFormat(fromFormat);
            SimpleDateFormat toFormatter = new SimpleDateFormat(toFormat);

            Date parsedFromDate = fromFormatter.parse(date);
            return toFormatter.format(parsedFromDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
