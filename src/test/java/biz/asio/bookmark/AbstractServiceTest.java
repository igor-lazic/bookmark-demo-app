package biz.asio.bookmark;

import biz.asio.bookmark.security.model.Token;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import static io.restassured.RestAssured.given;

@TestComponent
@SpringBootTest(webEnvironment =  SpringBootTest.WebEnvironment.RANDOM_PORT, classes = BookmarkApplication.class)
@Slf4j
public class AbstractServiceTest {

    public static final String baseURI = "http://localhost:8080/api";
    public static String token = "";

    static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.0.10"));

    static {
        mongoDBContainer.start();
    }

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("mongodb.uri", mongoDBContainer::getConnectionString);
    }

    @BeforeEach
    public void login_test() throws JsonProcessingException {
        if(token != null && !token.isBlank()) {
            Token ticketJSON = given().body("{\"username\":\"marko_test\",\"password\":\"LK60lJ74sZ\"}")
                    .get(baseURI + "/login")
                    .then()
                    .statusCode(200)
                    .extract()
                    .as(Token.class);

            // save for further tests
            token = ticketJSON.getToken();
        }
    }
}
