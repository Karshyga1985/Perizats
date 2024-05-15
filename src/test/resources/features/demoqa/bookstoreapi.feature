Feature: Book Store Functionality
  @demoqa
  Scenario: End to End (E2E) Test for Book Store Functionality
    Given A Test user is created
    And A token is generated for the Test user
    When A book is added to Test user profile
    Then At frontend-UI page of the application user and profile can be verified
    And Test user is deleted from system