package com.MaxHighReach;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.scene.paint.Color;

public class Config {
    // --------------------------------------------------
    // Window and UI Dimensions
    // --------------------------------------------------
    public static final double WINDOW_WIDTH = 320;
    public static final double WINDOW_HEIGHT = 828;
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

    // --------------------------------------------------
    // Tokens and keys
    // --------------------------------------------------
    public static final String GOOGLE_KEY = "AIzaSyBPqUosXIYsD2XMPYYeWphhDS7XwphdVB0";
    
    public static final String SPIREON_ACCOUNT_NAME = "Max High Reach Inc";
    public static final String SPIREON_NSPIRE_ID = "5301680";
    public static final String SPIREON_USR = "Spireonintegration@MaxHigh.com";
    public static final String SPIREON_PSWD = "hRs8$6Mw#g";
    public static final String SPIREON_EMAIL = "office@maxhighreach.com";
    public static final String SPIREON_TOKEN = "e2cfc07a-255a-4677-8f4f-07b8ae7149e8";

    public static final String API_BASE_URL = "http://5.78.73.173:8080";

    // --------------------------------------------------
    // SDK Path (Using PathConfig)
    // --------------------------------------------------
    public static final String SDK_PATH = PathConfig.getPrefix() + "\\Intuit Applications\\IDN\\QBSDK16.0\\tools\\SDKTest\\SDKTestPlus3.exe";

    // --------------------------------------------------
    // Employee Data
    // --------------------------------------------------
    public static final String[][] EMPLOYEES = {
            {"Isaiah Sabala", "IS", "I"},
            {"Kaleb Streit", "KS", "K"},
            {"Sandy Mulberry", "SM", "S"},
            {"Adrian Barraza", "AB", "A"},
            {"Jacob Streit", "JS", "JS"},
            {"Jackson Cline", "JC", "JC"},
            {"John Wright", "JW", "J"},
            {"Ken Mulberry", "KM", "KM"},
            {"Byron Chilton", "BC", "B"}
    };

    public static final String[][] DRIVERS;

    static {
        DRIVERS = Arrays.stream(EMPLOYEES)
            .filter(emp -> !emp[0].equals("Sandy Mulberry") && !emp[0].equals("Ken Mulberry"))
            .toArray(String[][]::new);
    }

    // --------------------------------------------------
    // Truck Data
    // --------------------------------------------------
    public static final double SHOP_LAT = 39.795135;
    public static final double SHOP_LON = -104.931914;
    public static final int NUMBER_OF_TRUCKS = 5;

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
    }

    public static final LocalTime CUT_OFF_TIME = LocalTime.of(10, 30);


    // --------------------------------------------------
    // Date Formatters
    // --------------------------------------------------
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Preset Colors (Rearranged from Bright to Dark)
    public static final String RIVORY = "#f7d0be";
    public static final String BRIGHT_CYAN = "#00FFFF";
    public static final String HOT_LIME = "#00FF00";
    public static final String CHARTREUSE = "#7FFF00";
    public static final String MINT_GREEN = "#98FF98";
    public static final String GOLDEN_YELLOW = "#FFD700";
    public static final String AMBER = "#FFBF00";
    public static final String ORANGE = "orange";
    public static final String PALE_TAN = "#FFDEAD";
    public static final String PEACH = "#FFDAB9";
    public static final String PASTEL_PINK = "#FFB3DE";
    public static final String SOFT_PINK = "#FFB6C1";
    public static final String LIGHT_CORAL = "#F08080";
    public static final String SALMON = "#FA8072";
    public static final String CORAL = "#FF7F50";
    public static final String TANGERINE = "#FF6347";
    public static final String WATERMELON = "#FC6C85";
    public static final String HOT_PINK = "#FF69B4";
    public static final String FUCHSIA = "#FF00FF";
    public static final String LIGHT_GRAY = "#D3D3D3";
    public static final String SKY_GRAY = "#B0C4DE";
    public static final String PERIWINKLE_BLUE = "#b4d1cc";
    public static final String PERIWINKLE = "#b8d4bf";
    public static final String SLATE_GRAY = "#708090";
    public static final String TURQUOISE = "#40E0D0";
    public static final String TURQUOISE_BLUE = "#00CED1";
    public static final String AQUAMARINE = "#7FFFD4";
    public static final String SKY_BLUE = "#87CEEB";
    public static final String ELECTRIC_BLUE = "#7DF9FF";
    public static final String GOLD = "#e3ca8f";
    public static final String SAPPHIRE_BLUE = "#0F52BA";
    public static final String NAVAJO_WHITE = "#fcffad";
    public static final String SEA_GREEN = "#2E8B57";
    public static final String FOREST_GREEN = "#228B22";
    public static final String LIME_GREEN = "#32CD32";
    public static final String YELLOW_GREEN = "#9ACD32";
    public static final String CELADON = "#ACE1AF";
    public static final String EMERALD_GREEN = "#50C878";
    public static final String RUBY_RED = "#9B111E";
    public static final String CARMINE = "#960018";
    public static final String CRIMSON_RED = "#DC143C";
    public static final String GARNET = "#9E1B32";
    public static final String PLUM_WINE = "#50b5a4";
    public static final String MAROON = "#800000";
    public static final String PLUM = "#DDA0DD";
    public static final String PLUM_PURPLE = "#8E4585";
    public static final String MAUVE = "#E0B0FF";
    public static final String AMETHYST = "#9966CC";
    public static final String INDIGO = "#f060b4";
    public static final String VIOLET = "#8A2BE2";
    public static final String DEEP_PURPLE = "#673AB7";
    public static final String CHOCOLATE = "#D2691E";
    public static final String SANDY_BROWN = "#F4A460";
    public static final String DARK_SIENNA = "#A0522D";
    public static final String BRONZE = "#CD7F32";
    public static final String DARK_OLIVE = "#556B2F";
    public static final String ORANGE_TWO = "#FFA500";
    public static final String CHARCOAL_GRAY = "#36454F";
    public static final String MIDNIGHT_BLUE = "#191970";
    public static final String NAVY_BLUE = "#000080";

    // List of all colors for dynamic handling (sorted from bright to dark)
    public static final List<String> ALL_COLORS = List.of(
            RIVORY, BRIGHT_CYAN, HOT_LIME, CHARTREUSE, MINT_GREEN, GOLDEN_YELLOW, AMBER, ORANGE, PALE_TAN, PEACH,
            PASTEL_PINK, SOFT_PINK, LIGHT_CORAL, SALMON, CORAL, TANGERINE, WATERMELON, HOT_PINK, FUCHSIA,
            LIGHT_GRAY, SKY_GRAY, PERIWINKLE_BLUE, PERIWINKLE, SLATE_GRAY, TURQUOISE, TURQUOISE_BLUE, AQUAMARINE,
            SKY_BLUE, ELECTRIC_BLUE, GOLD, SAPPHIRE_BLUE, NAVAJO_WHITE, SEA_GREEN, FOREST_GREEN, LIME_GREEN,
            YELLOW_GREEN, CELADON, EMERALD_GREEN, RUBY_RED, CARMINE, CRIMSON_RED, GARNET, PLUM_WINE, MAROON,
            PLUM, PLUM_PURPLE, MAUVE, AMETHYST, INDIGO, VIOLET, DEEP_PURPLE, CHOCOLATE, SANDY_BROWN, DARK_SIENNA,
            BRONZE, DARK_OLIVE, ORANGE_TWO, CHARCOAL_GRAY, MIDNIGHT_BLUE, NAVY_BLUE
    );

    // Define text color codes: 0 = Undefined, 1 = Black, 2 = White
    public static final Map<String, Integer> COLOR_TEXT_MAP = Map.ofEntries(
        Map.entry(RIVORY, 1), Map.entry(BRIGHT_CYAN, 1), Map.entry(HOT_LIME, 1), Map.entry(CHARTREUSE, 1), 
        Map.entry(MINT_GREEN, 1), Map.entry(GOLDEN_YELLOW, 1), Map.entry(AMBER, 1), Map.entry(ORANGE, 1), 
        Map.entry(PALE_TAN, 1), Map.entry(PEACH, 1), Map.entry(PASTEL_PINK, 1), Map.entry(SOFT_PINK, 1), 
        Map.entry(LIGHT_CORAL, 1), Map.entry(SALMON, 1), Map.entry(CORAL, 1), Map.entry(TANGERINE, 1), 
        Map.entry(WATERMELON, 1), Map.entry(HOT_PINK, 1), Map.entry(FUCHSIA, 1), Map.entry(LIGHT_GRAY, 1), 
        Map.entry(SKY_GRAY, 1), Map.entry(PERIWINKLE_BLUE, 1), Map.entry(PERIWINKLE, 1), Map.entry(SLATE_GRAY, 1), 
        Map.entry(TURQUOISE, 1), Map.entry(TURQUOISE_BLUE, 1), Map.entry(AQUAMARINE, 1), Map.entry(SKY_BLUE, 1), 
        Map.entry(ELECTRIC_BLUE, 1), Map.entry(GOLD, 1), Map.entry(SAPPHIRE_BLUE, 2), Map.entry(NAVAJO_WHITE, 1), 
        Map.entry(SEA_GREEN, 1), Map.entry(FOREST_GREEN, 1), Map.entry(LIME_GREEN, 1), Map.entry(YELLOW_GREEN, 1), 
        Map.entry(CELADON, 1), Map.entry(EMERALD_GREEN, 1), Map.entry(RUBY_RED, 2), Map.entry(CARMINE, 2), 
        Map.entry(CRIMSON_RED, 2), Map.entry(GARNET, 2), Map.entry(PLUM_WINE, 2), Map.entry(MAROON, 2), 
        Map.entry(PLUM, 2), Map.entry(PLUM_PURPLE, 2), Map.entry(MAUVE, 2), Map.entry(AMETHYST, 2), 
        Map.entry(INDIGO, 2), Map.entry(VIOLET, 2), Map.entry(DEEP_PURPLE, 2), Map.entry(CHOCOLATE, 2), 
        Map.entry(SANDY_BROWN, 2), Map.entry(DARK_SIENNA, 2), Map.entry(BRONZE, 2), Map.entry(DARK_OLIVE, 2), 
        Map.entry(ORANGE_TWO, 1), Map.entry(CHARCOAL_GRAY, 2), Map.entry(MIDNIGHT_BLUE, 2), Map.entry(NAVY_BLUE, 2)
    );


    // Default UI Colors (User Changeable)
    private static String PRIMARY_COLOR = ORANGE;
    private static String SECONDARY_COLOR = PALE_TAN;
    private static String TERTIARY_COLOR = getTertiaryColor(); // dark grey

    private static String PREVIOUS_PRIMARY_COLOR = PRIMARY_COLOR;
    private static String PREVIOUS_SECONDARY_COLOR = SECONDARY_COLOR;
    private static String PREVIOUS_TERTIARY_COLOR = TERTIARY_COLOR;

    private static String[] user = null;


    // --------------------------------------------------
    // Customers
    // --------------------------------------------------

    public static final Map<String, String> CUSTOMER_NAME_MAP = Map.ofEntries(
        Map.entry("Golden Way Mech", "Golden Way"),
        Map.entry("Cintas Fire", "Cintas"),
        Map.entry("Kelly Electrical", "Kelly Elec."),
        Map.entry("Empire Electric", "Empire Elec."),
        Map.entry("Titan Electric", "Titan"),
        Map.entry("Eagle Electric", "Eagle Elec."),

        Map.entry("Clear Creek Mechanical", "Clear Creek"),
        Map.entry("Canyon Plumbing", "Canyon"),
        Map.entry("Rogers and Sons", "Rogers & Sons"),
        Map.entry("Integrated Syst", "ISI"),
        Map.entry("SJO Electric", "SJO"),
        Map.entry("Meridian Fire", "Meridian"), 
                
        Map.entry("Colorado Garage", "CO Garage"),
        Map.entry("First Industrial", "1st Ind."),
        Map.entry("A.A.A. Fire Protection", "A.A.A. Fire"),
        Map.entry("Bear Electric", "Bear Elec."),
        Map.entry("Capra Plumbing", "Capra"),
        Map.entry("CMT ELectric", "CMT"), 
                
        Map.entry("North/Western Elec", "NW Elec."),
        Map.entry("DLR/Equipment Giant", "Discount Lift"),
        Map.entry("Sentinel Fire", "Sentinel"),
        Map.entry("Superior Heating and Air", "Superior"),
        Map.entry("Arrcon Electric", "Arrcon Elec."),
        Map.entry("KZ Electric Inc", "KZ Elec."), 
                
        Map.entry("Colorado Finest Doors", "CO Finest"),
        Map.entry("Rocky Industries", "Rocky Ind."),
        Map.entry("Blue Sky Plumbing", "Blue Sky"),
        Map.entry("Englewood Lock / Eteksystems", "E-tek"),
        Map.entry("RC HVAC&Construction", "RC HVAC"),
        Map.entry("Concentric Fire Protection", "Concentric"), 
                
        Map.entry("Gold Label Specialties", "Gold Label"),
        Map.entry("Frontier Fire Protection", "Frontier Fire"),
        Map.entry("Kennedy Electric", "Kennedy"),
        Map.entry("Piper Electric", "Piper"),
        Map.entry("JC2 Lighting & Electric", "JC2"),
        Map.entry("Legacy Garage Doors", "Legacy"), 
                
        Map.entry("Titanium Electric", "Titanium"),
        Map.entry("JC Garage Doors", "JC Garage"),
        Map.entry("Rocky Mtn Hydrostatics", "Rocky Mtn Hydro"),
        Map.entry("Gonzales Fire Protection", "Gonzales"),
        Map.entry("Maven Fire Protection", "Maven"),
        Map.entry("Bare Bright Electric", "Bare Bright"),
                        
        Map.entry("Bullseye Electrical", "Bullseye"),
        Map.entry("Lincoln Fire", "Lincoln"),
        Map.entry("Stanmark Electric", "Stanmark"),
        Map.entry("B & B Garage Door", "B & B"),
        Map.entry("K M Electric", "KM Elec."),
        Map.entry("P & L Electric", "P & L Elec."),
                        
        Map.entry("Colorado Crane and Hoist", "CO Crane"),
        Map.entry("Vigil Electric", "Vigil Elec."),
        Map.entry("Jefferies Inc.", "Jeffries"),
        Map.entry("Thompson Safety/Complete Fire", "Thompson"),
        Map.entry("Electrical Solutions", "Elec. Sol'ns"),
        Map.entry("Colorado Fire Serivces", "CO Fire"),
                        
        Map.entry("Paragon Electric", "Paragon Elec."),
        Map.entry("Wildwood Serivces", "Wildwood"),
        Map.entry("Capital Electric", "Capital"),
        Map.entry("Noble Overhead Door, llc", "Noble"),
        Map.entry("Noble OH Door, llc", "Noble"),
        Map.entry("Harmon Construction", "Harmon"),
        Map.entry("New Life Tree Triming", "New Life"), 
                                
        Map.entry("Community Mechanical Service", "Com. Mech."),
        Map.entry("Access Electrical", "Access Elec."),
        Map.entry("Vortex Doors", "Vortex"),
        Map.entry("Legault Electric", "Legault"),
        Map.entry("Denver Fire Protection", "Denver Fire"),
        Map.entry("Vulcan Construction Group", "Vulcan"),
                                
        Map.entry("Sentry Protection Systems", "Sentry Protec."),
        Map.entry("Apex Companies", "Apex"),
        Map.entry("Team Electric", "Team Elec."),
        Map.entry("Pagett Elec", "Pagett"),
        Map.entry("Diamond Fire", "Diamond"), 

        Map.entry("Redline/Colorado Fire Services", "Redline"), 
        Map.entry("Highland Ranch HVAC", "Highland HVAC"), 
        Map.entry("Clarion Design", "Clarion"), 
        Map.entry("Colorado Electrical Contractors", "CO Elec. Contractors"),
        Map.entry("Altitude Network Solutions", "Altitude Network"),

        Map.entry("Hendriks Construction", "Hendriks"),
        Map.entry("Continued Technologies", "Continued Tech")
    );

    public static String simplifyCustomerName(String fullName) {
        return CUSTOMER_NAME_MAP.getOrDefault(fullName, fullName);
    }

    public static final String A_TEST_CUSTOMER_ID = "80000D36-1727373143";

    //''                                                                        ''//
    //*****''//////***********''    M E T H O D S    '''************///////''*****//
    //''                                                                        ''//

    // Utility method to check if a date is a holiday
    public static boolean isHoliday(LocalDate date) {
        return COMPANY_HOLIDAYS.contains(date);
    }

    public static String getPrimaryColor() {
        return PRIMARY_COLOR;
    }

    public static String getSecondaryColor() {
        return SECONDARY_COLOR;
    }

    public static String getTertiaryColor() {
        return getTertiaryColorFromPrimary();
    }

    public static String getPreviousPrimaryColor() {
        return PREVIOUS_PRIMARY_COLOR;
    }

    public static String getPreviousSecondaryColor() {
        return PREVIOUS_SECONDARY_COLOR;
    }

    public static String getPreviousTertiaryColor() {
        return PREVIOUS_TERTIARY_COLOR;
    }

    public static String getTertiaryColorFromPrimary() {
        Color primary = Color.web(PRIMARY_COLOR);

        // Interpolate halfway between primary and black
        Color tertiary = primary.interpolate(Color.BLACK, 0.7);

        // Convert interpolated color back to hex
        TERTIARY_COLOR = String.format("#%02X%02X%02X",
                (int) (tertiary.getRed() * 255),
                (int) (tertiary.getGreen() * 255),
                (int) (tertiary.getBlue() * 255));

        return TERTIARY_COLOR;
    }

    public static void setPrimaryColor(String newColor) {
        PREVIOUS_PRIMARY_COLOR = PRIMARY_COLOR;
        PRIMARY_COLOR = newColor;
        String tertiary = getTertiaryColorFromPrimary();
    }

    public static void setSecondaryColor(String newColor) {
        PREVIOUS_SECONDARY_COLOR = SECONDARY_COLOR;
        SECONDARY_COLOR = newColor;
    }

    public static void setTertiaryColor(String newColor) {
        PREVIOUS_TERTIARY_COLOR = TERTIARY_COLOR;
        TERTIARY_COLOR = newColor;
    }

    public static void setUser(String fullName) {
        for (String[] employee : EMPLOYEES) {
            if (employee[0].equals(fullName)) {
                user = employee; // Set the user to the corresponding employee line
                break;
            }
        }
    }

    public static String getAbbreviatedLiftType(String value) {
        // Iterate through the map to find the key corresponding to the given value
        for (Map.Entry<String, String> entry : LIFT_BUTTON_TEXT_MAP.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey(); // Return the key
            }
        }
        return null; // Return null if value is not found
    }

    public static String[] getUser() {
        return user;
    }
    
}