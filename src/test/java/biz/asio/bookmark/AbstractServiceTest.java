package biz.asio.bookmark;

import biz.asio.bookmark.security.model.Token;
import io.restassured.http.ContentType;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import static io.restassured.RestAssured.given;

@TestComponent
@SpringBootTest(webEnvironment =  SpringBootTest.WebEnvironment.DEFINED_PORT, classes = BookmarkApplication.class)
public class AbstractServiceTest {

    public static final String baseURI = "http://localhost:8080/api";

    static final MongoDBContainer mongoDBContainer;

    static {
        mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.0.10"))
                .withExposedPorts(27017);
        mongoDBContainer.start();
    }

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    /**
     * Call login and get token for each test
     * @return token object
     */
    protected String login() {
        String requestBody = """
                            {
                              "userName": "ilazic",
                              "password": "jkDEWghflk218i32!!!"
                            }
                    """;

        Token ticketJSON = given()
                .body(requestBody)
                .contentType(ContentType.JSON)
                .post(baseURI + "/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .as(Token.class);

        // save for further tests
        return "Bearer " + ticketJSON.getToken();
    }
}
