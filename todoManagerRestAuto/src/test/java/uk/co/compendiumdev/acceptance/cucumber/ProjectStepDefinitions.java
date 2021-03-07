package uk.co.compendiumdev.acceptance.cucumber;

import cucumber.api.DataTable;
import cucumber.api.PendingException;
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

public class ProjectStepDefinitions {
    private static final String PROJECT_CATEGORIES = "projects/{id}/categories";
    private static final String PROJECT_CATEGORIES_ID = "projects/{id}/categories/{categoryId}";

    private static final String PROJECT_TASKS = "projects/{id}/tasks";
    private static final String PROJECT_TASKS_ID = "projects/{id}/tasks/{taskId}";


    private static final String ID_FIELD = "id";
    private static final String TITLE_FIELD = "title";
    private static final String DESCRIPTION_FIELD = "description";
    private static final String ACTIVE_FIELD = "active";
    private static final String COMPLETED_FIELD = "completed";

    public static final Map<String, String> projects = new HashMap<>();

    @And("^Projects exist:$")
    public void ProjectsExist(DataTable table) {

        List<DataTableRow> rows = table.getGherkinRows();

        for(int i = 1; i < rows.size(); i++){
            List<String> cells = rows.get(i).getCells();

            String projectTitle = cells.get(0);
            String projectDescription = cells.get(1);
            String active = cells.get(2);
            String completed = cells.get(3);

            final HashMap<String, Object> givenBody = new HashMap<>();
            givenBody.put(TITLE_FIELD, projectTitle);
            givenBody.put(DESCRIPTION_FIELD, projectDescription);
            givenBody.put(ACTIVE_FIELD,  Boolean.parseBoolean(active));
            givenBody.put(COMPLETED_FIELD, Boolean.parseBoolean(completed));

            String id = given().
                    body(givenBody).
                    when().
                    post("/projects").
                    then().
                    contentType(ContentType.JSON).
                    statusCode(HttpStatus.SC_CREATED).
                    body(
                            TITLE_FIELD, equalTo(projectTitle),
                            DESCRIPTION_FIELD, equalTo(projectDescription),
                            ACTIVE_FIELD, equalTo(active),
                            COMPLETED_FIELD, equalTo(completed)
                    ).
                    extract().
                    path(ID_FIELD);
            projects.put(projectTitle, id);
        }
    }

    @When("^I add a project with title \"([^\"]*)\" and \"([^\"]*)\" as description and \"([^\"]*)\" active status and \"([^\"]*)\" completed status$")
    public void iCreateAProject(String arg0, String arg1, String arg2, String arg3) throws Throwable {

        final HashMap<String, Object> givenBody = new HashMap<>();
        // Create project
        givenBody.put(TITLE_FIELD, arg0);
        givenBody.put(DESCRIPTION_FIELD, arg1);
        givenBody.put(ACTIVE_FIELD, Boolean.parseBoolean(arg2));
        givenBody.put(COMPLETED_FIELD, Boolean.parseBoolean(arg3));


        AppRunningStepDefinition.lastResponse.addFirst(
                given().
                        body(givenBody).
                        when().
                        post("/projects").
                        then().
                        contentType(ContentType.JSON).
                        statusCode(HttpStatus.SC_CREATED).
                        body(
                                TITLE_FIELD, equalTo(arg0),
                                DESCRIPTION_FIELD, equalTo(arg1)
                        ).
                        extract()
        );

        String projectId = AppRunningStepDefinition.lastResponse.getFirst().path(ID_FIELD);

        projects.put(arg0, projectId);
        //throw new PendingException();
    }

    @And("^Project with title \"([^\"]*)\" with description \"([^\"]*)\" should exist$" )
    public void theProjectWithTitleAndDescriptionShouldExist(String arg0, String arg1) throws Throwable {
        List<Map<String, Object>> projects =
                given().
                        when().
                        get("/projects").
                        then().
                        statusCode(HttpStatus.SC_OK).
                        contentType(ContentType.JSON).
                        extract().
                        body().
                        jsonPath().
                        getList("projects");

        assertTrue(projects.stream().anyMatch(
                project-> project.get(TITLE_FIELD).equals(arg0) &&
                          project.get(DESCRIPTION_FIELD).equals(arg1)
                )
        );
    }

    @And("^The project should have active status \"([^\"]*)\" and completed status \"([^\"]*)\"$")
    public void theProjectShouldHaveActiveStatusAndCompletedStatus(String arg0, String arg1) throws Throwable {
        List<Map<String, Object>> projects =
                given().
                        when().
                        get("/projects").
                        then().
                        statusCode(HttpStatus.SC_OK).
                        contentType(ContentType.JSON).
                        extract().
                        body().
                        jsonPath().
                        getList("projects");

        assertTrue(projects.stream().anyMatch(
                project-> project.get(ACTIVE_FIELD).equals(arg0) &&
                        project.get(COMPLETED_FIELD).equals(arg1)
                )
        );
    }

    @When("I add a a project with title \"([^\"]*)\" and \"([^\"]*)\" active status and \"([^\"]*)\" completed status")
    public void iCreateAProjectWithNoDescription(String arg0, String arg1, String arg2) throws Throwable {

        final HashMap<String, Object> givenBody = new HashMap<>();
        // Create project
        givenBody.put(TITLE_FIELD, arg0);
        givenBody.put(ACTIVE_FIELD, arg1);
        givenBody.put(COMPLETED_FIELD, arg1);


        AppRunningStepDefinition.lastResponse.addFirst(
                given().
                        body(givenBody).
                        when().
                        post("projects").
                        then().
                        contentType(ContentType.JSON).
                        statusCode(HttpStatus.SC_CREATED).
                        body(
                                TITLE_FIELD, equalTo(arg0),
                                DESCRIPTION_FIELD, equalTo(arg1)
                        ).
                        extract()
        );

        String projectId = AppRunningStepDefinition.lastResponse.getFirst().path(ID_FIELD);

        projects.put(arg0, projectId);
        //throw new PendingException();
    }

    @And("^Project with title \"([^\"]*)\" should exist$" )
    public void theProjectWithTitleShouldExist(String arg0, String arg1) throws Throwable {
        List<Map<String, Object>> projects =
                given().
                        when().
                        get("/projects").
                        then().
                        statusCode(HttpStatus.SC_OK).
                        contentType(ContentType.JSON).
                        extract().
                        body().
                        jsonPath().
                        getList("projects");

        assertTrue(projects.stream().anyMatch(
                project-> project.get(TITLE_FIELD).equals(arg0)
                )
        );
    }

    @And("^Project \"([^\"]*)\" should not exist in the system$")
    public void courseToDoListShouldNotExistInTheSystem(String projectTitle) throws Throwable {
        List<Map<String, Object>> projects =
                given().
                        when().
                        get("/projects").
                        then().
                        statusCode(HttpStatus.SC_OK).
                        contentType(ContentType.JSON).
                        extract().
                        body().
                        jsonPath().
                        getList("projects");

        assertTrue(projects.stream().noneMatch(
                project-> project.get(TITLE_FIELD).equals(projectTitle)
                )
        );
    }


}

