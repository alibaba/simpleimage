/**
 * Project: simpleimage-1.1 File Created at 2010-8-31 $Id$ Copyright 2008 Alibaba.com Croporation Limited. All rights
 * reserved. This software is the confidential and proprietary information of Alibaba Company.
 * ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.alibaba.simpleimage.codec.jpeg;

import junit.framework.TestCase;

/**
 * TODO Comment of ShortSpeedTest
 * 
 * @author wendell
 */
public class NumberSpeedTest extends TestCase {

	public void testNull() {
		
	}

    public void doTestShortSpeed(long times) {
        long start = System.currentTimeMillis();

        short x = 1;
        short y = 1;
        short a = 1;
        for (long i = 0; i < times; i++) {
            a = (short) (a + x * y);
        }

        start = System.currentTimeMillis() - start;
        System.out.println("Short speed test");
        System.out.println("Time : " + start);
        System.out.println(a);
    }

    public void doTestIntSpeed(long times) {
        long start = System.currentTimeMillis();

        int x = 1;
        int y = 1;
        int a = 1;
        for (long i = 0; i < times; i++) {
            a = (a + x * y);
        }

        start = System.currentTimeMillis() - start;
        System.out.println("Int speed test");
        System.out.println("Time : " + start);
        System.out.println(a);
    }

    public void doTestLongSpeed(long times) {
        long start = System.currentTimeMillis();

        long x = 1;
        long y = 1;
        long a = 1;
        for (long i = 0; i < times; i++) {
            a = (long) (a + x * y);
        }

        start = System.currentTimeMillis() - start;
        System.out.println("Long speed test");
        System.out.println("Time : " + start);
        System.out.println(a);
    }

    public void doTestFloatSpeed(long times) {
        long start = System.currentTimeMillis();

        float x = 1.0f;
        float y = 1.0f;
        float a = 1.0f;
        for (long i = 0; i < times; i++) {
            a = (float) (a + x * y);
        }

        start = System.currentTimeMillis() - start;
        System.out.println("Float speed test");
        System.out.println("Time : " + start);
        System.out.println(a);
    }

    public void doTestDoubleSpeed(long times) {
        long start = System.currentTimeMillis();

        double x = 1.0;
        double y = 1.0;
        double a = 1.0;
        for (long i = 0; i < times; i++) {
            a = (double) (a + x * y);
        }

        start = System.currentTimeMillis() - start;
        System.out.println("Double speed test");
        System.out.println("Time : " + start);
        System.out.println(a);
    }

    public static void main(String[] args) {
        String type = "short";
        long times = 10000000000L;

        if (args.length > 0) {
            type = args[0].trim().toLowerCase();
        }

        if (args.length > 1) {
            times = Long.parseLong(args[1]);
        }

        NumberSpeedTest tester = new NumberSpeedTest();
        if ("short".equals(type)) {
            tester.doTestShortSpeed(times);
        } else if ("int".equals(type)) {
            tester.doTestIntSpeed(times);
        } else if ("long".equals(type)) {
            tester.doTestLongSpeed(times);
        } else if ("float".equals(type)) {
            tester.doTestFloatSpeed(times);
        } else if ("double".equals(type)) {
            tester.doTestDoubleSpeed(times);
        }
    }
 
}
