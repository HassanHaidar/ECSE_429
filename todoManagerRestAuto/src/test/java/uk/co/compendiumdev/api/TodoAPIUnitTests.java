package uk.co.compendiumdev.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;

public class TodoAPIUnitTests {
    // Todos Paths
    private static final String ALL_TODOS_PATH = "/todos";
    private static final String SPECIFIC_TODO_PATH = "/todos/{id}";
    private static final String CLEAR_PATH = "/admin/data/thingifier";
    private static final String TODOS = "todos";
    private static final String TODO= "todo";

    // Todos Fields
    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String DESC = "description";
    private static final String DONE_STATUS = "doneStatus";

    // Test Fields
    private static final String TEST_TITLE = "Do my homework.";
    private static final String TEST_DESC = "So much homework to do.";
    private static final String TEST_TITLE_2 = "Feed the dog.";
    private static final String TEST_DESC_2 = "So many mouths to feed.";

    // Useful Constants
    private static final String EMPTY_STRING = "";
    private static final String WHITESPACE_STRING = " ";
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private static final int INT = 1;
    private static final boolean SOME_BOOLEAN = true;
    private static final String ERROR_TITLE = "title : field is mandatory";
    private static final String CATEGORIES = "categories";

    private static int todoID = 0;
    private static int categoryID = 0;

    // Response Codes

    @BeforeEach
    public void clearDataFromEnv(){

        RestAssured.baseURI = Environment.getBaseUri();
        if(RestAssured.baseURI == null) fail("To Do Manager isn't running!");

        post(CLEAR_PATH);

        final JsonPath clearedData = when().get(ALL_TODOS_PATH)
                .then().statusCode(200).extract().body().jsonPath();

        final int newNumberOfTodos = clearedData.getList(TODOS).size();

        // Assume instead of
        Assumptions.assumeTrue(newNumberOfTodos == 0);
    }


    @Test
    public void postTodoSingleFieldJsonSuccess(){
        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(TITLE, TEST_TITLE);

        String responseId = given().body(jsonBody)
                .when().post(ALL_TODOS_PATH)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_CREATED)
                .body(TITLE, equalTo(TEST_TITLE),
                        DESC, equalTo(EMPTY_STRING),
                        DONE_STATUS, equalTo(FALSE))
                .extract()
                .path("id");
        todoID = Integer.parseInt(responseId);
    }

    @Test
    public void postTodoMultipleFieldsJsonSuccess(){
        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(TITLE, TEST_TITLE_2);
        jsonBody.put(DESC, TEST_DESC_2);
        jsonBody.put(DONE_STATUS, true);


        given().body(jsonBody)
                .when().post(ALL_TODOS_PATH)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_CREATED)
                .body(TITLE, equalTo(TEST_TITLE_2),
                        DESC, equalTo(TEST_DESC_2),
                        DONE_STATUS, equalTo(TRUE));
    }

    @Test
    public void postUpdateAtTodoIDJsonSuccess(){

        postTodoSingleFieldJsonSuccess();

        String newDesc = "This is blursed. Blessed, and also cursed.";
        final HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put(DESC, newDesc);
        jsonBody.put(DONE_STATUS, true);


        given().body(jsonBody)
                .when().post(ALL_TODOS_PATH +"/" +todoID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_OK)
                .body(TITLE, equalTo(TEST_TITLE),
                        DESC, equalTo(newDesc),
                        DONE_STATUS, equalTo(TRUE));
    }



    @Test
    public void postIntegerDoneStatus(){
        final HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(TITLE, TEST_TITLE);
        givenBody.put(DESC, TEST_DESC);
        givenBody.put(DONE_STATUS, INT);


        given().
                body(givenBody).
                when().
                post(ALL_TODOS_PATH)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }




    @Test
    public void postStringDoneStatus(){
        final HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(TITLE, TEST_TITLE);
        givenBody.put(DESC, TEST_DESC);
        givenBody.put(DONE_STATUS, "almost");


        given().
                body(givenBody).
                when().
                post(ALL_TODOS_PATH)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void postEmptyTodo(){
        final HashMap<String, Object> givenBody = new HashMap<>();
        String response = given().
                body(givenBody).
                when().
                post(ALL_TODOS_PATH)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract().body().jsonPath().getList("errorMessages").get(0).toString();

        Assertions.assertEquals(response, ERROR_TITLE);
    }

    @Test
    public void postTodoWithIDNoTitle(){

        postTodoSingleFieldJsonSuccess();

        final HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(DESC, TEST_DESC);
        givenBody.put(DONE_STATUS, true);

        given().body(givenBody)
                .when().post(ALL_TODOS_PATH +"/"+todoID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_OK)
                .body(TITLE, equalTo(TEST_TITLE),
                        DESC, equalTo(TEST_DESC),
                        DONE_STATUS, equalTo(TRUE));

        //Assertions.assertEquals(response, ERROR_TITLE);
    }

    @Test
    public void postTodoIDNonExistent(){


        final HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(DESC, TEST_DESC);
        givenBody.put(DONE_STATUS, true);

        given().body(givenBody)
                .when().post(ALL_TODOS_PATH +"/"+todoID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_NOT_FOUND);
        //Assertions.assertEquals(response, ERROR_TITLE);
    }

    @Test
    public void deleteTodoWithID(){

        postTodoSingleFieldJsonSuccess();


        when().delete(ALL_TODOS_PATH +"/"+todoID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_OK);

        //Can't find it after deletion
        when().get(ALL_TODOS_PATH +"/"+todoID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_NOT_FOUND);

    }

    @Test
    public void postTodoWithIDBadTitle(){

        postTodoSingleFieldJsonSuccess();

        final HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(TITLE, WHITESPACE_STRING);
        givenBody.put(DESC, TEST_DESC);
        givenBody.put(DONE_STATUS, true);

        given().body(givenBody)
                .when().post(ALL_TODOS_PATH +"/"+todoID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void getTodos(){

        postTodoSingleFieldJsonSuccess();
        postTodoMultipleFieldsJsonSuccess();


        List<Map<String, Object>> responses =
                when().
                get(ALL_TODOS_PATH)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_OK)
                .extract().body().jsonPath().getList(TODOS);

        Assertions.assertEquals(responses.size(), 2);
        Assertions.assertEquals(responses.get(0).get(TITLE),TEST_TITLE);
        Assertions.assertEquals(responses.get(1).get(TITLE),TEST_TITLE_2);

    }



    @Test
    public void postWhiteSpaceTitleError(){
        final HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(TITLE, WHITESPACE_STRING);
        givenBody.put(DESC, TEST_DESC);
        givenBody.put(DONE_STATUS, true);


        given().
                body(givenBody).
                when().
                post(ALL_TODOS_PATH)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }


    @Test
    public void getTodoWithId(){

        postTodoSingleFieldJsonSuccess();


        List<Map<String, Object>> responses =
                when().
                        get(ALL_TODOS_PATH +"/" + todoID)
                        .then()
                        .contentType(ContentType.JSON)
                        .statusCode(HttpStatus.SC_OK)
                        .extract().body().jsonPath().getList(TODOS);

        Assertions.assertEquals(responses.size(), 1);
        Assertions.assertEquals(responses.get(0).get(TITLE),TEST_TITLE);

    }


    @Test
    public void getTodoCategory(){

        postTodoSingleFieldJsonSuccess();


        List<Map<String, Object>> responses =
                when().
                        get(ALL_TODOS_PATH +"/" + todoID + "/"+ CATEGORIES)
                        .then()
                        .contentType(ContentType.JSON)
                        .statusCode(HttpStatus.SC_OK)
                        .extract().body().jsonPath().getList(CATEGORIES);

        Assertions.assertEquals(responses.size(), 0);

    }

    //BUG: UNEXPECTED BEHAVIOR: CREATES CATEGORY FROM SCRATCH

    @Test
    public void postTodoCategoryNonExistent(){

        postTodoSingleFieldJsonSuccess();

        final HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(ID, 1);

        given().body(givenBody).when().
                post(ALL_TODOS_PATH +"/" + todoID + "/"+ CATEGORIES)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .extract().body().jsonPath().getList(CATEGORIES);

    }

    //BUG/ISSUE: Don't define an ID, but it creates a category with that title? Is this expected behavior?
    @Test
    public void postTodoCategorySuccess(){

        postTodoSingleFieldJsonSuccess();

        final HashMap<String, Object> givenBody = new HashMap<>();
        givenBody.put(TITLE, TEST_TITLE_2);


        String responseId = given().body(givenBody).
            when().
            post(ALL_TODOS_PATH +"/" + todoID + "/"+ CATEGORIES)
            .then()
            .contentType(ContentType.JSON)
            .statusCode(HttpStatus.SC_CREATED)
                .body(TITLE, equalTo(TEST_TITLE_2),
                        DESC, equalTo(EMPTY_STRING)).extract().path(ID);

        categoryID = Integer.parseInt(responseId);

    }

    //BUG: The association just made fails to get recognized!
    @Test
    public void getTodoCategoryWithIDs(){

        postTodoCategorySuccess();

        when().
                get(ALL_TODOS_PATH +"/" + todoID + "/"+ CATEGORIES + "/" + categoryID)
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);


    }

    @Test
    public void postTodoCategoryNotDefined(){

        postTodoSingleFieldJsonSuccess();


        when().
            post(ALL_TODOS_PATH +"/" + todoID + "/"+ CATEGORIES)
            .then()
            .contentType(ContentType.JSON)
            .statusCode(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE);

    }

    //xml
    @Test
    public void postTodoXML() throws ParserConfigurationException, TransformerException, TransformerConfigurationException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();

        Document body = documentBuilder.newDocument();
        Element root = body.createElement(TODO);
        body.appendChild(root);

        Element title = body.createElement(TITLE);
        title.appendChild(body.createTextNode(TEST_TITLE));
        root.appendChild(title);

        Element doneStatus = body.createElement(DONE_STATUS);
        doneStatus.appendChild(body.createTextNode(TRUE));
        root.appendChild(doneStatus);

        Element description = body.createElement(DESC);
        description.appendChild(body.createTextNode(TEST_DESC));
        root.appendChild(description);

        Transformer transformer = (TransformerFactory.newInstance()).newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(body), new StreamResult(writer));

        String xmlBody = writer.getBuffer().toString();

        given().
                body(xmlBody).
                contentType(ContentType.XML).
                accept(ContentType.XML).
                when().
                post(ALL_TODOS_PATH).
                then().
                contentType(ContentType.XML).
                statusCode(HttpStatus.SC_CREATED).
                body(
                        TODO+"."+TITLE, equalTo(TEST_TITLE),
                        TODO+"."+DESC, equalTo(TEST_DESC),
                        TODO+"."+DONE_STATUS, equalTo(TRUE)

                );
    }


    // the taskof tests go bellow
    
    // create a taskof
    
    //get a taskof
    
    // BUG: return a project when we use an non existent ID of a todo.


    
    
     
}    