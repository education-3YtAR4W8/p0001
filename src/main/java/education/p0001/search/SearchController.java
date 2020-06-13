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
import lombok.NonNull;
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

        //返却用リスト
        List<SearchedItem> resSearchedItemList = new ArrayList<>();
        //一時格納用リスト
        List<SearchedItem> tmpSearchedItemList = new ArrayList<>();

        List<Item> allItems = itemDao.selectAll();
        List<Tag> allTags = tagDao.selectAll();
        List<Position> allPositions = positionDao.selectAll();
        List<ItemTag> allItemTags = itemTagDao.selectAll();

        SearchedItem searchedItem;
        String strPositionName = "";

        if (!text.equals("")) {

            //全件分の備品情報リストを作成
            for (Item item : allItems) {

                //position_tbl.name取得
                for (Position position : allPositions) {
                    //ポジションテーブル.ポジションID と アイテムテーブル.ポジションID が一致する場合
                    if (position.getPositionId().equals(item.getPositionId())) {

                        strPositionName = position.getName();
                        break;
                    }
                }

                //tag_tbl.name取得
                List<String> tagNameList = new ArrayList<>();
                for (ItemTag itemTag : allItemTags) {
                    //アイテムタグテーブル.アイテムID と アイテムテーブル.アイテムID が一致する場合
                    if (itemTag.getItemId().equals(item.getItemId())) {

                        for (Tag tag : allTags) {
                            //タグテーブル.タグID と アイテムタグテーブル.タグID が一致する場合
                            if (tag.getTagId().equals(itemTag.getTagId())) {

                                tagNameList.add(tag.getName());
                            }
                        }
                    }
                }

                searchedItem = new SearchedItem(item.getName(), strPositionName, tagNameList, item.getDescription());
                //一時格納用リストに追加
                tmpSearchedItemList.add(searchedItem);
            }

            //検索文字列と部分一致する備品情報のみ抽出
            for (SearchedItem tmpSchItem : tmpSearchedItemList) {

                if (tmpSchItem.getName().contains(text)
                        || tmpSchItem.getPosition().contains(text)
                        || tmpSchItem.getDescription().contains(text)) {

                    //返却用リストに追加
                    resSearchedItemList.add(tmpSchItem);
                    continue;
                }

                for (String tmpTagName : tmpSchItem.getTags()) {

                    if (tmpTagName.contains(text)) {

                        //返却用リストに追加
                        resSearchedItemList.add(tmpSchItem);
                        break;
                    }
                }
            }
        }

        return resSearchedItemList;
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
