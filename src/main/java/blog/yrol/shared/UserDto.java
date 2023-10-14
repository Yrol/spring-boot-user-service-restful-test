package blog.yrol.shared;


import lombok.Data;

@Data
public class UserDto {

    private long id;
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String encryptedPassword;

}
