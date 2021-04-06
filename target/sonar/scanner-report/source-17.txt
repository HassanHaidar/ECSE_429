package uk.co.compendiumdev.acceptance.cucumber;

import cucumber.api.java.After;
import cucumber.api.java.en.Given;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assumptions;
import uk.co.compendiumdev.Environment;

import java.util.LinkedList;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.post;

public class AppRunningStepDefinition {
    private static final String CLEAR_PATH = "/admin/data/thingifier";

    public static LinkedList<ExtractableResponse<Response>> lastResponse = new LinkedList<>();

    @Given("^The application is running$")
    public void theApplicationIsRunning() {
        RestAssured.baseURI = Environment.getBaseUri();
        Assumptions.assumeTrue(Environment.getBaseUri() != null, "Server not Running!");
        post(CLEAR_PATH);

        CategoriesStepDefinitions.categories.clear();
        ProjectsStepDefinition.projects.clear();
        TodosStepDefinitions.todos.clear();

    }

    @After
    public void clearEnv() {
        post(CLEAR_PATH);
    }
}
