package com.library.steps;

import java.sql.ResultSet;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.library.pages.BookPage;
import com.library.pages.LoginPage;
import com.library.utility.*;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Assert;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.print.Book;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class APIStepDefs {

    RequestSpecification givenPart;
    Response response;
    ValidatableResponse thenPart;
    JsonPath jsonPath;
    Map<String, Object> apiMap = new LinkedHashMap<>();
    String token;
    String apiPassword;


    /**
     * US - 01
     */

    @Given("I logged Library api as a {string}")
    public void i_logged_library_api_as_a(String userType) {
        givenPart = RestAssured.given().log().uri()
                .header("x-library-token", LibraryAPI_Util.getToken(userType));
    }

    @Given("Accept header is {string}")
    public void accept_header_is(String accept) {
        givenPart.accept(accept);

    }

    @When("I send GET request to {string} endpoint")
    public void i_send_get_request_to_endpoint(String endpoint) {
        response = givenPart.when().get(ConfigurationReader.getProperty("library.baseUri") + endpoint)
                .prettyPeek();

        thenPart = response.then();

    }

    @Then("status code should be {int}")
    public void status_code_should_be(Integer statusCode) {

        thenPart.statusCode(statusCode);
    }

    @Then("Response Content type is {string}")
    public void response_content_type_is(String contentType) {

        thenPart.contentType(contentType);
    }

    @Then("Each {string} field should not be null")
    public void each_field_should_not_be_null(String path) {

        thenPart.body(path, everyItem(notNullValue()));
    }

    // US 2 IS STARTING
    String id;

    @Given("Path param {string} is {string}")
    public void path_param_is(String path, String value) {
        givenPart.pathParam(path, value);
        id = value;
    }

    @Then("{string} field should be same with path param")
    public void field_should_be_same_with_path_param(String idPath) {
        thenPart.body(idPath, is(id));
    }

    @Then("following fields should not be null")
    public void following_fields_should_not_be_null(List<String> listOfData) {
        for (String eachData : listOfData) {
            thenPart.body(eachData, is(notNullValue()));
        }


    }

    // US 3 STARTS
    @Given("Request Content Type header is {string}")
    public void request_content_type_header_is(String requestContentType) {

        givenPart.contentType(requestContentType);
    }

    @Given("I create a random {string} as request body") // switch method
    public void i_create_a_random_as_request_body(String randomObject) {
        Map<String, Object> mapRequestBody = new LinkedHashMap<>();
        switch (randomObject) {

            case "book":
                mapRequestBody = LibraryAPI_Util.getRandomBookMap();

                break;
            case "user":
                mapRequestBody = LibraryAPI_Util.getRandomUserMap();
                break;
            default:
                throw new RuntimeException();
        }
        apiMap = mapRequestBody;
        givenPart.formParams(apiMap);
        System.out.println("apiMap = " + apiMap);

    }

    @When("I send POST request to {string} endpoint")
    public void i_send_post_request_to_endpoint(String endpoint) {
        response = givenPart.when().post(ConfigurationReader.getProperty("library.baseUri") + endpoint)
                .prettyPeek();
        thenPart = response.then();
    }

    @Then("the field value for {string} path should be equal to {string}")
    public void the_field_value_for_path_should_be_equal_to(String path, String message) {
        thenPart.body(path, is(message));
    }

    @Then("{string} field should not be null")
    public void field_should_not_be_null(String bookId) {
        thenPart.body(bookId, is(notNullValue()));
        thenPart.extract().response();
    }


    @Then("UI, Database and API created book information must match")
    public void ui_database_and_api_created_book_information_must_match() {
        jsonPath = response.jsonPath();
        int bookId = jsonPath.getInt("book_id");
        //DB Assertion
        DB_Util.runQuery("select*from books where id =" + bookId + "");
        Map<String, Object> dbMap = DB_Util.getRowMap(1);
        dbMap.remove("id");
        dbMap.remove("added_date");
        Assert.assertEquals(apiMap, dbMap);
//UI Assertion
        BookPage bookPage = new BookPage();
        Map<String, Object> uiData = new LinkedHashMap<>();
        String bookName = (String) apiMap.get("name");
        bookPage.search.sendKeys(bookName);
        bookPage.editBook(bookName).click();
        String uiName = bookPage.bookName.getAttribute("value");
        String uiIsbn = bookPage.isbn.getAttribute("value");
        String uiYear = bookPage.year.getAttribute("value");
        String uiAuthor = bookPage.author.getAttribute("value");
        String uiDescription = bookPage.description.getAttribute("value");
        String selected_category = BrowserUtil.getSelectedOption(bookPage.categoryDropdown);
        DB_Util.runQuery("select * from book_categories where name = '" + selected_category + "'");
        String book_category_id = DB_Util.getFirstRowFirstColumn();
        uiData.put("name", uiName);
        uiData.put("isbn", uiIsbn);
        uiData.put("year", uiYear);
        uiData.put("author", uiAuthor);
        uiData.put("book_category_id", book_category_id);
        uiData.put("description", uiDescription);
        Assert.assertEquals(apiMap, uiData);
        System.out.println("uiData = " + uiData);
        System.out.println("dbMap = " + dbMap);
        System.out.println("apiMap = " + apiMap);

    }


    // US 4 IS STARTED
    @Then("created user information should match with Database")
    public void created_user_information_should_match_with_database() throws NoSuchAlgorithmException {
        jsonPath = response.jsonPath();
        int userId = jsonPath.getInt("user_id");
        System.out.println("userId = " + userId);
        // String bookId = String.valueOf(bookIdInt);
        apiPassword = (String) apiMap.get("password");

        /////
        DB_Util.runQuery("select * from users where id = " + userId + "");
        Map<String, Object> dbMap = DB_Util.getRowMap(1);
        dbMap.remove("password");
        dbMap.remove("image");
        dbMap.remove("id");
        dbMap.remove("extra_data");
        dbMap.remove("is_admin");
        //delete password from apiMap
        apiMap.remove("password");
        Assert.assertEquals(apiMap, dbMap);
        System.out.println("dbMap = " + dbMap);
        System.out.println("apiMap = " + apiMap);
    }

    @Then("created user should be able to login Library UI")
    public void created_user_should_be_able_to_login_library_ui() {
        LoginPage loginPage = new LoginPage();
        String email = (String) apiMap.get("email");
        loginPage.login(email, apiPassword);
        BrowserUtil.waitFor(3);
    }

    @Then("created user name should appear in Dashboard Page")
    public void created_user_name_should_appear_in_dashboard_page() {
        BookPage bookPage = new BookPage();
        String expectedUserName = (String) apiMap.get("full_name");
        String actualUserName = bookPage.accountHolderName.getText();
        Assert.assertEquals(expectedUserName, actualUserName);
    }

    @Given("I logged Library api with credentials {string} and {string}")
    public void i_logged_library_api_with_credentials_and(String email, String password) {
        token = LibraryAPI_Util.getToken(email, password);
        givenPart= RestAssured.given().log().uri();

    }

    @Given("I send token information as request body")
    public void i_send_token_information_as_request_body() {
        givenPart.formParam("token", token);
    }


}
