# Rest Assured Capstone – GoRest API Automation Framework

A simple, maintainable API automation framework built with **Java, Maven, Rest Assured, TestNG and Extent Reports**
for the GoRest application (`https://gorest.co.in/`).

## 1. What this project covers

| Requirement (from the capstone guide) | Where it is implemented |
|---|---|
| Maven project (no archetype) + Rest Assured + TestNG | `pom.xml` |
| Environment property files (`qa`, `uat`) | `src/test/resources/config/` |
| Property reader utility | `config/ConfigReader.java` |
| Run with `mvn test -Denv=qa` | `ConfigReader` reads the `env` system property |
| Common methods / request spec | `utils/CommonUtils.java`, `base/BaseTest.java` |
| Test data management | `utils/TestDataManager.java` |
| Authentication technique | Bearer token header in `CommonUtils.buildRequestSpec()` |
| User APIs – 5 tests (GET all, GET by id, POST, PUT, DELETE) | `tests/UserApiTests.java` |
| Product APIs – 3 tests (list, details, search) | `tests/PostApiTests.java` (see note below) |
| Post APIs (create post for user, comments on a post) | `tests/PostApiTests.java` |
| Validations: status code, details, response time | in every test |
| Extent HTML report (summary, passed, failed) | `listeners/ExtentTestListener.java`, `utils/ExtentManager.java` |
| Bonus: POJO class for user creation | `pojo/User.java` (also `Post`, `Comment`) |

**Total automated tests: 10** (within the required 8–10).

> **Note on "Product" APIs:** GoRest exposes only `/users`, `/posts` and `/comments` – there is no `/products` endpoint.
> The product assignment (list, details, search; validate name, ID and status code) is therefore automated on `/posts`,
> where the post **title** plays the role of the product name.

## 2. Project structure

```
restassured-capstone
├── pom.xml                              Maven build & dependencies
├── README.md
├── docs/DEMO_GUIDE.md                   5-minute demo script
└── src/test
    ├── java/com/perficient/capstone
    │   ├── base/BaseTest.java           common setup (request spec, logging)
    │   ├── config/ConfigReader.java     reads qa/uat property files + token
    │   ├── pojo/                        User, Post, Comment (JSON body classes)
    │   ├── utils/                       CommonUtils, TestDataManager, ExtentManager
    │   ├── listeners/ExtentTestListener.java   feeds results to Extent report
    │   └── tests/                       UserApiTests, PostApiTests
    └── resources
        ├── config/qa.properties
        ├── config/uat.properties
        └── testng.xml                   suite definition + listener
```

## 3. Prerequisites

* Java 11 or newer
* Maven 3.8+
* A free GoRest access token – create one at <https://gorest.co.in/consumer/login>
  (GoRest requires a token for POST / PUT / DELETE).

## 4. How to run

```bash
# QA environment (default)
mvn test -Denv=qa -Dtoken=YOUR_TOKEN

# UAT environment
mvn test -Denv=uat -Dtoken=YOUR_TOKEN
```

The token can also be supplied as the environment variable `GOREST_TOKEN`
(Windows PowerShell: `$env:GOREST_TOKEN="YOUR_TOKEN"`), then simply run `mvn test -Denv=qa`.
Token lookup order: `-Dtoken` → `GOREST_TOKEN` → `token=` in the property file.
The token is deliberately **not** stored in the repository.

## 5. Test report

After a run open: `target/extent-report/ExtentReport.html`
It shows the execution summary dashboard, passed tests, failed tests, per-test log steps, and the environment used.
(TestNG's own reports are also created in `target/surefire-reports/`.)

## 6. Framework design / execution flow

1. `mvn test` starts Maven Surefire, which loads `testng.xml`.
2. TestNG registers `ExtentTestListener` and starts the test classes.
3. `BaseTest.baseSetup()` builds one shared **request specification** (base URL from the property file,
   JSON headers, Bearer token).
4. Each test calls the API with Rest Assured (`given / when / then`) and validates status code,
   response body and response time (limit set per environment in the property file).
5. The listener records every pass / fail / skip in the Extent report, written when the run finishes.

Test design notes:

* User tests are chained (`dependsOnMethods`): create → get → update → delete, so the same user is used throughout.
* Post tests create their own owner user in `@BeforeClass` and delete it in `@AfterClass`, leaving no test data behind.
* All data is unique per run (random e-mail, titles) because GoRest rejects duplicate e-mails.

## 7. Endpoints used

| Action | Method | Path (after `/public/v2`) | Expected |
|---|---|---|---|
| Get all users | GET | `/users` | 200 |
| Get user by id | GET | `/users/{id}` | 200 |
| Create user | POST | `/users` | 201 |
| Update user | PUT | `/users/{id}` | 200 |
| Delete user | DELETE | `/users/{id}` | 204 |
| List / details / search posts | GET | `/posts`, `/posts/{id}`, `/posts?title=` | 200 |
| Create post for user | POST | `/users/{id}/posts` | 201 |
| Add / list comments | POST / GET | `/posts/{id}/comments` | 201 / 200 |
