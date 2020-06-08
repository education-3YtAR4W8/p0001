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

import java.lang.reflect.Array;
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
        List<Item> itemList = itemDao.selectAll();
        List<Tag> tagList = tagDao.selectAll();
        List<Position> positionList = positionDao.selectAll();
        List<ItemTag> itemTagList = itemTagDao.selectAll();
        List<SearchedItem> resultList = new ArrayList<>();

        //itemIdをソートして格納するTreeSetを用意する
        //ソートについて指摘があれば修正する
        Set<String> itemIds = new TreeSet<>(Comparator.comparing(Integer::valueOf));

        //検索するitemIdをHashSetに追加する
        for(Item item : itemList){
            if(item.getName().contains(text)||
                    item.getDescription().contains(text)){
                itemIds.add(item.getItemId());
            }
        }

        for(Tag tag :tagList){
            if(tag.getName().contains(text)){
                for(ItemTag itemTag : itemTagList){
                    if(itemTag.getTagId().equals(tag.getTagId())){
                        itemIds.add(itemTag.getItemId());
                    }
                }
            }
        }

        for(Position position : positionList){
            if(position.getName().equals(text)){
                for(Item item : itemList){
                    if(item.getPositionId().equals(position.getPositionId())){
                        itemIds.add(item.getItemId());
                    }
                }
            }
        }

        //HashSetに追加したitemIdに対応した要素を検索する
        for(String itemId : itemIds){
            String searchedItemName = null;
            String searchedItemPosition = null;
            List<String> searchedItemTags = new ArrayList<>();
            String searchedDescription = null;

            for(Item item : itemList){
                if(item.getItemId().equals(itemId)){

                    searchedItemName = item.getName();
                    searchedDescription = item.getDescription();

                    for(Position position :positionList){
                        if(position.getPositionId().equals(item.getPositionId())){
                            searchedItemPosition = position.getName();
                            break;
                        }
                    }

                    break;

                }
            }

            Set<String> searchedItemTagsIds = new HashSet<>();

            for(ItemTag itemTag :itemTagList){
                if(itemTag.getItemId().equals(itemId)){
                    searchedItemTagsIds.add(itemTag.getTagId());
                }
            }

            for(String searchedItemTagsId : searchedItemTagsIds){
                for(Tag tag : tagList){
                    if(tag.getTagId().equals(searchedItemTagsId)){
                        searchedItemTags.add(tag.getName());
                        break;
                    }
                }
            }

            resultList.add(
                    new SearchedItem(
                            searchedItemName,
                            searchedItemPosition,
                            searchedItemTags,
                            searchedDescription
                    )
            );

        }

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


        return resultList;
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
