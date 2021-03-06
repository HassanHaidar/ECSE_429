package uk.co.compendiumdev.acceptance.cucumber;

import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import gherkin.formatter.model.DataTableRow;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class ProjectsStepDefinition {


    private static final String ALL_PROJECTS_PATH = "/projects";
    private static final String SPECIFIC_PROJECTS_PATH = "/projects/{id}";
    private static final String CATEGORY_TO_PROJECTS_PATH = "/categories/{id}/projects";
    private static final String TODO_TO_CATEGORY_PATH = "/projects/{id}/categories";


    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String DESC = "description";
    private static final String STATUS = "active";
    private static final String COMPLETED = "completed";

    public static final Map<String, String> projects = new HashMap<>();

    @And("^Projects exist:$")
    public void projectsExist(DataTable table) {

        List<DataTableRow> rows = table.getGherkinRows();

        for(int i = 1; i<rows.size() ; i++) {
            List<String> cells = rows.get(i).getCells();

            String project = cells.get(0);
            String status = cells.get(1);
            String completed = cells.get(2);
            String desc = cells.get(3);

            final HashMap<String, Object> givenBody = new HashMap<>();
            givenBody.put(TITLE, project);

            if (status.equals("true")) {
                givenBody.put(STATUS, true);
            } else if (status.equals("false")) {
                givenBody.put(STATUS, false);
            }

            if (completed.equals("true")) {
                givenBody.put(COMPLETED, true);
            } else if (completed.equals("false")) {
                givenBody.put(COMPLETED, false);
            }

            givenBody.put(DESC, desc);

            String id = given().
                    body(givenBody).
                    when().
                    post(ALL_PROJECTS_PATH).
                    then().
                    contentType(ContentType.JSON).
                    statusCode(HttpStatus.SC_CREATED).
                    body(
                            TITLE, equalTo(project),
                            COMPLETED, equalTo(completed),
                            STATUS, equalTo(status),
                            DESC, equalTo(desc)
                    ).
                    extract().
                    path(ID);

            projects.put(project, id);
        }
    }

    @And("^The project \"([^\"]*)\" exists$")
    public void theProjectExists(String arg0) throws Throwable {

        List<Map<String, Object>> projects =
                given().
                        pathParam(ID, ProjectsStepDefinition.projects.get(arg0)).
                        when().
                        get(SPECIFIC_PROJECTS_PATH).
                        then().
                        statusCode(HttpStatus.SC_OK).
                        contentType(ContentType.JSON).
                        extract().
                        body().
                        jsonPath().
                        getList("projects");

        Assertions.assertTrue(projects.stream().allMatch(object -> object.get("title").equals(arg0)));
    }

    @And("^the project \"([^\"]*)\" does not exist$")
    public void theProjectDoesNotExist(String arg0) throws Throwable {

        Assertions.assertEquals(ProjectsStepDefinition.projects.getOrDefault(arg0, "Does not exist"), "Does not exist");
    }

    @And("^The project \"([^\"]*)\" should show$")
    public void theProjectShouldShow(String arg0) throws Throwable {
        List<Map<String, Object>> projects =
                given().
                        when().
                        get("/projects?title="+arg0).
                        then().
                        statusCode(HttpStatus.SC_OK).
                        contentType(ContentType.JSON).
                        extract().
                        body().
                        jsonPath().
                        getList("projects");

        Assertions.assertTrue(projects.stream().allMatch(object -> object.get("title").equals(arg0)));
    }


}
