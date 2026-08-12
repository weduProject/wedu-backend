package com.wedu.proposal.repository;

import com.wedu.proposal.domain.Cart;
import com.wedu.proposal.dto.ProductInterestCount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserId(Long userId);

    /** 담긴 수량이 아니라 담은 장바구니 수를 센다. 한 사용자가 수량을 늘려도 관심 1건이다. */
    @Query("""
            SELECT new com.wedu.proposal.dto.ProductInterestCount(KEY(item), COUNT(cart))
            FROM Cart cart
            JOIN cart.items item
            GROUP BY KEY(item)
            """)
    List<ProductInterestCount> countCartsByProduct();
}
