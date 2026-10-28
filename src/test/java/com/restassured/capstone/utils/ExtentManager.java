package com.restassured.capstone.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.restassured.capstone.config.ConfigReader;

/**
 * ExtentManager - creates and holds the Extent HTML report (Assignment 5).
 *
 * <p>The report contains: execution summary (dashboard), passed tests and
 * failed tests. Output file: {@code target/extent-report/ExtentReport.html}</p>
 */
public final class ExtentManager {

    /** Location of the generated HTML report. */
    public static final String REPORT_PATH = "target/extent-report/ExtentReport.html";

    private static ExtentReports extent;

    /** Holds the "current test" for each running thread so tests can add log lines to the report. */
    private static final ThreadLocal<ExtentTest> CURRENT_TEST = new ThreadLocal<>();

    private ExtentManager() {
    }

    /** @return the single ExtentReports object (created on first call). */
    public static synchronized ExtentReports getExtent() {
        if (extent == null) {
            ExtentSparkReporter spark = new ExtentSparkReporter(REPORT_PATH);
            spark.config().setDocumentTitle("Rest Assured Capstone - Execution Report");
            spark.config().setReportName("GoRest API Automation Report");
            spark.config().setTheme(Theme.STANDARD);

            extent = new ExtentReports();
            extent.attachReporter(spark);

            // Extra details displayed in the report's "Environment" section
            extent.setSystemInfo("Environment", ConfigReader.getEnvironment().toUpperCase());
            extent.setSystemInfo("Base URL", ConfigReader.getBaseUrl());
            extent.setSystemInfo("Java Version", System.getProperty("java.version"));
            extent.setSystemInfo("OS", System.getProperty("os.name"));
        }
        return extent;
    }

    /** Stores the test that is running on the current thread. */
    public static void setCurrentTest(ExtentTest test) {
        CURRENT_TEST.set(test);
    }

    /** @return the test running on the current thread (may be null outside a test). */
    public static ExtentTest getCurrentTest() {
        return CURRENT_TEST.get();
    }

    /** Adds an informational step to the current test in the report. Safe to call anywhere. */
    public static void logStep(String message) {
        ExtentTest test = CURRENT_TEST.get();
        if (test != null) {
            test.info(message);
        }
    }
}
