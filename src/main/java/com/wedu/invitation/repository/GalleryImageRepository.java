package com.wedu.invitation.repository;

import com.wedu.invitation.domain.GalleryImage;
import com.wedu.invitation.domain.Invitation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GalleryImageRepository extends JpaRepository<GalleryImage, Long> {

    List<GalleryImage> findByInvitationOrderBySortOrderAsc(Invitation invitation);

    Optional<GalleryImage> findByIdAndInvitationUserId(Long id, Long userId);
}