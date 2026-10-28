package com.restassured.capstone.utils;

import com.restassured.capstone.pojo.Comment;
import com.restassured.capstone.pojo.Post;
import com.restassured.capstone.pojo.User;

import java.util.UUID;

/**
 * TestDataManager - Test data management utility.
 *
 * <p>Creates fresh, unique test data for every run. Uniqueness matters because
 * GoRest rejects a user whose e-mail already exists (HTTP 422). Keeping all
 * data creation here means test classes stay clean and short.</p>
 */
public final class TestDataManager {

    private TestDataManager() {
    }

    /** @return a short random text such as "a1b2c3d4" used to make data unique. */
    private static String unique() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /** Builds a new valid user with a unique name and e-mail. */
    public static User newUser() {
        String id = unique();
        return new User("QA User " + id, "qa.user." + id + "@example.com", "male", "active");
    }

    /** Builds the data used to UPDATE an existing user (new name, status inactive). */
    public static User updatedUser(User original) {
        return new User("Updated " + original.getName(), original.getEmail(), "female", "inactive");
    }

    /** Builds a new post with a unique title. */
    public static Post newPost() {
        String id = unique();
        return new Post("Capstone post " + id, "This post was created by the Rest Assured capstone suite. Ref: " + id);
    }

    /** Builds a new comment with a unique body. */
    public static Comment newComment() {
        String id = unique();
        return new Comment("QA Commenter " + id, "qa.commenter." + id + "@example.com",
                "Automated comment " + id);
    }
}
