package com.MaxHighReach;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Config {
    // --------------------------------------------------
    // Window and UI Dimensions
    // --------------------------------------------------
    public static final double WINDOW_WIDTH = 320;
    public static final double WINDOW_HEIGHT = 800;
    public static final double SCISSOR_LIFT_WIDTH = WINDOW_WIDTH;
    public static final double SCISSOR_LIFT_INITIAL_HEIGHT = 182;
    public static final double PADDING = 10;
    public static final double BACK_BUTTON_X = 250;
    public static final double BACK_BUTTON_Y = 10;

    public static final double DELIVERY_DATE_COLUMN_WIDTH = 100;
    public static final double DB_ROW_HEIGHT = 66;
    public static final double DB_ROW_HEIGHT_EMPTY = 20;

    // --------------------------------------------------
    // Database Configuration
    // --------------------------------------------------
    public static final String DB_URL = "jdbc:mysql://maxhighreach.com:3306/dispatch";
    public static final String DB_USR = "dispatch";
    public static final String DB_PSWD = "SQL3225422!a123";
    public static final String DB_NAME = "dispatch";

    // Local database configuration (optional switch)
    /*
    public static final String DB_URL = "jdbc:mysql://localhost:3306/practice_db";
    public static final String DB_USR = "root";
    public static final String DB_PSWD = "SQL3225422!a";
    */

    public static final String SDK_PATH = "C:\\Users\\maxhi\\OneDrive\\Documents\\MaxReachPro\\Intuit Applications\\IDN\\QBSDK16.0\\tools\\SDKTest\\SDKTestPlus3.exe";

    // --------------------------------------------------
    // Employee Data
    // --------------------------------------------------
    public static final String[][] EMPLOYEES = {
            {"Isaiah Sabala", "IS", "I"},
            {"Kaleb Streit", "KS", "K"},
            {"Sandy Mulberry", "SM", "S"},
            {"Adrian Barraza", "AB", "A"},
            {"Jackson Cline", "JC", "JC"},
            {"John Wright", "JW", "J"},
            {"Ken Mulberry", "KM", "KM"},
            {"Byron Chilton", "BC", "B"}
    };

    // --------------------------------------------------
    // Lift Type Maps
    // --------------------------------------------------
    public static final Map<String, Integer> LIFT_TYPE_MAP = new HashMap<>();
    public static final Map<String, String> LIFT_BUTTON_TEXT_MAP = new HashMap<>();

    static {
        // LIFT_TYPE_MAP: Maps standardized lift type text to IDs
        LIFT_TYPE_MAP.put("12' Mast", 1001);
        LIFT_TYPE_MAP.put("19' Slim", 1002);
        LIFT_TYPE_MAP.put("26' Slim", 1003);
        LIFT_TYPE_MAP.put("26'", 1004);
        LIFT_TYPE_MAP.put("32'", 1005);
        LIFT_TYPE_MAP.put("40'", 1006);
        LIFT_TYPE_MAP.put("33' RT", 1007);
        LIFT_TYPE_MAP.put("45' Boom", 1008);

        // LIFT_BUTTON_TEXT_MAP: Maps button text to standardized lift type text
        LIFT_BUTTON_TEXT_MAP.put("12m", "12' Mast");
        LIFT_BUTTON_TEXT_MAP.put("19s", "19' Slim");
        LIFT_BUTTON_TEXT_MAP.put("26s", "26' Slim");
        LIFT_BUTTON_TEXT_MAP.put("26", "26'");
        LIFT_BUTTON_TEXT_MAP.put("32", "32'");
        LIFT_BUTTON_TEXT_MAP.put("40", "40'");
        LIFT_BUTTON_TEXT_MAP.put("33rt", "33' RT");
        LIFT_BUTTON_TEXT_MAP.put("45b", "45' Boom");
    }

    public static final String[] ASCENDING_LIFT_TYPES = {"12m", "19s", "26s", "26", "32", "40", "33rt", "45b"};

    // --------------------------------------------------
    // Company Holidays
    // --------------------------------------------------
    public static final Set<LocalDate> COMPANY_HOLIDAYS = new HashSet<>();

    static {
        // Add company holidays (example: 2024 holidays)
        COMPANY_HOLIDAYS.add(LocalDate.of(2024, 12, 25));  // Christmas Day
        COMPANY_HOLIDAYS.add(LocalDate.of(2025, 1, 1));
        COMPANY_HOLIDAYS.add(LocalDate.of(2025, 5, 26));
        COMPANY_HOLIDAYS.add(LocalDate.of(2025, 9, 1));
        COMPANY_HOLIDAYS.add(LocalDate.of(2025, 11, 27));
        COMPANY_HOLIDAYS.add(LocalDate.of(2025, 7, 4));
        COMPANY_HOLIDAYS.add(LocalDate.of(2025, 12, 25));
        COMPANY_HOLIDAYS.add(LocalDate.of(2026, 1, 1));
        // Add more holidays as needed
    }

    // Utility method to check if a date is a holiday
    public static boolean isHoliday(LocalDate date) {
        return COMPANY_HOLIDAYS.contains(date);
    }

    // --------------------------------------------------
    // Date Formatters
    // --------------------------------------------------
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final LocalTime CUT_OFF_TIME = LocalTime.of(10, 30);
}



