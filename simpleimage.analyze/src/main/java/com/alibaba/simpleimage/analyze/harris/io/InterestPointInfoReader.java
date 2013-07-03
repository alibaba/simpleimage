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

import com.alibaba.simpleimage.analyze.harissurf.SURFInterestPoint;

/**
 * 类KeyPointFileReader.java的实现描述：TODO 类实现描述 
 * @author axman 2013-5-15 上午11:04:59
 */
public class InterestPointInfoReader {
    private final static Logger logger = Logger.getLogger(InterestPointInfoReader.class);
    public static InterestPointListInfo readComplete (String filePath) {
        
        ObjectInputStream fis = null;
        try {
            File f  = new File(filePath);
            fis = new  ObjectInputStream(new FileInputStream(f));
            int count = fis.readInt();
            List<SURFInterestPoint> al = new  ArrayList<SURFInterestPoint>();
            for(int i=0;i<count;i++){
                SURFInterestPoint ip = (SURFInterestPoint) fis.readObject();
                al.add(ip);
            }
            int w = fis.readInt();
            int h = fis.readInt();
            InterestPointListInfo ipl = new InterestPointListInfo();
            ipl.setImageFile(f.getName());
            ipl.setList(al);
            ipl.setWidth(w);
            ipl.setHeight(h);
            return ipl;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }finally{
            if(fis != null){
                try {
                    fis.close();
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }
        }
        
    }
    public static InterestPointListInfo readComplete (ObjectInputStream fis,String name) {
         try {
            int count = fis.readInt();
            List<SURFInterestPoint> al = new  ArrayList<SURFInterestPoint>();
            for(int i=0;i<count;i++){
                SURFInterestPoint ip = (SURFInterestPoint) fis.readObject();
                al.add(ip);
            }
            int w = fis.readInt();
            int h = fis.readInt();
            InterestPointListInfo ipl = new InterestPointListInfo();
            ipl.setImageFile(name);
            ipl.setList(al);
            ipl.setWidth(w);
            ipl.setHeight(h);
            return ipl;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }finally{
            if(fis != null){
                try {
                    fis.close();
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }
        }
        
    }
}
