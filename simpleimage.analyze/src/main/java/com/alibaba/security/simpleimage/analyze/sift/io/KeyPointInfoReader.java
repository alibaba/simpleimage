/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.security.simpleimage.analyze.sift.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.alibaba.security.simpleimage.analyze.sift.scala.KeyPointN;

/**
 * 类KeyPointFileReader.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-5-15 上午11:04:59
 */
public class KeyPointInfoReader {

    private final static Logger logger = Logger.getLogger(KeyPointInfoReader.class);

    public static KeyPointListInfo readComplete(String filePath) {

        ObjectInputStream fis = null;
        try {
            File f = new File(filePath);
            fis = new ObjectInputStream(new FileInputStream(f));
            int count = fis.readInt();
            if (count == 0) return null;
            List<KeyPointN> al = new ArrayList<KeyPointN>();
            for (int i = 0; i < count; i++) {
                KeyPointN kp = (KeyPointN) fis.readObject();
                al.add(kp);
            }
            int w = fis.readInt();
            int h = fis.readInt();
            KeyPointListInfo kfl = new KeyPointListInfo();
            kfl.setImageFile(f.getName());
            kfl.setList(al);
            kfl.setWidth(w);
            kfl.setHeight(h);
            return kfl;
        } catch (Exception e) {
            logger.equals(e.getMessage());
            return null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    logger.equals(e.getMessage());
                }
            }
        }

    }

    public static KeyPointListInfo readComplete(ObjectInputStream fis, String name) {

        try {
            int count = fis.readInt();
            if (count == 0) return null;
            List<KeyPointN> al = new ArrayList<KeyPointN>();
            for (int i = 0; i < count; i++) {
                KeyPointN kp = (KeyPointN) fis.readObject();
                al.add(kp);
            }
            int w = fis.readInt();
            int h = fis.readInt();
            KeyPointListInfo kfl = new KeyPointListInfo();
            kfl.setImageFile(name);
            kfl.setList(al);
            kfl.setWidth(w);
            kfl.setHeight(h);
            return kfl;
        } catch (Exception e) {
            logger.equals(e.getMessage());
            return null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    logger.equals(e.getMessage());
                }
            }
        }
    }
}
