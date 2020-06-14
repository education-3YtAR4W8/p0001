package education.p0001.search;

import education.p0001.common.dao.ItemDao;
import education.p0001.common.dao.ItemTagDao;
import education.p0001.common.dao.PositionDao;
import education.p0001.common.dao.TagDao;
import education.p0001.common.entity.Item;
import education.p0001.common.entity.ItemTag;
import education.p0001.common.entity.Position;
import education.p0001.common.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Controller
public class SearchController {
    @Autowired
    ItemDao itemDao;
    @Autowired
    TagDao tagDao;
    @Autowired
    PositionDao positionDao;
    @Autowired
    ItemTagDao itemTagDao;

    @GetMapping(path = "search")
    String searchGet(@RequestParam Map<String, String> queryParam, Model model) {
        Page page = new Page();
        if (queryParam.containsKey("key")) {
            page.searchText = queryParam.get("key");
            page.items = getSearchedItems(page.searchText);
        }
        model.addAttribute("page", page);

        return "search";
    }

    List<SearchedItem> getSearchedItems(String text) {
        // 使用するレコードを取得します。Listだと検索に不向きなので、Mapにしておきます
        Map<String, Position> positionMap = new HashMap<>();
        for (Position position : positionDao.selectAll()) {
            positionMap.put(position.getPositionId(), position);
        }
        Map<String, Tag> tagMap = new HashMap<>();
        for (Tag tag : tagDao.selectAll()) {
            tagMap.put(tag.getTagId(), tag);
        }
        Map<String, List<ItemTag>> mappedItemTagListByItemId = new HashMap<>();
        for (ItemTag itemTag : itemTagDao.selectAll()) {
            if (!mappedItemTagListByItemId.containsKey(itemTag.getItemId())) {
                mappedItemTagListByItemId.put(itemTag.getItemId(), new ArrayList<>());
            }
            mappedItemTagListByItemId.get(itemTag.getItemId()).add(itemTag);
        }

        // 検索ヒットする場所とタグを絞り込んでおきます
        Set<String> filteredPositionIdSet = new HashSet<>();
        for (Position position : positionMap.values()) {
            if (position.getName().contains(text)) {
                filteredPositionIdSet.add(position.getPositionId());
            }
        }
        Set<String> filteredTagIdSet = new HashSet<>();
        for (Tag tag: tagMap.values()) {
            if (tag.getName().contains(text)) {
                filteredTagIdSet.add(tag.getTagId());
            }
        }

        // すべての備品から検索ヒットするものを収集します
        List<SearchedItem> SearchedItemList = new ArrayList<>();
        for (Item item : itemDao.selectAll()) {
            boolean hit = false;
            if (item.getName().contains(text)) {
                hit = true;
            }
            if (!hit && item.getDescription().contains(text)) {
                hit = true;
            }
            if (!hit && filteredPositionIdSet.contains(item.getPositionId())) {
                hit = true;
            }
            if (!hit) {
                if (mappedItemTagListByItemId.containsKey(item.getItemId())) {
                    for (ItemTag itemTag : mappedItemTagListByItemId.get(item.getItemId())) {
                        if (filteredTagIdSet.contains(itemTag.getTagId())) {
                            hit = true;
                            break;
                        }
                    }
                }
            }

            if (hit) {
                String positionName = "";
                if (positionMap.containsKey(item.getPositionId())) {
                    positionName = positionMap.get(item.getPositionId()).getName();
                }
                List<String> tagNames = new ArrayList<>();
                if (mappedItemTagListByItemId.containsKey(item.getItemId())) {
                    for (ItemTag itemTag : mappedItemTagListByItemId.get(item.getItemId())) {
                        if (tagMap.containsKey(itemTag.getTagId())) {
                            tagNames.add(tagMap.get(itemTag.getTagId()).getName());
                        }
                    }
                }

                SearchedItemList.add(
                        new SearchedItem(
                                item.getName(),
                                positionName,
                                tagNames,
                                item.getDescription()
                        )
                );
            }

        }

        return SearchedItemList;
    }



    @Getter
    @Setter
    static public class Page {
        private String searchText;
        private List<SearchedItem> items = new ArrayList<>();
    }

    @AllArgsConstructor
    @Getter
    static public class SearchedItem {
        private String name;
        private String position;
        private List<String> tags;
        private String description;
    }
}
