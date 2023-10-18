package blog.yrol.junit.ui.controllers;

import blog.yrol.sceurity.SecurityConstants;
import blog.yrol.ui.response.UserRest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.List;

/**
 * Single instance test class where the test cases run in an order (@Order) and dependent on each other.
 * The test cases need to be run at the class level in order to pass them successfully (where in UserControllerIntegrationTest, tests can be run independently).
 * **/
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = {"server.port=8081"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserControllerIntegrationSingleInstanceTest {

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

    String email;

    String password;
    
    String authorizationToken;


    /**
     * Creating a new user successfully
     * **/
    @Test
    @DisplayName("User can be created")
    @Order(1)
    void testCreateUser_whenValidDetailsProvided_returnsUserDetails() throws JSONException {
        // Arrange

        email = "test@test.com";
        password = "12345678";

        JSONObject userDetailsRequestJson = new JSONObject();
        userDetailsRequestJson.put("firstName", "Yrol");
        userDetailsRequestJson.put("lastName", "Fernando");
        userDetailsRequestJson.put("email", email);
        userDetailsRequestJson.put("password", password);
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
     * Attempting to get users and fails as expected due to missing JWT
     * **/
    @Test
    @DisplayName("GET /users requires JWT")
    @Order(2)
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
    @Order(3)
    void testUserLogin_whenValidCredentialsProvided_returnsJWTinAuthorizationHeader() throws JSONException {
        // Arrange
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
                getValuesAsList("UserID").get(0), "Response should contain UserID in the header");
    }

    /**
     * Getting users via an authenticated request
     * **/
    @Test
    @DisplayName("GET /users works")
    @Order(4)
    void testGetUser_whenValidJWTProvided_returnsUsers() throws JSONException {

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

}
