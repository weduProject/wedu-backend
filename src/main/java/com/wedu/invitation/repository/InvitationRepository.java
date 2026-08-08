package com.wedu.invitation.repository;

import com.wedu.invitation.domain.Invitation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    Optional<Invitation> findByUserId(Long userId);
}