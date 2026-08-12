package com.wedu.proposal.service;

import com.wedu.proposal.dto.ProductInterestCount;
import com.wedu.proposal.repository.CartRepository;
import com.wedu.proposal.repository.WishlistRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 상품이 사용자에게 얼마나 관심받았는지 집계해 다른 도메인에 제공한다.
 *
 * <p>찜·장바구니는 proposal 도메인이 소유하므로, 상품 인기 순위를 매기는 쪽이 두 테이블을 직접
 * 들여다보지 않고 이 경계를 통해 집계만 가져간다.
 */
@Service
@RequiredArgsConstructor
public class ProductInterestService {

    private final WishlistRepository wishlistRepository;
    private final CartRepository cartRepository;

    /** 상품 id 별 관심 수(찜 + 담기)를 집계한다. 관심이 없는 상품은 결과에 없다. */
    @Transactional(readOnly = true)
    public List<ProductInterestCount> countInterestsByProduct() {
        Map<Long, Long> totals = new LinkedHashMap<>();
        accumulate(totals, wishlistRepository.countWishesByProduct());
        accumulate(totals, cartRepository.countCartsByProduct());
        return totals.entrySet().stream()
                .map(entry -> new ProductInterestCount(entry.getKey(), entry.getValue()))
                .toList();
    }

    private void accumulate(Map<Long, Long> totals, List<ProductInterestCount> counts) {
        for (ProductInterestCount count : counts) {
            totals.merge(count.productId(), count.count(), Long::sum);
        }
    }
}
