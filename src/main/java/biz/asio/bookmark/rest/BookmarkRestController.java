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

    @PostMapping(value = "/private")
    public User saveUserBookmarks(@RequestBody List<Bookmark> bookmarks) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findUserByUserName(userDetails.getUsername());
        user.setPrivateBookmarks(bookmarks);
        userRepository.save(user);
        // remove password on return
        user.setPassword(null);
        return user;
    }

    @PostMapping(value = "/add")
    public User addUserBookmarks(@RequestBody Bookmark bookmark) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findUserByUserName(userDetails.getUsername());
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
        List<Bookmark> bookmarkList = bookmarkRepository.findBookmarksByOwnerUserName(userDetails.getUsername());
        // add private
        User user = userRepository.findUserByUserName(userDetails.getUsername());
        bookmarkList.addAll(user.getPrivateBookmarks());
        return bookmarkList;
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
    public void removeBookmark(@RequestBody Bookmark bookmark) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Removing url {} for user {}.", bookmark.getUrl(), userDetails.getUsername());

        if(!bookmark.getOwnerUserName().equals(userDetails.getUsername())) {
            throw new IllegalAccessException("Not allowed to remove other peoples bookmarks.");
        }

        // delete public
        Bookmark publicBookmark = bookmarkRepository.findBookmarksByUrl(bookmark.getUrl());
        if(publicBookmark != null) {
            bookmarkRepository.deleteById(publicBookmark.getBookmarkId());
        }

        User user = userRepository.findUserByUserName(userDetails.getUsername());
        // delete private (if exists)
        List<Bookmark> privateBookmarks = user.getPrivateBookmarks();
        List<Bookmark> updatedPrivateBookmarks = privateBookmarks.stream()
                .filter(bookmark1 -> !bookmark1.getUrl().equals(bookmark.getUrl()))
                .toList();

        user.setPrivateBookmarks(updatedPrivateBookmarks);
        userRepository.save(user);
    }
}
