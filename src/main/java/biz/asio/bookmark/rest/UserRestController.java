package biz.asio.bookmark.rest;

import biz.asio.bookmark.model.Bookmark;
import biz.asio.bookmark.model.User;
import biz.asio.bookmark.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/users")
public class UserRestController {

    private UserRepository userRepository;

    @GetMapping(value = "/")
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        users.forEach(user -> user.setPassword(null));
        return users;
    }

    @PostMapping(value = "/update/{userName}")
    public User saveUserBookmarks(@PathVariable String userName, @RequestBody List<Bookmark> bookmarks) {
        User user = userRepository.findUserByUserName(userName);
        user.setPrivateBookmarks(bookmarks);
        userRepository.save(user);
        // remove password on return
        user.setPassword(null);
        return user;
    }
}
