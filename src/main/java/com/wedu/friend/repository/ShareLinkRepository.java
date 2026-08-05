package com.wedu.friend.repository;

import com.wedu.friend.domain.ShareLink;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShareLinkRepository extends JpaRepository<ShareLink, Long> {

    Optional<ShareLink> findByOwnerId(Long ownerId);

    Optional<ShareLink> findByToken(String token);
}
