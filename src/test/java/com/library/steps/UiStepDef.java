package com.library.steps;

import com.library.pages.BookPage;
import com.library.pages.LoginPage;
import com.library.utility.BrowserUtil;
import io.cucumber.java.en.Given;

public class UiStepDef {


    @Given("I logged in Library UI as {string}")
    public void i_logged_in_library_ui_as(String user) {
        LoginPage loginPage = new LoginPage();
        loginPage.login(user);
        BrowserUtil.waitFor(3);
    }

    @Given("I navigate to {string} page")
    public void i_navigate_to_page(String moduleName) {
        BookPage bookPage = new BookPage();
        bookPage.navigateModule(moduleName);
        BrowserUtil.waitFor(3);
    }
}
