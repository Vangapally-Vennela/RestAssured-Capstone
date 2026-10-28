package com.restassured.capstone.base;

import com.restassured.capstone.config.ConfigReader;
import com.restassured.capstone.utils.CommonUtils;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;

/**
 * BaseTest - parent class of every test class.
 *
 * <p>It performs the common setup once per test class so the individual tests
 * only contain the API calls and validations.</p>
 */
public class BaseTest {

    /** Shared request specification (base URL, headers, auth token). */
    protected RequestSpecification requestSpec;

    /** Maximum accepted response time in ms for the active environment. */
    protected long maxResponseTime;

    @BeforeClass(alwaysRun = true)
    public void baseSetup() {
        // If a validation fails, print the full request and response in the console - helps debugging.
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        requestSpec = CommonUtils.buildRequestSpec();
        maxResponseTime = CommonUtils.maxResponseTime();

        System.out.println("Running against environment: " + ConfigReader.getEnvironment().toUpperCase()
                + "  |  Base URL: " + ConfigReader.getBaseUrl());
    }
}
