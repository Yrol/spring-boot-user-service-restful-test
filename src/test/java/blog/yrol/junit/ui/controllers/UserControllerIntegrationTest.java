package blog.yrol.junit.ui.controllers;

import blog.yrol.sceurity.SecurityConstants;
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

    @Test
    @DisplayName("/login works")
    void testUserLogin_whenValidCredentialsProvided_returnsJWTinAuthorizationHeader() throws JSONException {
        // Arrange

        String email = "john@cena.com";
        String password = "12345678";
        String authorizationToken;

        /**
         * User Create request
         * **/
        this.createUser("John", "Cena", email, password, password);


        /**
         * Login request
         * **/
        JSONObject loginCredentials = new JSONObject();
        loginCredentials.put("email", email);
        loginCredentials.put("password", password);

        HttpEntity<String> loginRequest = new HttpEntity<>(loginCredentials.toString());

        // Act
        /**
         * Attempting to login using the newly created user
         * The authentication post endpoint (/users/login) set up in: src/main/java/blog/yrol/sceurity/WebSecurity.java
         * **/
        ResponseEntity<Object> response = testRestTemplate.postForEntity("/users/login", loginRequest, null);

        authorizationToken = response.getHeaders().
                getValuesAsList(SecurityConstants.HEADER_STRING).get(0);

        System.out.println(authorizationToken);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP status code should be 200");

        /**
         * Making sure the Authorization header is not null
         * **/
        Assertions.assertNotNull(authorizationToken,
                "Response should contain Authorization header with JWT");

        /**
         * Getting the User ID that has been added to the header
         * **/
        Assertions.assertNotNull(response.getHeaders().
                getValuesAsList("UserID").get(0), "Response should contain UserID in teh header");
    }

    @Test
    @DisplayName("GET /users works")
    void testGetUser_whenValidJWTProvided_returnsUsers() throws JSONException {

        // Arrange
        String email = "john@cena.com";
        String password = "12345678";
        String authorizationToken;

        /**
         * User Create request
         * **/
        this.createUser("John", "Cena", email, password, password);

        /**
         * Login request
         * **/
        JSONObject loginCredentials = new JSONObject();
        loginCredentials.put("email", email);
        loginCredentials.put("password", password);

        HttpEntity<String> loginRequest = new HttpEntity<>(loginCredentials.toString());

        ResponseEntity<Object> loginResponse = testRestTemplate.postForEntity("/users/login", loginRequest, null);

        authorizationToken = loginResponse.getHeaders().
                getValuesAsList(SecurityConstants.HEADER_STRING).get(0);


        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(authorizationToken);

        HttpEntity getUsersHttpEntity = new HttpEntity(headers);

        // Act
        ResponseEntity<List<UserRest>> getUsersResponse = testRestTemplate.exchange("/users",
                HttpMethod.GET,
                getUsersHttpEntity,
                new ParameterizedTypeReference<List<UserRest>>() {
                });

        // Assert check for the response code
        Assertions.assertEquals(HttpStatus.OK, getUsersResponse.getStatusCode());

        // Assert check for the list of users (the users created at the beginning of this test case should exist / returned)
        Assertions.assertTrue(getUsersResponse.getBody().size() == 1, "There should be exactly one user in the list");
    }


    private ResponseEntity<UserRest> createUser(String firstName, String lastName, String email, String password, String repeatPassword) throws JSONException {

        JSONObject userDetailsRequestJson = new JSONObject();
        userDetailsRequestJson.put("firstName", firstName);
        userDetailsRequestJson.put("lastName", lastName);
        userDetailsRequestJson.put("email", email);
        userDetailsRequestJson.put("password", password);
        userDetailsRequestJson.put("repeatPassword", repeatPassword);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<String> request = new HttpEntity<>(userDetailsRequestJson.toString(), httpHeaders);

        return testRestTemplate.postForEntity("/users", request, UserRest.class);
    }
}
