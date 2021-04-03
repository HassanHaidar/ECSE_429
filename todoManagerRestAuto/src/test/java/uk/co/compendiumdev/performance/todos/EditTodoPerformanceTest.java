package uk.co.compendiumdev.performance.todos;

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

public class EditTodoPerformanceTest {

    private static final String ALL_TODOS_PATH = "/todos";
    private static final String SPECIFIC_TODO_PATH = "/todos/{id}";
    private static final String CLEAR_PATH = "/admin/data/thingifier";
    private static final String TODOS = "todos";

    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String DONE = "doneStatus";

    private static final String TITLE_EXAMPLE = "water plants";
    private static final String DESCRIPTION_EXAMPLE = "water bedroom plants";
    private static final String NEW_TITLE = "clean dishes";
    private static final String NEW_DESCRIPTION = "wash dishes";

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

        final JsonPath clearedData = when().get(ALL_TODOS_PATH)
                .then().statusCode(200).extract().body().jsonPath();

        final int todos = clearedData.getList(TODOS).size();

        Assumptions.assumeTrue(todos == 0);
    }

    @Test
    public void editTodos() throws InterruptedException {
        System.out.println("TestNum, Test Start Time (ms), action Time (ms), post Time(ms):");
        for(int i = 1; i<= SAMPLES; i++){
            createAndUpdateTodo(i);
            Thread.sleep(SLEEP_TIME);
        }
    }

    private static int createTodo(){
        final HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(TITLE, TITLE_EXAMPLE);
        givenBody.put(DESCRIPTION, DESCRIPTION_EXAMPLE);
        givenBody.put(DONE, true);

        Response r = given().
                body(givenBody).
                when().
                post(ALL_TODOS_PATH).thenReturn();

        String id = r.then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_CREATED)
                .body(
                        TITLE, equalTo(TITLE_EXAMPLE),
                        DESCRIPTION, equalTo(DESCRIPTION_EXAMPLE),
                        DONE, equalTo(TRUE)
                )
                .extract()
                .path(ID);

        return Integer.parseInt(id);
    }

    private static int createAndUpdateTodo(int experimentNumber){
        int id = createTodo();

        final HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(TITLE, NEW_TITLE);
        givenBody.put(DESCRIPTION, NEW_DESCRIPTION);
        givenBody.put(DONE, false);

        long createStartTime = System.currentTimeMillis();

        long postStartTime = System.currentTimeMillis();
        Response r = given().
                pathParam(ID, id).
                body(givenBody).
                when().
                put(SPECIFIC_TODO_PATH)
                .thenReturn();

        long postTime = System.currentTimeMillis() - postStartTime;
                r.then().contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_OK)
                .body(
                        ID, equalTo(String.valueOf(id)),
                        TITLE, equalTo(NEW_TITLE),
                        DESCRIPTION, equalTo(NEW_DESCRIPTION),
                        DONE, equalTo(FALSE)

                );

        long createTime = System.currentTimeMillis() - createStartTime;


        long globalTime = System.currentTimeMillis() - globalStartTime;

        if(experimentNumber != -1) {
            System.out.printf("%d,%d,%d,%d\n",
                    experimentNumber, globalTime, createTime, postTime);
        }

        return id;
    }

}
