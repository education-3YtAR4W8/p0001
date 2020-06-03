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
        // TODO: このメソッドを実装してください。
        // 1. 入力文字列で絞り込んだSearchedItemのリストを返す
        //   SearchedItem (
        //     name        -> item_tbl.name
        //     position    -> position_tbl.name
        //     tags        -> tag_tbl.nameのリスト
        //     description -> item_tbl.description
        //   )
        // 2. 絞り込み条件はitem_tbl.name, item_tbl.description, tag_tbl.name, position_tbl.nameに対し1つでも入力textが部分一致するもの
        // 3. コーディングの学習のため、sqlで絞り込まない。用意されているdaoメソッドを使用すること
        Set<String> itemIds = new HashSet<>();
//        Set<SearchedItem> searchedItems = new HashSet<>();
        List<SearchedItem> searchedItems = new ArrayList<>();
        List<Item> items = itemDao.selectAll();
        List<Tag> tags = tagDao.selectAll();
        List<Position> positions = positionDao.selectAll();
        List<ItemTag> itemTags = itemTagDao.selectAll();

        for (Item item : items) {
            if (item.getName().matches(".*" + text + ".*") || item.getDescription().matches(".*" + text + ".*")) {
//                SearchedItem searchedItem = new SearchedItem(item.getName(), getPositionName(positions, item.getPositionId()), getTags(itemTags, tags, item.getItemId()), item.getDescription());
//                searchedItems.add(searchedItem);
                itemIds.add(item.getItemId());
            }
        }

        for (Position position : positions) {
            if (position.getName().matches(".*" + text + ".*")) {
                for (Item item : items) {
                    if (item.getPositionId().equals(position.getPositionId())) {
//                        SearchedItem searchedItem = new SearchedItem(item.getName(), position.getName(), getTags(itemTags, tags, item.getItemId()), item.getDescription());
//                        searchedItems.add(searchedItem);
                        itemIds.add(item.getItemId());
                    }
                }
            }
        }

        for (Tag tag : tags) {
            if (tag.getName().matches(".*" + text + ".*")) {
                for (ItemTag itemTag : itemTags) {
                    if (itemTag.getTagId().equals(tag.getTagId())) {
                        itemIds.add(itemTag.getItemId());
                    }
                }
            }
        }

        for (String itemId : itemIds) {
            for (Item item : items) {
                if (item.getItemId().equals(itemId)) {
                    SearchedItem searchedItem = new SearchedItem(item.getName(), getPositionName(positions, item.getPositionId()), getTags(itemTags, tags, item.getItemId()), item.getDescription());
                    searchedItems.add(searchedItem);
                    break;
                }
            }
        }

        return searchedItems;
    }

    String getPositionName(List<Position> positions, String positionId) {
        for (Position position: positions) {
            if (position.getPositionId().equals(positionId)) {
                return position.getName();
            }
        }
        return null;
    }

    List<String> getTags(List<ItemTag> itemTags, List<Tag> tags, String item_id) {
        List<String> searchTags = new ArrayList<>();
        for (ItemTag itemTag : itemTags) {
            if (itemTag.getItemId().equals(item_id)) {
                for (Tag tag : tags) {
                    if (tag.getTagId().equals(itemTag.getTagId())) {
                        searchTags.add(tag.getName());
                    }
                }
            }
        }
        return searchTags;
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
