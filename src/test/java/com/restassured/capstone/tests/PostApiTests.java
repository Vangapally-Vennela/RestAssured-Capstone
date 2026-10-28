package com.restassured.capstone.tests;

import com.restassured.capstone.base.BaseTest;
import com.restassured.capstone.pojo.Comment;
import com.restassured.capstone.pojo.Post;
import com.restassured.capstone.pojo.User;
import com.restassured.capstone.utils.CommonUtils;
import com.restassured.capstone.utils.ExtentManager;
import com.restassured.capstone.utils.TestDataManager;
import io.restassured.path.json.JsonPath;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * PostApiTests - Assignment 4 ("Product" APIs) plus the Post/Comment APIs of the guide.
 *
 * <p><b>Note about "Product":</b> the GoRest application exposes /users, /posts and
 * /comments only - there is no /products endpoint. The "product" assignment is therefore
 * automated on the catalogue-style endpoint that exists, /posts:</p>
 * <ul>
 *   <li>Get Product List     -> GET /posts</li>
 *   <li>Get Product Details  -> GET /posts/{id}   (post title = "product name")</li>
 *   <li>Search Product       -> GET /posts?title=...</li>
 * </ul>
 * Plus the Post APIs listed in the guide: create a post for a user, comments on a post.
 *
 * <p>Total: 5 test cases.</p>
 */
public class PostApiTests extends BaseTest {

    /** A user created in @BeforeClass that owns the posts we create. */
    private int ownerUserId;

    /** Id of the post created by the "createPostForUser" test. */
    private int createdPostId;

    /** Comment text that we add, then look for when reading comments. */
    private Comment createdComment;

    // ------------------------------------------------------------------
    // SETUP / CLEAN-UP
    // ------------------------------------------------------------------

    /** Creates a user who will own the posts (a post must belong to a user). */
    @BeforeClass(alwaysRun = true)   // runs after BaseTest.baseSetup() because parent @BeforeClass methods run first
    public void createOwnerUser() {
        User owner = TestDataManager.newUser();
        ownerUserId = given()
                .spec(requestSpec)
                .body(owner)
        .when()
                .post("/users")
        .then()
                .log().all()
                .statusCode(201)
                .extract().path("id");
    }

    /** Deletes the owner user (and with it the posts/comments) to leave the system clean. */
    @AfterClass(alwaysRun = true)
    public void deleteOwnerUser() {
        given()
                .spec(requestSpec)
                .pathParam("id", ownerUserId)
        .when()
                .delete("/users/{id}")
        .then()
                .log().all()
                .statusCode(anyOf(is(204), is(404)));
    }

    // ------------------------------------------------------------------
    // TEST 1 : Get Product List  -> GET /posts
    // ------------------------------------------------------------------
    @Test(priority = 1, description = "Get product list (GET /posts)")
    public void getProductList() {
        given()
                .spec(requestSpec)
        .when()
                .get("/posts")
        .then()
                .log().all()
                .statusCode(200)
                .time(lessThan(maxResponseTime))
                .body("size()", greaterThan(0))
                .body("[0].id", notNullValue())                    // product ID present
                .body("[0].title", not(emptyOrNullString()));      // product name present
    }

    // ------------------------------------------------------------------
    // TEST 2 : Get Product Details  -> GET /posts/{id}
    // ------------------------------------------------------------------
    @Test(priority = 2, description = "Get product details (GET /posts/{id})")
    public void getProductDetails() {
        // Step 1: read the list and pick the first item (id + name we will verify)
        JsonPath list = given().spec(requestSpec).when().get("/posts").then().statusCode(200)
                .extract().jsonPath();
        int productId = list.getInt("[0].id");
        String productName = list.getString("[0].title");
        ExtentManager.logStep("Looking up product id " + productId);

        // Step 2: fetch the single item and validate id, name and status code
        given()
                .spec(requestSpec)
                .pathParam("id", productId)
        .when()
                .get("/posts/{id}")
        .then()
                .log().all()
                .statusCode(200)
                .time(lessThan(maxResponseTime))
                .body("id", equalTo(productId))                    // Product ID
                .body("title", equalTo(productName));              // Product Name
    }

    // ------------------------------------------------------------------
    // TEST 3 : Search Product  -> GET /posts?title=<text>
    // ------------------------------------------------------------------
    @Test(priority = 3, description = "Search product by name (GET /posts?title=...)")
    public void searchProduct() {
        // Take the name of an existing item and search for it
        JsonPath list = given().spec(requestSpec).when().get("/posts").then().statusCode(200)
                .extract().jsonPath();
        int productId = list.getInt("[0].id");
        String productName = list.getString("[0].title");
        ExtentManager.logStep("Searching for product name: " + productName);

        given()
                .spec(requestSpec)
                .queryParam("title", productName)                  // search filter
        .when()
                .get("/posts")
        .then()
                .log().all()
                .statusCode(200)
                .time(lessThan(maxResponseTime))
                .body("size()", greaterThan(0))                    // at least one match
                .body("id", hasItem(productId))                    // our product is in the results
                .body("title", hasItem(productName));              // with the same name
    }

    // ------------------------------------------------------------------
    // TEST 4 : Create a post for a user  -> POST /users/{id}/posts
    // ------------------------------------------------------------------
    @Test(priority = 4, description = "Create a post for a user (POST /users/{id}/posts)")
    public void createPostForUser() {
        Post post = TestDataManager.newPost();
        ExtentManager.logStep("Creating post '" + post.getTitle() + "' for user " + ownerUserId);

        createdPostId = CommonUtils.extractId(
                given()
                        .spec(requestSpec)
                        .pathParam("userId", ownerUserId)
                        .body(post)
                .when()
                        .post("/users/{userId}/posts")
                .then()
                        .log().all()
                        .statusCode(201)
                        .time(lessThan(maxResponseTime))
                        .body("title", equalTo(post.getTitle()))
                        .body("body", equalTo(post.getBody()))
                        .body("user_id", equalTo(ownerUserId))     // post belongs to our user
                        .extract().response());
    }

    // ------------------------------------------------------------------
    // TEST 5 : Comments on a post  -> POST + GET /posts/{id}/comments
    // ------------------------------------------------------------------
    @Test(priority = 5, dependsOnMethods = "createPostForUser",
            description = "Add and read comments on a post (/posts/{id}/comments)")
    public void commentsOnPost() {
        // Step 1: add a comment to the post created in the previous test
        createdComment = TestDataManager.newComment();
        ExtentManager.logStep("Adding comment to post " + createdPostId);

        given()
                .spec(requestSpec)
                .pathParam("postId", createdPostId)
                .body(createdComment)
        .when()
                .post("/posts/{postId}/comments")
        .then()
                .statusCode(201)
                .time(lessThan(maxResponseTime))
                .body("post_id", equalTo(createdPostId))
                .body("body", equalTo(createdComment.getBody()));

        // Step 2: read the comments of the post and confirm ours is listed
        given()
                .spec(requestSpec)
                .pathParam("postId", createdPostId)
        .when()
                .get("/posts/{postId}/comments")
        .then()
                .log().all()
                .statusCode(200)
                .time(lessThan(maxResponseTime))
                .body("size()", greaterThan(0))
                .body("body", hasItem(createdComment.getBody()))
                .body("post_id", everyItem(equalTo(createdPostId)));
    }
}
