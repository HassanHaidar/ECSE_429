package uk.co.compendiumdev.performance.projects;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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


    @BeforeAll
    public static void startTimer(){
        globalStartTime = System.currentTimeMillis();
    }

    @BeforeEach
    public void clearDataFromEnv(){
        RestAssured.baseURI = Environment.getBaseUri();
        if(RestAssured.baseURI == null) fail("To Do Manager isn't running!");

        post(CLEAR_PATH);

        final JsonPath clearedData = when().get(ALL_PROJECTS_PATH)
                .then().statusCode(200).extract().body().jsonPath();

        final int projects = clearedData.getList(PROJECTS).size();

        Assumptions.assumeTrue(projects == 0);
    }

    @Test
    public void createProjects() throws InterruptedException {
        System.out.println("TestNum, Test Start Time (ms), action Time (ms), post Time(ms):");
        for(int i = 1; i<= SAMPLES; i++){
            createProject(i);
            Thread.sleep(SLEEP_TIME);
        }
    }

    private static int createProject(int experimentNumber){
        final HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(TITLE, TITLE_EXAMPLE);
        givenBody.put(DESCRIPTION, DESCRIPTION_EXAMPLE);
        givenBody.put(ACTIVE, true);
        givenBody.put(COMPLETED, false);

        long createStartTime = System.currentTimeMillis();

        long postStartTime = System.currentTimeMillis();

        Response r = given().
                body(givenBody).
                when().
                post(ALL_PROJECTS_PATH)
                .thenReturn();
        long postTime = System.currentTimeMillis() - postStartTime;

        String id  = r.then().contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_CREATED)
                .body(
                        TITLE, equalTo(TITLE_EXAMPLE),
                        DESCRIPTION, equalTo(DESCRIPTION_EXAMPLE),
                        ACTIVE, equalTo(TRUE),
                        COMPLETED, equalTo(FALSE)
                )
                .extract()
                .path(ID);
        long createTime = System.currentTimeMillis() - createStartTime;
        long globalTime = System.currentTimeMillis() - globalStartTime;

        if(experimentNumber != -1) {
            System.out.printf("%d,%d,%d,%d\n",
                    experimentNumber, globalTime, createTime, postTime);
        }

        return Integer.parseInt(id);
    }

}
