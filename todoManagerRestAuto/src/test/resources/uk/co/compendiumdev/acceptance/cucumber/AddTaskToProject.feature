Feature: Add Tasks to A Project
  As a user, I want to plan my activities for my trip to Toronto by creating tasks

  Background:
  Given The application is running
  And Projects exist:
    | project_title     | description | active | completed |
    | Toronto Trip      |             | false  | false     |
    | Vancouver Trip    |             | false  | false     |
    | Interview         |             | false  | false     |
    | Hackathon         |             | false  | false     |


  Scenario Outline: The user successfully adds a task to project (Normal Flow)
    Given Existing projects do not have any tasks
    When I create a task "<task_title>" with "<task_description>" description for existing project "<project_title>"
    Then I should receive a confirmation that my operation was successful
    And "<project_title>" project should have "<task_title>" task

    Examples:
    | project_title   | task_title      | task_description   |
    | Toronto Trip    | Niagra Falls    | Visit Niagra falls |
    | Vancouver Trip  | Friends Meetup  | Meetup with friend |


  Scenario Outline: The user successfully adds a task to project without providing task description (Alternate Flow)
    Given Existing projects do not have any tasks
    When I create a task "<task_title>" without providing task description for existing project "<project_title>"
    Then I should receive a confirmation that my operation was successful
    And "<project_title>" project should have "<task_title>" task

    Examples:
      | project_title   | task_title          |
      | Interview       | Leetcode practice   |
      | Hackathon       | Python Review       |


  Scenario Outline: The user tries to add a task to a non-existing project(Error Flow)
    When I create a task "<task_title>" with "<task_description>" for a non-existing project "<project_title>"
    Then I should receive an error informing me that the requested resource was not found
    And "<task_title>" task should not exist in the system for "<project_title>"

    Examples:
      | project_title | task_title   | task_description   |
      | Trip to Paris | Eiffel Tower | Visit Eiffel Tower |