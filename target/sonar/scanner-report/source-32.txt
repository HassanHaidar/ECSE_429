package uk.co.compendiumdev.performance.projects;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;
import uk.co.compendiumdev.Environment;

import java.util.HashMap;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.co.compendiumdev.performance.TestVars.SAMPLES;
import static uk.co.compendiumdev.performance.TestVars.SLEEP_TIME;

public class CreateProjectPerformanceTest {

    private static final String ALL_PROJECTS_PATH = "/projects";
    private static final String CLEAR_PATH = "/admin/data/thingifier";
    private static final String PROJECTS = "projects";

    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String COMPLETED = "completed";
    private static final String ACTIVE = "active";

    private static final String TITLE_EXAMPLE = "ECSE429";
    private static final String DESCRIPTION_EXAMPLE = "Deliverable C";

    private static final String TRUE = "true";
    private static final String FALSE = "false";

    private static long globalStartTime;

    private static final String PROJECT_CATEGORIES = "projects/{id}/categories";
    private static final String PROJECT_CATEGORIES_ID = "projects/{id}/categories/{categoryId}";

    private static final String PROJECT_TASKS = "projects/{id}/tasks";
    private static final String PROJECT_TASKS_ID = "projects/{id}/tasks/{taskId}";

    private static final String CATEGORY_TITLE = "cia deserunt mollita";
    private static final String CATEGORY_DESCRIPTION = "dolore eu fugiat nua";

    private static final String TODO_TITLE = "cia deserunt mollita";
    private static final String TODO_DESCRIPTION = "dolore eu fugiat nua";

    private static final String ID_FIELD = "id";
    private static final String TITLE_FIELD = "title";
    private static final String DESCRIPTION_FIELD = "description";
    private static final String COMPLETED_FIELD = "completed";
    private static final String ACTIVE_FIELD = "active";
    private static final String DONE_FIELD = "doneStatus";

    private static final String PROJECT_DESCRIPTION = "itation ullamco labo";
    private static final String PROJECT_TITLE = "test project";

    private static final String ERROR = "errorMessages";
    private static final String BAD_REQUEST = "Could not find field: ";
    private static final String NOT_FOUND = "Could not find any instances with ";
    public static final String ERRONEOUS_FIELD = "erroneous_field";

    private static int projectId = 0;
    private static int categoryId = 0;
    private static int taskId = 0;



    @BeforeAll
    public static void startTimer(){
        globalStartTime = System.currentTimeMillis();
    }

    @BeforeEach
    public void clearDataFromEnv(){
        // avoid the use of Environment.getEnv("/todos") etc. to keep code a little clearer
        RestAssured.baseURI = Environment.getBaseUri();

        when().post("/admin/data/thingifier")
                .then().statusCode(200);

        final JsonPath clearedData = when().get("/projects")
                .then().statusCode(200).extract().body().jsonPath();

        final int newNumberOfTodos = clearedData.getList("projects").size();

        Assertions.assertEquals(0, newNumberOfTodos);
    }

    @Test
    public void createProjects() throws InterruptedException {
        System.out.println("Sample Number, Global time (ms), transaction time (ms):");
        for(int i = 1; i <= SAMPLES; i++){
            postProject(i);
            Thread.sleep(SLEEP_TIME);
        }
    }

    // adapted post project helper method taken from the projects api test
    public void postProject(int sampleNumber) {
        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(TITLE_FIELD, PROJECT_TITLE);
        jsonBody.put(ACTIVE_FIELD, true);
        jsonBody.put(COMPLETED_FIELD, false);


        // measure time of api transaction
        // transaction start time
        long transactionStartTime = System.currentTimeMillis();

        // send api request
        Response response = given().body(jsonBody)
                .when().post("/projects")
                .thenReturn();

        // transaction end time
        long transactionTime = System.currentTimeMillis() - transactionStartTime;

        String responseId = response.then().contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_CREATED)
                .body(TITLE_FIELD, equalTo(PROJECT_TITLE),
                        COMPLETED_FIELD, equalTo(String.valueOf(false)),
                        ACTIVE_FIELD, equalTo(String.valueOf(true)))
                .extract()
                .path("id");
        projectId = Integer.parseInt(responseId);

        long globalTime = System.currentTimeMillis() - globalStartTime;

        System.out.printf("%d,%d,%d\n",
                sampleNumber, globalTime, transactionTime);
    }

}
