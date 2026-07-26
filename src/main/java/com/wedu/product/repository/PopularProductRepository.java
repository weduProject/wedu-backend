package com.wedu.product.repository;

import com.wedu.product.domain.PopularProduct;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopularProductRepository extends JpaRepository<PopularProduct, Long> {

    List<PopularProduct> findTop20ByOrderByRankAsc();
}
