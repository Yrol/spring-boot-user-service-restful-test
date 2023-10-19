package blog.yrol.junit.io;

import java.util.UUID;

import javax.persistence.PersistenceException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import blog.yrol.entity.UserEntity;

/*
 * Testing the data / entity layer of the application
 * Using @DataJpaTest which is data / entity specific
 */
@DataJpaTest
public class UserEntityIntegrationTest {

    @Autowired
    private TestEntityManager testEntityManager;

    UserEntity userEntity;

    String userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID().toString();
        userEntity = new UserEntity();
        userEntity.setUserId(userId);
        userEntity.setFirstName("Yrol");
        userEntity.setLastName("Fernando");
        userEntity.setEmail("yrol@test.com");
        userEntity.setEncryptedPassword("12345678");
    }

    /*
     * Validating the entities successfully
     */
    @Test
    void testUserEntity_whenValidUserDetailsProvided_shouldReturnStoredUserDetails() {

        // Arrange

        // Act
        /*
         * Persist userEntity object into DB using TestEntityManager
         */
        UserEntity storedUserEntity = testEntityManager.persistAndFlush(userEntity);

        // Assert
        /*
         * The user ID can be anything greater than 0 depending on test case
         * (which consist of user entities) run order
         */
        Assertions.assertTrue(storedUserEntity.getId() > 0, "The user ID of the first user should be equal to 0");
        Assertions.assertTrue(storedUserEntity.getId() == storedUserEntity.getId(), "The user IDs should match");
        Assertions.assertTrue(storedUserEntity.getFirstName() == storedUserEntity.getFirstName(),
                "The user first name should match");
        Assertions.assertTrue(storedUserEntity.getLastName() == storedUserEntity.getLastName(),
                "The user last name should match");
        Assertions.assertTrue(storedUserEntity.getEmail() == storedUserEntity.getEmail(),
                "The email should match");

    }

    /*
     * The first name can only have less than 50 chars
     */
    @Test
    void testUserEntity_whenFirstIsTooLong_shouldThrowException() {
        // Arrange
        userEntity.setFirstName(
                "Loremipsumdolorsitamet,consectetueradipiscingelit.Aeneancommodoligulaegetdolor.Aeneanmassa.Cumsociisnatoquepenatibusetmagnisdisparturientmontes");

        // Assert & Act
        /*
         * Will throw a PersistenceException since the first name cannot be > 50
         * characters
         */
        Assertions.assertThrows(PersistenceException.class, () -> {
            testEntityManager.persistAndFlush(userEntity);
        }, "PersistenceException is expected to be thrown");

    }

    /*
     * The will make sure each user should have a unique ID
     */
    @Test
    void testUserEntity_whenUserIdAlreadyExist_shouldThrowException() {
        UserEntity anotherUser = new UserEntity();
        anotherUser.setUserId(userId);
        anotherUser.setFirstName("John");
        anotherUser.setLastName("Cena");
        anotherUser.setEmail("john@cena.com");
        anotherUser.setEncryptedPassword("12345678");

        /*
         * Creating the other user with same user ID
         */
        testEntityManager.persistAndFlush(anotherUser);

        // Assert & Act
        /*
         * Will throw a PersistenceException since the user ID should be unique for each
         * user
         */
        Assertions.assertThrows(PersistenceException.class, () -> {
            testEntityManager.persistAndFlush(userEntity);
        }, "PersistenceException is expected to be thrown");

    }
}
