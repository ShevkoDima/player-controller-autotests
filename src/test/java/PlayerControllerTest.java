
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.restassured.http.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.emptyOrNullString;

@Feature("Player Controller CRUD tests")
public class PlayerControllerTest {
    private static final Logger logger = LoggerFactory.getLogger(PlayerControllerTest.class);


    private final static String URL = "http://localhost:8080";
    private final static String createPlayer = "/player/create";
    private final static String getPlayer = "/player?id=%s";
    private final static String deletePlayer = "/player/delete/%d";
    private final static String updatePlayer = "/player/update/%d";

    @Test
    @Description("Getting player with id=3")
    public void getPlayerTest() {
        given()
                .when()
                .get(String.format(URL + getPlayer, "3"))
                .then().log().all()
                .statusCode(200)
                .body("age", equalTo(40));
    }

    @Test
    @Description("Getting non existing player with id=5")
    public void getPlayerNegativeTest() {
        logger.info("Starting getPlayerNegativeTest");
        given()
                .when()
                .get(String.format(URL + getPlayer, "5"))
                .then().log().all()
                .statusCode(404)
                .body("message", equalTo("Player not found"));
    }

    @Test
    @Description("Creating new player")
    public void createPlayerTest() {
        logger.info("Starting createPlayerTest");
        given()
                .contentType(ContentType.JSON)
                .body("{\"age\": 20, \"gender\": \"male\", \"login\": \"firstPlayer2000\", \"password\": \"firstPassword1\", \"role\": \"user\", \"screenName\": \"Josef\"}")
                .when()
                .post(URL + createPlayer)
                .then().log().all()
                .statusCode(201)
                .body("id", matchesPattern("\\b\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}\\b"));
    }

    @Test
    @Description("Creating new player")
    public void createPlayerNegativeAgeTest() {
        logger.info("Starting createPlayerNegativeAgeTest");
        given()
                .contentType(ContentType.JSON)
                .body("{\"age\": 15, \"gender\": \"male\", \"login\": \"Player94\", \"password\": \"firstPassword1\", \"role\": \"user\", \"screenName\": \"John\"}")
                .when()
                .post(URL + createPlayer)
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("Validation failed"))
                .body("errors", hasItem("User must be older than 16 and younger than 60 years old"));
    }

    @Test
    @Description("Creating new player")
    public void createPlayerNegativeGenderLoginPasswordRoleScreenNameTest() {
        logger.info("Starting createPlayerNegativeGenderLoginPasswordRoleScreenNameTest");
        given()
                .contentType(ContentType.JSON)
                .body("{\"age\": 25, \"gender\": \"shemale\", \"login\": \"secondPlayer\", \"password\": \"zeroPassword\", \"role\": \"SuperUser\", \"screenName\": \"Borat\"}")
                .when()
                .post(URL + createPlayer)
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("Validation failed"))
                .body("errors", hasItem("Role must be either 'male' or 'female'"))
                .body("errors", hasItem("Login must be unique"))
                .body("errors", hasItem("Password must contain Latin letters and numbers (min 7 max 15 characters)"))
                .body("errors", hasItem("Role must be either 'admin' or 'user'"))
                .body("errors", hasItem("Screen name must be unique"));
    }

    @Test
    @Description("Deleting player with id=1")
    public void deletePlayerTest() {
        logger.info("Starting deletePlayerTest");
        given()
                .when()
                .delete(String.format(URL + deletePlayer, 1))
                .then().log().all()
                .statusCode(204)
                .body(emptyOrNullString());
    }

    @Test
    @Description("Deleting non existing player with id=5")
    public void deleteNonExistingPlayerNegativeTest() {
        logger.info("Starting deleteNonExistingPlayerNegativeTest");
        given()
                .when()
                .delete(String.format(URL + deletePlayer, 5))
                .then().log().all()
                .statusCode(400)
                .body(hasToString("Failed to delete user"));
    }

    @Test
    @Description("Update player")
    public void updatePlayerTest() {
        logger.info("Starting updatePlayerTest");
        given()
                .contentType(ContentType.JSON)
                .body("{\"age\": 22, \"gender\": \"female\", \"login\": \"updatedLogin\", \"password\": \"updatedPass1\", \"role\": \"user\", \"screenName\": \"Adam\"}")
                .when()
                .patch(String.format(URL + updatePlayer, 1))
                .then().log().all()
                .statusCode(200)
                .body("age", equalTo(22))
                .body("gender", equalTo("female"))
                .body("login", equalTo("updatedLogin"))
                .body("password", equalTo("updatedPass1"))
                .body("role", equalTo("user"))
                .body("screenName", equalTo("Adam"));
    }

    @Test
    @Description("Update non existing player with id=10")
    public void updateNonExistingPlayerNegativeTest() {
        logger.info("Starting updateNonExistingPlayerNegativeTest");
        given()
                .contentType(ContentType.JSON)
                .body("{\"age\": 22, \"gender\": \"female\", \"login\": \"updatedLogin\", \"password\": \"updatedPass1\", \"role\": \"user\", \"screenName\": \"Adam\"}")
                .when()
                .patch(String.format(URL + updatePlayer, 10))
                .then().log().all()
                .statusCode(404)
                .body("message", equalTo("Player not found"));
    }

    @Test
    @Description("BUG - it is possible to update field 'role', but should not")
    public void updateRoleTest() {
        logger.info("Starting bugged updateRoleTest");
        given()
                .contentType(ContentType.JSON)
                .body("{\"age\": 22, \"gender\": \"female\", \"login\": \"updatedLogin\", \"password\": \"updatedPass1\", \"role\": \"admin\", \"screenName\": \"Adam\"}")
                .when()
                .patch(String.format(URL + updatePlayer, 1))
                .then().log().all()
                .statusCode(200)
                .body("role", equalTo("admin"));
    }
}
