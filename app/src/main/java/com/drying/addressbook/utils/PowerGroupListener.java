package com.drying.addressbook.utils;

import android.view.View;

/**
 * Author: drying
 * E-mail: drying@erongdu.com
 * Date: 2018/11/1 11:48
 * <p/>
 * Description:
 */
public interface PowerGroupListener {
    String getGroupName(int position);

    View getGroupView(int position);
}
