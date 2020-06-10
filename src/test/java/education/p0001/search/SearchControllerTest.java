package education.p0001.search;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class SearchControllerTest {
    private MockMvc mockMvc;

    @Autowired
    SearchController sut;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(sut).build();
    }

    @Test
    public void getSearchedItemsTest() {
        // 備品名での絞り込み
        String result1 = sut.getSearchedItems("タップ").stream()
                .sorted(Comparator.comparing(it -> it.getName()))
                .map(it -> String.format("%s:%s:%s:%s", it.getName(), it.getDescription(), it.getPosition(), String.join(",", it.getTags().stream().sorted().collect(Collectors.toList()))))
                .collect(Collectors.joining("\n"));
        assertEquals("電源タップ-1:6個口:キャビネットA:開発部\n電源タップ-2:8個口:事務室:管理部\n電源タップ-3:8個口:開発室:開発部", result1);

        // 説明での絞り込み
        String result2 = sut.getSearchedItems("要").stream()
                .sorted(Comparator.comparing(it -> it.getName()))
                .map(it -> String.format("%s:%s:%s:%s", it.getName(), it.getDescription(), it.getPosition(), String.join(",", it.getTags().stream().sorted().collect(Collectors.toList()))))
                .collect(Collectors.joining("\n"));
        assertEquals("CD-Rメディア:利用する場合は要申請:キャビネットB:消耗品,開発部\nDVD-Rメディア:利用する場合は要申請:キャビネットB:消耗品,開発部", result2);

        // 場所で絞り込み
        String result3 = sut.getSearchedItems("議").stream()
                .sorted(Comparator.comparing(it -> it.getName()))
                .map(it -> String.format("%s:%s:%s:%s", it.getName(), it.getDescription(), it.getPosition(), String.join(",", it.getTags().stream().sorted().collect(Collectors.toList()))))
                .collect(Collectors.joining("\n"));
        assertEquals("ホワイトボード1::会議室B:営業部", result3);

        // タグでの絞り込み
        String result4 = sut.getSearchedItems("耗").stream()
                .sorted(Comparator.comparing(it -> it.getName()))
                .map(it -> String.format("%s:%s:%s:%s", it.getName(), it.getDescription(), it.getPosition(), String.join(",", it.getTags().stream().sorted().collect(Collectors.toList()))))
                .collect(Collectors.joining("\n"));
        assertEquals("CD-Rメディア:利用する場合は要申請:キャビネットB:消耗品,開発部\nDVD-Rメディア:利用する場合は要申請:キャビネットB:消耗品,開発部\n使い捨てマスク:使用するときは管理部まで:キャビネットB:消耗品,管理部", result4);
    }
}
