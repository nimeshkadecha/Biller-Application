package com.nimeshkadecha.myapplication;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class date_convertor {

    public static String convertDateFormat(String inputDate, String inputFormat, String outputFormat) {
        SimpleDateFormat inputDateFormat = new SimpleDateFormat(inputFormat, Locale.getDefault());
        SimpleDateFormat outputDateFormat = new SimpleDateFormat(outputFormat, Locale.getDefault());

        try {
            Date date = inputDateFormat.parse(inputDate);
            String convertedDate = outputDateFormat.format(date);
            return convertedDate;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}