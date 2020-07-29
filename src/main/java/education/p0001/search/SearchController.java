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
import org.seasar.doma.Select;
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

        // item取得
        List<Item> itemList = itemDao.selectAll();
        // position取得
        List<Position> positionList = positionDao.selectAll();
        // tag取得
        List<Tag> tagList = tagDao.selectAll();
        // itemtag取得
        List<ItemTag> itemTagList = itemTagDao.selectAll();

        // positionをMapに登録
        Map<String, Position> positionMap = new HashMap<>();
        for (Position position : positionList) {
            positionMap.put(position.getPositionId(), position);
        }
        System.out.println(positionMap);

        // tagをMapに登録
        Map<String, Tag> tagMap = new HashMap<>();
        for(Tag tag : tagList){
            tagMap.put(tag.getTagId(), tag);
        }
        System.out.println(tagMap);

        // itemTagをMapに登録
        Map<String, ItemTag> itemTagMap = new HashMap<>();
        for(ItemTag itemTag : itemTagList){
            itemTagMap.put(itemTag.getItemId() + "_" + itemTag.getTagId(), itemTag);
        }
        System.out.println(itemTagMap);

        // itemをMapに登録
        Map<String, Item> itemMap = new HashMap<>();
        for(Item item : itemList){
            itemMap.put(item.getItemId(), item);
        }
        System.out.println(itemMap);

        List<SearchedItem> SearchedItemList = new ArrayList<>();
        for(Item item : itemList){
            boolean exist = false;
            if((item.getName().contains(text))
                    || (item.getDescription().contains(text))
            ){
                exist = true;
            }
            if((!exist) && (positionMap.containsKey(item.getPositionId()))){
                if(positionMap.get(item.getPositionId()).getName().contains(text)) {
                    exist = true;
                }
            }
            if(!exist){
                for(Tag tag : tagList){
                    if(itemTagMap.containsKey(item.getItemId() + "_"  + tag.getTagId())){
                        if(tagMap.get(tag.getTagId()).getName().contains(text)){
                            exist = true;
                            break;
                        }
                    }
                }
            }

            if(exist){
                List<String> tagNameList = new ArrayList<>();
                for(Tag tag : tagList){
                    if(itemTagMap.containsKey(item.getItemId() + "_"  + tag.getTagId())){
                        tagNameList.add(tagMap.get(tag.getTagId()).getName());
                    }
                }

                SearchedItemList.add(
                        new SearchedItem(
                                item.getName(),
                                positionMap.get(item.getPositionId()).getName(),
                                tagNameList,
                                item.getDescription()
                        )
                );
                System.out.println("dddddddd");
                System.out.println(SearchedItemList);
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
