package kitchenpos.menu.domain.repository;

import java.util.List;
import kitchenpos.menu.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    Long countByIdIn(List<Long> ids);
}
