package education.p0001.common.dao;

import education.p0001.common.entity.ItemTag;
import org.seasar.doma.Dao;
import org.seasar.doma.Select;
import org.seasar.doma.boot.ConfigAutowireable;

import java.util.List;

@Dao
@ConfigAutowireable
public interface ItemTagDao {
    @Select
    List<ItemTag> selectAll();
}
