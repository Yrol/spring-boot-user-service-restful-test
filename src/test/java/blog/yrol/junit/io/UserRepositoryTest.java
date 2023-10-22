package blog.yrol.junit.io;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import blog.yrol.entity.UserEntity;
import blog.yrol.entity.UsersRepository;

/**
 * Testing the query methods in Repository such as findByEmail & etc
 */
@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    UsersRepository usersRepository;

    UserEntity user;

    String userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID().toString();

        user = new UserEntity();
        user.setFirstName("Yrol");
        user.setLastName("Fernando");
        user.setEmail("yrol@test.com");
        user.setUserId(userId);
        user.setEncryptedPassword("12345678");
    }

    /*
     * Testing findByEmail()
     */
    @Test
    void testFindByEmail_whenGivenCorrectEmail_returnUserEntity() {
        // Arrange
        testEntityManager.persistAndFlush(user);

        // Act
        UserEntity storedUser = usersRepository.findByEmail(user.getEmail());

        // Assert
        Assertions.assertEquals(user.getEmail(), storedUser.getEmail(), "User email should match");
    }

    /*
     * Testing findByUseId
     */
    @Test
    void testFindByUserId_whenGivenCorrectUserId_returnUserEntity() {
        // Arrange
        testEntityManager.persistAndFlush(user);

        // Act
        UserEntity storedUser = usersRepository.findByUserId(userId);

        // Assert
        Assertions.assertNotNull(storedUser, "User should not be null");
        Assertions.assertEquals(user.getUserId(), storedUser.getUserId(), "User ID does not match");
        Assertions.assertEquals(user.getEmail(), storedUser.getEmail(), "User email should match");
    }

    /*
     * Testing the JPQL query method
     */
    @Test
    void testFindUsersWithEmailEndsWith_whenGivenEmailDomain_returnsUserWithGivenDomain() {
        // Arrange
        UserEntity userEntity = new UserEntity();
        userEntity.setFirstName("John");
        userEntity.setLastName("cena");
        userEntity.setEmail("hohn@gmail.com");
        userEntity.setUserId(UUID.randomUUID().toString());
        userEntity.setEncryptedPassword("12345678");
        testEntityManager.persistAndFlush(userEntity);

        String emailDomainName = "@gmail.com";

        // Act
        List<UserEntity> users = usersRepository.findUserWithEmailEndingWith(emailDomainName);

        // Assert
        Assertions.assertEquals(1, users.size(), "There should be only one user in the list");
        Assertions.assertTrue(users.get(0).getEmail().endsWith(emailDomainName));
    }
}
