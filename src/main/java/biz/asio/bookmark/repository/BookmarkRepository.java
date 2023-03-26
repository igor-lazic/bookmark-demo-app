package biz.asio.bookmark.repository;

import biz.asio.bookmark.model.Bookmark;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookmarkRepository extends MongoRepository<Bookmark, String> {
    List<Bookmark> findBookmarksByOwnerUserName(String ownerUserId);
}
