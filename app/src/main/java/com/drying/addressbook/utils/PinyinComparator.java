package com.drying.addressbook.utils;

import java.util.Comparator;

/**
 * Author: drying
 * E-mail: drying@erongdu.com
 * Date: 2018/11/1 11:46
 * <p/>
 * Description:拼音排序
 */
public class PinyinComparator<T extends BaseComparator> implements Comparator<T> {
    public int compare(T o1, T o2) {
        if (o1.getLetters().equals("@")
                || o2.getLetters().equals("#")) {
            return 1;
        } else if (o1.getLetters().equals("#")
                || o2.getLetters().equals("@")) {
            return -1;
        } else {
            return o1.getLetters().compareTo(o2.getLetters());
        }
    }
}

