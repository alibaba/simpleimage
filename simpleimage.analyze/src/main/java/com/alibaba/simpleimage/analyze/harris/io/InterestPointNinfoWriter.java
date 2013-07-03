/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.analyze.harris.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.log4j.Logger;

import com.alibaba.simpleimage.analyze.harissurf.SURFInterestPointN;

/**
 * 类InterestPointWriter.java的实现描述：TODO 类实现描述 
 * @author axman 2013-5-15 上午11:59:07
 */
public class InterestPointNinfoWriter {
    private final static Logger logger = Logger.getLogger(InterestPointNinfoWriter.class);
    public static void writeComplete(String filename, InterestPointNListInfo ipln) {

        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(filename));
            List<SURFInterestPointN> list = ipln.getList();
            out.writeInt(list.size());
            for (SURFInterestPointN ip : list) {
                out.writeObject(ip);
            }
            out.writeInt(ipln.getWidth());
            out.writeInt(ipln.getHeight());
            out.flush();
        } catch (Exception e) {
            logger.equals(e.getMessage());
        }finally{
            if(out != null){
                try {
                    out.close();
                } catch (IOException e) {
                    logger.equals(e.getMessage());
                }
            }
        }
    }
}
