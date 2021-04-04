Feature: Remove a Project
  As a user I want to remove projects to mark that they are finished.

  Background:
    Given The application is running
    And Following projects exist:
      | project_title  | description | active | completed |
      | Toronto Trip   |             | false  | false     |
      | Vancouver Trip |             | false  | false     |
      | ECSE_429       |             | false  | false     |
      | ECSE_428       |             | false  | false     |

    And project to tasks exist:
      | project_title   | task_title      |
      | Toronto Trip    | Niagara Falls   |
      | Vancouver Trip  | Friends Meetup  |

  Scenario Outline: The user successfully removes an existing project with no todos (Normal Flow)
    When I remove a project with title "<project_title>"
    Then I should receive a confirmation that my operation was successful
    And Project "<project_title>" should not show

    Examples:
      | project_title  |
      | ECSE_429       |
      | ECSE_428       |

  Scenario Outline: The user successfully removes a project related to a todo (Alternate Flow)
    When I remove a project with title "<project_title>"
    Then I should receive a confirmation that my operation was successful
    And Task "<task_title>" exists for project "<project_title>" should not show

    Examples:
      | project_title   | task_title      |
      | Toronto Trip    | Niagara Falls   |
      | Vancouver Trip  | Friends Meetup  |

  Scenario Outline: The user attempts to delete a project that does not exist (Error Flow)
    When I remove a non-existant project with title "<project_title>"
    Then I should receive an error informing me that the requested resource was not found

    Examples:
      | project_title   |
      | Faulty project  |


