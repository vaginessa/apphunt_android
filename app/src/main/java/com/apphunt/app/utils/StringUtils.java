package com.apphunt.app.utils;

import android.text.Html;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nmp on 15-5-28.
 */
public class StringUtils {
    public static String decodeString(String str) {
        try {
            return URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String htmlDecodeString(String str) {
        return Html.fromHtml(str).toString();
    }

    public static String getDateStringFromCalendar(int minusDays) {
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.ENGLISH);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -minusDays);

        return dayFormat.format(calendar.getTime());
    }

    public static String getMonthStringFromCalendar(int month) {
        SimpleDateFormat dayFormat = new SimpleDateFormat("MMMM", Locale.ENGLISH);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, month);

        return dayFormat.format(calendar.getTime());
    }

    public static String getYearStringFromCalendar(int year) {
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy", Locale.ENGLISH);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);

        return dayFormat.format(calendar.getTime());
    }

    public static boolean isEmailValid(String email) {
        String regExpn =
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                        +"((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        +"([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                        +"([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if(matcher.matches())
            return true;
        else
            return false;
    }

    public static String getDateAsTitleString(String dateStr) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();

        if(formatter.format(today).equals(dateStr)) {
            return "Today";
        }

        calendar.add(Calendar.DATE, -1);
        today = calendar.getTime();
        if(formatter.format(today).equals(dateStr)) {
            return "Yesterday";
        }

        return dateStr;
    }
}
