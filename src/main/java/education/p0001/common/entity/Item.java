package education.p0001.common.entity;

import lombok.Data;
import org.seasar.doma.*;

import java.util.UUID;

@Entity
@Table(name = "item_tbl")
@Data
public class Item {
    @Id
    @Column(name = "item_id")
    String itemId = UUID.randomUUID().toString();

    @Column(name = "name")
    String name;

    @Column(name = "position_id")
    String positionId;

    @Column(name = "description")
    String description = "";
}
