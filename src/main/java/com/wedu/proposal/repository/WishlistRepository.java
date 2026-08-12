package com.wedu.proposal.repository;

import com.wedu.proposal.domain.Wishlist;
import com.wedu.proposal.dto.ProductInterestCount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    Optional<Wishlist> findByUserId(Long userId);

    @Query("""
            SELECT new com.wedu.proposal.dto.ProductInterestCount(productId, COUNT(wishlist))
            FROM Wishlist wishlist
            JOIN wishlist.productIds productId
            GROUP BY productId
            """)
    List<ProductInterestCount> countWishesByProduct();
}
