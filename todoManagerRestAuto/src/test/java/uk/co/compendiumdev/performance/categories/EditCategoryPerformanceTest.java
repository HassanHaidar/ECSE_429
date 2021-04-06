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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    List<Integer> categoryIds = new ArrayList<Integer>();

    private static long globalStartTime;


    @BeforeAll
    public static void startTimer(){
        globalStartTime = System.currentTimeMillis();
    }

    @BeforeEach
    public void clearDataFromEnv() throws InterruptedException {
        RestAssured.baseURI = Environment.getBaseUri();
        if(RestAssured.baseURI == null) fail("To Do Manager isn't running!");

        post(CLEAR_PATH);

        final JsonPath clearedData = when().get(ALL_CATEGORIES_PATH)
                .then().statusCode(200).extract().body().jsonPath();

        final int categories = clearedData.getList(CATEGORIES).size();

        Assumptions.assumeTrue(categories == 0);

        // populate
        for(int i = 1; i <= SAMPLES; i++){
            categoryIds.add(createCategory());
            Thread.sleep(SLEEP_TIME);
        }
    }

    @Test
    public void updateProjects() throws InterruptedException {
        System.out.println("Sample Number, Global time (ms), transaction time (ms):");
        int i = 0;
        for(Integer id: categoryIds){
            updateCategory(i, id);
            i++;
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

    public void updateCategory(int experimentNumber, int categoryId){
        final HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(TITLE, NEW_TITLE);
        givenBody.put(DESCRIPTION, NEW_DESCRIPTION);

        long globalTime = System.currentTimeMillis() - globalStartTime;

        long postStartTime = System.currentTimeMillis();

        Response r = given().
                pathParam(ID, categoryId).
                body(givenBody).
                when().
                put(SPECIFIC_CATEGORIES_PATH)
                .thenReturn();

        long postTime = System.currentTimeMillis() - postStartTime;

        r.then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_OK)
                .body(
                        ID, equalTo(String.valueOf(categoryId)),
                        TITLE, equalTo(NEW_TITLE),
                        DESCRIPTION, equalTo(NEW_DESCRIPTION)
                );

        if(experimentNumber != -1) {
            System.out.printf("%d,%d,%d\n",
                    experimentNumber, globalTime, postTime);
        }
    }

}
