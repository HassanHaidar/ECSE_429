Feature: Assign Project to Task
  As a user, I want to be able to assign projects directly to my tasks so I can keep better organized

  Background:
  Given The application is running
    And Todos exist:
      | todo_title            | status  | todo_desc             |
      | Prank Clowns          | false   | water interior plants |
      | Have fun              | true    | asssignment 2         |
      | Think deeply          | false   | Quiz 1                |
    And Following projects exist:
      | project_title   | description | active | completed |
      | House remodel   |             | false  | false     |
      | Surprise party  |             | false  | false     |
      | Lab             |             | false  | false     |


  Scenario Outline: The user successfully adds a project to a task (Normal Flow)
    Given Existing projects do not have any tasks
    When I assign a task "<task_title>"  for existing project "<project_title>"
    Then I should receive a confirmation that my operation was successful
    And "<task_title>" task should have "<project_title>" project

    Examples:
    | project_title   | task_title      |
    | House remodel   | Prank Clowns    |
    | Lab             | Have fun        |


  Scenario Outline: The user successfully adds a task to a project which doesn't exist yet (Alternate Flow)
    Given Existing projects do not have any tasks
    When I assign a task "<task_title>" for non-existent project "<project_title>"
    Then I should receive a confirmation that my operation was successful
    And "<task_title>" task should have a project with name "<project_title>"

    Examples:
      | project_title   | task_title          |
      | Interview       | Prank Clowns        |
      | Hackathon       | Have fun            |


  Scenario Outline: The user tries to add a task to a non-existing project (Error Flow)
    When I create a task "<task_title>" with "<task_description>" for a non-existing project "<project_title>"
    Then I should receive an error informing me that the requested resource was not found
    And "<task_title>" task should not exist in the system for "<project_title>"

    Examples:
      | project_title | task_title   | task_description   |
      | Trip to Paris | Eiffel Tower | Visit Eiffel Tower |