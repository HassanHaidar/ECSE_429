Feature: Add Tasks to A Project
  As a user I want to separate my projects by category to organize them in a clear way.

  Background:
    Given The application is running
    And Following projects exist:
      | project_title     | description | active | completed |
      | Toronto Trip      |             | false  | false     |
      | Vancouver Trip    |             | false  | false     |
      | Interview         |             | false  | false     |
      | Hackathon         |             | false  | false     |


  Scenario Outline: The user successfully categorizes a project (Normal Flow)
    Given Existing projects do not have any categories
    When I create a category "<category_title>" for existing project "<project_title>"
    Then I should receive a confirmation that my operation was successful
    And "<project_title>" project should have "<category_title>" category

    Examples:
      | project_title   | category_title |
      | Toronto Trip    | Vacation |
      | Interview        | Work |


  Scenario Outline: The user successfully adds a category to project category description (Alternate Flow)
    Given Existing projects do not have any categories
    When I create a category "<category_title>" with description "<category_description>" for existing project "<project_title>"
    Then I should receive a confirmation that my operation was successful
    And "<project_title>" project should have "<category_title>" category

    Examples:
      | project_title   | category_title  | category_description  |
      | Vancouver Trip  | Vacation        | vacation stuff        |
      | Hackathon       | School          | work stuff            |


  Scenario Outline: The user tries to add a task to a non-existing project(Error Flow)
    When I create a category "<category_title>" with "<category_description>" for a non-existing project "<project_title>"
    Then I should receive an error informing me that the requested resource was not found
    And "<category_title>" category should not exist in the system for "<project_title>"

    Examples:
      | project_title | category_title   | category_description   |
      | Trip to Paris | Trips | Projects about trips |