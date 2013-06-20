/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.security.simpleimage.analyze.kdtree;

import java.util.ArrayList;

/**
 * 类StoredLimitedList.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-3-22 下午3:27:53
 */

public class SortedLimitedList<E> extends ArrayList<E> {

    /**
     * 
     */
    private static final long serialVersionUID = 5621558689972829345L;
    int max;

    public SortedLimitedList(int maxElements){
        super(maxElements);
        this.max = maxElements;
    }

    @SuppressWarnings("unchecked")
    public boolean add(E e) {       
        int pos = size();
        while (pos > 0 &&  ((Comparable<E>)get(pos - 1)).compareTo(e) >= 0) {
            if (pos < max) setIdx(pos, get(pos - 1));
            pos--;
        }
        if (pos < max) setIdx(pos, e);
        else return false;
        return true;
    }

    private E setIdx(int idx, E e) {
        if (idx < size()) super.set(idx, e);
        else super.add(e);
        return e;
    }
}
