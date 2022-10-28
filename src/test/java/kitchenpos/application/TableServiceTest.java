package kitchenpos.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import kitchenpos.RepositoryTest;
import kitchenpos.application.request.OrderTableRequest;
import kitchenpos.application.response.OrderTableResponse;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.repository.MenuRepository;
import kitchenpos.domain.repository.OrderLineItemRepository;
import kitchenpos.domain.repository.OrderRepository;
import kitchenpos.domain.repository.OrderTableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@RepositoryTest
class TableServiceTest {

    private static final long SEQUENCE = 1L;
    private static final long ORDER_ID = 1L;
    private static final long MENU_ID = 1L;
    private static final long QUANTITY = 1L;

    private TableService sut;
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private OrderTableRepository orderTableRepository;

    @Autowired
    private OrderLineItemRepository orderLineItemRepository;

    @BeforeEach
    void setUp() {
        sut = new TableService(orderRepository, orderTableRepository);
        orderService = new OrderService(menuRepository, orderRepository, orderLineItemRepository, orderTableRepository);
    }

    @DisplayName("새로운 주문 테이블을 생성할 수 있다.")
    @Test
    void create() {
        // given
        final OrderTableRequest request = new OrderTableRequest(0, true);

        // when
        final OrderTableResponse response = sut.create(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        final OrderTable foundOrderTable = orderTableRepository.findById(response.getId()).get();
        assertThat(response)
                .extracting(OrderTableResponse::getTableGroupId, OrderTableResponse::getNumberOfGuests, OrderTableResponse::isEmpty)
                .containsExactly(foundOrderTable.getTableGroupId(), foundOrderTable.getNumberOfGuests(), foundOrderTable.isEmpty());
    }

    @DisplayName("전체 주문 테이블 목록을 조회할 수 있다.")
    @Test
    void list() {
        // when
        final List<OrderTableResponse> responses = sut.list();

        // then
        assertThat(responses)
                .hasSize(8)
                .extracting(OrderTableResponse::getNumberOfGuests, OrderTableResponse::isEmpty)
                .containsExactlyInAnyOrder(
                        tuple(0, true), tuple(0, true),
                        tuple(0, true), tuple(0, true),
                        tuple(0, true), tuple(0, true),
                        tuple(0, true), tuple(0, true)
                );
    }

    @DisplayName("주문 테이블의 조회결과가 없는 경우 테이블을 빈 상태로 변경할 수 없다.")
    @Test
    void changeEmptyWithEmptyOrderTable() {
        // given
        final long invalidOrderTableId = -1L;
        final OrderTableRequest orderTableRequest = new OrderTableRequest(0, false);

        // when & then
        assertThatThrownBy(() -> sut.changeEmpty(invalidOrderTableId, orderTableRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
