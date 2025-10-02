package com.rsi.comelit.repository;

import com.rsi.comelit.entity.Comment;
import com.rsi.comelit.entity.Post;
import com.rsi.comelit.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    void deleteByEmail(String email);
    List<User> findUsersByFollowerUsers(User user, Pageable pageable);
    List<User> findUsersByFollowingUsers(User user, Pageable pageable);
    List<User> findUsersByLikedPosts(Post post, Pageable pageable);
    List<User> findUsersByLikedComments(Comment comment, Pageable pageable);

    @Query(value = "select * from users u " +
            "where concat(u.first_name, ' ', u.last_name) like %:name% " +
            "order by u.first_name asc, u.last_name asc",
           nativeQuery = true)
    List<User> findUsersByName(String name, Pageable pageable);


    @Query("SELECT u FROM User u WHERE " +
            "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND( u.role = 'USER')")
    Page<User> search(@Param("search") String search, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role = 'ROLE_USER'")
    Page<User> findAllUsers(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role = 'admin'")
    User findAdmin();

}
