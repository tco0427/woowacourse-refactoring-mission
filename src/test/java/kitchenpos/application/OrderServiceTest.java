package kitchenpos.application;

import static kitchenpos.order.domain.OrderStatus.COMPLETION;
import static kitchenpos.order.domain.OrderStatus.COOKING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDateTime;
import java.util.List;
import kitchenpos.RepositoryTest;
import kitchenpos.menu.domain.Menu;
import kitchenpos.menu.domain.repository.MenuRepository;
import kitchenpos.order.application.OrderService;
import kitchenpos.order.application.request.OrderLineItemRequest;
import kitchenpos.order.application.request.OrderRequest;
import kitchenpos.order.application.response.OrderResponse;
import kitchenpos.order.domain.Order;
import kitchenpos.order.domain.OrderLineItem;
import kitchenpos.order.domain.repository.OrderRepository;
import kitchenpos.table.application.TableService;
import kitchenpos.table.application.request.OrderTableRequest;
import kitchenpos.table.application.response.OrderTableResponse;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.table.domain.repository.OrderTableRepository;
import kitchenpos.table.validator.TableEmptyValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@RepositoryTest
class OrderServiceTest {

    private static final long QUANTITY = 1L;
    private static final long MENU_ID = 1L;
    private static final long ORDER_ID = 1L;
    private static final long SEQUENCE = 1L;

    private OrderService sut;
    private TableService tableService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderTableRepository orderTableRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        final TableEmptyValidator tableEmptyValidator = new TableEmptyValidator(orderTableRepository);
        sut = new OrderService(menuRepository, orderRepository, tableEmptyValidator);
        tableService = new TableService(orderTableRepository);
    }

    @DisplayName("????????? ????????? ??? ??????. (????????? ?????? ?????? ????????? ??????.)")
    @Test
    void create() {
        // given
        final OrderLineItemRequest orderLineItemRequest = createOrderLineItemRequest();
        final OrderTableRequest orderTableRequest = new OrderTableRequest(null, 1, false);
        final OrderTableResponse orderTableResponse = tableService.create(orderTableRequest);

        final OrderRequest request = new OrderRequest(orderTableResponse.getId(), null, LocalDateTime.now(),
                List.of(orderLineItemRequest));

        // when
        final OrderResponse response = sut.create(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getOrderStatus()).isEqualTo(COOKING.name());
        final Order foundOrder = orderRepository.findById(response.getId()).get();
        assertThat(foundOrder.getId()).isNotNull();
    }

    @DisplayName("????????? ?????? ?????? ????????? ?????? ????????? ?????? ??????????????????.")
    @Test
    void orderLineItemSizeEqualToMenuSize() {
        // given
        final OrderTableRequest orderTableRequest = new OrderTableRequest(null, 1, false);
        final OrderTableResponse orderTableResponse = tableService.create(orderTableRequest);

        final OrderRequest request = new OrderRequest(orderTableResponse.getId(), null, LocalDateTime.now(),
                invalidQuantityOrderLineItemRequest());

        // when & then
        assertThatThrownBy(() -> sut.create(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("?????? ???????????? ??????????????? ?????????.")
    @Test
    void createWithNonEmptyOrderTable() {
        // given
        final OrderLineItemRequest orderLineItemRequest = createOrderLineItemRequest();
        final OrderTableRequest orderTableRequest = new OrderTableRequest(null, 1, true);
        final OrderTableResponse orderTableResponse = tableService.create(orderTableRequest);

        final OrderRequest request = new OrderRequest(orderTableResponse.getId(), null, LocalDateTime.now(),
                List.of(orderLineItemRequest));

        // when & then
        assertThatThrownBy(() -> sut.create(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("????????? ????????? ????????? ??? ??????.")
    @Test
    void canChangeOrderStatus() {
        // given
        final OrderLineItemRequest orderLineItemRequest = createOrderLineItemRequest();
        final OrderTableRequest orderTableRequest = new OrderTableRequest(null, 1, false);
        final OrderTableResponse orderTableResponse = tableService.create(orderTableRequest);

        final OrderRequest request = new OrderRequest(orderTableResponse.getId(), null, LocalDateTime.now(),
                List.of(orderLineItemRequest));
        final OrderResponse response = sut.create(request);

        // when
        final OrderRequest changeRequest = new OrderRequest(orderTableResponse.getId(), "COMPLETION",
                LocalDateTime.now(),
                List.of(orderLineItemRequest));
        final OrderResponse changedOrderResponse = sut.changeOrderStatus(response.getId(), changeRequest);

        // then
        assertThat(changedOrderResponse.getOrderStatus()).isEqualTo(COMPLETION.name());
    }

    @DisplayName("????????? ??????????????? ?????? ?????? ????????? ????????? ????????? ??? ??????.")
    @Test
    void canNotChangeOrderStatusWhenEmptyOrder() {
        // given
        final long notExistOrderId = -1L;
        final OrderLineItemRequest orderLineItemRequest = createOrderLineItemRequest();
        final OrderTableRequest orderTableRequest = new OrderTableRequest(null, 1, false);
        final OrderTableResponse orderTableResponse = tableService.create(orderTableRequest);

        final OrderLineItem orderLineItem = toOrderLineItem(orderLineItemRequest);
        final Order order = new Order(notExistOrderId, orderTableResponse.getId(), COOKING, LocalDateTime.now(),
                List.of(orderLineItem));
        final OrderRequest changeRequest = new OrderRequest(orderTableResponse.getId(), "COMPLETION",
                LocalDateTime.now(),
                List.of(orderLineItemRequest));

        // when & then
        assertThatThrownBy(() -> sut.changeOrderStatus(order.getId(), changeRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("????????? ?????? ????????? ????????? ??? ??????.")
    @Test
    void list() {
        // given
        final OrderTable orderTable = OrderTable.of(1, false);
        final OrderTable anotherOrderTable = OrderTable.of(1, false);

        final OrderRequest orderRequest1 = createdOrderRequest(orderTable, createOrderLineItemRequest());
        final OrderRequest orderRequest2 = createdOrderRequest(anotherOrderTable, createOrderLineItemRequest());

        final OrderResponse orderResponse1 = sut.create(orderRequest1);
        final OrderResponse orderResponse2 = sut.create(orderRequest2);

        // when
        final List<OrderResponse> orders = sut.list();

        // then
        assertThat(orders)
                .hasSize(2)
                .extracting(OrderResponse::getId, OrderResponse::getOrderTableId, OrderResponse::getOrderStatus)
                .containsExactlyInAnyOrder(
                        tuple(orderResponse1.getId(), orderResponse1.getOrderTableId(),
                                orderResponse1.getOrderStatus()),
                        tuple(orderResponse2.getId(), orderResponse2.getOrderTableId(), orderResponse2.getOrderStatus())
                );
    }

    @DisplayName("????????? ????????? ??????????????? ?????? ????????? ?????? ????????? ?????? ????????? ????????? ???????????? ??????.")
    @Test
    void afterChangeMenu() {
        // given
        final OrderTable orderTable = OrderTable.of(1, false);
        final OrderRequest orderRequest = createdOrderRequest(orderTable, createOrderLineItemRequest());

        final OrderResponse orderResponse = sut.create(orderRequest);

        // when
        final int updatedRowCount = jdbcTemplate.update(
                "UPDATE menu SET menu.name = '????????? ?????? ??????' WHERE menu.id = " + MENU_ID);
        final Order order = orderRepository.findById(orderResponse.getId()).get();
        final List<OrderLineItem> orderLineItems = order.getOrderLineItems();

        // then
        assertThat(updatedRowCount).isEqualTo(1);
        assertThat(orderLineItems.get(0).getMenuName()).isEqualTo("??????????????????");
        assertThat(orderLineItems.get(0).getMenuPrice().longValue()).isEqualTo(16000L);
    }

    private OrderLineItem toOrderLineItem(final OrderLineItemRequest orderLineItemRequest) {
        final Menu menu = menuRepository.findById(orderLineItemRequest.getMenuId()).get();
        return new OrderLineItem(menu, orderLineItemRequest.getQuantity());
    }

    private OrderLineItemRequest createOrderLineItemRequest() {
        return new OrderLineItemRequest(SEQUENCE, ORDER_ID, MENU_ID, QUANTITY);
    }

    private OrderRequest createdOrderRequest(final OrderTable orderTable,
                                             final OrderLineItemRequest orderLineItemRequest) {
        final OrderTableRequest orderTableRequest = new OrderTableRequest(orderTable);
        final OrderTableResponse orderTableResponse = tableService.create(orderTableRequest);

        return new OrderRequest(orderTableResponse.getId(), null, LocalDateTime.now(), List.of(orderLineItemRequest));
    }

    private static List<OrderLineItemRequest> invalidQuantityOrderLineItemRequest() {
        final OrderLineItemRequest orderLineItemRequest1 = new OrderLineItemRequest(1L, 1L, 1L, 1L);
        final OrderLineItemRequest orderLineItemRequest2 = new OrderLineItemRequest(2L, 1L, 1L, 1L);
        return List.of(orderLineItemRequest1, orderLineItemRequest2);
    }
}
