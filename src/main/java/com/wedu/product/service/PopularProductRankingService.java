package com.wedu.product.service;

import com.wedu.product.domain.PopularProduct;
import com.wedu.product.domain.Product;
import com.wedu.product.repository.PopularProductRepository;
import com.wedu.product.repository.ProductRepository;
import com.wedu.proposal.dto.ProductInterestCount;
import com.wedu.proposal.service.ProductInterestService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 찜·장바구니 관심 수를 기준으로 인기 상품 순위를 다시 매긴다.
 *
 * <p>정보 구조도의 "인기순"은 외부 사이트 랭킹이 아니라 편집샵 안에서의 인기다. 그래서 순위는
 * 자체 카탈로그 상품에 대한 사용자 관심(찜 + 담기)으로 산출한다. 관심 데이터가 아직 없는 초기에도
 * 홈·편집샵의 인기 영역이 비지 않도록, 부족한 자리는 카탈로그 등록 순서로 채운다.
 */
@Service
@RequiredArgsConstructor
public class PopularProductRankingService {

    /** {@link PopularProductRepository#findTop20ByOrderByRankAsc()} 가 노출하는 개수와 맞춘다. */
    private static final int RANK_LIMIT = 20;

    private final PopularProductRepository popularProductRepository;
    private final ProductRepository productRepository;
    private final ProductInterestService productInterestService;

    /** 인기 순위를 지금 시점 기준으로 전부 다시 만든다. 저장할 상품이 없으면 기존 순위를 그대로 둔다. */
    @Transactional
    public int refresh() {
        List<Product> ranked = pickRanked();
        if (ranked.isEmpty()) {
            return 0;
        }
        popularProductRepository.deleteAllInBatch();
        List<PopularProduct> rankings = new ArrayList<>(ranked.size());
        for (int index = 0; index < ranked.size(); index++) {
            rankings.add(toRanking(ranked.get(index), index + 1));
        }
        popularProductRepository.saveAll(rankings);
        return rankings.size();
    }

    /** 순위가 한 번도 만들어지지 않았을 때만 채운다. 배포 직후 인기 영역이 비어 보이는 것을 막는다. */
    @Transactional
    public int refreshIfEmpty() {
        if (popularProductRepository.count() > 0) {
            return 0;
        }
        return refresh();
    }

    private List<Product> pickRanked() {
        Map<Long, Long> interests = interestsByProductId();
        Map<Long, Product> picked = new LinkedHashMap<>();
        productRepository.findAllById(interests.keySet()).stream()
                .sorted(Comparator
                        .comparingLong((Product product) -> interests.get(product.getId())).reversed()
                        .thenComparing(Product::getId))
                .limit(RANK_LIMIT)
                .forEach(product -> picked.put(product.getId(), product));
        if (picked.size() < RANK_LIMIT) {
            fillFromCatalog(picked);
        }
        return List.copyOf(picked.values());
    }

    private void fillFromCatalog(Map<Long, Product> picked) {
        PageRequest catalogOrder = PageRequest.of(0, RANK_LIMIT, Sort.by(Sort.Direction.ASC, "id"));
        for (Product product : productRepository.findAll(catalogOrder)) {
            if (picked.size() >= RANK_LIMIT) {
                return;
            }
            picked.putIfAbsent(product.getId(), product);
        }
    }

    private Map<Long, Long> interestsByProductId() {
        Map<Long, Long> interests = new HashMap<>();
        for (ProductInterestCount count : productInterestService.countInterestsByProduct()) {
            interests.put(count.productId(), count.count());
        }
        return interests;
    }

    private PopularProduct toRanking(Product product, int rank) {
        return PopularProduct.rank(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getVendorName(),
                "/api/products/" + product.getId(),
                product.getThumbnailUrl(),
                rank);
    }
}
