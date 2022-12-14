package kitchenpos.table.domain;

import static javax.persistence.GenerationType.IDENTITY;
import static kitchenpos.order.domain.OrderStatus.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import kitchenpos.order.domain.Order;

@Entity
public class OrderTable {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "table_group_id")
    private Long tableGroupId;

    @Column(nullable = false)
    private int numberOfGuests;

    @Column(nullable = false)
    private boolean empty;

    @OneToMany
    @JoinColumn(name = "order_table_id")
    private List<Order> orders = new ArrayList<>();

    protected OrderTable() {
    }

    public OrderTable(final Long tableGroupId, final int numberOfGuests, final boolean empty) {
        this(null, tableGroupId, numberOfGuests, empty);
    }

    public OrderTable(final Long id, final Long tableGroupId, final int numberOfGuests, final boolean empty) {
        this(id, tableGroupId, numberOfGuests, empty, null);
    }

    public OrderTable(final Long id, final Long tableGroupId, final int numberOfGuests, final boolean empty,
                      final List<Order> orders) {
        this.id = id;
        this.tableGroupId = tableGroupId;
        this.numberOfGuests = numberOfGuests;
        this.empty = empty;
        this.orders = orders;
    }

    public static OrderTable of(final int numberOfGuests, final boolean empty) {
        validateNegativeNumberOfGuests(numberOfGuests);

        return new OrderTable(null, null, numberOfGuests, empty, new ArrayList<>());
    }

    public void changeEmptyStatus(final boolean empty) {
        if (Objects.nonNull(tableGroupId)) {
            throw new IllegalArgumentException("테이블 상태 변경을 위해선 테이블이 그룹으로 묶여있으면 안됩니다.");
        }

        if (containsCookingOrMealOrder()) {
            throw new IllegalArgumentException("조리중이거나 식사중인 상태이면 테이블을 비울 수 없습니다.");
        }

        this.empty = empty;
    }

    public void updateNumberOfGuests(final int numberOfGuests) {
        validateNegativeNumberOfGuests(numberOfGuests);

        if (isEmpty()) {
            throw new IllegalArgumentException("테이블이 비어있는 상태일 수 없습니다.");
        }

        this.numberOfGuests = numberOfGuests;
    }

    public boolean containsCookingOrMealOrder() {
        return orders.stream()
                .anyMatch(it -> it.getOrderStatus().equals(COOKING) || it.getOrderStatus().equals(MEAL));
    }

    public void ungroup() {
        this.tableGroupId = null;
        this.empty = false;
    }

    public void merge(final Long id) {
        this.tableGroupId = id;
        this.empty = false;
    }

    private static void validateNegativeNumberOfGuests(final int numberOfGuests) {
        if (numberOfGuests < 0) {
            throw new IllegalArgumentException("음수로 주문 테이블의 손님 수를 변경할 수 없습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getTableGroupId() {
        return tableGroupId;
    }

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public boolean isEmpty() {
        return empty;
    }
}
