package com.restassured.capstone.utils;

import com.restassured.capstone.config.ConfigReader;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * CommonUtils - reusable helper methods shared by all test classes.
 */
public final class CommonUtils {

    private CommonUtils() {
    }

    /**
     * Builds the request specification that every test re-uses.
     * A specification holds settings that would otherwise be repeated in each test:
     * base URL, base path, JSON headers and the AUTHENTICATION header.
     *
     * <p><b>Authentication technique used:</b> Bearer token (OAuth2 style).
     * The header sent is {@code Authorization: Bearer <token>}.
     * GoRest requires it for POST, PUT and DELETE calls.</p>
     *
     * @return a ready to use RequestSpecification
     */
    public static RequestSpecification buildRequestSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(ConfigReader.getBaseUrl())            // from qa/uat property file
                .setBasePath(ConfigReader.getBasePath())          // public/v2
                .setContentType(ContentType.JSON)                 // we send JSON
                .setAccept(ContentType.JSON)                      // we expect JSON
                .addHeader("Authorization", "Bearer " + ConfigReader.getToken())
                .build();
    }

    /**
     * Reads the "id" field from a JSON response, e.g. the id of a newly created user.
     *
     * @param response Rest Assured response
     * @return the id as an int
     */
    public static int extractId(Response response) {
        return response.jsonPath().getInt("id");
    }

    /** @return the maximum accepted response time (ms) for the active environment. */
    public static long maxResponseTime() {
        return ConfigReader.getMaxResponseTime();
    }
}
