/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.analyze.harris.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.alibaba.simpleimage.analyze.harissurf.SURFInterestPointN;

/**
 * 类KeyPointFileReader.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-5-15 上午11:04:59
 */
public class InterestPointNInfoReader {

    private final static Logger logger = Logger.getLogger(InterestPointNInfoReader.class);

    public static InterestPointNListInfo readComplete(String fileName) {

        ObjectInputStream fis = null;
        try {
            File f = new File(fileName);
            fis = new ObjectInputStream(new FileInputStream(f));
            int count = fis.readInt();
            List<SURFInterestPointN> al = new ArrayList<SURFInterestPointN>();
            for (int i = 0; i < count; i++) {
                SURFInterestPointN ip = (SURFInterestPointN) fis.readObject();
                al.add(ip);
            }
            int w = fis.readInt();
            int h = fis.readInt();
            InterestPointNListInfo ipln = new InterestPointNListInfo();
            ipln.setImageFile(f.getName());
            ipln.setList(al);
            ipln.setWidth(w);
            ipln.setHeight(h);
            return ipln;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }
        }

    }
}
