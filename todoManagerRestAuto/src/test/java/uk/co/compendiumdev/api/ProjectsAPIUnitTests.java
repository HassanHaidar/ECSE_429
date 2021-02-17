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
import javax.xml.transform.TransformerConfigurationException;
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
public class ProjectsAPIUnitTests {
    private static final String PROJECT_CATEGORIES = "projects/{id}/categories";
    private static final String PROJECT_CATEGORIES_ID = "projects/{id}/categories/{categoryId}";

    private static final String PROJECT_TASKS = "projects/{id}/tasks";
    private static final String PROJECT_TASKS_ID = "projects/{id}/tasks/{taskId}";

    private static final String CATEGORY_TITLE = "cia deserunt mollita";
    private static final String CATEGORY_DESCRIPTION = "dolore eu fugiat nua";

    private static final String TODO_TITLE = "cia deserunt mollita";
    private static final String TODO_DESCRIPTION = "dolore eu fugiat nua";

    private static final String ID_FIELD = "id";
    private static final String TITLE_FIELD = "title";
    private static final String DESCRIPTION_FIELD = "description";
    private static final String COMPLETED_FIELD = "completed";
    private static final String ACTIVE_FIELD = "active";
    private static final String DONE_FIELD = "doneStatus";

    private static final String PROJECT_DESCRIPTION = "itation ullamco labo";
    private static final String PROJECT_TITLE = "test project";

    private static final String ERROR = "errorMessages";
    private static final String BAD_REQUEST = "Could not find field: ";
    private static final String NOT_FOUND = "Could not find any instances with ";
    public static final String ERRONEOUS_FIELD = "erroneous_field";

    private static int projectId = 0;
    private static int categoryId = 0;
    private static int taskId = 0;

    @BeforeEach
    public void clearDataFromEnv() {

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
    public void postProjectSingleFieldJsonSuccess() {
        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(TITLE_FIELD, PROJECT_TITLE);

        given().body(jsonBody)
                .when().post("/projects")
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_CREATED)
                .body(TITLE_FIELD, equalTo(PROJECT_TITLE));
    }

    @Test
    public void postProjectAllFieldsJsonSuccess() {
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

    @Test
    public void postProjectAllFieldsJsonFailure() {
        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(ERRONEOUS_FIELD, PROJECT_TITLE);
        jsonBody.put(COMPLETED_FIELD, false);
        jsonBody.put(ACTIVE_FIELD, false);
        jsonBody.put(DESCRIPTION_FIELD, PROJECT_DESCRIPTION);

        String response;
        response = given().body(jsonBody)
                .when().post("/projects")
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
    public void postProjectAllFieldsXMLSuccess() throws ParserConfigurationException, TransformerException {
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
                accept(ContentType.XML).
                contentType(ContentType.XML).
                when().
                post("/projects").
                then().
                statusCode(HttpStatus.SC_CREATED).
                contentType(ContentType.XML).
                body("projects." + TITLE_FIELD, equalTo(PROJECT_TITLE),
                        "project." + ACTIVE_FIELD, equalTo("false"),
                        "project." + COMPLETED_FIELD, equalTo("false"),
                        "project." + DESCRIPTION_FIELD, equalTo(PROJECT_DESCRIPTION));
    }

    @Test
    public void getProjectsReturnsProjectsSuccess() {
        postProjectSingleFieldJsonSuccess();
        postProjectAllFieldsJsonSuccess();

        List<Map<String, Object>> projects = when().
                get("/projects").then().
                contentType(ContentType.JSON).
                statusCode(HttpStatus.SC_OK).
                extract().
                body().
                jsonPath().
                getList("projects");

        Assertions.assertEquals(projects.size(), 2);
        Assertions.assertTrue(projects.stream().allMatch(object -> object.get(TITLE_FIELD).equals(PROJECT_TITLE) ||
                object.get(TITLE_FIELD).equals(PROJECT_TITLE)));
    }

    @Test
    public void getProjectsReturnsProjectsFilterByCompletedSuccess() {
        postProjectSingleFieldJsonSuccess();
        postProject(true);

        List<Map<String, Object>> projects = when().
                get("/projects?completed=true").then().
                contentType(ContentType.JSON).
                statusCode(HttpStatus.SC_OK).
                extract().
                body().
                jsonPath().
                getList("projects");

        Assertions.assertEquals(1, projects.size());
        Assertions.assertEquals("true", projects.get(0).get("completed"));
    }

    @Test
    public void getProjectsReturnsProjectsFilterTitleSuccess() {
        postProjectSingleFieldJsonSuccess();
        postProject("Title");

        List<Map<String, Object>> projects = when().
                get("/projects?title=Title").then().
                contentType(ContentType.JSON).
                statusCode(HttpStatus.SC_OK).
                extract().
                body().
                jsonPath().
                getList("projects");

        Assertions.assertEquals(1, projects.size());
        Assertions.assertEquals("Title", projects.get(0).get("title"));
    }


    @Test
    public void deleteProjectsNotAllowed() {
        given().
                contentType(ContentType.JSON).
                accept(ContentType.JSON).
                when().
                delete("/projects").
                then().
                statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    public void deleteProjectByIDProjectSuccess() {
        // populate body
        postProjectAllFieldsJsonSuccess();
        given().
                pathParam(ID_FIELD, projectId).
                when().
                get("/projects/{id}").
                then().
                statusCode(HttpStatus.SC_OK).
                contentType(ContentType.JSON);
    }

    @Test
    public void deleteDeletedProjectByIDProjectSuccess() {
        // populate body
        postProjectAllFieldsJsonSuccess();
        given().
                pathParam(ID_FIELD, projectId).
                when().
                delete("/projects/{id}").
                then().
                statusCode(HttpStatus.SC_OK).
                contentType(ContentType.JSON);

        given().
                pathParam(ID_FIELD, projectId).
                when().
                delete("/projects/{id}").
                then().
                statusCode(HttpStatus.SC_NOT_FOUND).
                contentType(ContentType.JSON);
    }

    @Test
    public void getProjectByIDProjectSuccess() {
        // populate body
        postProjectAllFieldsJsonSuccess();
        Map<String, Object> response;
        response = (Map<String, Object>) given().
                pathParam(ID_FIELD, projectId).
                when().
                get("/projects/{id}").
                then().
                statusCode(HttpStatus.SC_OK).
                contentType(ContentType.JSON).
                extract().
                body().
                jsonPath().
                getList("projects").
                get(0);

        Assertions.assertEquals(String.valueOf(projectId), response.get(ID_FIELD));
        Assertions.assertEquals("false", response.get(ACTIVE_FIELD));
        Assertions.assertEquals("false", response.get(COMPLETED_FIELD));
        Assertions.assertEquals(PROJECT_DESCRIPTION, response.get(DESCRIPTION_FIELD));
    }

    @Test
    public void getProjectByIdXMLSuccess() {
        // populate body
        postProjectAllFieldsJsonSuccess();
        given().
                pathParam(ID_FIELD, projectId).
                accept(ContentType.XML).
                when().
                get("/projects/{id}").
                then().
                statusCode(HttpStatus.SC_OK).
                contentType(ContentType.XML).
                body("projects.project." + TITLE_FIELD, equalTo(PROJECT_TITLE),
                        "project.project." + ACTIVE_FIELD, equalTo("false"),
                        "project.project." + COMPLETED_FIELD, equalTo("false"),
                        "project.project." + DESCRIPTION_FIELD, equalTo(PROJECT_DESCRIPTION));
    }

    @Test
    public void getProjectByIDProjectNotFound() {
        // populate body
        String response;
        response = (String) given().
                pathParam(ID_FIELD, projectId).
                when().
                get("/projects/{id}").
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
    public void postProjectWithIdJSONSuccess() {
        postProjectAllFieldsJsonSuccess();

        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(TITLE_FIELD, "random title");
        jsonBody.put(COMPLETED_FIELD, true);
        jsonBody.put(ACTIVE_FIELD, true);
        jsonBody.put(DESCRIPTION_FIELD, "random description");

        given().pathParam("id", projectId)
                .body(jsonBody)
                .when().post("/projects/{id}")
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_OK)
                .body(TITLE_FIELD, equalTo("random title"),
                        COMPLETED_FIELD, equalTo("true"),
                        ACTIVE_FIELD, equalTo("true"),
                        DESCRIPTION_FIELD, equalTo("random description"));
    }

    @Test
    public void putProjectWithIdJSONSuccess() {
        postProjectAllFieldsJsonSuccess();

        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(TITLE_FIELD, "random title");
        jsonBody.put(COMPLETED_FIELD, true);
        jsonBody.put(ACTIVE_FIELD, true);
        jsonBody.put(DESCRIPTION_FIELD, "random description");

        given().pathParam("id", projectId)
                .body(jsonBody)
                .when().put("/projects/{id}")
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_OK)
                .body(TITLE_FIELD, equalTo("random title"),
                        COMPLETED_FIELD, equalTo("true"),
                        ACTIVE_FIELD, equalTo("true"),
                        DESCRIPTION_FIELD, equalTo("random description"));
    }

    @Test
    public void postProjectWithIdNotFound() {
        postProjectAllFieldsJsonSuccess();

        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(TITLE_FIELD, "random title");
        jsonBody.put(COMPLETED_FIELD, true);
        jsonBody.put(ACTIVE_FIELD, true);
        jsonBody.put(DESCRIPTION_FIELD, "random description");
        given()
                .body(jsonBody)
                .pathParam("id", 136812)
                .when()
                .post("/projects/{id}")
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void postProjectWithIdJSONTitleDoesNotChangeOtherFieldsSuccess() {
        postProjectAllFieldsJsonSuccess();

        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(TITLE_FIELD, "random title");

        given().pathParam("id", projectId)
                .body(jsonBody)
                .when().post("/projects/{id}")
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_OK)
                .body(TITLE_FIELD, equalTo("random title"),
                        COMPLETED_FIELD, equalTo("false"),
                        ACTIVE_FIELD, equalTo("false"),
                        DESCRIPTION_FIELD, equalTo(PROJECT_DESCRIPTION));
    }

    @Test
    public void postProjectWithIdJSONDescriptionDoesNotChangeOtherFields() {
        postProjectAllFieldsJsonSuccess();

        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(DESCRIPTION_FIELD, "random description");

        given().pathParam("id", projectId)
                .body(jsonBody)
                .when().post("/projects/{id}")
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_OK)
                .body(TITLE_FIELD, equalTo(PROJECT_TITLE),
                        COMPLETED_FIELD, equalTo("false"),
                        ACTIVE_FIELD, equalTo("false"),
                        DESCRIPTION_FIELD, equalTo("random description"));
    }

    @Test
    public void postProjectWithIdJSONActiveDoesNotChangeOtherFieldsSuccess() {
        postProjectAllFieldsJsonSuccess();

        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(ACTIVE_FIELD, true);

        given().pathParam("id", projectId)
                .body(jsonBody)
                .when().post("/projects/{id}")
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_OK)
                .body(TITLE_FIELD, equalTo(PROJECT_TITLE),
                        COMPLETED_FIELD, equalTo("false"),
                        ACTIVE_FIELD, equalTo("true"),
                        DESCRIPTION_FIELD, equalTo(PROJECT_DESCRIPTION));
    }

    @Test
    public void postProjectWithIdJSONCompletedDoesNotChangeOtherFields() {
        postProjectAllFieldsJsonSuccess();

        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(COMPLETED_FIELD, true);

        given().pathParam("id", projectId)
                .body(jsonBody)
                .when().post("/projects/{id}")
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_OK)
                .body(TITLE_FIELD, equalTo(PROJECT_TITLE),
                        COMPLETED_FIELD, equalTo("true"),
                        ACTIVE_FIELD, equalTo("false"),
                        DESCRIPTION_FIELD, equalTo(PROJECT_DESCRIPTION));
    }

    @Test
    public void postCategoryForProjectJsonSuccess() {
        postProjectAllFieldsJsonSuccess();

        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(TITLE_FIELD, CATEGORY_TITLE);
        jsonBody.put(DESCRIPTION_FIELD, CATEGORY_DESCRIPTION);

        String responseId = given().pathParam("id", projectId)
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

    @Test
    public void postCategoryTitleOnlyForProjectJsonSuccess() {
        postProjectAllFieldsJsonSuccess();

        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(TITLE_FIELD, CATEGORY_TITLE);

        String responseId = given().pathParam("id", projectId)
                .body(jsonBody)
                .when().post(PROJECT_CATEGORIES)
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
    public void postCategoryForProjectXMLSuccess() throws ParserConfigurationException, TransformerException, TransformerConfigurationException {
        postProjectAllFieldsJsonSuccess();

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
                pathParam("id", projectId).
                accept(ContentType.XML).
                contentType(ContentType.XML).
                when().
                post(PROJECT_CATEGORIES).
                then().
                statusCode(HttpStatus.SC_CREATED).
                contentType(ContentType.XML).
                body("project" + "." + TITLE_FIELD, equalTo(CATEGORY_TITLE),
                        "project" + "." + DESCRIPTION_FIELD, equalTo(CATEGORY_DESCRIPTION));
    }

    @Test
    public void getCategoriesForProjectJsonSuccess() {
        postProjectAllFieldsJsonSuccess();
        postCategoryForProject(projectId);
        postCategoryForProject(projectId);

        List<Map<String, Object>> categories = given().
                pathParam("id", projectId).
                when().
                get(PROJECT_CATEGORIES).then().
                contentType(ContentType.JSON).
                statusCode(HttpStatus.SC_OK).
                extract().
                body().
                jsonPath().
                getList("categories");

        Assertions.assertEquals(2, categories.size());
        for (Map<String, Object> category : categories) {
            Assertions.assertEquals(CATEGORY_TITLE, category.get(TITLE_FIELD));
            Assertions.assertEquals(CATEGORY_DESCRIPTION, category.get(DESCRIPTION_FIELD));
        }
    }
    @Test
    public void getCategoriesForProjectFilterByTitleJsonSuccess() {
        postProjectAllFieldsJsonSuccess();
        postCategoryForProject(projectId);
        postCategoryForProject(projectId, "Title");

        List<Map<String, Object>> categories = given().
                pathParam("id", projectId).
                when().
                get(PROJECT_CATEGORIES+"?title=Title").then().
                contentType(ContentType.JSON).
                statusCode(HttpStatus.SC_OK).
                extract().
                body().
                jsonPath().
                getList("categories");

        Assertions.assertEquals(1, categories.size());
        for (Map<String, Object> category : categories) {
            Assertions.assertEquals("Title", category.get(TITLE_FIELD));
            Assertions.assertEquals(CATEGORY_DESCRIPTION, category.get(DESCRIPTION_FIELD));
        }
    }

    @Test
    public void getCategoriesForProjectXMLSuccess() {
        postProjectAllFieldsJsonSuccess();
        postCategoryForProject(projectId);
        postCategoryForProject(projectId);

        given().
                pathParam("id", projectId).
                accept(ContentType.XML).
                when().
                get(PROJECT_CATEGORIES).then().
                contentType(ContentType.XML).
                statusCode(HttpStatus.SC_OK).
                assertThat()
                .body("categories.category[0].title", equalTo(CATEGORY_TITLE),
                        "categories.category[1].title", equalTo(CATEGORY_TITLE),
                        "categories.category[0].description", equalTo(CATEGORY_DESCRIPTION),
                        "categories.category[1].description", equalTo(CATEGORY_DESCRIPTION));
    }

    // Bug. It returns ok with empty array even though it is preferred to return
    // project not found
    @Test
    public void getCategoriesForProjectNotFound() {
        List<Map<String, Object>> categories = given().
                pathParam("id", projectId).
                when().
                get(PROJECT_CATEGORIES).
                then().
                contentType(ContentType.JSON).
                statusCode(HttpStatus.SC_OK).
                extract().
                body().
                jsonPath().
                getList("projects");

        Assertions.assertEquals(0, categories.size());
    }

    @Test
    public void deleteCategoriesForProjectJsonFailure() {
        postProjectAllFieldsJsonSuccess();
        postCategoryForProject(projectId);
        given().
                when().
                delete(PROJECT_CATEGORIES_ID, projectId, categoryId).
                then().
                contentType(ContentType.JSON).
                statusCode(HttpStatus.SC_OK);

        String response;
        response = given().
                when().
                delete(PROJECT_CATEGORIES_ID, projectId, categoryId).
                then().
                contentType(ContentType.JSON).
                statusCode(HttpStatus.SC_NOT_FOUND)
                .extract().
                        body().
                        jsonPath().
                        getList(ERROR).
                        get(0).toString();
        Assertions.assertEquals(NOT_FOUND + String.format("projects/%s/categories/%s", projectId, categoryId),response);
    }


    @Test
    public void deleteCategoriesForProjectXMLSuccess() {
        postProjectAllFieldsJsonSuccess();
        postCategoryForProject(projectId);
        given().
                accept(ContentType.XML).
                when().
                delete(PROJECT_CATEGORIES_ID, projectId, categoryId).
                then().
                contentType(ContentType.XML).
                statusCode(HttpStatus.SC_OK);

    }

    @Test
    public void deleteDeletedCategoriesForProjectXMLFailure() {
        postProjectAllFieldsJsonSuccess();
        postCategoryForProject(projectId);
        given().
                accept(ContentType.XML).
                when().
                delete(PROJECT_CATEGORIES_ID, projectId, categoryId).
                then().
                contentType(ContentType.XML).
                statusCode(HttpStatus.SC_OK);

        String response = given().
                accept(ContentType.XML).
                when().
                delete(PROJECT_CATEGORIES_ID, projectId, categoryId).
                then().
                contentType(ContentType.XML).
                statusCode(HttpStatus.SC_NOT_FOUND)
                .extract().
                        body().
                        xmlPath().
                        getList(ERROR).
                        get(0).toString();

        Assertions.assertEquals(NOT_FOUND + String.format("projects/%s/categories/%s", projectId, categoryId),response);
    }



    @Test
    public void deleteCategoriesForProjectJsonNotFound() {
        given().
                when().
                delete(PROJECT_CATEGORIES_ID, projectId, categoryId).
                then().
                contentType(ContentType.JSON).
                statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void postTaskForProjectJsonSuccess() {
        postProjectAllFieldsJsonSuccess();

        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(TITLE_FIELD, TODO_TITLE);
        jsonBody.put(DESCRIPTION_FIELD, TODO_DESCRIPTION);
        jsonBody.put(DONE_FIELD, true);

        String responseId = given().pathParam("id", projectId)
                .body(jsonBody)
                .when().post(PROJECT_TASKS)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_CREATED)
                .body(TITLE_FIELD, equalTo(TODO_TITLE),
                        DESCRIPTION_FIELD, equalTo(TODO_DESCRIPTION),
                        DONE_FIELD, equalTo("true"))
                .extract()
                .path("id");
        categoryId = Integer.parseInt(responseId);
    }

    @Test
    public void postTaskTitleOnlyForProjectJsonSuccess() {
        postProjectAllFieldsJsonSuccess();

        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(TITLE_FIELD, TODO_TITLE);

        String responseId = given().pathParam("id", projectId)
                .body(jsonBody)
                .when().post(PROJECT_TASKS)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_CREATED)
                .body(TITLE_FIELD, equalTo(TODO_TITLE),
                        DESCRIPTION_FIELD, equalTo(""),
                        DONE_FIELD, equalTo("false"))
                .extract()
                .path("id");
        categoryId = Integer.parseInt(responseId);
    }

    @Test
    public void postTaskForProjectXMLSuccess() throws ParserConfigurationException, TransformerException{
        postProjectAllFieldsJsonSuccess();

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Document xmlDocument = documentBuilder.newDocument();
        Element request = xmlDocument.createElement("todo");
        xmlDocument.appendChild(request);

        Element title = xmlDocument.createElement(TITLE_FIELD);
        title.appendChild(xmlDocument.createTextNode(TODO_TITLE));
        request.appendChild(title);

        Element description = xmlDocument.createElement(DESCRIPTION_FIELD);
        description.appendChild(xmlDocument.createTextNode(TODO_DESCRIPTION));
        request.appendChild(description);

        Element doneStatus = xmlDocument.createElement(DONE_FIELD);
        doneStatus.appendChild(xmlDocument.createTextNode("false"));
        request.appendChild(description);

        Transformer transformer = (TransformerFactory.newInstance()).newTransformer();
        StringWriter stringWriter = new StringWriter();
        transformer.transform(new DOMSource(xmlDocument), new StreamResult(stringWriter));

        String requestXmlBody = stringWriter.getBuffer().toString();
        given().
                body(requestXmlBody).
                pathParam("id", projectId).
                accept(ContentType.XML).
                contentType(ContentType.XML).
                when().
                post(PROJECT_TASKS).
                then().
                statusCode(HttpStatus.SC_CREATED).
                contentType(ContentType.XML).
                body("todo." + TITLE_FIELD, equalTo(TODO_TITLE),
                        "todo." + DESCRIPTION_FIELD, equalTo(TODO_DESCRIPTION),
                        "todo." + DONE_FIELD, equalTo("false"));
    }

    @Test
    public void getTasksForProjectJsonSuccess() {
        postProjectAllFieldsJsonSuccess();
        postTaskForProject(projectId);
        postTaskForProject(projectId);

        List<Map<String, Object>> todos = given().
                pathParam("id", projectId).
                when().
                get(PROJECT_TASKS).then().
                contentType(ContentType.JSON).
                statusCode(HttpStatus.SC_OK).
                extract().
                body().
                jsonPath().
                getList("todos");

        Assertions.assertEquals(2, todos.size());
        for (Map<String, Object> category : todos) {
            Assertions.assertEquals(TODO_TITLE, category.get(TITLE_FIELD));
            Assertions.assertEquals(TODO_DESCRIPTION, category.get(DESCRIPTION_FIELD));
            Assertions.assertEquals("true", category.get(DONE_FIELD));
        }
    }

    @Test
    public void getTasksForProjectFilterByTitleJsonSuccess() {
        postProjectAllFieldsJsonSuccess();
        postTaskForProject(projectId);
        postTaskForProject(projectId, "Title");

        List<Map<String, Object>> todos = given().
                pathParam("id", projectId).
                when().
                get(PROJECT_TASKS+"?title=Title").then().
                contentType(ContentType.JSON).
                statusCode(HttpStatus.SC_OK).
                extract().
                body().
                jsonPath().
                getList("todos");

        Assertions.assertEquals(1, todos.size());
        for (Map<String, Object> category : todos) {
            Assertions.assertEquals("Title", category.get(TITLE_FIELD));
            Assertions.assertEquals(TODO_DESCRIPTION, category.get(DESCRIPTION_FIELD));
            Assertions.assertEquals("true", category.get(DONE_FIELD));
        }
    }

    @Test
    public void getTasksForProjectXMLSuccess() {
        postProjectAllFieldsJsonSuccess();
        postTaskForProject(projectId);
        postTaskForProject(projectId);

        given().
                pathParam("id", projectId).
                accept(ContentType.XML).
                when().
                get(PROJECT_TASKS).then().
                contentType(ContentType.XML).
                statusCode(HttpStatus.SC_OK).
                assertThat()
                .body("todos.todo[0].title", equalTo(TODO_TITLE),
                        "todos.todo[1].title", equalTo(TODO_TITLE),
                        "todos.todo[0].description", equalTo(TODO_DESCRIPTION),
                        "todos.todo[1].description", equalTo(TODO_DESCRIPTION),
                        "todos.todo[0].doneStatus", equalTo("true"),
                        "todos.todo[1].doneStatus", equalTo("true"));
    }

    // Bug. It returns ok with empty array even though it is preferred to return
    // project not found
    @Test
    public void getTasksForProjectNotFound() {
        List<Map<String, Object>> todos = given().
                pathParam("id", projectId).
                when().
                get(PROJECT_TASKS).
                then().
                contentType(ContentType.JSON).
                statusCode(HttpStatus.SC_OK).
                extract().
                body().
                jsonPath().
                getList("projects");

        Assertions.assertEquals(0, todos.size());
    }

    @Test
    public void deleteTaskForProjectJsonSuccess() {
        postProjectAllFieldsJsonSuccess();
        postTaskForProject(projectId);
        given().
                when().
                delete(PROJECT_TASKS_ID, projectId, taskId).
                then().
                contentType(ContentType.JSON).
                statusCode(HttpStatus.SC_OK);
    }


    @Test
    public void deleteTasksForProjectXMLSuccess() {
        postProjectAllFieldsJsonSuccess();
        postTaskForProject(projectId);
        given().
                accept(ContentType.XML).
                when().
                delete(PROJECT_TASKS_ID, projectId, taskId).
                then().
                contentType(ContentType.XML).
                statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void headProjectJson() {
        given().
                when().
                head("/projects").
                then().
                contentType(ContentType.JSON).
                statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void headSpecificProjectJson() {
        postProjectAllFieldsJsonSuccess();
        given().
                when().
                head("/projects/{id}", projectId).
                then().
                contentType(ContentType.JSON).
                statusCode(HttpStatus.SC_OK);
    }


    @Test
    public void headProjectCategoryJson() {
        postProjectAllFieldsJsonSuccess();
        given().
                when().
                head("/projects/{id}/categories",projectId).
                then().
                contentType(ContentType.JSON).
                statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void headProjectTodoJson() {
        given().
                when().
                head("/projects/{id}/categories",projectId).
                then().
                contentType(ContentType.JSON).
                statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void deleteTasksForProjectJsonNotFound() {
        given().
                when().
                delete(PROJECT_TASKS_ID, projectId, taskId).
                then().
                contentType(ContentType.JSON).
                statusCode(HttpStatus.SC_NOT_FOUND);
    }

    // helper methods
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

    private static void postCategoryForProject(int id, String title){
        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(TITLE_FIELD, title);
        jsonBody.put(DESCRIPTION_FIELD, CATEGORY_DESCRIPTION);

        String responseId = given().pathParam("id", id)
                .body(jsonBody)
                .when().post(PROJECT_CATEGORIES)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_CREATED)
                .body(TITLE_FIELD, equalTo(title),
                        DESCRIPTION_FIELD, equalTo(CATEGORY_DESCRIPTION))
                .extract()
                .path("id");
        categoryId = Integer.parseInt(responseId);
    }

    private static void postTaskForProject(int id){
        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(TITLE_FIELD, TODO_TITLE);
        jsonBody.put(DESCRIPTION_FIELD, TODO_DESCRIPTION);
        jsonBody.put(DONE_FIELD, true);

        String responseId = given().pathParam("id", id)
                .body(jsonBody)
                .when().post(PROJECT_TASKS)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_CREATED)
                .body(TITLE_FIELD, equalTo(TODO_TITLE),
                        DESCRIPTION_FIELD, equalTo(TODO_DESCRIPTION),
                        DONE_FIELD, equalTo("true"))
                .extract()
                .path("id");
        taskId = Integer.parseInt(responseId);
    }

    private static void postTaskForProject(int id, String title){
        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(TITLE_FIELD, title);
        jsonBody.put(DESCRIPTION_FIELD, TODO_DESCRIPTION);
        jsonBody.put(DONE_FIELD, true);

        String responseId = given().pathParam("id", id)
                .body(jsonBody)
                .when().post(PROJECT_TASKS)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_CREATED)
                .body(TITLE_FIELD, equalTo(title),
                        DESCRIPTION_FIELD, equalTo(TODO_DESCRIPTION),
                        DONE_FIELD, equalTo("true"))
                .extract()
                .path("id");
        taskId = Integer.parseInt(responseId);
    }

    public void postProject(boolean completed) {
        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(TITLE_FIELD, PROJECT_TITLE);
        jsonBody.put(COMPLETED_FIELD, completed);

        String responseId = given().body(jsonBody)
                .when().post("/projects")
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_CREATED)
                .body(TITLE_FIELD, equalTo(PROJECT_TITLE),
                        COMPLETED_FIELD, equalTo(String.valueOf(completed)))
        .extract()
                .path("id");
        projectId = Integer.parseInt(responseId);
    }

    public void postProject(String title) {
        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(TITLE_FIELD, title);

        String responseId = given().body(jsonBody)
                .when().post("/projects")
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_CREATED)
                .body(TITLE_FIELD, equalTo(title))
                .extract()
                .path("id");
        projectId = Integer.parseInt(responseId);
    }
}
