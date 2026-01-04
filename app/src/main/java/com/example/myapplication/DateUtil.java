package com.example.myapplication;

import java.time.LocalDate;

public class DateUtil {
    public static String todayIso() {
        return LocalDate.now().toString(); // yyyy-MM-dd
    }
    public static String plusDaysIso(int days) {
        return LocalDate.now().plusDays(days).toString();
    }
}
