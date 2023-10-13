package blog.yrol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserServicesRest implements CommandLineRunner {

    public static final Logger LOG = LoggerFactory.getLogger(UserServicesRest.class);

    public static void main(String[] args) {
        SpringApplication.run(UserServicesRest.class);
    }


    @Override
    public void run(String... args) throws Exception {
        LOG.info("User Services Rest started");
    }
}