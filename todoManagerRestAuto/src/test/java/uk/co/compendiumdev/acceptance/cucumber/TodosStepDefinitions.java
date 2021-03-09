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
    private static final String TODO_TO_PROJECT_PATH = "/todos/{id}/tasksof";
    private static final String TODO_TO_SPECIFIC_PROJECT_PATH = "/todos/{id}/tasksof/{id}";
    private static final String PROJECT_TO_TODO_PATH = "/projects/{id}/tasks";

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

    @Given("^The todo \"([^\"]*)\" exists$")
    @And("^The Todo \"([^\"]*)\" exists$")
    public void theTodoExists(String arg0) throws Throwable {
        List<Map<String, Object>> todos =
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

        Assertions.assertTrue(todos.stream().allMatch(object -> object.get("title").equals(arg0)));
    }

    @And("^the Todo \"([^\"]*)\" does not exist$")
    @Given("^The todo \"([^\"]*)\" does not exist$")
    public void theTodoDoesNotExist(String arg0) throws Throwable {

        Assertions.assertEquals(TodosStepDefinitions.todos.getOrDefault(arg0, "Does not exist"), "Does not exist");
    }

    @And("^The Todo \"([^\"]*)\" should show$")
    public void theTodoShouldShow(String arg0) throws Throwable {
        List<Map<String, Object>> todos =
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

        Assertions.assertTrue(todos.stream().allMatch(object -> object.get("title").equals(arg0)));
    }



    @When("^I add a todo \"([^\"]*)\"$")
    public void iAddATodo(String arg0) throws Throwable {
        final HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(TITLE, arg0);

        AppRunningStepDefinition.lastResponse.addFirst(
                given().
                        body(givenBody).
                        when().
                        post(ALL_TODOS_PATH).
                        then().
                        contentType(ContentType.JSON).
                        statusCode(HttpStatus.SC_CREATED).
                        body(
                                TITLE, equalTo(arg0)
                        ).
                        extract()
        );

        String catId = AppRunningStepDefinition.lastResponse.getFirst().path(ID);

        todos.put(arg0, catId);
        //throw new PendingException();
    }

    @And("^Todo \"([^\"]*)\" with description \"([^\"]*)\" and status \"([^\"]*)\" should show$")
    public void todoWithDescriptionAndStatusShouldShow(String arg0, String arg1, String arg2) throws Throwable {
        List<Map<String, Object>> todos =
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

        Assertions.assertTrue(todos.stream().allMatch(object -> object.get("title").equals(arg0)));
        Assertions.assertTrue(todos.stream().allMatch(object -> object.get("description").equals(arg1)));
    }

    @When("^I add a todo title\"([^\"]*)\" with \"([^\"]*)\" as description and status \"([^\"]*)\"$")
    public void iAddATodoTitleWithAsDescriptionAndStatus(String arg0, String arg1, String arg2) throws Throwable {
        final HashMap<String, Object> givenBody = new HashMap<>();


        givenBody.put(TITLE, arg0);
        if (arg2.equals("true")) {
            givenBody.put("doneStatus", true);
        } else if (arg2.equals("false")) {
            givenBody.put("doneStatus", false);
        }
        givenBody.put(DESC, arg1);

        AppRunningStepDefinition.lastResponse.addFirst(
                given().
                        body(givenBody).
                        when().
                        post(ALL_TODOS_PATH).
                        then().
                        contentType(ContentType.JSON).
                        statusCode(HttpStatus.SC_CREATED).
                        body(
                                TITLE, equalTo(arg0),
                                STATUS, equalTo(arg2),
                                DESC, equalTo(arg1)
                        ).
                        extract()
        );

        String todoId = AppRunningStepDefinition.lastResponse.getFirst().path(ID);

        todos.put(arg0, todoId);
        //throw new PendingException();
    }

    @When("^I add todo \"([^\"]*)\" with \"([^\"]*)\" as description and status \"([^\"]*)\"$")
    public void iAddTodoWithAsDescriptionAndStatus(String arg0, String arg1, String arg2) throws Throwable {
            final HashMap<String, Object> givenBody = new HashMap<>();


            givenBody.put(TITLE, arg0);
            if (arg2.equals("true")) {
                givenBody.put("doneStatus", true);
            } else if (arg2.equals("false")) {
                givenBody.put("doneStatus", false);
            }
            givenBody.put(DESC, arg1);

            AppRunningStepDefinition.lastResponse.addFirst(given().
                    body(givenBody).
                    when().
                    post(ALL_TODOS_PATH).
                    then().
                    contentType(ContentType.JSON).
                    statusCode(HttpStatus.SC_BAD_REQUEST).extract());




    }

    @And("^todos to a project exist:$")
    public void todosToAProjectExist(DataTable table) {
        List<DataTableRow> rows = table.getGherkinRows();

        String projectId = "";

        for (int i = 1; i < rows.size(); i++) {
            List<String> cells = rows.get(i).getCells();

            String projectTitle = cells.get(1);

            projectId = ProjectsStepDefinition.projects.get(projectTitle);

            String taskTitle = cells.get(0);

            final HashMap<String, Object> givenBody = new HashMap<>();
            givenBody.put(TITLE, taskTitle);

            String id = given().
                    pathParam(ID, projectId).
                    body(givenBody).
                    when().
                    post(PROJECT_TO_TODO_PATH).
                    then().
                    contentType(ContentType.JSON).
                    statusCode(HttpStatus.SC_CREATED).
                    body(
                            TITLE, equalTo(taskTitle)
                    ).
                    extract().path(ID);
            todos.put(taskTitle, id);
        }
    }

    @When("^I remove a todo with title \"([^\"]*)\"$")
    public void iRemoveATodoWithTitle(String arg0) throws Throwable {
        String todoId =todos.get(arg0);
        System.out.println(todoId);
        AppRunningStepDefinition.lastResponse.addFirst(
                given().
                        pathParam(ID, todoId).
                        when().
                        delete(SPECIFIC_TODOS_PATH).
                        then().
                        statusCode(HttpStatus.SC_OK).
                        contentType(ContentType.JSON).extract()
        );
    }

    @And("^todo \"([^\"]*)\" should not show$")
    public void todoShouldNotShow(String arg0) throws Throwable {
        List<Map<String, Object>> todos =
                given().
                        when().
                        get(ALL_TODOS_PATH).
                        then().
                        statusCode(HttpStatus.SC_OK).
                        contentType(ContentType.JSON).
                        extract().
                        body().
                        jsonPath().
                        getList("todos");

        assertTrue(todos.stream().noneMatch(
                todo -> todo.get(TITLE).equals(arg0)
                )
        );
    }

    @When("^I remove a non-existent todo with title \"([^\"]*)\"$")
    public void iRemoveANonExistentTodoWithTitle(String arg0) throws Throwable {
        AppRunningStepDefinition.lastResponse.addFirst(
                given().
                        pathParam(ID, "13927488").
                        when().
                        delete(SPECIFIC_TODOS_PATH).
                        then().
                        statusCode(HttpStatus.SC_NOT_FOUND).
                        contentType(ContentType.JSON).extract()
        );
    }



    @And("^todo \"([^\"]*)\" for project \"([^\"]*)\" should not show$")
    public void todoForProjectShouldNotShow(String arg0, String arg1) throws Throwable {
        String projectId = ProjectsStepDefinition.projects.get(arg1);
        List<Map<String, Object>> tasks =
                given().
                        pathParam(ID, projectId)
                        .when().get(PROJECT_TO_TODO_PATH).
                        then().
                        statusCode(HttpStatus.SC_OK).
                        contentType(ContentType.JSON).
                        extract().
                        body().
                        jsonPath().getList("todos");
        System.out.println(tasks.size());
        System.out.println(tasks.toString());
        assertTrue(tasks.size() == 0 );
    }

    @When("^I try to change the non existent todo title \"([^\"]*)\" to \"([^\"]*)\"$")
    public void iTryToChangeTheNonExistentTodoTitleTo(String arg0, String arg1) throws Throwable {
        final  HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(TITLE, arg1);

        AppRunningStepDefinition.lastResponse.addFirst(
                given().
                        pathParam(ID, TodosStepDefinitions.todos.getOrDefault(arg0, "10000")).
                        body(givenBody).
                        when().
                        put(SPECIFIC_TODOS_PATH).
                        then().
                        contentType(ContentType.JSON).
                        statusCode(HttpStatus.SC_BAD_REQUEST).
                        extract()
        );
    }

    @When("^I change the todo title \"([^\"]*)\" to \"([^\"]*)\"$")
    public void iChangeTheTodoTitleTo(String arg0, String arg1) throws Throwable {
        final  HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(TITLE, arg1);

        AppRunningStepDefinition.lastResponse.addFirst(
                given().
                        pathParam(ID,TodosStepDefinitions.todos.getOrDefault(arg0, "-1")).
                        body(givenBody).
                        when().
                        put(SPECIFIC_TODOS_PATH).
                        then().
                        contentType(ContentType.JSON).
                        statusCode(HttpStatus.SC_OK).
                        extract()
        );
    }

    @And("^todo \"([^\"]*)\" with updated title \"([^\"]*)\" should show$")
    public void todoWithUpdatedTitleShouldShow(String arg0, String arg1) throws Throwable {
        List<Map<String, Object>> todos =
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

        String todoId = AppRunningStepDefinition.lastResponse.getFirst().path(ID);
        Assertions.assertTrue(todos.stream().allMatch(object -> object.get("title").equals(arg1)));
        Assertions.assertEquals(TodosStepDefinitions.todos.get(arg0), todoId);
    }

    @When("^I change the todo title \"([^\"]*)\" to \"([^\"]*)\" and the description to \"([^\"]*)\"$")
    public void iChangeTheTodoTitleToAndTheDescriptionTo(String arg0, String arg1, String arg2) throws Throwable {
        final  HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(TITLE, arg1);
        givenBody.put(DESC, arg2);
        AppRunningStepDefinition.lastResponse.addFirst(
                given().
                        pathParam(ID, TodosStepDefinitions.todos.get(arg0)).
                        body(givenBody).
                        when().
                        put(SPECIFIC_TODOS_PATH).
                        then().
                        contentType(ContentType.JSON).
                        statusCode(HttpStatus.SC_OK).
                        extract());
    }

    @And("^todo \"([^\"]*)\" with updated title \"([^\"]*)\" and description \"([^\"]*)\" should show$")
    public void todoWithUpdatedTitleAndDescriptionShouldShow(String arg0, String arg1, String arg2) throws Throwable {
        List<Map<String, Object>> todos =
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

        String todoId = AppRunningStepDefinition.lastResponse.getFirst().path(ID);
        Assertions.assertTrue(todos.stream().allMatch(object -> object.get("title").equals(arg1)));
        Assertions.assertTrue(todos.stream().allMatch(object -> object.get("description").equals(arg2)));
        Assertions.assertEquals(TodosStepDefinitions.todos.get(arg0), todoId);
    }

    @And("^todo \"([^\"]*)\" should have status true and description \"([^\"]*)\" should show$")
    public void todoShouldHaveStatusTrueAndDescriptionShouldShow(String arg0, String arg1) throws Throwable {
        List<Map<String, Object>> todos =
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

        String todoId = AppRunningStepDefinition.lastResponse.getFirst().path(ID);
        Assertions.assertTrue(todos.stream().allMatch(object -> object.get(STATUS).equals("true")));
        Assertions.assertTrue(todos.stream().allMatch(object -> object.get("description").equals(arg1)));
        Assertions.assertEquals(TodosStepDefinitions.todos.get(arg0), todoId);
    }

    @When("^I change the todo title \"([^\"]*)\" to complete and the description to \"([^\"]*)\"$")
    public void iChangeTheTodoTitleToCompleteAndTheDescriptionTo(String arg0, String arg1) throws Throwable {
        final  HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(STATUS, true);
        givenBody.put(DESC, arg1);

        AppRunningStepDefinition.lastResponse.addFirst(
                given().
                        pathParam(ID,TodosStepDefinitions.todos.getOrDefault(arg0, "-1")).
                        body(givenBody).
                        when().
                        post(SPECIFIC_TODOS_PATH).
                        then().
                        contentType(ContentType.JSON).
                        statusCode(HttpStatus.SC_OK).
                        extract());
    }

    @When("^I change the todo title \"([^\"]*)\" to complete$")
    public void iChangeTheTodoTitleToComplete(String arg0) throws Throwable {
        final  HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(STATUS, true);

        AppRunningStepDefinition.lastResponse.addFirst(
                given().
                        pathParam(ID,TodosStepDefinitions.todos.getOrDefault(arg0, "-1")).
                        body(givenBody).
                        when().
                        post(SPECIFIC_TODOS_PATH).
                        then().
                        contentType(ContentType.JSON).
                        statusCode(HttpStatus.SC_OK).
                        extract());
    }

    @And("^todo \"([^\"]*)\"'s status should be true$")
    public void todoSStatusShouldBeTrue(String arg0) throws Throwable {
        List<Map<String, Object>> todos =
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

        String todoId = AppRunningStepDefinition.lastResponse.getFirst().path(ID);
        Assertions.assertTrue(todos.stream().allMatch(object -> object.get(STATUS).equals("true")));
        Assertions.assertEquals(TodosStepDefinitions.todos.get(arg0), todoId);
    }

    @When("^I try to change the status of title \"([^\"]*)\" to \"([^\"]*)\"$")
    public void iTryToChangeTheStatusOfTitleTo(String arg0, String arg1) throws Throwable {
        final  HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(STATUS, arg1);


        AppRunningStepDefinition.lastResponse.addFirst(
                given().
                        pathParam(ID, TodosStepDefinitions.todos.getOrDefault(arg0, "-1")).
                        body(givenBody).
                        when().
                        put(SPECIFIC_TODOS_PATH).
                        then().
                        contentType(ContentType.JSON).
                        statusCode(HttpStatus.SC_BAD_REQUEST).
                        extract()
        );
    }


    @And("^\"([^\"]*)\" task should have \"([^\"]*)\" project$")
    public void taskShouldHaveProject(String arg0, String arg1) throws Throwable {
        List<Map<String, Object>> tasks =
                given().
                        pathParam(ID, todos.getOrDefault(arg0, "-1"))
                        .when().get(TODO_TO_PROJECT_PATH).
                        then().
                        statusCode(HttpStatus.SC_OK).
                        contentType(ContentType.JSON).
                        extract().
                        body().
                        jsonPath().getList("projects");

        Assertions.assertTrue(tasks.size() == 1);
        Assertions.assertEquals(tasks.get(0).get("id"), ProjectsStepDefinition.projects.get(arg1));
    }

    @When("^I assign a task \"([^\"]*)\"  for existing project \"([^\"]*)\"$")
    public void iAssignATaskForExistingProject(String arg0, String arg1) throws Throwable {
        final  HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(ID, ProjectsStepDefinition.projects.get(arg1));


        AppRunningStepDefinition.lastResponse.addFirst(
                given().
                        pathParam(ID, TodosStepDefinitions.todos.getOrDefault(arg0, "-1")).
                        body(givenBody).
                        when().
                        post(TODO_TO_PROJECT_PATH).
                        then().
                        contentType(ContentType.JSON).
                        statusCode(HttpStatus.SC_CREATED).
                        extract()
        );
    }

    @When("^I assign a task \"([^\"]*)\" for non-existent project \"([^\"]*)\"$")
    public void iCreateATaskForNonExistentProject(String arg0, String arg1) throws Throwable {
        final  HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(TITLE, arg1);


        AppRunningStepDefinition.lastResponse.addFirst(
                given().
                        pathParam(ID, TodosStepDefinitions.todos.getOrDefault(arg0, "-1")).
                        body(givenBody).
                        when().
                        post(TODO_TO_PROJECT_PATH).
                        then().
                        contentType(ContentType.JSON).
                        statusCode(HttpStatus.SC_CREATED).
                        extract()
        );
    }

    @And("^\"([^\"]*)\" task should have a project with name \"([^\"]*)\"$")
    public void taskShouldHaveAProjectWithName(String arg0, String arg1) throws Throwable {
        List<Map<String, Object>> tasks =
                given().
                        pathParam(ID, todos.getOrDefault(arg0, "-1"))
                        .when().get(TODO_TO_PROJECT_PATH).
                        then().
                        statusCode(HttpStatus.SC_OK).
                        contentType(ContentType.JSON).
                        extract().
                        body().
                        jsonPath().getList("projects");

        Assertions.assertTrue(tasks.size() == 1);
        Assertions.assertEquals(tasks.get(0).get("title"), arg1);
    }
}
