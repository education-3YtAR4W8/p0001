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

        //データ格納用のリストインスタンス作成
        List<SearchedItem> itemList = new ArrayList<SearchedItem>();

        //item_tbl以外の検索結果格納リスト宣言
        List<ItemTag> searchItemTagList = new ArrayList<ItemTag>();
        List<Tag> searchTagList = new ArrayList<Tag>();
        List<Position> searchPositionList = new ArrayList<Position>();

        //item_tbl以外のキーワード検索を行い、結果をリストに格納
        {
            //キーワードに引っかかるタグをリスト抽出
            for (Tag tag : tagDao.selectAll()) {
                if (tag.getName().contains(text))
                    searchTagList.add(tag);
            }

            //関連テーブルに対して抽出したタグリストを利用し、リスト抽出
            for (ItemTag itemTag : itemTagDao.selectAll()) {
                for (Tag searchTag : searchTagList) {
                    if (itemTag.getTagId().equals(searchTag.getTagId())) {
                        searchItemTagList.add(itemTag);
                        break;
                    }
                }
            }

            //キーワードに引っかかる場所をリスト抽出
            for (Position position : positionDao.selectAll()) {
                if (position.getName().contains(text))
                    searchPositionList.add(position);
            }
        }


        //item_tblに対してここまで抽出したリストを利用し検索、その後データ格納
        {
            boolean judge; //検索対象フラグ
            List<String> tags;
            SearchedItem searchedItem;

            for (Item item : itemDao.selectAll()) {
                judge = false;
                searchedItem = new SearchedItem(null, null, null, null);
                tags = new ArrayList<String>();

                //アイテムタグで検索
                for (ItemTag searchItemTag : searchItemTagList) {
                    if (item.getItemId().equals(searchItemTag.getItemId())) {
                        judge = true;
                    }
                }

                //場所で検索
                if (!judge) {
                    for (Position searchPosition : searchPositionList) {
                        if (item.getPositionId().equals(searchPosition.getPositionId())) {
                            judge = true;
                            break;
                        }
                    }
                }

                //名前と備考で検索
                if (!judge) {
                    if (item.getName().contains(text) || item.getDescription().contains(text))
                        judge = true;
                }

                //キーワードが引っかかっているデータを格納
                if (judge) {
                    searchedItem.setName(item.getName());
                    searchedItem.setDescription(item.getDescription());

                    for (ItemTag itemTag : itemTagDao.selectAll()) {
                        if (item.getItemId().equals(itemTag.getItemId())) {
                            for (Tag tag : tagDao.selectAll()) {
                                if (itemTag.getTagId().equals(tag.getTagId()))
                                    tags.add(tag.getName());
                            }
                        }
                    }
                    searchedItem.setTags(tags);

                    for (Position position : positionDao.selectAll()) {
                        if (item.getPositionId().equals(position.getPositionId())) {
                            searchedItem.setPosition(position.getName());
                            break;
                        }
                    }
                    itemList.add(searchedItem);
                }
            }
        }

        return itemList;
//        return new ArrayList<>();
    }

    @Getter
    @Setter
    static public class Page {
        private String searchText;
        private List<SearchedItem> items = new ArrayList<>();
    }

    @AllArgsConstructor
    @Getter
    @Setter
    static public class SearchedItem {
        private String name;
        private String position;
        private List<String> tags;
        private String description;
    }
}
