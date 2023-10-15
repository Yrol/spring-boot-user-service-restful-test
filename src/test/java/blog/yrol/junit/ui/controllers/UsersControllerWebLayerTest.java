package blog.yrol.junit.ui.controllers;

import blog.yrol.service.UsersService;
import blog.yrol.shared.UserDto;
import blog.yrol.ui.controllers.UsersController;
import blog.yrol.ui.request.UserDetailsRequestModel;
import blog.yrol.ui.response.UserRest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Using @WebMvcTest only to make sure ONLY the web layer is tested (i.e. no service layer & etc and the beans related to the web layer will only be added to spring context when running the test).
 * Also restricting to run tests against "blog.yrol.ui.controllers.UsersController" only (or adding this class only to the spring context when running tests).
 * @AutoConfigureMockMvc(addFilters = false) - exclude @service, @Component and @repository loading into teh spring context when running
 * **/
@WebMvcTest(controllers = UsersController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UsersControllerWebLayerTest {

    @Autowired
    private MockMvc mockMvc;


    /**
     * Using MockBean to mock the UsersService layer. This will be added to the Spring context.
     */
    @MockBean
    UsersService usersService;


    UserDetailsRequestModel userDetailsRequestModel;


    /**
     * Common test code before each test method
     * **/
    @BeforeEach
    void beforeEachTestMethod() {
        userDetailsRequestModel = new UserDetailsRequestModel();
        userDetailsRequestModel.setFirstName("Yrol");
        userDetailsRequestModel.setLastName("Fernando");
        userDetailsRequestModel.setEmail("test@test.com");
        userDetailsRequestModel.setPassword("12345678");
        userDetailsRequestModel.setRepeatPassword("12345678");
    }

    @Test
    @DisplayName("User can be created")
    void testCreateUser_whenValidUserDetailsProvided_returnCreatedUserDetails() throws Exception {
        // Arrange

        // Manually creating the UserDto
//        UserDto userDto = new UserDto();
//        userDto.setUserId(UUID.randomUUID().toString());
//        userDto.setFirstName("Yrol");
//        userDto.setLastName("Fernando");
//        userDto.setEmail("test@test.com");

        /**
         * Mocking usersService.createUser and return the mock
         * Creating the user DTO using ModelMapper and userDetailsRequestModel above
         * **/
        UserDto userDto = new ModelMapper().map(userDetailsRequestModel, UserDto.class);
        userDto.setUserId(UUID.randomUUID().toString());
        when(usersService.createUser(any(UserDto.class))).thenReturn(userDto);

        /**
         * Creating a mock request using MockMvcRequestBuilders and pointing to "/users" controller
         * Also preparing to send header and body data
         * **/
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userDetailsRequestModel));

        // Act
        /**
         * Making a mock request to the controller using mockMvc.perform
         * Getting the result back converting it to string first and then convert it to UserRest type which createUser() returns
         * **/
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        String responseBodyAsString = mvcResult.getResponse().getContentAsString();
        UserRest createdUser = new ObjectMapper().readValue(responseBodyAsString, UserRest.class);

        // Assert
        Assertions.assertEquals(userDetailsRequestModel.getFirstName(), createdUser.getFirstName(), "The returned user first name doesn't match");

    }

    @Test
    @DisplayName("First name is not empty")
    void testCreateUser_whenFirstNameIsNotProvided_returns400StatusCode() throws Exception {

        // Arrange
        /**
         * Setting the first name to null / empty
         * **/
        userDetailsRequestModel.setFirstName("");

        /**
         * Doesn't need to mock usersService.createUser since validation happens prior to this
         * **/
//        UserDto userDto = new ModelMapper().map(userDetailsRequestModel, UserDto.class);
//        userDto.setUserId(UUID.randomUUID().toString());
//        when(usersService.createUser(any(UserDto.class))).thenReturn(userDto);

        /**
         * Creating a mock request using MockMvcRequestBuilders and pointing to "/users" controller
         * Also preparing to send header and body data
         * **/
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userDetailsRequestModel));


        // Act
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();

        // Assert
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus(), "Incorrect HTTP Status Code returned.");
    }

    @Test
    @DisplayName("First name is less than two characters")
    void testCreateUser_whenFirstNameLessThanTwoCharacters_returns400StatusCode() throws Exception {

        // Arrange
        /**
         * Setting the first name to be less than 2 characters
         * **/
        userDetailsRequestModel.setFirstName("Y");


        /**
         * Doesn't need to mock usersService.createUser since validation happens prior to this
         * **/
//        UserDto userDto = new ModelMapper().map(userDetailsRequestModel, UserDto.class);
//        userDto.setUserId(UUID.randomUUID().toString());
//        when(usersService.createUser(any(UserDto.class))).thenReturn(userDto);

        /**
         * Creating a mock request using MockMvcRequestBuilders and pointing to "/users" controller
         * Also preparing to send header and body data
         * **/
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userDetailsRequestModel));

        // Act
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();

        // Assert
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus(), "Incorrect HTTP Status Code returned.");
    }

}
