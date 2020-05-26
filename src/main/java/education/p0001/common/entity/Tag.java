package education.p0001.common.entity;

import lombok.Data;
import org.seasar.doma.Column;
import org.seasar.doma.Entity;
import org.seasar.doma.Id;
import org.seasar.doma.Table;

import java.util.UUID;

@Entity
@Table(name = "tag_tbl")
@Data
public class Tag {
    @Id
    @Column(name = "tag_id")
    String tagId = UUID.randomUUID().toString();

    @Column(name = "name")
    String name;
}
