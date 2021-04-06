package uk.co.compendiumdev.acceptance.cucumber;

import cucumber.api.DataTable;
import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import gherkin.formatter.model.DataTableRow;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.String;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CategoriesStepDefinitions {

    private static final String ALL_CATEGORIES_PATH = "/categories";
    private static final String SPECIFIC_CATEGORIES_PATH = "/categories/{id}";
    private static final String CATEGORY_TO_TODO_PATH = "/categories/{id}/todos";
    private static final String CATEGORY_TO_PROJECT_PATH = "/categories/{id}/projects";
    private static final String PROJECT_TO_CATEGORY_PATH = "/projects/{id}/categories";


    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String DESC = "description";
    private static final String TODOS = "todos";

    public static final Map<String, String> categories = new HashMap<>();

    @And("^Categories exist:$")
    public void categoriesExist(DataTable table) {

        List<DataTableRow> rows = table.getGherkinRows();

        for(int i = 1; i<rows.size() ; i++){
            List<String> cells = rows.get(i).getCells();

            String category = cells.get(0);
            String desc = cells.get(1);

            final HashMap<String, Object> givenBody = new HashMap<>();
            givenBody.put(TITLE, category);
            givenBody.put(DESC, desc);

            String id = given().
                    body(givenBody).
                    when().
                    post(ALL_CATEGORIES_PATH).
                    then().
                    contentType(ContentType.JSON).
                    statusCode(HttpStatus.SC_CREATED).
                    body(
                            TITLE, equalTo(category),
                            DESC, equalTo(desc)
                    ).
                    extract().
                    path(ID);

            categories.put(category, id);
        }
    }

    @When("^I add a category title\"([^\"]*)\" with \"([^\"]*)\" as description$")
    public void iAddACategoryTitleWithAsDescription(String arg0, String arg1) throws Throwable {

        final HashMap<String, Object> givenBody = new HashMap<>();
        // Create ctaegory
        givenBody.put(TITLE, arg0);
        givenBody.put(DESC, arg1);

        AppRunningStepDefinition.lastResponse.addFirst(
                given().
                        body(givenBody).
                        when().
                        post(ALL_CATEGORIES_PATH).
                        then().
                        contentType(ContentType.JSON).
                        statusCode(HttpStatus.SC_CREATED).
                        body(
                                TITLE, equalTo(arg0),
                                DESC, equalTo(arg1)
                        ).
                        extract()
        );

        String catId = AppRunningStepDefinition.lastResponse.getFirst().path(ID);

        CategoriesStepDefinitions.categories.put(arg0, catId);
        //throw new PendingException();
    }

    @And("^Category \"([^\"]*)\" with description \"([^\"]*)\" should show$")
    public void categoryWithDescriptionShouldShow(String arg0, String arg1) throws Throwable {
        List<Map<String, Object>> categories =
                given().
                        pathParam(ID, CategoriesStepDefinitions.categories.get(arg0)).
                        when().
                        get(SPECIFIC_CATEGORIES_PATH).
                        then().
                        statusCode(HttpStatus.SC_OK).
                        contentType(ContentType.JSON).
                        extract().
                        body().
                        jsonPath().
                        getList("categories");

        Assertions.assertTrue(categories.stream().allMatch(object -> object.get("title").equals(arg0)));
        Assertions.assertTrue(categories.stream().allMatch(object -> object.get("description").equals(arg1)));
    }

    @When("^I add a category title\"([^\"]*)\"$")
    public void iAddACategoryTitle(String arg0) throws Throwable {
        final HashMap<String, Object> givenBody = new HashMap<>();
        // Create category
        givenBody.put(TITLE, arg0);

        AppRunningStepDefinition.lastResponse.addFirst(
                given().
                        body(givenBody).
                        when().
                        post(ALL_CATEGORIES_PATH).
                        then().
                        contentType(ContentType.JSON).
                        statusCode(HttpStatus.SC_CREATED).
                        body(
                                TITLE, equalTo(arg0)
                        ).
                        extract()
        );

        String catId = AppRunningStepDefinition.lastResponse.getFirst().path(ID);

        CategoriesStepDefinitions.categories.put(arg0, catId);
        //throw new PendingException();
    }

    @And("^Category \"([^\"]*)\"  should show$")
    public void categoryShouldShow(String arg0) throws Throwable {
        List<Map<String, Object>> categories =
                given().
                        pathParam(ID, CategoriesStepDefinitions.categories.get(arg0)).
                        when().
                        get(SPECIFIC_CATEGORIES_PATH).
                        then().
                        statusCode(HttpStatus.SC_OK).
                        contentType(ContentType.JSON).
                        extract().
                        body().
                        jsonPath().
                        getList("categories");

        Assertions.assertTrue(categories.stream().allMatch(object -> object.get("title").equals(arg0)));
    }

    @When("^I add a category with only a description \"([^\"]*)\"$")
    public void iAddACategoryWithOnlyADescription(String arg0) throws Throwable {

        final HashMap<String, Object> givenBody = new HashMap<>();
        // Create category
        givenBody.put(DESC, arg0);

        AppRunningStepDefinition.lastResponse.addFirst(
                given().
                        body(givenBody).
                        when().
                        post(ALL_CATEGORIES_PATH).
                        then().
                        contentType(ContentType.JSON).
                        statusCode(HttpStatus.SC_BAD_REQUEST).
                        extract()
        );
    }

    @Given("^The category \"([^\"]*)\" exists$")
    public void theCategoryExists(String arg0) throws Throwable {
        List<Map<String, Object>> categories =
                given().
                        pathParam(ID, CategoriesStepDefinitions.categories.get(arg0)).
                        when().
                        get(SPECIFIC_CATEGORIES_PATH).
                        then().
                        statusCode(HttpStatus.SC_OK).
                        contentType(ContentType.JSON).
                        extract().
                        body().
                        jsonPath().
                        getList("categories");

        Assertions.assertTrue(categories.stream().allMatch(object -> object.get("title").equals(arg0)));
    }

    @When("^I change the category title \"([^\"]*)\" to \"([^\"]*)\"$")
    public void iChangeTheCategoryTitleTo(String arg0, String arg1) throws Throwable {

        final  HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(TITLE, arg1);

        AppRunningStepDefinition.lastResponse.addFirst(
                given().
                        pathParam(ID, CategoriesStepDefinitions.categories.getOrDefault(arg0, "-1")).
                        body(givenBody).
                        when().
                        put(SPECIFIC_CATEGORIES_PATH).
                        then().
                        contentType(ContentType.JSON).
                        statusCode(HttpStatus.SC_OK).
                        extract()
        );
    }

    @And("^Category \"([^\"]*)\" with updated title \"([^\"]*)\" should show$")
    public void categoryWithUpdatedTitleShouldShow(String arg0, String arg1) throws Throwable {
        List<Map<String, Object>> categories =
                given().
                        pathParam(ID, CategoriesStepDefinitions.categories.get(arg0)).
                        when().
                        get(SPECIFIC_CATEGORIES_PATH).
                        then().
                        statusCode(HttpStatus.SC_OK).
                        contentType(ContentType.JSON).
                        extract().
                        body().
                        jsonPath().
                        getList("categories");

        String catId = AppRunningStepDefinition.lastResponse.getFirst().path(ID);
        Assertions.assertTrue(categories.stream().allMatch(object -> object.get("title").equals(arg1)));
        Assertions.assertEquals(CategoriesStepDefinitions.categories.get(arg0), catId);
    }

    @When("^I change the category title \"([^\"]*)\" to \"([^\"]*)\" and the description to \"([^\"]*)\"$")
    public void iChangeTheCategoryTitleToAndTheDescriptionTo(String arg0, String arg1, String arg2) throws Throwable {
        final  HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(TITLE, arg1);
        givenBody.put(DESC, arg2);

        AppRunningStepDefinition.lastResponse.addFirst(
                given().
                        pathParam(ID, CategoriesStepDefinitions.categories.get(arg0)).
                        body(givenBody).
                        when().
                        put(SPECIFIC_CATEGORIES_PATH).
                        then().
                        contentType(ContentType.JSON).
                        statusCode(HttpStatus.SC_OK).
                        extract()
        );
    }

    @And("^Category \"([^\"]*)\" with updated title \"([^\"]*)\" and description \"([^\"]*)\" should show$")
    public void categoryWithUpdatedTitleAndDescriptionShouldShow(String arg0, String arg1, String arg2) throws Throwable {
        List<Map<String, Object>> categories =
                given().
                        pathParam(ID, CategoriesStepDefinitions.categories.get(arg0)).
                        when().
                        get(SPECIFIC_CATEGORIES_PATH).
                        then().
                        statusCode(HttpStatus.SC_OK).
                        contentType(ContentType.JSON).
                        extract().
                        body().
                        jsonPath().
                        getList("categories");


        String catId = AppRunningStepDefinition.lastResponse.getFirst().path(ID);
        Assertions.assertTrue(categories.stream().allMatch(object -> object.get("title").equals(arg1)));
        Assertions.assertTrue(categories.stream().allMatch(object -> object.get("description").equals(arg2)));
        Assertions.assertEquals(CategoriesStepDefinitions.categories.get(arg0), catId);
    }

    @Given("^The category \"([^\"]*)\" does not exist$")
    public void theCategoryDoesNotExist(String arg0) throws Throwable {

        Assertions.assertEquals(CategoriesStepDefinitions.categories.getOrDefault(arg0, "Does not exist"), "Does not exist");
    }

    @When("^I try to change the non existent category title \"([^\"]*)\" to \"([^\"]*)\"$")
    public void iTryToChangeTheNonExistentCategoryTitleTo(String arg0, String arg1) throws Throwable {
        final  HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(TITLE, arg1);

        AppRunningStepDefinition.lastResponse.addFirst(
                given().
                        pathParam(ID, CategoriesStepDefinitions.categories.getOrDefault(arg0, "10000")).
                        body(givenBody).
                        when().
                        put(SPECIFIC_CATEGORIES_PATH).
                        then().
                        contentType(ContentType.JSON).
                        statusCode(HttpStatus.SC_NOT_FOUND).
                        extract()
        );
    }

    @When("^I add a Todo \"([^\"]*)\" under the category \"([^\"]*)\"$")
    public void iAddATodoUnderTheCategory(String arg0, String arg1) throws Throwable {

        final HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(ID, TodosStepDefinitions.todos.get(arg0));

        AppRunningStepDefinition.lastResponse.addFirst(
                given().
                        pathParam(ID, CategoriesStepDefinitions.categories.getOrDefault(arg1, "-1")).
                        body(givenBody).
                        when().
                        post(CATEGORY_TO_TODO_PATH)
                        .then().extract()
        );

    }

    @And("^The Todo \"([^\"]*)\" under category \"([^\"]*)\" should show$")
    public void theTodoUnderCategoryShouldShow(String arg0, String arg1) throws Throwable {

        List<Map<String, Object>> todos =
                given().
                        pathParam(ID, CategoriesStepDefinitions.categories.get(arg1)).
                        when().
                        get(CATEGORY_TO_TODO_PATH).
                        then().
                        statusCode(HttpStatus.SC_OK).
                        contentType(ContentType.JSON).
                        extract().
                        body().
                        jsonPath().
                        getList("todos");

        Assertions.assertTrue(todos.stream().allMatch(object -> object.get("title").equals(arg0)));
    }

    @When("^I add the new Todo \"([^\"]*)\" under the category \"([^\"]*)\"$")
    public void iAddTheNewTodoUnderTheCategory(String arg0, String arg1) throws Throwable {

        final HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(TITLE, arg0);

        AppRunningStepDefinition.lastResponse.addFirst(
                given().
                        pathParam(ID, CategoriesStepDefinitions.categories.getOrDefault(arg1, "-1")).
                        body(givenBody).
                        when().
                        post(CATEGORY_TO_TODO_PATH)
                        .then().extract()
        );
    }

    @When("^I add the new Todo with description \"([^\"]*)\" under the category \"([^\"]*)\"$")
    public void iAddTheNewTodoWithDescriptionUnderTheCategory(String arg0, String arg1) throws Throwable {

        final HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(DESC, arg0);

        AppRunningStepDefinition.lastResponse.addFirst(
                given().
                        pathParam(ID, CategoriesStepDefinitions.categories.getOrDefault(arg1, "-1")).
                        body(givenBody).
                        when().
                        post(CATEGORY_TO_TODO_PATH)
                        .then().extract()
        );
    }

    @When("^I add the new Todo \"([^\"]*)\" with status \"([^\"]*)\" and \"([^\"]*)\"under the category \"([^\"]*)\"$")
    public void iAddTheNewTodoWithStatusAndUnderTheCategory(String arg0, String arg1, String arg2, String arg3) throws Throwable {

        final HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(TITLE, arg0);

        if (arg1.equals("true")) {
            givenBody.put("doneStatus", true);
        } else if (arg1.equals("false")) {
            givenBody.put("doneStatus", false);
        }
        givenBody.put(DESC, arg2);

        AppRunningStepDefinition.lastResponse.addFirst(
                given().
                        pathParam(ID, CategoriesStepDefinitions.categories.getOrDefault(arg3, "-1")).
                        body(givenBody).
                        when().
                        post(CATEGORY_TO_TODO_PATH)
                        .then().extract()
        );
    }

    @When("^I add the new project \"([^\"]*)\" with the description \"([^\"]*)\"under the category \"([^\"]*)\"$")
    public void iAddTheNewProjectWithTheDescriptionUnderTheCategory(String arg0, String arg1, String arg2) throws Throwable {
        final HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(TITLE, arg0);


        givenBody.put(DESC, arg1);

        AppRunningStepDefinition.lastResponse.addFirst(
                given().
                        pathParam(ID, CategoriesStepDefinitions.categories.getOrDefault(arg2, "-1")).
                        body(givenBody).
                        when().
                        post(CATEGORY_TO_PROJECT_PATH)
                        .then().extract()
        );
    }

    @And("^The project \"([^\"]*)\" under category \"([^\"]*)\" should show$")
    public void theProjectUnderCategoryShouldShow(String arg0, String arg1) throws Throwable {

        List<Map<String, Object>> projects =
                given().
                        pathParam(ID, CategoriesStepDefinitions.categories.get(arg1)).
                        when().
                        get(CATEGORY_TO_PROJECT_PATH).
                        then().
                        statusCode(HttpStatus.SC_OK).
                        contentType(ContentType.JSON).
                        extract().
                        body().
                        jsonPath().
                        getList("projects");

        Assertions.assertTrue(projects.stream().allMatch(object -> object.get("title").equals(arg0)));
    }

    @When("^I add the new project \"([^\"]*)\" under the category \"([^\"]*)\"$")
    public void iAddTheNewProjectUnderTheCategory(String arg0, String arg1) throws Throwable {

        final HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(TITLE, arg0);

        AppRunningStepDefinition.lastResponse.addFirst(
                given().
                        pathParam(ID, CategoriesStepDefinitions.categories.getOrDefault(arg1, "-1")).
                        body(givenBody).
                        when().
                        post(CATEGORY_TO_PROJECT_PATH)
                        .then().extract()
        );
    }

    @When("^I add a project \"([^\"]*)\" under the category \"([^\"]*)\"$")
    public void iAddAProjectUnderTheCategory(String arg0, String arg1) throws Throwable {

        final HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(ID, ProjectsStepDefinition.projects.get(arg0));

        AppRunningStepDefinition.lastResponse.addFirst(
                given().
                        pathParam(ID, CategoriesStepDefinitions.categories.getOrDefault(arg1, "-1")).
                        body(givenBody).
                        when().
                        post(CATEGORY_TO_PROJECT_PATH)
                        .then().extract()
        );
    }

    @When("^I add the new project with description \"([^\"]*)\" under the category \"([^\"]*)\"$")
    public void iAddTheNewProjectWithDescriptionUnderTheCategory(String arg0, String arg1) throws Throwable {

        final HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(DESC, arg0);

        AppRunningStepDefinition.lastResponse.addFirst(
                given().
                        pathParam(ID, CategoriesStepDefinitions.categories.getOrDefault(arg1, "-1")).
                        body(givenBody).
                        when().
                        post(CATEGORY_TO_PROJECT_PATH)
                        .then().extract()
        );
    }

    @When("^I remove a category with title \"([^\"]*)\"$")
    public void iRemoveACategoryWithTitle(String arg0) throws Throwable {
        AppRunningStepDefinition.lastResponse.addFirst(
                given().
                        pathParam(ID, CategoriesStepDefinitions.categories.getOrDefault(arg0, "-1")).
                        when().
                        delete(SPECIFIC_CATEGORIES_PATH)
                        .then()
                        .extract()
        );
        CategoriesStepDefinitions.categories.remove(arg0);
    }

    @And("^Category \"([^\"]*)\" should not show$")
    public void categoryShouldNotShow(String arg0) throws Throwable {
        Assertions.assertEquals(CategoriesStepDefinitions.categories.getOrDefault(arg0, "Does not exist"), "Does not exist");
    }

    @And("^categories to a project exist:$")
    public void categoriesToAProjectExist(DataTable table) {

        List<DataTableRow> rows = table.getGherkinRows();

        for(int i = 1; i<rows.size() ; i++){
            List<String> cells = rows.get(i).getCells();

            String category = cells.get(0);
            String project = cells.get(1);

            final HashMap<String, Object> givenBody = new HashMap<>();
            givenBody.put(ID, CategoriesStepDefinitions.categories.get(category));

            AppRunningStepDefinition.lastResponse.addFirst(
                    given().
                            pathParam(ID, ProjectsStepDefinition.projects.getOrDefault(project, "-1")).
                            body(givenBody).
                            when().
                            post(PROJECT_TO_CATEGORY_PATH)
                            .then().extract()
            );
        }
    }

    @And("^Category \"([^\"]*)\" for project \"([^\"]*)\" should not show$")
    public void categoryForProjectShouldNotShow(String arg0, String arg1) throws Throwable {
        List<Map<String, Object>> cats =
                given().
                        pathParam(ID, ProjectsStepDefinition.projects.get(arg1)).
                        when().
                        get(PROJECT_TO_CATEGORY_PATH).
                        then().
                        statusCode(HttpStatus.SC_OK).
                        contentType(ContentType.JSON).
                        extract().
                        body().
                        jsonPath().
                        getList("categories");

        Assertions.assertTrue(cats.isEmpty());
    }

    @Given("^The category \"([^\"]*)\" related to the project \"([^\"]*)\" exists$")
    public void theCategoryRelatedToTheProjectExists(String arg0, String arg1) throws Throwable {
        List<Map<String, Object>> projects =
                given().
                        pathParam(ID, ProjectsStepDefinition.projects.get(arg1)).
                        when().
                        get(PROJECT_TO_CATEGORY_PATH).
                        then().
                        statusCode(HttpStatus.SC_OK).
                        contentType(ContentType.JSON).
                        extract().
                        body().
                        jsonPath().
                        getList("categories");

        Assertions.assertTrue(projects.stream().allMatch(object -> object.get("title").equals(arg0)));
    }

    @When("^I remove a category with title\"([^\"]*)\" related to the project \"([^\"]*)\"$")
    public void iRemoveACategoryWithTitleRelatedToTheProject(String arg0, String arg1) throws Throwable {
        String cat = CategoriesStepDefinitions.categories.get(arg0);
        String project = ProjectsStepDefinition.projects.get(arg1);
        AppRunningStepDefinition.lastResponse.addFirst(
                given().
                        when().
                        delete("/projects/"+project+"/categories/"+cat)
                        .then()
                        .extract()
        );
    }
}
