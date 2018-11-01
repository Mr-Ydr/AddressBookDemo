package com.drying.addressbook;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.drying.addressbook.utils.PinyinComparator;
import com.drying.addressbook.utils.PinyinUtils;
import com.drying.addressbook.utils.PowerGroupListener;
import com.drying.addressbook.utils.SectionDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Activity                activity;
    private RecyclerView            recyclerView;
    private LetterSideBar           waveSideBar;
    private List<AddressBookItemMo> list;
    private LinearLayoutManager     manager;
    private MyRecyclerAdapter       adapter;
    String[] nameArray = {"张三", "李四", "王五", "赵六", "123", "哈哈哈", "adf", "@阿道夫",
            "这阿道夫", "%a啊f", "法尔", "dfd", "啊哥哥", "ccc", "43", "daa", "啊实打实", "会看人看", "从VB从", "就愿意叫", "一放假"
            , "肉肉", "他说", "问", "去", "的", "在", "额"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;
        recyclerView = findViewById(R.id.address_book_recycler);
        waveSideBar = findViewById(R.id.sideBar);
        initRecyclerView();
        waveSideBar.setLetters(getWaveSideBarList());
        waveSideBar.setOnTouchLetterChangeListener(new LetterSideBar.OnTouchLetterChangeListener() {
            @Override
            public void onLetterChange(String letter) {
                //该字母首次出现的位置
                int position = getPositionForSection(letter.charAt(0));
                if (position != -1) {
                    manager.scrollToPositionWithOffset(position, 0);
                }
            }
        });
    }

    /**
     * 初始化recyclerView
     */
    private void initRecyclerView() {
        list = new ArrayList<>();
        adapter = new MyRecyclerAdapter(activity, list);
        recyclerView.setAdapter(adapter);
        manager = new LinearLayoutManager(activity);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        initDecoration();
        //初始化数据
        initData();
        //对数据进行排序
        Collections.sort(list, new PinyinComparator<AddressBookItemMo>());
        adapter.notifyDataSetChanged();
        //RecyclerView点击事件
        adapter.setListener(new MyRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(view.getContext(), list.get(position).getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(View view, int position) {
                Toast.makeText(view.getContext(), "长按：" + list.get(position).getName(), Toast.LENGTH_SHORT).show();
            }
        });
        //RecyclerView滚动事件用与和侧边滑动栏关联
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (manager != null) {
                    int firstPosition = manager.findFirstVisibleItemPosition();
                    waveSideBar.setCheckItem(list.get(firstPosition).getLetters());
                }
            }
        });
    }

    /**
     * 数据转换
     */
    private void initData() {
        for (String n : nameArray) {
            AddressBookItemMo vm = new AddressBookItemMo();
            if (n != null && !"".equals(n)) {
                vm.setName(n);
                //汉字转换成拼音
                String pinyin     = PinyinUtils.getPingYin(n);
                String sortString = pinyin.substring(0, 1).toUpperCase();
                // 正则表达式，判断首字母是否是英文字母
                if (sortString.matches("[A-Z]")) {
                    vm.setLetters(sortString.toUpperCase());
                } else {
                    vm.setLetters("#");
                }
            } else {
                vm.setLetters("#");
            }

            list.add(vm);
        }
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    private int getPositionForSection(int section) {
        for (int i = 0; i < list.size(); i++) {
            String sortStr   = list.get(i).getLetters();
            char   firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 获取侧边数据列表
     *
     * @return
     */
    private List<String> getWaveSideBarList() {
        List<String> barList = new ArrayList<>();
        if (list != null && list.size() > 0) {
            for (AddressBookItemMo m : list) {
                if (!barList.contains(m.getLetters())) {
                    barList.add(m.getLetters());
                }
            }
        }
        return barList;
    }

    /**
     * 添加悬浮布局
     */
    private void initDecoration() {
        SectionDecoration decoration = SectionDecoration.Builder
                .init(new PowerGroupListener() {
                    @Override
                    public String getGroupName(int position) {
                        //获取组名，用于判断是否是同一组
                        if (list.size() > position) {
                            return list.get(position).getLetters();
                        }
                        return null;
                    }

                    @Override
                    public View getGroupView(int position) {
                        //获取自定定义的组View
                        if (list.size() > position) {
                            TextView textView = new TextView(activity);
                            textView.setPadding(30, 12, 20, 12);
                            textView.setBackgroundColor(activity.getResources().getColor(android.R.color.holo_blue_bright));
                            textView.setText(list.get(position).getLetters());
                            textView.setTextColor(activity.getResources().getColor(android.R.color.black));
                            return textView;
                        } else {
                            return null;
                        }
                    }
                })
                //设置高度
                .setGroupHeight(60)
                .build();
        recyclerView.addItemDecoration(decoration);
    }
}
