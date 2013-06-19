/**
 * Project: simple-image File Created at 2010-6-28 $Id$ Copyright 2008 Alibaba.com Croporation Limited. All rights
 * reserved. This software is the confidential and proprietary information of Alibaba Company.
 * ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.alibaba.simpleimage;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.alibaba.simpleimage.render.DrawTextItem;
import com.alibaba.simpleimage.render.DrawTextParameter;
import com.alibaba.simpleimage.render.FixDrawTextItem;
import com.alibaba.simpleimage.render.FootnoteDrawTextItem;
import com.alibaba.simpleimage.render.ScaleParameter;

/**
 * TODO Comment of PressureTester
 * 
 * @author wendell
 */
public class PressureTester {

    File           sourceDir               = new File("./src/test/resources/conf.test/simpleimage");

    // 开启线程数
    int            threadsNum              = 2;
    // 总处理执行次数
    AtomicLong     total                   = new AtomicLong(100L);

    CountDownLatch countDownLatch          = new CountDownLatch(threadsNum);

    Font           DEFAULT_MAIN_TEXT_FONT  = new Font("Monospace", 0, 1);
    Color          DEFAULT_MAIN_TEXT_COLOR = new Color(0.7F, 0.7F, 0.7F, 0.50F);
    Font           DEFAULT_FOOT_TEXT_FONT  = new Font("arial", 0, 1);
    ScaleParameter DEFAULT_SCALE_PARAM     = new ScaleParameter(1024, 1024);

    public static void main(String[] args) throws Exception {
        int threads = 1;
        long tasks = Long.MAX_VALUE;
        String rootDir = "";

        if (args.length >= 1) {
            rootDir = args[0];
        }

        if (args.length >= 2) {
            threads = Integer.parseInt(args[1]);
        }

        if (args.length >= 3) {
            tasks = Long.parseLong(args[2]);
        }

        if ("null".equalsIgnoreCase(rootDir)) {
            rootDir = null;
        }
        System.out.println("task begin");
        new PressureTester(threads, tasks, rootDir).run();
        System.out.println("task end");
    }

    public PressureTester(int threadsNum, long total, String rootDir){
        this.threadsNum = threadsNum;
        this.total = new AtomicLong(total);
        this.countDownLatch = new CountDownLatch(threadsNum);
        if (StringUtils.isNotBlank(rootDir)) {
            sourceDir = new File(rootDir + "/src/test/resources/conf.test/simpleimage");
        }else{
            throw new IllegalArgumentException("root dir must not be null");
        }
    }

    public void run() throws Exception {
        Thread[] threads = new Thread[threadsNum];

        for (int i = 0; i < threadsNum; i++) {
            threads[i] = new Thread(new ProcessImage("Thread-" + i), "Thread-" + i);
        }

        for (int i = 0; i < threadsNum; i++) {
            threads[i].start();
        }

        countDownLatch.await();
    }

    class ProcessImage implements Runnable {

        private String name;

        public ProcessImage(String name){
            this.name = name;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        public void run() {
            try {
                CompositeImageProcessor processor = new CompositeImageProcessor();

                for (;;) {
                    if (total.getAndDecrement() <= 0) {
                        return;
                    }

                    List<File> images = new ArrayList<File>();
                    
                    File imgDir = new File(sourceDir, "bmp");
                    for(File img : imgDir.listFiles()){
                        images.add(img);
                    }
                    imgDir = new File(sourceDir, "cmyk");
                    for(File img : imgDir.listFiles()){
                        images.add(img);
                    }
                    imgDir = new File(sourceDir, "png");
                    for(File img : imgDir.listFiles()){
                        images.add(img);
                    }
                    imgDir = new File(sourceDir, "tiff");
                    for(File img : imgDir.listFiles()){
                        images.add(img);
                    }
                    imgDir = new File(sourceDir, "rgb");
                    for(File img : imgDir.listFiles()){
                        images.add(img);
                    }
                    imgDir = new File(sourceDir, "gif");
                    for(File img : imgDir.listFiles()){
                        images.add(img);
                    }
                    imgDir = new File(sourceDir, "gray");
                    for(File img : imgDir.listFiles()){
                        images.add(img);
                    }
                    imgDir = new File(sourceDir, "malformed");
                    for(File img : imgDir.listFiles()){
                        images.add(img);
                    }
                    imgDir = new File(sourceDir, "quality");
                    for(File img : imgDir.listFiles()){
                        images.add(img);
                    }
                    imgDir = new File(sourceDir, "errorjpeg");
                    for(File img : imgDir.listFiles()){
                        images.add(img);
                    }

                    for (File img : images) {
                        if (img.isDirectory()) {
                            continue;
                        }

                        if(img.getName().indexOf("result") > 0){
                            continue;
                        }
                        
                        System.out.println("[" + name + "] test " + img.getName());
                        FileInputStream inputStream = null;
                        OutputStream out = null;
                        try {
                            inputStream = new FileInputStream(img);
                            out = processor.process(inputStream, null, 1024, 1024);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            IOUtils.closeQuietly(inputStream);
                            IOUtils.closeQuietly(out);
                        }
                    }
                }
            } finally {
                countDownLatch.countDown();
            }
        }

        public DrawTextParameter createDrawTextParameter(String mainTxt, boolean drawMainTxt, boolean drawFootTxt) {
            List<DrawTextItem> textItems = new ArrayList<DrawTextItem>(4);
            if (drawMainTxt) {
                DrawTextItem mainTextItem = new FixDrawTextItem(mainTxt);
                textItems.add(mainTextItem);
            }

            if (drawFootTxt) {
                DrawTextItem footTextItem = new FootnoteDrawTextItem(mainTxt, "www.alibaba.com.cn");
                textItems.add(footTextItem);
            }

            DrawTextParameter dtp = new DrawTextParameter(textItems);

            return dtp;
        }
    }
}
