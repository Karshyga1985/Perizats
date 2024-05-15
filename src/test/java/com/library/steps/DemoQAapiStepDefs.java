package com.library.steps;

import com.github.javafaker.Faker;
import com.library.utility.Driver;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static io.restassured.RestAssured.*;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.junit.Assert;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DemoQAapiStepDefs {
    static String userId;
    static String userName;
    static String token;
    static String expires;
    Map<String, String> mapBody;

    @Given("A Test user is created")
    public void a_test_user_is_created() {

        mapBody = new LinkedHashMap<>();
        Faker faker = new Faker();
        userName = faker.name().username();
        mapBody.put("userName", faker.name().username());
        mapBody.put("password", "123445!yT");

        JsonPath jsonPath = given().accept(ContentType.JSON).log().uri()
                .contentType(ContentType.JSON)
                .body(mapBody)
                .when().post("https://demoqa.com/Account/v1/User").prettyPeek()
                .then()
                .statusCode(201)
                .contentType("application/json; charset=utf-8")
                .extract().jsonPath();

        userId = jsonPath.getString("userID");
        userName = jsonPath.getString("username");
        System.out.println("mapBody = " + mapBody);
    }

    @Given("A token is generated for the Test user")
    public void a_token_is_generated_for_the_test_user() {
        Map<String, String> mapBody = new LinkedHashMap<>();
        mapBody.put("userName", userName);
        mapBody.put("password", "123445!yT");


        JsonPath jsonPath2 = given().accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(mapBody)
                .when().post("https://demoqa.com/Account/v1/GenerateToken").prettyPeek()
                .then()
                .statusCode(200)
                .contentType("application/json; charset=utf-8")
                .extract().jsonPath();
        token = jsonPath2.getString("token");
        expires = jsonPath2.getString("expires");
        System.out.println("token = " + token);
        System.out.println("expires = " + expires);
       /*
        "status": "Success",
    "result": "User authorized successfully."
        */
        Assert.assertEquals("Success", jsonPath2.getString("status"));
        Assert.assertEquals("User authorized successfully.", jsonPath2.getString("result"));
    }

    @When("A book is added to Test user profile")
    public void a_book_is_added_to_test_user_profile() {
        String bookCreate = "{\n" +
                "  \"userId\": \"" + userId + "\",\n" +
                "  \"collectionOfIsbns\": [\n" +
                "    {\n" +
                "      \"isbn\": \"9781449325862\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        JsonPath jsonPath = given().accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(bookCreate)
                .when().post("https://demoqa.com/Bookstore/v1/Books").prettyPeek()
                .then()
                .statusCode(201)
                .extract().jsonPath();
        String isbn = jsonPath.getString("books[0].isbn");
        System.out.println("isbn = " + isbn);
    }

    @Then("At frontend-UI page of the application user and profile can be verified")
    public void at_frontend_ui_page_of_the_application_user_and_profile_can_be_verified() throws InterruptedException {

//provide cookies to login with API request
        Map<String, String> cookies = new HashMap<>();
        cookies.put("userID", userId);
        cookies.put("username", userName);
        cookies.put("token", token);
        cookies.put("expires", expires);
        Driver.getDriver().get("https://demoqa.com");
//add the cookies to website browser
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
//addCookie method from Selenium\
            Driver.getDriver().manage().addCookie(new Cookie(entry.getKey(), entry.getValue()));
        }
        Thread.sleep(5000);
        Driver.getDriver().manage().window().maximize();
        Driver.getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        Driver.getDriver().get("https://demoqa.com/profile");
    }

    @Then("Test user is deleted from system")
    public void test_user_is_deleted_from_system() {
//given().accept(ContentType.JSON)
//        .header("Authorization", "Bearer " + token)
//        .when().delete("https://demoqa.com/Account/v1/User/"+userId)
//        .then()
//        .statusCode(204);
    }

}
