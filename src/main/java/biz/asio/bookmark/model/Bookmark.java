package biz.asio.bookmark.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Bookmark {
    @Id
    private String bookmarkId;
    private boolean publicBookmark;
    private String ownerUserName;
    private String url;
    private String category;
    private String stars;
}
