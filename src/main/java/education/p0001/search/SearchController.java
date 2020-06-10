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
        Map<String, Position> positionMap = positionDao.selectAll().stream()
                .collect(Collectors.toMap(it -> it.getPositionId(), it -> it));
        Map<String, Tag> tagMap = tagDao.selectAll().stream()
                .collect(Collectors.toMap(it -> it.getTagId(), it -> it));
        Map<String, List<ItemTag>> mappedItemTagsByItemId = itemTagDao.selectAll().stream()
                .collect(Collectors.groupingBy(it -> it.getItemId()));

        // 検索ヒットする場所とタグを絞り込んでおきます
        Set<String> filteredPositionIds = positionMap.values().stream()
                .filter(it -> it.getName().contains(text))
                .map(it -> it.getPositionId())
                .collect(Collectors.toSet());
        Set<String> filteredTagIds = tagMap.values().stream()
                .filter(it -> it.getName().contains(text))
                .map(it -> it.getTagId())
                .collect(Collectors.toSet());

        // 検索ヒットした場合にtrueを返す関数を用意します
        Predicate<Item> hit = (item) -> {
            if (item.getName().contains(text)) {
                return true;
            }
            if (item.getDescription().contains(text)) {
                return true;
            }
            if (filteredPositionIds.contains(item.getPositionId())) {
                return true;
            }
            if (mappedItemTagsByItemId.get(item.getItemId()).stream()
                    .anyMatch(itemTag -> filteredTagIds.contains(itemTag.getTagId()))) {
                return true;
            }
            return false;
        };

        // すべての備品から検索ヒットするものを収集します
        return itemDao.selectAll().stream()
                .filter(hit)
                .map(item -> {
                    String positionName = Optional.ofNullable(positionMap.get(item.getPositionId()))
                            .orElseThrow(() -> new RuntimeException("データ不整合です。備品に紐づく場所が存在しません。"))
                            .getName();
                    List<String> tagNames = mappedItemTagsByItemId.get(item.getItemId()).stream()
                            .map(it -> Optional.ofNullable(tagMap.get(it.getTagId()))
                                    .orElseThrow(() -> new RuntimeException("データ不整合です。備品に紐づくタグが存在しません。"))
                                    .getName())
                            .collect(Collectors.toList());

                    return new SearchedItem(
                            item.getName(),
                            positionName,
                            tagNames,
                            item.getDescription()
                    );
                })
                .collect(Collectors.toList());
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
