package kitchenpos.application;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import kitchenpos.RepositoryTest;
import kitchenpos.table.application.TableGroupService;
import kitchenpos.table.application.TableService;
import kitchenpos.table.application.request.OrderTableRequest;
import kitchenpos.table.application.request.TableGroupRequest;
import kitchenpos.table.application.response.OrderTableResponse;
import kitchenpos.table.application.response.TableGroupResponse;
import kitchenpos.table.domain.TableGroup;
import kitchenpos.table.domain.repository.OrderTableRepository;
import kitchenpos.table.domain.repository.TableGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@RepositoryTest
class TableGroupServiceTest {

    private TableGroupService sut;
    private TableService tableService;

    @Autowired
    private OrderTableRepository orderTableRepository;

    @Autowired
    private TableGroupRepository tableGroupRepository;

    @BeforeEach
    void setUp() {
        sut = new TableGroupService(orderTableRepository, tableGroupRepository);
        tableService = new TableService(orderTableRepository);
    }

    @DisplayName("새로운 단체 지정(table group)을 생성할 수 있다.")
    @Test
    void create() {
        // given
        final List<OrderTableRequest> orderTableRequests = toOrderTableRequests(tableService.list());
        final TableGroupRequest tableGroupRequest = new TableGroupRequest(LocalDateTime.now(), orderTableRequests);

        // when
        final TableGroupResponse createdTableGroup = sut.create(tableGroupRequest);

        // then
        assertThat(createdTableGroup).isNotNull();
        assertThat(createdTableGroup.getId()).isNotNull();
        final TableGroup foundTableGroup = tableGroupRepository.findById(createdTableGroup.getId()).get();
        assertThat(foundTableGroup)
                .usingRecursiveComparison()
                .ignoringFields("id", "orderTables")
                .isEqualTo(createdTableGroup);
    }

    @DisplayName("단체 지정하려는 개별 주문 테이블이 실제 존재하는 주문 테이블이어야 한다.")
    @Test
    void canCreateTableGroupWhenExistOrderTable() {
        // given
        final OrderTableRequest orderTable1 = new OrderTableRequest(1, true);
        final OrderTableRequest orderTable2 = new OrderTableRequest(1, true);
        final TableGroupRequest tableGroup = new TableGroupRequest(LocalDateTime.now(),
                List.of(orderTable1, orderTable2));

        // when & then
        assertThatThrownBy(() -> sut.create(tableGroup))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("단체 지정(table group)을 해제할 수 있다.")
    @Test
    void ungroup() {
        // given
        final List<OrderTableRequest> orderTables = toOrderTableRequests(tableService.list());
        final TableGroupResponse tableGroup = sut.create(new TableGroupRequest(LocalDateTime.now(), orderTables));

        // when
        sut.ungroup(tableGroup.getId());

        // then
        final List<OrderTableResponse> results = tableService.list();
        assertThat(results)
                .hasSize(8)
                .extracting(OrderTableResponse::getTableGroupId)
                .containsExactly(null, null, null, null, null, null, null, null);
    }

    private List<OrderTableRequest> toOrderTableRequests(final List<OrderTableResponse> orderTableResponses) {
        return orderTableResponses.stream()
                .map(it -> new OrderTableRequest(it.getId(), it.getTableGroupId(), it.getNumberOfGuests(), it.isEmpty()))
                .collect(toList());
    }
}
