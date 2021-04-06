package uk.co.compendiumdev.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;
import uk.co.compendiumdev.Environment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;

@TestMethodOrder(MethodOrderer.Random.class)
public class APIBidirectionalBugsUnitTests {

    private static final String ID_FIELD = "id";
    private static final String TITLE_FIELD = "title";
    private static final String DESCRIPTION_FIELD = "description";

    private static final String CATEGORY_DESCRIPTION = "itation ullamco labo";
    private static final String CATEGORY_TITLE = "test category";

    private static final String CATEGORY_PROJECT = "categories/{id}/projects";

    private static final String PROJECT_DESCRIPTION = "itation ullamco labo";
    private static final String PROJECT_TITLE = "test project";

    private static final String COMPLETED_FIELD = "completed";
    private static final String ACTIVE_FIELD = "active";

    private static int categoryId = 0;
    private static int projectId = 0;
    private static final String PROJECT_CATEGORIES = "projects/{id}/categories";

    @BeforeEach
    public void clearDataFromEnv() {

        // avoid the use of Environment.getEnv("/todos") etc. to keep code a little clearer
        RestAssured.baseURI = Environment.getBaseUri();

        when().post("/admin/data/thingifier")
                .then().statusCode(200);

        final JsonPath clearedData = when().get("/categories")
                .then().statusCode(200).extract().body().jsonPath();

        final int newNumberOfTodos = clearedData.getList("categories").size();

        Assertions.assertEquals(0, newNumberOfTodos);
    }

    /**
     * BUG: the target of this test is to show that when a category is made for a
     * project using project/:id/categories, the category is not shown when calling
     * category/id/project/:id
     * i.e. association is not set on both sides
     */
    @Test
    public void postCategoryForProjectIsNotBidirectional() {
        // post a project
        postProject();
        // post a category for a project
        postCategoryForProject(projectId);

        List<Map<String, Object>> categoryOfProject = given().
                pathParam("id", categoryId).
                when().
                get(PROJECT_CATEGORIES).then().
                contentType(ContentType.JSON).
                statusCode(HttpStatus.SC_OK).
                extract().
                body().
                jsonPath().
                getList("categories");

        Assertions.assertEquals(1, categoryOfProject.size());
        Assertions.assertEquals(categoryOfProject.get(0).get(ID_FIELD), String.valueOf(categoryId));


        // empty list is returned
        List<Map<String, Object>> projectOfCategory = given().
                pathParam("id", categoryId).
                when().
                get(CATEGORY_PROJECT).then().
                contentType(ContentType.JSON).
                statusCode(HttpStatus.SC_OK).
                extract().
                body().
                jsonPath().
                getList("projects");

        Assertions.assertEquals(0, projectOfCategory.size());
    }

    /**
     * BUG: the target of this test is to show that when a project is made for a
     * category using categories/:id/project, the project is not shown when calling
     * project/id/category/
     * i.e. association is not set on both sides
     */
    @Test
    public void postProjectForCategoryIsNotBidirectional() {
        // post a project
        postCategory();
        // post a category for a project
        postProjectForCategory(categoryId);

        List<Map<String, Object>> projectOfCategory = given().
                pathParam("id", categoryId).
                when().
                get(CATEGORY_PROJECT).then().
                contentType(ContentType.JSON).
                statusCode(HttpStatus.SC_OK).
                extract().
                body().
                jsonPath().
                getList("projects");

        Assertions.assertEquals(1, projectOfCategory.size());
        Assertions.assertEquals(projectOfCategory.get(0).get(ID_FIELD), String.valueOf(projectId));

        List<Map<String, Object>> categoryOfProject = given().
                pathParam("id", categoryId).
                when().
                get(PROJECT_CATEGORIES).then().
                contentType(ContentType.JSON).
                statusCode(HttpStatus.SC_OK).
                extract().
                body().
                jsonPath().
                getList("categories");

        Assertions.assertEquals(0, categoryOfProject.size());
    }



    // HELPER METHODS
    private static void postCategoryForProject(int id){
        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(TITLE_FIELD, CATEGORY_TITLE);
        jsonBody.put(DESCRIPTION_FIELD, CATEGORY_DESCRIPTION);

        String responseId = given().pathParam("id", id)
                .body(jsonBody)
                .when().post(PROJECT_CATEGORIES)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_CREATED)
                .body(TITLE_FIELD, equalTo(CATEGORY_TITLE),
                        DESCRIPTION_FIELD, equalTo(CATEGORY_DESCRIPTION))
                .extract()
                .path("id");
        categoryId = Integer.parseInt(responseId);
    }


    public void postProject() {
        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(TITLE_FIELD, PROJECT_TITLE);
        jsonBody.put(COMPLETED_FIELD, false);
        jsonBody.put(ACTIVE_FIELD, false);
        jsonBody.put(DESCRIPTION_FIELD, PROJECT_DESCRIPTION);

        String responseId = given().body(jsonBody)
                .when().post("/projects")
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_CREATED)
                .body(TITLE_FIELD, equalTo(PROJECT_TITLE),
                        DESCRIPTION_FIELD, equalTo(PROJECT_DESCRIPTION),
                        COMPLETED_FIELD, equalTo("false"),
                        ACTIVE_FIELD, equalTo("false"))
                .extract()
                .path("id");
        projectId = Integer.parseInt(responseId);
    }


    private static void postProjectForCategory(int id){
        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(TITLE_FIELD, PROJECT_TITLE);
        jsonBody.put(DESCRIPTION_FIELD, PROJECT_DESCRIPTION);

        String responseId = given().pathParam("id", id)
                .body(jsonBody)
                .when().post(CATEGORY_PROJECT)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_CREATED)
                .body(TITLE_FIELD, equalTo(PROJECT_TITLE),
                        DESCRIPTION_FIELD, equalTo(PROJECT_DESCRIPTION))
                .extract()
                .path("id");
        projectId = Integer.parseInt(responseId);
    }


    public void postCategory() {
        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(TITLE_FIELD, CATEGORY_TITLE);
        jsonBody.put(DESCRIPTION_FIELD, CATEGORY_DESCRIPTION);

        String responseId = given().body(jsonBody)
                .when().post("/categories")
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_CREATED)
                .body(TITLE_FIELD, equalTo(CATEGORY_TITLE))
                .extract()
                .path("id");
        categoryId = Integer.parseInt(responseId);
    }
}