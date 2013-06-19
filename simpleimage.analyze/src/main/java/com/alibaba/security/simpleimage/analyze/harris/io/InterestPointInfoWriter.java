/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.security.simpleimage.analyze.harris.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.log4j.Logger;

import com.alibaba.security.simpleimage.analyze.harissurf.SURFInterestPoint;

/**
 * 类InterestPointWriter.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-5-15 上午11:59:07
 */
public class InterestPointInfoWriter {

    private final static Logger logger = Logger.getLogger(InterestPointInfoWriter.class);

    public static void writeComplete(String filename, InterestPointListInfo ipl) {

        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(filename));
            List<SURFInterestPoint> list = ipl.getList();
            out.writeInt(list.size());
            for (SURFInterestPoint ip : list) {
                out.writeObject(ip);
            }
            out.writeInt(ipl.getWidth());
            out.writeInt(ipl.getHeight());
            out.flush();
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }
        }
    }

    public static void writeComplete(ObjectOutputStream out, InterestPointListInfo ipl) {
        try {

            List<SURFInterestPoint> list = ipl.getList();
            out.writeInt(list.size());
            for (SURFInterestPoint ip : list) {
                out.writeObject(ip);
            }
            out.writeInt(ipl.getWidth());
            out.writeInt(ipl.getHeight());
            out.flush();
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }
        }
    }
}
