package com.restassured.capstone.listeners;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.restassured.capstone.utils.ExtentManager;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ExtentTestListener - connects TestNG to the Extent report.
 *
 * <p>TestNG calls the methods below automatically when a test starts, passes,
 * fails or is skipped (the listener is registered in testng.xml). We use each
 * event to write an entry into the HTML report.</p>
 */
public class ExtentTestListener implements ITestListener {

    /** Names of tests that have actually started (used to detect "skipped without starting"). */
    private static final Set<String> STARTED_TESTS = ConcurrentHashMap.newKeySet();

    /** A test is about to start: create its entry in the report. */
    @Override
    public void onTestStart(ITestResult result) {
        STARTED_TESTS.add(result.getMethod().getQualifiedName());
        String testName = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription();

        ExtentTest test = ExtentManager.getExtent()
                .createTest(testName, description)
                .assignCategory(result.getTestClass().getRealClass().getSimpleName());

        ExtentManager.setCurrentTest(test);
    }

    /** Test passed. */
    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentManager.getCurrentTest().log(Status.PASS, "Test PASSED");
    }

    /** Test failed: record the exception so the reason is visible in the report. */
    @Override
    public void onTestFailure(ITestResult result) {
        ExtentManager.getCurrentTest().log(Status.FAIL, "Test FAILED: " + result.getThrowable().getMessage());
        ExtentManager.getCurrentTest().fail(result.getThrowable());
    }

    /** Test skipped (for example because a test it depends on failed). */
    @Override
    public void onTestSkipped(ITestResult result) {
        // A test skipped because a test it depends on failed never "started", so it has
        // no report entry yet - create one. Tests skipped after starting already have one.
        if (!STARTED_TESTS.contains(result.getMethod().getQualifiedName())) {
            ExtentTest test = ExtentManager.getExtent().createTest(result.getMethod().getMethodName());
            ExtentManager.setCurrentTest(test);
        }
        String reason = result.getThrowable() != null ? result.getThrowable().getMessage() : "Skipped";
        ExtentManager.getCurrentTest().log(Status.SKIP, "Test SKIPPED: " + reason);
    }

    /** All tests finished: write everything to the HTML file. */
    @Override
    public void onFinish(ITestContext context) {
        ExtentManager.getExtent().flush();
        System.out.println("Extent report generated at: " + ExtentManager.REPORT_PATH);
    }
}
