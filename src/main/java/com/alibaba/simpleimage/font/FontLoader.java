/*
 * Copyright 1999-2101 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.simpleimage.font;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 类FontLoader.java的实现描述：TODO 类实现描述
 * 
 * @author wendell 2011-4-19 下午04:03:59
 */
public class FontLoader {

    private volatile Font font;

    protected String      name;
    protected int         fontType;
    protected String      path;
    protected final Lock  lock = new ReentrantLock();

    public FontLoader(String name, int fontType, String path){
        this.name = name;
        this.fontType = fontType;
        this.path = path;
    }

    public FontLoader(String name, String path){
        this(name, Font.TRUETYPE_FONT, path);
    }

    protected Font loadFont(String path) {
        return loadFont(Font.TRUETYPE_FONT, path);
    }

    public String getName() {
        return name;
    }

    public Font getFont() {
        if (font == null) {
            lock.lock();
            try {
                if (font == null) {
                    font = loadFont(fontType, path);
                }
            } finally {
                lock.unlock();
            }
        }

        return font;
    }

    protected Font loadFont(int fontType, String path) {
        InputStream fontStream = null;
        try {
            fontStream = FontLoader.class.getResourceAsStream(path);

            return Font.createFont(fontType, fontStream);
        } catch (FontFormatException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (fontStream != null) {
                try {
                    fontStream.close();
                } catch (IOException ignore) {

                }
            }
        }
    }
}
