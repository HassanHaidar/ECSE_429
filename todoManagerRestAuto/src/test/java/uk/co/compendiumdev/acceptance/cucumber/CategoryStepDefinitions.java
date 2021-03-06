package uk.co.compendiumdev.acceptance.cucumber;

import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.When;
import gherkin.formatter.model.DataTableRow;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CategoryStepDefinitions {

    private static final String ALL_CATEGORIES_PATH = "/categories";
    private static final String SPECIFIC_CATEGORIES_PATH = "/categories/{id}";
    private static final String CATEGORY_TO_TODO_PATH = "/categories/{id}/todos";
    private static final String TODO_TO_CATEGORY_PATH = "/todos/{id}/categories";


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

        CategoryStepDefinitions.categories.put(arg0, catId);
        //throw new PendingException();
    }

    @And("^Category \"([^\"]*)\" with description \"([^\"]*)\" should show$")
    public void categoryWithDescriptionShouldShow(String arg0, String arg1) throws Throwable {
        List<Map<String, Object>> categories =
                given().
                        pathParam(ID, CategoryStepDefinitions.categories.get(arg0)).
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

        CategoryStepDefinitions.categories.put(arg0, catId);
        //throw new PendingException();
    }

    @And("^Category \"([^\"]*)\"  should show$")
    public void categoryShouldShow(String arg0) throws Throwable {
        List<Map<String, Object>> categories =
                given().
                        pathParam(ID, CategoryStepDefinitions.categories.get(arg0)).
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
}
