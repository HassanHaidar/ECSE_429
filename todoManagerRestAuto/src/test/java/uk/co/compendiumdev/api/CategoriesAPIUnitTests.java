package uk.co.compendiumdev.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import uk.co.compendiumdev.Environment;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.equalTo;

@TestMethodOrder(MethodOrderer.Random.class)
public class CategoriesAPIUnitTests {

    private static final String ID_FIELD = "id";
    private static final String TITLE_FIELD = "title";
    private static final String DESCRIPTION_FIELD = "description";

    private static final String CATEGORY_DESCRIPTION = "itation ullamco labo";
    private static final String CATEGORY_TITLE = "test category";

    private static final String CATEGORY_PROJECT = "categories/{id}/projects";

    private static final String ERROR = "errorMessages";
    private static final String BAD_REQUEST = "Could not find field: ";
    public static final String ERRONEOUS_FIELD = "erroneous_field";

    private static final String PROJECT_DESCRIPTION = "itation ullamco labo";
    private static final String PROJECT_TITLE = "test project";

    private static final String COMPLETED_FIELD = "completed";
    private static final String ACTIVE_FIELD = "active";

    private static int categoryId = 0;
    private static int projectId = 0;
    private static int taskId = 0;

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

    @Test
    public void postCategorySingleFieldJsonSuccess() {
        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(TITLE_FIELD, CATEGORY_TITLE);

        given().body(jsonBody)
                .when().post("/categories")
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_CREATED)
                .body(TITLE_FIELD, equalTo(CATEGORY_TITLE));
    }

    @Test
    public void postCategoryAllFieldsJsonSuccess() {
        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(TITLE_FIELD, CATEGORY_TITLE);
        jsonBody.put(DESCRIPTION_FIELD, CATEGORY_DESCRIPTION);

        String responseId = given().body(jsonBody)
                .when().post("/categories")
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_CREATED)
                .body(TITLE_FIELD, equalTo(CATEGORY_TITLE),
                        DESCRIPTION_FIELD, equalTo(CATEGORY_DESCRIPTION))
                .extract()
                .path("id");
        categoryId = Integer.parseInt(responseId);
    }

    @Test
    public void postCategoryAllFieldsJsonFailure() {
        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(ERRONEOUS_FIELD, CATEGORY_TITLE);
        jsonBody.put(DESCRIPTION_FIELD, CATEGORY_DESCRIPTION);

        String response;
        response = given().body(jsonBody)
                .when().post("/categories")
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract().
                        body().
                        jsonPath().
                        getList(ERROR).
                        get(0).toString();
        Assertions.assertEquals(response, BAD_REQUEST + ERRONEOUS_FIELD);
    }

    @Test
    public void postCategoryAllFieldsXMLSuccess() throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Document xmlDocument = documentBuilder.newDocument();
        Element request = xmlDocument.createElement("category");
        xmlDocument.appendChild(request);

        Element title = xmlDocument.createElement(TITLE_FIELD);
        title.appendChild(xmlDocument.createTextNode(CATEGORY_TITLE));
        request.appendChild(title);

        Element description = xmlDocument.createElement(DESCRIPTION_FIELD);
        description.appendChild(xmlDocument.createTextNode(CATEGORY_DESCRIPTION));
        request.appendChild(description);

        Transformer transformer = (TransformerFactory.newInstance()).newTransformer();
        StringWriter stringWriter = new StringWriter();
        transformer.transform(new DOMSource(xmlDocument), new StreamResult(stringWriter));

        String requestXmlBody = stringWriter.getBuffer().toString();

        given().
                body(requestXmlBody).
                accept(ContentType.XML).
                contentType(ContentType.XML).
                when().
                post("/categories").
                then().
                statusCode(HttpStatus.SC_CREATED).
                contentType(ContentType.XML).
                body("category." + TITLE_FIELD, equalTo(CATEGORY_TITLE),
                        "category." + DESCRIPTION_FIELD, equalTo(CATEGORY_DESCRIPTION));
    }

    @Test
    public void getCategoriesReturnsCategoriesSuccess() {
        postCategorySingleFieldJsonSuccess();
        postCategoryAllFieldsJsonSuccess();

        List<Map<String, Object>> categories = when().
                get("/categories").then().
                contentType(ContentType.JSON).
                statusCode(HttpStatus.SC_OK).
                extract().
                body().
                jsonPath().
                getList("categories");

        Assertions.assertEquals(categories.size(), 2);
        Assertions.assertTrue(categories.stream().allMatch(object -> object.get(TITLE_FIELD).equals(CATEGORY_TITLE)));
    }

    @Test
    public void getCategoriesReturnsCategoriesFilterTitleSuccess() {
        postCategorySingleFieldJsonSuccess();
        postCategory("Title");

        List<Map<String, Object>> categories = when().
                get("/categories?title=Title").then().
                contentType(ContentType.JSON).
                statusCode(HttpStatus.SC_OK).
                extract().
                body().
                jsonPath().
                getList("categories");

        Assertions.assertEquals(1, categories.size());
        Assertions.assertEquals("Title", categories.get(0).get("title"));
    }


    @Test
    public void deleteCategoriesNotAllowed() {
        given().
                contentType(ContentType.JSON).
                accept(ContentType.JSON).
                when().
                delete("/categories").
                then().
                statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    public void deleteCategoryByIDProjectSuccess() {
        // populate body
        postCategory();
        given().
                pathParam(ID_FIELD, categoryId).
                when().
                get("/categories/{id}").
                then().
                statusCode(HttpStatus.SC_OK).
                contentType(ContentType.JSON);
    }

    @Test
    public void deleteDeletedCategoryByID() {
        // populate body
        postCategory();
        given().
                pathParam(ID_FIELD, categoryId).
                when().
                delete("/categories/{id}").
                then().
                statusCode(HttpStatus.SC_OK).
                contentType(ContentType.JSON);

        given().
                pathParam(ID_FIELD, categoryId).
                when().
                delete("/categories/{id}").
                then().
                statusCode(HttpStatus.SC_NOT_FOUND).
                contentType(ContentType.JSON);
    }


    @Test
    public void getCategoryByIDProjectSuccess() {
        // populate body
        postCategoryAllFieldsJsonSuccess();
        Map<String, Object> response;
        response = (Map<String, Object>) given().
                pathParam(ID_FIELD, categoryId).
                when().
                get("/categories/{id}").
                then().
                statusCode(HttpStatus.SC_OK).
                contentType(ContentType.JSON).
                extract().
                body().
                jsonPath().
                getList("categories").
                get(0);

        Assertions.assertEquals(String.valueOf(categoryId), response.get(ID_FIELD));
        Assertions.assertEquals(CATEGORY_DESCRIPTION, response.get(DESCRIPTION_FIELD));
        Assertions.assertEquals(CATEGORY_TITLE, response.get(TITLE_FIELD));
    }

    @Test
    public void getCategoryByIdXMLSuccess() {
        // populate body
        postCategoryAllFieldsJsonSuccess();
        given().
                pathParam(ID_FIELD, categoryId).
                accept(ContentType.XML).
                when().
                get("/categories/{id}").
                then().
                statusCode(HttpStatus.SC_OK).
                contentType(ContentType.XML).
                body("categories.category." + TITLE_FIELD, equalTo(CATEGORY_TITLE),
                        "categories.category." + DESCRIPTION_FIELD, equalTo(CATEGORY_DESCRIPTION));
    }

    @Test
    public void getCategoryByIDCategoryNotFound() {
        String response;
        response = (String) given().
                pathParam(ID_FIELD, categoryId).
                when().
                get("/categories/{id}").
                then().
                statusCode(HttpStatus.SC_NOT_FOUND).
                contentType(ContentType.JSON).
                extract().
                body().
                jsonPath().
                getList(ERROR).
                get(0);
    }

    @Test
    public void postCategoryWithIdJSONSuccess() {
        postCategoryAllFieldsJsonSuccess();

        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(TITLE_FIELD, "random title");
        jsonBody.put(DESCRIPTION_FIELD, "random description");

        given().pathParam("id", categoryId)
                .body(jsonBody)
                .when().post("/categories/{id}")
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_OK)
                .body(TITLE_FIELD, equalTo("random title"),
                        DESCRIPTION_FIELD, equalTo("random description"));
    }

    @Test
    public void putCategoryWithIdJSONSuccess() {
        postCategoryAllFieldsJsonSuccess();

        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(TITLE_FIELD, "random title");
        jsonBody.put(DESCRIPTION_FIELD, "random description");

        given().pathParam("id", categoryId)
                .body(jsonBody)
                .when().put("/categories/{id}")
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_OK)
                .body(TITLE_FIELD, equalTo("random title"),
                        DESCRIPTION_FIELD, equalTo("random description"));
    }

    @Test
    public void postCategoryWithIdNotFound() {
        postCategoryAllFieldsJsonSuccess();

        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(TITLE_FIELD, "random title");
        jsonBody.put(DESCRIPTION_FIELD, "random description");
        given()
                .body(jsonBody)
                .pathParam("id", 136812)
                .when()
                .post("/categories/{id}")
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void postCategoryWithIdJSONTitleDoesNotChangeOtherFieldsSuccess() {
        postCategoryAllFieldsJsonSuccess();

        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(TITLE_FIELD, "random title");

        given().pathParam("id", categoryId)
                .body(jsonBody)
                .when().post("/categories/{id}")
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_OK)
                .body(TITLE_FIELD, equalTo("random title"),
                        DESCRIPTION_FIELD, equalTo(CATEGORY_DESCRIPTION));
    }

    @Test
    public void postCategoryWithIdJSONDescriptionDoesNotChangeOtherFields() {
        postCategoryAllFieldsJsonSuccess();

        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(DESCRIPTION_FIELD, "random description");

        given().pathParam("id", categoryId)
                .body(jsonBody)
                .when().post("/categories/{id}")
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_OK)
                .body(TITLE_FIELD, equalTo(CATEGORY_TITLE),
                        DESCRIPTION_FIELD, equalTo("random description"));
    }

    @Test
    public void postProjectForCategoryJsonSuccess() {
        postCategoryAllFieldsJsonSuccess();

        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(TITLE_FIELD, PROJECT_TITLE);
        jsonBody.put(DESCRIPTION_FIELD, PROJECT_DESCRIPTION);

        given().body(jsonBody)
                .pathParam("id", categoryId)
                .when().post(CATEGORY_PROJECT)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_CREATED)
                .body(TITLE_FIELD, equalTo(PROJECT_TITLE),
                        DESCRIPTION_FIELD, equalTo(PROJECT_DESCRIPTION))
                .extract()
                .path("id");
    }

    @Test
    public void postCategoryTitleOnlyForProjectJsonSuccess() {
        postCategory();

        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(TITLE_FIELD, CATEGORY_TITLE);

        String responseId = given().pathParam("id", categoryId)
                .body(jsonBody)
                .when().post(CATEGORY_PROJECT)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_CREATED)
                .body(TITLE_FIELD, equalTo(CATEGORY_TITLE),
                        DESCRIPTION_FIELD, equalTo(""))
                .extract()
                .path("id");
        categoryId = Integer.parseInt(responseId);
    }

    @Test
    public void postProjectForCategoryXMLSuccess() throws ParserConfigurationException, TransformerException{
        postCategory();

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Document xmlDocument = documentBuilder.newDocument();
        Element request = xmlDocument.createElement("project");
        xmlDocument.appendChild(request);

        Element title = xmlDocument.createElement(TITLE_FIELD);
        title.appendChild(xmlDocument.createTextNode(PROJECT_TITLE));
        request.appendChild(title);

        Element active = xmlDocument.createElement(ACTIVE_FIELD);
        active.appendChild(xmlDocument.createTextNode("false"));
        request.appendChild(active);

        Element completed = xmlDocument.createElement(COMPLETED_FIELD);
        completed.appendChild(xmlDocument.createTextNode("false"));
        request.appendChild(completed);

        Element description = xmlDocument.createElement(DESCRIPTION_FIELD);
        description.appendChild(xmlDocument.createTextNode(PROJECT_DESCRIPTION));
        request.appendChild(description);

        Transformer transformer = (TransformerFactory.newInstance()).newTransformer();
        StringWriter stringWriter = new StringWriter();
        transformer.transform(new DOMSource(xmlDocument), new StreamResult(stringWriter));

        String requestXmlBody = stringWriter.getBuffer().toString();

        given().
                body(requestXmlBody).
                pathParam(ID_FIELD, categoryId).
                accept(ContentType.XML).
                contentType(ContentType.XML).
                when().
                post(CATEGORY_PROJECT).
                then().
                statusCode(HttpStatus.SC_CREATED).
                contentType(ContentType.XML).
                body("projects." + TITLE_FIELD, equalTo(PROJECT_TITLE),
                        "project." + ACTIVE_FIELD, equalTo("false"),
                        "project." + COMPLETED_FIELD, equalTo("false"),
                        "project." + DESCRIPTION_FIELD, equalTo(PROJECT_DESCRIPTION));
    }

    @Test
    public void getProjectsCategoriesJsonSuccess() {
        postCategory();
        postProjectForCategory(categoryId);
        postProjectForCategory(categoryId);

        List<Map<String, Object>> todos = given().
                pathParam("id", categoryId).
                when().
                get(CATEGORY_PROJECT).then().
                contentType(ContentType.JSON).
                statusCode(HttpStatus.SC_OK).
                extract().
                body().
                jsonPath().
                getList("projects");

        Assertions.assertEquals(2, todos.size());
        for (Map<String, Object> category : todos) {
            Assertions.assertEquals(PROJECT_TITLE, category.get(TITLE_FIELD));
        }
    }

    @Test
    public void deleteProjectsCategoriesJsonSuccess() {
        postCategory();
        postProjectForCategory(categoryId);

        given().
                when().
                delete("categories/{id}/projects/{projectId}", categoryId, projectId ).then().
                contentType(ContentType.JSON).
                statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void headCategoriesJson() {
        given().
                when().
                head("/categories").
                then().
                contentType(ContentType.JSON).
                statusCode(HttpStatus.SC_OK);
    }

    // HELPER METHODS

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

    public void postCategory(String title) {
        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(TITLE_FIELD, title);

        String responseId = given().body(jsonBody)
                .when().post("/categories")
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_CREATED)
                .body(TITLE_FIELD, equalTo(title))
                .extract()
                .path("id");
        categoryId = Integer.parseInt(responseId);
    }
}