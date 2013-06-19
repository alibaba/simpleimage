/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.security.simpleimage.analyze.testbed;

import java.io.IOException;

import com.alibaba.security.simpleimage.analyze.sift.ModifiableConst;

/**
 * 类TestSurf.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-5-23 上午11:37:12
 */
public class TestSurf extends Thread {
    static {
        System.setProperty(ModifiableConst._TOWPNTSCALAMINUS, "8.0");
        System.setProperty(ModifiableConst._SLOPEARCSTEP, "2");
        System.setProperty(ModifiableConst._TOWPNTORIENTATIONMINUS, "0.05");

    }

    private String[] args;
    private int      idx;

    public TestSurf(String[] args, int idx){
        this.args = args;
        this.idx = idx;
    }

    public void run() {
        try {
            testSurf(args, idx);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("argrements must be more than 3.");
            return;
        }
        System.out.println("model path:" + args[0]);
        System.out.println("logo path:" + args[1]);
        System.out.println("diff file path:" + args[2]);
        for (int i = 0; i < 10; i++)
            new MakeSurfPoint(args, i).start();
    }

    public static void testSurf(String[] args, int offset) throws IOException {

 
    }
}
