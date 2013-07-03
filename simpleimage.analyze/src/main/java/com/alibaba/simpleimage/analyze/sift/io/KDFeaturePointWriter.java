/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.analyze.sift.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.log4j.Logger;

import com.alibaba.simpleimage.analyze.sift.scale.KDFeaturePoint;

/**
 * @author axman 2013-5-15 10:57:14
 */
public class KDFeaturePointWriter {

    private final static Logger logger = Logger.getLogger(KDFeaturePointWriter.class);

    public static void writeComplete(String filename, KDFeaturePointListInfo kfl) {

        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(filename));
            List<KDFeaturePoint> list = kfl.getList();
            out.writeInt(list.size());
            for (KDFeaturePoint kp : list) {
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

    public static void writeComplete(ObjectOutputStream out, KDFeaturePointListInfo kfl) {

        try {
            List<KDFeaturePoint> list = kfl.getList();
            out.writeInt(list.size());
            for (KDFeaturePoint kp : list) {
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

