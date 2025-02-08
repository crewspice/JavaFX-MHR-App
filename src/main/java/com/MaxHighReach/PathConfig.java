package com.MaxHighReach;

import java.nio.file.Paths;

public class PathConfig {
    // --------------------------------------------------
    // Dynamic Base Paths
    // --------------------------------------------------
    private static final String USER_HOME = System.getProperty("user.home");

    public static final String BASE_DIR = Paths.get(USER_HOME, "OneDrive", "Documents", "MaxReachPro").toString();
    public static final String CONTRACTS_DIR = Paths.get(BASE_DIR, "Composing Contracts").toString();
    public static final String INVOICES_DIR = Paths.get(BASE_DIR, "Composing Invoices").toString();
    
    // --------------------------------------------------
    // Original Paths (Reorganized)
    // --------------------------------------------------
    private static final String PREFIX = Paths.get(USER_HOME, "OneDrive", "Documents", "Max High Reach", "MONTH END").toString();
    private static final String SRCDIR = Paths.get(BASE_DIR, "Composing Invoices").toString();

    public static final String INVOICE_QUERY = Paths.get(PREFIX, "scripts", "invoice_batch.xml").toString();
    public static final String SDK_PATH = Paths.get(BASE_DIR, "Intuit Applications", "IDN", "QBSDK16.0", 
                                                     "tools", "SDKTest", "SDKTestPlus3.exe").toString();

    // --------------------------------------------------
    // Contract & Invoice File Paths
    // --------------------------------------------------
    public static final String CONTRACT_TEMPLATE = Paths.get(CONTRACTS_DIR, "contract template.pdf").toString();

    public static String getContractFilePath(String rentalId) {
        return Paths.get(CONTRACTS_DIR, "contract_" + rentalId + ".pdf").toString();
    }

    public static String getFinalContractFilePath(String dateString) {
        return Paths.get(CONTRACTS_DIR, "contracts_" + dateString + ".pdf").toString();
    }

    // --------------------------------------------------
    // Getters for Key Paths
    // --------------------------------------------------
    public static String getPrefix() {
        return PREFIX;
    }

    public static String getSrcDir() {
        return SRCDIR;
    }

    public static String getInvoiceQuery() {
        return INVOICE_QUERY;
    }
}
