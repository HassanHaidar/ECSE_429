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

public class TodosStepDefinitions {

    private static final String ALL_TODOS_PATH = "/todos";
    private static final String SPECIFIC_TODOS_PATH = "/todos/{id}";
    private static final String CATEGORY_TO_TODO_PATH = "/categories/{id}/todos";
    private static final String TODO_TO_CATEGORY_PATH = "/todos/{id}/categories";


    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String DESC = "description";
    private static final String STATUS = "doneStatus";

    public static final Map<String, String> todos = new HashMap<>();

    @And("^Todos exist:$")
    public void todosExist(DataTable table) {

        List<DataTableRow> rows = table.getGherkinRows();

        for(int i = 1; i<rows.size() ; i++) {
            List<String> cells = rows.get(i).getCells();

            String todo = cells.get(0);
            String status = cells.get(1);
            String desc = cells.get(2);

            final HashMap<String, Object> givenBody = new HashMap<>();
            givenBody.put(TITLE, todo);

            if (status.equals("true")) {
                givenBody.put(STATUS, true);
            } else if (status.equals("false")) {
                givenBody.put(STATUS, false);
            }
            givenBody.put(DESC, desc);

            String id = given().
                    body(givenBody).
                    when().
                    post(ALL_TODOS_PATH).
                    then().
                    contentType(ContentType.JSON).
                    statusCode(HttpStatus.SC_CREATED).
                    body(
                            TITLE, equalTo(todo),
                            STATUS, equalTo(status),
                            DESC, equalTo(desc)
                    ).
                    extract().
                    path(ID);

            todos.put(todo, id);
        }
    }

    @And("^The Todo \"([^\"]*)\" exists$")
    public void theTodoExists(String arg0) throws Throwable {
        List<Map<String, Object>> categories =
                given().
                        pathParam(ID, TodosStepDefinitions.todos.get(arg0)).
                        when().
                        get(SPECIFIC_TODOS_PATH).
                        then().
                        statusCode(HttpStatus.SC_OK).
                        contentType(ContentType.JSON).
                        extract().
                        body().
                        jsonPath().
                        getList("todos");

        Assertions.assertTrue(categories.stream().allMatch(object -> object.get("title").equals(arg0)));
    }

    @And("^the Todo \"([^\"]*)\" does not exist$")
    public void theTodoDoesNotExist(String arg0) throws Throwable {

        Assertions.assertEquals(TodosStepDefinitions.todos.getOrDefault(arg0, "Does not exist"), "Does not exist");
    }

    @And("^The Todo \"([^\"]*)\" should show$")
    public void theTodoShouldShow(String arg0) throws Throwable {
        List<Map<String, Object>> categories =
                given().
                        when().
                        get("/todos?title="+arg0).
                        then().
                        statusCode(HttpStatus.SC_OK).
                        contentType(ContentType.JSON).
                        extract().
                        body().
                        jsonPath().
                        getList("todos");

        Assertions.assertTrue(categories.stream().allMatch(object -> object.get("title").equals(arg0)));
    }
}
