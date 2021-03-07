Feature: Create Project
  As a user, I want to create a project for my trip to Toronto, so I can plan my activities there.

  Background:
  Given The application is running
  And Following projects exist:
    | project_title  | description | active | completed |
    | Vancouver Trip |             | false  | false     |
    | Camping        |             | false  | false     |
    | ECSE_429       |             | false  | false     |

  Scenario Outline: The user successfully adds new project with the title and description (Normal Flow)
    When I add a project with title "<project_title>" and "<description>" as description and "<active>" active status and "<completed>" completed status
    Then I should receive a confirmation that my operation was successful
    And Project with title "<project_title>" with description "<description>" should exist
    And The project should have active status "<active>" and completed status "<completed>"

    Examples:
    | project_title   | description               | active  | completed |
    | Toronto Trip    | Plans for Toronto         | false   | false     |
    | Vancouver Trip  | Plans for Vancouver       | false   | true      |
    | Hackathon       | Plans for Hackathon       | true    | true      |
    | Interview       | Preparation for Interview | false   | false     |

  Scenario Outline: The user successfully adds a new project with title and no description (Alternate Flow)
    When I add a a project with title "<project_title>" and "<active>" active status and "<completed>" completed status
    Then I should receive a confirmation that my operation was successful
    And Project with title "<project_title>" should exist
    And The project should have active status "<active>" and completed status "<completed>"


    Examples:
      | project_title  | active | completed |
      | Toronto Trip   | false  | false     |
      | Vancouver Trip | false  | true      |
      | Hackathon      | true   | true      |
      | Interview      | false  | false     |


  Scenario Outline: The user tries to add a project with an invalid active status (Error Flow)
    When I add a project with title "<project_title>" and "<description>" as description and "<active>" active status and "<completed>" completed status
    Then I should receive an error informing me that the passed information was invalid
    And Project "<project_title>" should not exist in the system

    Examples:
      | project_title   | description       | active  | completed |
      | Faulty Project  | Plans for Toronto | almost  | false     |






