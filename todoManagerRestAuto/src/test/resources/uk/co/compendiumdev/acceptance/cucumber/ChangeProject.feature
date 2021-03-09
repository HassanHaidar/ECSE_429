Feature: Create Project
  As a user I want to edit my existing projects to make modifications to a project I already created.

  Background:
    Given The application is running
    And Following projects exist:
      | project_title  | description | active | completed |
      | Vancouver Trip |             | false  | false     |
      | Camping        |             | false  | false     |
      | ECSE_429       |             | false  | false     |

  Scenario Outline: The user successfully edits the title and description of the project (Normal Flow)
    When I change the project title from "<old_project_title>" to "<project_title>"
    Then I should receive a confirmation that my operation was successful
    And Project "<old_project_title>" will have "<project_title>"

    Examples:
      | old_project_title   | project_title |
      | Vancouver Trip      | Toronto Trip  |
      | Camping             | Camping Trip  |

  Scenario Outline: The user successfully edits the description of the projects (Alternate Flow)
    When I change the description of "<project_title>" to "<description>"
    Then I should receive a confirmation that my operation was successful
    And Project with title "<project_title>" should have description "<description>"

    Examples:
      | project_title  | description        |
      | ECSE_429       | Software Validation |


  Scenario Outline: The user tries to edit the title of a project that does not exist (Error Flow)
    When I add edit the title "<project_title>" of a project that does not exist
    Then I should receive an error informing me that the requested resource was not found
    And Project "<project_title>" should not exist in the system

    Examples:
      | project_title   |
      | Faulty Project  |