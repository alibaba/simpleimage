/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.security.simpleimage.analyze.sift.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.log4j.Logger;

import com.alibaba.security.simpleimage.analyze.sift.scala.KeyPointN;

/**
 * 类KeyPointFileWriter.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-5-15 上午10:57:14
 */
public class KeyPointInfoWriter {

    private final static Logger logger = Logger.getLogger(KeyPointInfoWriter.class);

    public static void writeComplete(String filename, KeyPointListInfo kfl) {

        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(filename));
            List<KeyPointN> list = kfl.getList();
            out.writeInt(list.size());
            for (KeyPointN kp : list) {
                out.writeObject(kp);
            }
            out.writeInt(kfl.getWidth());
            out.writeInt(kfl.getHeight());
            out.flush();
        } catch (Exception e) {
            logger.equals(e.getMessage());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    logger.equals(e.getMessage());
                }
            }
        }
    }

    public static void writeComplete(ObjectOutputStream out, KeyPointListInfo kfl) {

        try {
            List<KeyPointN> list = kfl.getList();
            out.writeInt(list.size());
            for (KeyPointN kp : list) {
                out.writeObject(kp);
            }
            out.writeInt(kfl.getWidth());
            out.writeInt(kfl.getHeight());
            out.flush();
        } catch (Exception e) {
            logger.equals(e.getMessage());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    logger.equals(e.getMessage());
                }
            }
        }
    }
}
