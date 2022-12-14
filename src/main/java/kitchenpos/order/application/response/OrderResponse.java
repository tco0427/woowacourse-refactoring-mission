package kitchenpos.order.application.response;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.time.LocalDateTime;
import java.util.List;
import kitchenpos.order.domain.Order;

public class OrderResponse {

    private Long id;
    private Long orderTableId;
    private String orderStatus;
    private LocalDateTime orderedTime;
    private List<OrderLineItemResponse> orderLineItems;

    public OrderResponse(final Order order, final List<OrderLineItemResponse> orderLineItems) {
        this(order.getId(), order.getOrderTableId(), order.getOrderStatus().name(), order.getOrderedTime(), orderLineItems);
    }

    @JsonCreator
    public OrderResponse(final Long id, final Long orderTableId, final String orderStatus,
                         final LocalDateTime orderedTime,
                         final List<OrderLineItemResponse> orderLineItems) {
        this.id = id;
        this.orderTableId = orderTableId;
        this.orderStatus = orderStatus;
        this.orderedTime = orderedTime;
        this.orderLineItems = orderLineItems;
    }

    public static OrderResponse from(final Order order) {
        final List<OrderLineItemResponse> orderLineItems = order.getOrderLineItems()
                .stream()
                .map(OrderLineItemResponse::new)
                .collect(toList());

        return new OrderResponse(order, orderLineItems);
    }

    public Long getId() {
        return id;
    }

    public Long getOrderTableId() {
        return orderTableId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public LocalDateTime getOrderedTime() {
        return orderedTime;
    }

    public List<OrderLineItemResponse> getOrderLineItems() {
        return orderLineItems;
    }
}
