package blog.yrol.junit.ui.controllers;

import blog.yrol.ui.response.UserRest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.List;


/**
 * Integration test which integrates all 3 layers (web layer, service layer and data layer) with Spring Context when executing the test (without any mocking).
 * @SpringBootTest - will start a real webserver and make real http calls to run tests
 * @SpringBootTest allow Spring Boot to looks for the main application class: src/main/java/blog/yrol/UserServicesRest.java and it will also start the spring application context.
 * @SpringBootTest - will create spring beans related to all 3 layers (web layer, service layer and data layer) and will be added to spring application context.
 * SpringBootTest.WebEnvironment.DEFINED_PORT - will run tests in the default localhost:8080 or the defined port in application.properties, if not overridden by properties.
 **/

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // Running o a random port
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = {"server.port=8081", "hostname=192.168.0.2"})
public class UserControllerIntegrationTest {


    /**
     * Assigning the current port to a local var
     * **/
    @LocalServerPort
    private int localServerPort;

    /**
     * Using TestRestTemplate for sending HTTP request
     * **/
    @Autowired
    private TestRestTemplate testRestTemplate;

    /**
     * Test for creating users
     * **/
    @Test
    @DisplayName("User can be created")
    void testCreateUser_whenValidDetailsProvided_returnsUserDetails() throws JSONException {
        // Arrange
        JSONObject userDetailsRequestJson = new JSONObject();
        userDetailsRequestJson.put("firstName", "Yrol");
        userDetailsRequestJson.put("lastName", "Fernando");
        userDetailsRequestJson.put("email", "test@test.com");
        userDetailsRequestJson.put("password", "12345678");
        userDetailsRequestJson.put("repeatPassword", "12345678");

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<String> request = new HttpEntity<>(userDetailsRequestJson.toString(), httpHeaders);

        // Act
        /**
         * Posting the HTTP request and returning UserRest type (the type returned by createUser() in UserRestController)
         * **/
        ResponseEntity<UserRest> createUserResponse = testRestTemplate.postForEntity("/users", request, UserRest.class);
        UserRest createdUserDetails = createUserResponse.getBody();

        // Assert
        Assertions.assertEquals(HttpStatus.OK, createUserResponse.getStatusCode());
        Assertions.assertEquals(userDetailsRequestJson.getString("firstName"), createdUserDetails.getFirstName(), "The first name doesn't match");
        Assertions.assertEquals(userDetailsRequestJson.getString("lastName"), createdUserDetails.getLastName(), "The last name doesn't match");
        Assertions.assertEquals(userDetailsRequestJson.getString("email"), createdUserDetails.getEmail(), "The email doesn't match");
    }

    /**
     * Attempting to get users without a JWT
     * **/
    @Test
    @DisplayName("GET /users requires JWT")
    void testGetUsers_whenMissigJWT_returns403() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "Application/json");

        HttpEntity requestEntity = new HttpEntity(null, headers);

        // Act
        /**
         * Sending an HTTP GET request
         * Returning a List of users (of type UserRest)
         * **/
        ResponseEntity<List<UserRest>> response = testRestTemplate.exchange("/users",
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<List<UserRest>>() {
                });

        // Assert
        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(), "HTTP code 403 should've been returned.");
    }
}
