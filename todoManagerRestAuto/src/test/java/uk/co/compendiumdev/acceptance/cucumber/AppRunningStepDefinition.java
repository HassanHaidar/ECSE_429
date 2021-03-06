package uk.co.compendiumdev.acceptance.cucumber;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import gherkin.formatter.model.DataTableRow;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assumptions;
import uk.co.compendiumdev.Environment;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.post;
import static org.hamcrest.Matchers.equalTo;

public class AppRunningStepDefinition {
    private static final String CLEAR_PATH = "/admin/data/thingifier";

    public static LinkedList<ExtractableResponse<Response>> lastResponse = new LinkedList<>();

    @Given("^The application is running$")
    public void theApplicationIsRunning() {
        RestAssured.baseURI = Environment.getBaseUri();
        Assumptions.assumeTrue(Environment.getBaseUri() != null, "Server not Running!");
        post(CLEAR_PATH);

        CategoryStepDefinitions.categories.clear();

    }
}
