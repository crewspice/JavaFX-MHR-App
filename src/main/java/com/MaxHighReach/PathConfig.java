package com.MaxHighReach;

import java.nio.file.Paths;

public class PathConfig {
    // --------------------------------------------------
    // Dynamic Base Paths
    // --------------------------------------------------
    private static final String USER_HOME = System.getProperty("user.home");

    private static final boolean atWork = true;

    

    public static final String BASE_DIR = Paths.get(USER_HOME, "OneDrive", "Documents", "MaxReachPro").toString();
    public static final String CONTRACTS_DIR = Paths.get(BASE_DIR, "Composing Contracts").toString();
    public static final String INVOICES_DIR = Paths.get(BASE_DIR, "Composing Invoices").toString();
    public static final String SYNC_DIR = Paths.get(BASE_DIR, "Syncing with QB").toString();
    public static final String IMAGES_DIR = Paths.get(BASE_DIR, "Max Reach Pro", "src", "main", "resources", "images").toString();
    
    // --------------------------------------------------
    // Original Paths (Reorganized)
    // --------------------------------------------------
    public static final String PREFIX = atWork
            ? Paths.get(USER_HOME, "OneDrive", "Documents", "Max High Reach", "MONTH END").toString()
            : Paths.get(USER_HOME, "OneDrive", "Desktop", "Professional", "Max High Reach", "SMM").toString();
    public static final String SRCDIR = atWork
            ? Paths.get(USER_HOME, "OneDrive", "Documents", "MaxReachPro", "SMM Filing").toString()
            : Paths.get(USER_HOME, "OneDrive", "Desktop", "Professional", "Max High Reach", "Max-High-Reach").toString();

    public static final String OUTPUT_DIRECTORY = PREFIX;
    public static final String TEMPLATE_PATH = Paths.get(PREFIX, "SMM template 2020.xlsx").toString();
    public static final String SCRIPT_PATH = Paths.get(SRCDIR, "scripts", "orchestrate_process.py").toString();
    public static final String INVOICE_QUERY = Paths.get(SRCDIR, "scripts", "invoice_query.xml").toString();
    public static final String SDK_OUTPUT = Paths.get(SRCDIR, "outputs", "QBResponse.xml").toString();
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

    public static String getSdkPath() {
        return SDK_PATH;
    }

    public static String getBaseDir() {
        return BASE_DIR;
    }

    public static String getContractsDir() {
        return CONTRACTS_DIR;
    }

    public static String getInvoicesDir() {
        return INVOICES_DIR;
    }
}
