Feature: Remove a Category
  As a user I want to remove a category to delete any categories that I do not need anymore.

  Background:
    Given The application is running
    And Categories exist:
      | cat_title  | description     |
      | Home       | home chores     |
      | Work       | work related    |
      | University | university work |
    And Following projects exist:
      | p_title        | p_desc                | active | completed |
      | House remodel  | water interior plants | true   | false     |
      | Lab            | asssignment 2         | false  | true      |
      | Surprise party |  Quiz 1               | false  | false     |
    And categories to a project exist:
      | cat_title   | p_title         |
      | Home        | House remodel   |
      | Personal    | Surprise party  |
      | University  | Lab             |

  Scenario Outline: The user successfully removes an existing category with no projects or todos (Normal Flow)
    Given The category "<cat_title>" exists
    When I remove a category with title "<cat_title>"
    Then I should receive a confirmation that my operation was successful
    And Category "<cat_title>" should not show

    Examples:
      | cat_title  |
      | Home       |
      | Work       |
      | University |

  Scenario Outline: The user successfully removes a category related to a project (Alternate Flow)
    Given The category "<cat_title>" related to the project "<pname>" exists
    When I remove a category with title"<cat_title>" related to the project "<pname>"
    Then I should receive a confirmation that my operation was successful
    And Category "<cat_title>" for project "<pname>" should not show

    Examples:
      | cat_title   | pname         |
      | Home        | House remodel   |
      | Personal    | Surprise party  |
      | University  | Lab             |

  Scenario Outline: The user attempts to delete a category that does not exist (Error Flow)
    Given The category "<cat_title>" does not exist
    When I remove a category with title "<cat_title>"
    Then I should receive an error informing me that the requested resource was not found

    Examples:
      | cat_title |
      | School    |
      | Vacation  |
      | Winter    |


