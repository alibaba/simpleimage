/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.analyze.search.engine;


/**
 * 类Score.java的实现描述：TODO 类实现描述 
 * @author axman 2013-7-24 下午1:37:56
 */
public class Score implements Comparable<Score> {
    private int identity;

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    private float score;

    public int getIdentity() {
        return identity;
    }

    public void setIdentity(int identity) {
        this.identity = identity;
    }

    public Score(int identity, float score) {
        this.identity = identity;
        this.score = score;
    }

    public int compareTo(Score s) {
        float tmp = s.score - this.score;
        if(tmp > 0.000000001f)
            return 1;
        else if(tmp < -0.000000001f)
            return -1;
        else 
            return 0;
    }
}