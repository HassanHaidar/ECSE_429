package uk.co.compendiumdev.performance.categories;

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

public class EditCategoryPerformanceTest {
    // Projects Paths
    private static final String ALL_CATEGORIES_PATH = "/categories";
    private static final String SPECIFIC_CATEGORIES_PATH = "/categories/{id}";
    private static final String CLEAR_PATH = "/admin/data/thingifier";
    private static final String CATEGORIES = "categories";

    // Project Fields
    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";

    // Test Fields
    private static final String TITLE_EXAMPLE = "University";
    private static final String DESCRIPTION_EXAMPLE = "McGill assignments";
    private static final String NEW_TITLE = "Home";
    private static final String NEW_DESCRIPTION = "Home chores";

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

        final JsonPath clearedData = when().get(ALL_CATEGORIES_PATH)
                .then().statusCode(200).extract().body().jsonPath();

        final int categories = clearedData.getList(CATEGORIES).size();

        Assumptions.assumeTrue(categories == 0);
    }

    @Test
    public void createAndUpdateCategories() throws InterruptedException {
        System.out.println("TestNum, Test Start Time (ms), action Time (ms), post Time(ms):");
        for(int i = 1; i<= SAMPLES; i++){
            createAndEditCategory(i);
            Thread.sleep(SLEEP_TIME);
        }
    }

    private static int createCategory(){
        final HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(TITLE, TITLE_EXAMPLE);
        givenBody.put(DESCRIPTION, DESCRIPTION_EXAMPLE);

        Response r = given().
                body(givenBody).
                when().
                post(ALL_CATEGORIES_PATH).
                thenReturn();

        String id = r.then().
                contentType(ContentType.JSON).
                statusCode(HttpStatus.SC_CREATED).
                body(
                        TITLE, equalTo(TITLE_EXAMPLE),
                        DESCRIPTION, equalTo(DESCRIPTION_EXAMPLE)
                ).
                extract().
                path(ID);

        return Integer.parseInt(id);
    }

    public void createAndEditCategory(int experimentNumber){
        int id = createCategory();

        final HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(TITLE, NEW_TITLE);
        givenBody.put(DESCRIPTION, NEW_DESCRIPTION);

        long createStartTime = System.currentTimeMillis();

        long postStartTime = System.currentTimeMillis();

        Response r = given().
                pathParam(ID, id).
                body(givenBody).
                when().
                put(SPECIFIC_CATEGORIES_PATH)
                .thenReturn();

        long postTime = System.currentTimeMillis() - postStartTime;

        r.then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_OK)
                .body(
                        ID, equalTo(String.valueOf(id)),
                        TITLE, equalTo(NEW_TITLE),
                        DESCRIPTION, equalTo(NEW_DESCRIPTION)
                );

        long testTime = System.currentTimeMillis() - createStartTime;
        long globalTime = System.currentTimeMillis() - globalStartTime;

        if(experimentNumber != -1) {
            System.out.printf("%d,%d,%d,%d\n",
                    experimentNumber, globalTime, testTime, postTime);
        }
    }

}
