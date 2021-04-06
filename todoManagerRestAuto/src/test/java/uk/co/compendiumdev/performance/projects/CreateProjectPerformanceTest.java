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


    private static long globalStartTime;
    private static final String TITLE_FIELD = "title";
    private static final String COMPLETED_FIELD = "completed";
    private static final String ACTIVE_FIELD = "active";

    private static final String PROJECT_TITLE = "test project";

    private static int projectId = 0;


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

        // sample time
        long globalTime = System.currentTimeMillis() - globalStartTime;

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
        System.out.printf("%d,%d,%d\n",
                sampleNumber, globalTime, transactionTime);
    }

}
