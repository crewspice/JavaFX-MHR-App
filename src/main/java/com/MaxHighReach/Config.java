package com.MaxHighReach;


import java.util.HashMap;
import java.util.Map;

public class Config {
   public static final double WINDOW_WIDTH = 320;
   public static final double WINDOW_HEIGHT = 800;
   public static final double SCISSOR_LIFT_WIDTH = WINDOW_WIDTH; // Example usage
   public static final double SCISSOR_LIFT_INITIAL_HEIGHT = 182; // Example usage
   public static final double PADDING = 10;
   public static final double BACK_BUTTON_X = 250;
   public static final double BACK_BUTTON_Y = 10;
   public static final double DELIVERY_DATE_COLUMN_WIDTH = 100;
   public static final double DB_ROW_HEIGHT = 66;
   public static final double DB_ROW_HEIGHT_EMPTY = 20;
   public static final String DB_URL = "jdbc:mysql://maxhighreach.com:3306/dispatch";
   public static final String DB_USR = "dispatch";
   public static final String DB_PSWD = "SQL3225422!a123";
   public static final String DB_NAME = "dispatch";

   /*
   -- keep to switch between local and remote databases --
   public static final String DB_URL = "jdbc:mysql://maxhighreach.com:3306/dispatch";
   public static final String DB_USR = "dispatch";
   public static final String DB_PSWD = "SQL3225422!a123";

   public static final String DB_URL = "jdbc:mysql://localhost:3306/practice_db";
   public static final String DB_USR = "root";
   public static final String DB_PSWD = "SQL3225422!a";

    */

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


}





