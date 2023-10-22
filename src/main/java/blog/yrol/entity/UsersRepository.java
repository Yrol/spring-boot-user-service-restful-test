package blog.yrol.entity;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends PagingAndSortingRepository<UserEntity, Long> {
    UserEntity findByEmail(String email);

    UserEntity findByUserId(String userId);

    UserEntity findByEmailEndsWith(String email);

    /*
     * Using a JPQL query to find users ending with email
     */
    @Query("select user from UserEntity user where user.email like %:emailDomain")
    List<UserEntity> findUserWithEmailEndingWith(@Param("emailDomain") String emailDomain);
}
