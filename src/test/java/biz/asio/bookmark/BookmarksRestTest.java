package biz.asio.bookmark;

import biz.asio.bookmark.model.Bookmark;
import biz.asio.bookmark.model.User;
import biz.asio.bookmark.repository.UserRepository;
import biz.asio.bookmark.security.model.Message;
import com.fasterxml.jackson.core.type.TypeReference;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static io.restassured.RestAssured.given;

/**
 * Using testcontainers to start the full application with MongoDB in the background and run the tests
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookmarksRestTest extends AbstractServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Order(1)
    @Test
    void test_register_new_user() {
        String requestBody = """
                {
                  "userName": "ilazic",
                  "firstName": "Igor",
                  "lastName": "Lazić",
                  "email": "igor.lazic@asio.biz",
                  "password": "jkDEWghflk218i32!!!"
                }
                """;

        Message response = given()
                .body(requestBody)
                .contentType(ContentType.JSON)
                .post(baseURI + "/auth/register")
                .then()
                .statusCode(200)
                .extract()
                .as(Message.class);

        Assertions.assertEquals("User registered successfully.", response.getMessage());
    }

    @Order(2)
    @Test
    void test_get_all_users() {
        TypeReference<List<User>> typeRef = new TypeReference<>() {};
        List<User> userList = given().header("Authorization", login())
                .get(baseURI + "/users/")
                .then()
                .statusCode(200)
                .extract()
                .as(typeRef.getType());

        Assertions.assertEquals(1, userList.size());
        Assertions.assertEquals("ilazic", userList.get(0).getUserName());
        Assertions.assertEquals("igor.lazic@asio.biz", userList.get(0).getEmail());
    }

    @Order(3)
    @Test
    void test_create_public_bookmark() {

        String requestBody = """
                    {
                        "publicBookmark": true,
                        "ownerUserName": "ilazic",
                        "url": "https://www.java.com/en/",
                        "category": "Programming",
                        "stars": "4 stars"
                    }
                """;

        Bookmark bookmark = given().header("Authorization", login())
                .body(requestBody)
                .contentType(ContentType.JSON)
                .post(baseURI + "/bookmarks/public")
                .then()
                .statusCode(200)
                .extract()
                .as(Bookmark.class);

        Assertions.assertNotNull(bookmark);
        Assertions.assertNotNull(bookmark.getBookmarkId());
    }

    @Order(4)
    @Test
    void test_create_private_bookmark() {

        String requestBody = """
                    [
                      {
                        "ownerUserName": "ilazic",
                        "url": "https://www.spring.io"
                      },
                      {
                        "ownerUserName": "ilazic",
                        "url": "https://www.google.com",
                        "rating": "5 stars"
                      }
                    ]
                """;

        User user = given().header("Authorization", login())
                .body(requestBody)
                .contentType(ContentType.JSON)
                .post(baseURI + "/bookmarks/private")
                .then()
                .statusCode(200)
                .extract()
                .as(User.class);

        Assertions.assertNotNull(user);
        Assertions.assertEquals(2, user.getPrivateBookmarks().size());
    }

    @Order(5)
    @Test
    void test_get_private_bookmark() {
        TypeReference<List<Bookmark>> typeRef = new TypeReference<>() {};
        List<Bookmark> bookmarkList = given().header("Authorization", login())
                .get(baseURI + "/bookmarks/my")
                .then()
                .statusCode(200)
                .extract()
                .as(typeRef.getType());

        Assertions.assertEquals(3, bookmarkList.size());
        // check that the owner of all is ilazic
        Assertions.assertTrue(bookmarkList.stream().allMatch(bookmark -> bookmark.getOwnerUserName().equals("ilazic")));
    }

    @Order(6)
    @Test
    void test_delete_bookmark() {

        String requestBody = """
                      {
                        "url": "https://www.spring.io",
                        "ownerUserName": "ilazic"
                      }
                """;

        given().header("Authorization", login())
                .body(requestBody)
                .contentType(ContentType.JSON)
                .delete(baseURI + "/bookmarks/remove")
                .then()
                .statusCode(200);

        // query the database and make sure only 2 bookmarks are remaining
        User user = userRepository.findUserByUserName("ilazic");

        Assertions.assertEquals(1, user.getPrivateBookmarks().size());
        Assertions.assertTrue(user.getPrivateBookmarks().stream().noneMatch(bookmark -> bookmark.getUrl().equals("https://www.spring.io")));
    }
}
