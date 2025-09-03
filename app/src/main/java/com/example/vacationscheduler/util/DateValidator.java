package com.example.vacationscheduler.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class DateValidator {
    private static final String PATTERN = "MM/dd/yyyy";

    private DateValidator() {}

    public static boolean isValidDate(String date) {
        if (date == null || date.trim().isEmpty()) return false;
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN, Locale.US);
        sdf.setLenient(false);
        try {
            sdf.parse(date.trim());
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static boolean isEndDateAfterStartDate(String startDate, String endDate) {
        if (!isValidDate(startDate) || !isValidDate(endDate)) return false;
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN, Locale.US);
        sdf.setLenient(false);
        try {
            Date start = sdf.parse(startDate.trim());
            Date end = sdf.parse(endDate.trim());
            return end.after(start);
        } catch (ParseException e) {
            return false;
        }
    }
}
