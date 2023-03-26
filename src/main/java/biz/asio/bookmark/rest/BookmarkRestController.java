package biz.asio.bookmark.rest;

import biz.asio.bookmark.model.Bookmark;
import biz.asio.bookmark.model.User;
import biz.asio.bookmark.repository.BookmarkRepository;
import biz.asio.bookmark.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/bookmarks")
public class BookmarkRestController {

    private BookmarkRepository bookmarkRepository;
    private UserRepository userRepository;

    /**
     * add a link
     * @param bookmark
     * @return
     */
    @PostMapping(value = "/public")
    public Bookmark saveBookmarkForUser(@RequestBody Bookmark bookmark) {
        return bookmarkRepository.save(bookmark);
    }

    @PostMapping(value = "/add/{userName}")
    public User addUserBookmarks(@PathVariable String userName, @RequestBody Bookmark bookmark) {
        User user = userRepository.findUserByUserName(userName);
        user.getPrivateBookmarks().add(bookmark);
        userRepository.save(user);
        // remove password on return
        user.setPassword(null);
        return user;
    }

    /**
     * view users private links
     */
    @GetMapping(value = "/my")
    public List<Bookmark> getCurrentUserBookmarks() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return bookmarkRepository.findBookmarksByOwnerUserName(userDetails.getUsername());
    }

    /**
     * view all public links.
     */
    @GetMapping(value = "/")
    public List<Bookmark> getPublicBookmarks() {
        return bookmarkRepository.findAll();
    }

    /**
     * remove any link they own
     */
    @DeleteMapping(value = "/remove")
    public List<Bookmark> removeBookmark(@RequestBody Bookmark bookmark) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Removing url {} for user {}.", bookmark.getUrl(), userDetails.getUsername());

        return bookmarkRepository.findBookmarksByOwnerUserName(userDetails.getUsername());
    }
}
