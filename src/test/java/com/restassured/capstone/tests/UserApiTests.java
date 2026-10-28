package com.restassured.capstone.tests;

import com.restassured.capstone.base.BaseTest;
import com.restassured.capstone.pojo.User;
import com.restassured.capstone.utils.CommonUtils;
import com.restassured.capstone.utils.ExtentManager;
import com.restassured.capstone.utils.TestDataManager;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * UserApiTests - Assignment 3: User API automation (5 test cases).
 *
 * <p>Covers GET, POST, PUT and DELETE. Every test validates:
 * <ul>
 *   <li>Status code</li>
 *   <li>User details in the response body</li>
 *   <li>API response time</li>
 * </ul>
 * The tests depend on each other (create -> get -> update -> delete) so that a
 * user created by the suite is the same user that is read, updated and deleted.</p>
 */
public class UserApiTests extends BaseTest {

    /** The user created by the "create" test and re-used by later tests. */
    private User createdUser;
    private int createdUserId;

    // ------------------------------------------------------------------
    // TEST 1 : POST /users  -> Create User  (uses the User POJO)
    // ------------------------------------------------------------------
    @Test(priority = 1, description = "Create a new user (POST /users)")
    public void createUser() {
        createdUser = TestDataManager.newUser();
        ExtentManager.logStep("Creating user with email: " + createdUser.getEmail());

        Response response = given()
                .spec(requestSpec)
                .body(createdUser)                       // POJO is converted to JSON automatically
        .when()
                .post("/users")
        .then()
                .log().all()
                .statusCode(201)                         // 201 = Created
                .time(lessThan(maxResponseTime))         // response time validation
                .body("id", notNullValue())              // server generated an id
                .body("name", equalTo(createdUser.getName()))
                .body("email", equalTo(createdUser.getEmail()))
                .body("gender", equalTo(createdUser.getGender()))
                .body("status", equalTo(createdUser.getStatus()))
                .extract().response();

        createdUserId = CommonUtils.extractId(response);
        ExtentManager.logStep("User created with id: " + createdUserId);
    }

    // ------------------------------------------------------------------
    // TEST 2 : GET /users  -> Get All Users
    // ------------------------------------------------------------------
    @Test(priority = 2, description = "Get all users (GET /users)")
    public void getAllUsers() {
        given()
                .spec(requestSpec)
        .when()
                .get("/users")
        .then()
                .log().all()
                .statusCode(200)
                .time(lessThan(maxResponseTime))
                .body("size()", greaterThan(0))          // list is not empty
                .body("[0].id", notNullValue())          // first user has an id ...
                .body("[0].name", not(emptyOrNullString()))   // ... a name ...
                .body("[0].email", containsString("@"));      // ... and a valid-looking email
    }

    // ------------------------------------------------------------------
    // TEST 3 : GET /users/{id}  -> Get single user by ID
    // ------------------------------------------------------------------
    @Test(priority = 3, dependsOnMethods = "createUser", description = "Get user by ID (GET /users/{id})")
    public void getUserById() {
        ExtentManager.logStep("Fetching user with id: " + createdUserId);

        given()
                .spec(requestSpec)
                .pathParam("id", createdUserId)
        .when()
                .get("/users/{id}")
        .then()
                .log().all()
                .statusCode(200)
                .time(lessThan(maxResponseTime))
                .body("id", equalTo(createdUserId))
                .body("name", equalTo(createdUser.getName()))
                .body("email", equalTo(createdUser.getEmail()))
                .body("gender", equalTo(createdUser.getGender()))
                .body("status", equalTo(createdUser.getStatus()));
    }

    // ------------------------------------------------------------------
    // TEST 4 : PUT /users/{id}  -> Update User
    // ------------------------------------------------------------------
    @Test(priority = 4, dependsOnMethods = "createUser", description = "Update user (PUT /users/{id})")
    public void updateUser() {
        User updated = TestDataManager.updatedUser(createdUser);
        ExtentManager.logStep("Updating user " + createdUserId + " -> name: " + updated.getName());

        given()
                .spec(requestSpec)
                .pathParam("id", createdUserId)
                .body(updated)
        .when()
                .put("/users/{id}")
        .then()
                .log().all()
                .statusCode(200)
                .time(lessThan(maxResponseTime))
                .body("id", equalTo(createdUserId))
                .body("name", equalTo(updated.getName()))      // name changed
                .body("gender", equalTo("female"))             // gender changed
                .body("status", equalTo("inactive"));          // status changed
    }

    // ------------------------------------------------------------------
    // TEST 5 : DELETE /users/{id}  -> Delete User
    // ------------------------------------------------------------------
    @Test(priority = 5, dependsOnMethods = {"createUser", "updateUser"},
            description = "Delete user (DELETE /users/{id})")
    public void deleteUser() {
        ExtentManager.logStep("Deleting user with id: " + createdUserId);

        given()
                .spec(requestSpec)
                .pathParam("id", createdUserId)
        .when()
                .delete("/users/{id}")
        .then()
                .log().all()
                .statusCode(204)                         // 204 = No Content (deleted)
                .time(lessThan(maxResponseTime));

        // Extra verification: the deleted user must no longer be found (404 Not Found)
        given()
                .spec(requestSpec)
                .pathParam("id", createdUserId)
        .when()
                .get("/users/{id}")
        .then()
                .log().all()
                .statusCode(404);
    }
}
