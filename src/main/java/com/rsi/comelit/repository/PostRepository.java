package com.rsi.comelit.repository;

import com.rsi.comelit.entity.Post;
import com.rsi.comelit.entity.Tag;
import com.rsi.comelit.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findPostsByAuthor(User author, Pageable pageable);

    List<Post> findAllByOrderByDateCreatedDesc(Pageable pageable);
    List<Post> findPostsByAuthorIdIn(List<Long> followingUserIds, Pageable pageable);
    List<Post> findPostsBySharedPost(Post post, Pageable pageable);

    List<Post> findPostsByPostTags(Tag tag, Pageable pageable);
}
