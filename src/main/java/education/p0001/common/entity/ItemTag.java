package education.p0001.common.entity;

import lombok.Data;
import org.seasar.doma.Column;
import org.seasar.doma.Entity;
import org.seasar.doma.Id;
import org.seasar.doma.Table;

@Entity
@Table(name = "item_tag_tbl")
@Data
public class ItemTag {
    @Id
    @Column(name = "item_id")
    String itemId;

    @Id
    @Column(name = "tag_id")
    String tagId;
}
