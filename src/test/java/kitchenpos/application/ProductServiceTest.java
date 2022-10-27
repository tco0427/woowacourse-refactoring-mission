package kitchenpos.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.math.BigDecimal;
import java.util.List;
import kitchenpos.RepositoryTest;
import kitchenpos.application.request.ProductRequest;
import kitchenpos.application.response.ProductResponse;
import kitchenpos.dao.ProductDao;
import kitchenpos.domain.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@RepositoryTest
class ProductServiceTest {

    private ProductService sut;

    @Autowired
    private ProductDao productDao;

    @BeforeEach
    void setUp() {
        sut = new ProductService(productDao);
    }

    @DisplayName("새로운 상품을 등록할 수 있다.")
    @Test
    void create() {
        // given
        final ProductRequest request = new ProductRequest("후라이드", BigDecimal.valueOf(16000));

        // when
        final ProductResponse productResponse = sut.create(request);

        // then
        assertThat(productResponse).isNotNull();
        assertThat(productResponse.getId()).isNotNull();
        final Product foundProduct = productDao.findById(productResponse.getId()).get();
        assertThat(foundProduct)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(productResponse);
    }

    @DisplayName("상품의 가격이 0보다 작으면 상품을 등록할 수 없다.")
    @Test
    void createWithMinusPrice() {
        // given
        final ProductRequest request = new ProductRequest("후라이드", BigDecimal.valueOf(-1));

        // when & then
        assertThatThrownBy(() -> sut.create(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("상품의 가격이 없는 경우 상품을 등록할 수 없다.")
    @Test
    void createWithNullPrice() {
        // given
        final ProductRequest request = new ProductRequest("후라이드", null);

        // when & then
        assertThatThrownBy(() -> sut.create(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("상품 목록을 전체 조회할 수 있다.")
    @Test
    void list() {
        // when
        final List<ProductResponse> products = sut.list();

        // then
        assertThat(products)
                .hasSize(6)
                .extracting(ProductResponse::getName, product -> product.getPrice().longValue())
                .containsExactlyInAnyOrder(
                        tuple("후라이드", 16_000L),
                        tuple("양념치킨", 16_000L),
                        tuple("반반치킨", 16_000L),
                        tuple("통구이", 16_000L),
                        tuple("간장치킨", 17_000L),
                        tuple("순살치킨", 17_000L)
                );
    }
}
