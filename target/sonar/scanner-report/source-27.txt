package uk.co.compendiumdev;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.Environment;

import static io.restassured.RestAssured.when;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Provided code from EvilTester repository
 * All credits go to the author of the repository
 */
public class CanClearEnvironmentTest {

    @BeforeAll
    public static void clearDataFromEnv(){
        RestAssured.baseURI = Environment.getBaseUri();
        if(RestAssured.baseURI == null) fail("To Do Manager isn't running!");

        when().post(Environment.getEnv("/admin/data/thingifier")).then().statusCode(200);

        final JsonPath clearedData = when().get(Environment.getEnv("/todos")).then().statusCode(200).extract().body().jsonPath();
        final int newNumberOfTodos = clearedData.getList("todos").size();

        Assertions.assertEquals(0, newNumberOfTodos);
    }


    @Test
    public void shouldBeNoTodos(){

        Assertions.assertEquals(0, when().get(Environment.getEnv("/todos")).
                then().
                statusCode(200).
                contentType("application/json").
                extract().body().jsonPath().
                getList("todos").size());
    }

    @Test
    public void shouldBeNoProjects(){

        Assertions.assertEquals(0, when().get(Environment.getEnv("/projects")).
                then().
                statusCode(200).
                contentType("application/json").
                extract().body().jsonPath().
                getList("projects").size());

    }

    @Test
    public void shouldBeNoCategories(){

        Assertions.assertEquals(0, when().get(Environment.getEnv("/categories")).
                then().
                statusCode(200).
                contentType("application/json").
                extract().body().jsonPath().
                getList("categories").size());
    }
}
