Feature: Filter projects by category
  As a user I want to order projects by category to see more clearly which projects belong to which category.

  Background:
    Given The application is running
    And   Categories exist:
      | cat_title   | description     |
      | Personal    |                 |
      | Home        | home chores     |
      | Work        | work related    |
      | University  | university work |
    And Following projects exist:
      | p_title        | p_desc                | active | completed |
      | House remodel  | water interior plants | true   | false     |
      | Lab            | asssignment 2         | false  | true      |
      | Surprise party |  Quiz 1               | false  | false     |


  Scenario Outline: The user successfully assigns a non existing project for the chosen category (Normal Flow)
    Given The category "<cat_title>" exists
    And the project "<p_title>" does not exist
    When I add the new project "<p_title>" with the description "<p_desc>"under the category "<cat_title>"
    Then I should receive a confirmation that my operation was successful
    And The project "<p_title>" under category "<cat_title>" should show
    And The project "<p_title>" should show

    Examples:
      | p_title           | p_desc                | cat_title |
      | Personal website  | blog website          | Personal  |
      | Internship prep   | leetcode questions    | Work      |
      | Birthday party    | Friend Birthday       | Home      |

  Scenario Outline: The user successfully assigns a non existing project with only the title for the chosen category
  (Alternate Flow)
    Given The category "<cat_title>" exists
    And the project "<p_title>" does not exist
    When I add the new project "<p_title>" under the category "<cat_title>"
    Then I should receive a confirmation that my operation was successful
    And The project "<p_title>" under category "<cat_title>" should show
    And The project "<p_title>" should show

    Examples:
      | cat_title | p_title          |
      | Personal  | Personal website |
      | Work      | Internship prep  |
      | Home      | Birthday party   |

  Scenario Outline: The user successfully assigns an existing project
  for the chosen category (Alternate Flow)
    Given The category "<cat_title>" exists
    And The project "<p_title>" exists
    When I add a project "<p_title>" under the category "<cat_title>"
    Then I should receive a confirmation that my operation was successful
    And The project "<p_title>" under category "<cat_title>" should show

    Examples:
      | cat_title   | p_title         |
      | Home        | House remodel   |
      | Personal    | Surprise party  |
      | University  | Lab             |

  Scenario Outline: The user attempts to assign a non existing project without title for a non existing category
  (Error Flow)
    Given The category "<cat_title>" does not exist
    When I add the new project with description "<description>" under the category "<cat_title>"
    Then I should receive an error informing me that the requested resource was not found

    Examples:
      | cat_title       | description  |
      | Extracurricular | home chores  |
      | Uni             | blog website |


