package com.wedu.friend.repository;

import com.wedu.friend.domain.Friendship;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    List<Friendship> findAllByUserId(Long userId);

    Optional<Friendship> findByUserIdAndFriendUserId(Long userId, Long friendUserId);

    boolean existsByUserIdAndFriendUserId(Long userId, Long friendUserId);
}
