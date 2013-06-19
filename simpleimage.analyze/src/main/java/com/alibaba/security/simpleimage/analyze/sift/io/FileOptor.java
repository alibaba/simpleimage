/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.security.simpleimage.analyze.sift.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.alibaba.security.simpleimage.analyze.sift.scala.KeyPointN;

/**
 * 类FileOptor.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-4-10 下午2:33:11
 */
public class FileOptor {

    public static void save(KeyPointN[][] objs, String[] names, String fileName) throws FileNotFoundException,
                                                                                IOException {
        if (objs.length != names.length) throw new IOException("array length isn't equal.");
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
        out.writeInt(objs.length);
        for (int i = 0; i < objs.length; i++) {
            out.writeChars(names[i]);
            out.writeInt(objs[i].length);
            for (KeyPointN kp : objs[i]) {
                out.writeObject(kp);
            }
        }
        out.close();
    }

    public KeyPointN[][] read(String fileName) throws Exception {

        ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
        Object line = null;
        int x = in.readInt();
        KeyPointN[][] objs = new KeyPointN[x][];
        int idx = 0;
        while ((line = in.readObject()) != null) {
            KeyPointN[] obj = null;
            if (line instanceof String) {
                int len = in.readInt();
                obj = new KeyPointN[len];
                for(int i=0;i<obj.length;i++){
                    obj[i] = (KeyPointN)in.readObject();
                }
            }
            objs[idx++] = obj;
        }
        in.close();
        return objs;
    }
}
