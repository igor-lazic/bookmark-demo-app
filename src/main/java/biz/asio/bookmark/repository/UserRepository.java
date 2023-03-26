package biz.asio.bookmark.repository;

import biz.asio.bookmark.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Boolean existsByUserName(String username);
    Boolean existsByEmail(String email);
    User findUserByUserName(String username);
}
