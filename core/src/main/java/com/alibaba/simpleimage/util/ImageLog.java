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
package com.alibaba.simpleimage.util;

import java.awt.image.RenderedImage;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * just use log image info
 * 
 * @author wendell
 */
public class ImageLog {

    private Log log;

    public static ImageLog getLog(Class<?> clazz) {
        Log log = LogFactory.getLog(clazz);

        return new ImageLog(log);
    }

    private ImageLog(Log log){
        this.log = log;
    }

    protected String formatMsg(Object action, RenderedImage img) {
        String msgFmt = action + ": {0}={1} DataType={2} Width={3} Height={4}";
        String clazz = img.getColorModel().getColorSpace().getClass().getSimpleName();
        int type = img.getColorModel().getColorSpace().getType();
        int dataType = img.getData().getDataBuffer().getDataType();
        int w = img.getWidth();
        int h = img.getHeight();

        String msg = MessageFormat.format(msgFmt, clazz, getColorSpaceName(type), getDataTypeName(dataType), w, h);

        return msg;
    }

    protected String getColorSpaceName(int type) {
        switch (type) {
            case 0:
                return "TYPE_XYZ";
            case 1:
                return "TYPE_Lab";
            case 2:
                return "TYPE_Luv";
            case 3:
                return "TYPE_YCbCr";
            case 4:
                return "TYPE_Yxy";
            case 5:
                return "TYPE_RGB";
            case 6:
                return "TYPE_GRAY";
            case 7:
                return "TYPE_HSV";
            case 8:
                return "TYPE_HLS";
            case 9:
                return "TYPE_CMYK";
            case 11:
                return "TYPE_CMY";
            case 12:
                return "TYPE_2CLR";
            case 13:
                return "TYPE_3CLR";
            case 14:
                return "TYPE_4CLR";
            case 15:
                return "TYPE_5CLR";
            case 16:
                return "TYPE_6CLR";
            case 17:
                return "TYPE_7CLR";
            case 18:
                return "TYPE_8CLR";
            case 19:
                return "TYPE_9CLR";
            case 20:
                return "TYPE_ACLR";
            case 21:
                return "TYPE_BCLR";
            case 22:
                return "TYPE_CCLR";
            case 23:
                return "TYPE_DCLR";
            case 24:
                return "TYPE_ECLR";
            case 25:
                return "TYPE_FCLR";
            default:
                return "TYPE_UNDEFINED";
        }
    }

    protected String getDataTypeName(int dataType) {
        switch (dataType) {
            case 0:
                return "TYPE_BYTE";
            case 1:
                return "TYPE_USHORT";
            case 2:
                return "TYPE_SHORT";
            case 3:
                return "TYPE_INT";
            case 4:
                return "TYPE_FLOAT";
            case 5:
                return "TYPE_DOUBLE";
            default:
                return "TYPE_UNDEFINED";
        }
    }

    public void info(Object msg) {
        if (log.isInfoEnabled()) {
            log.info(msg);
        }
    }

    public void info(Object action, RenderedImage img) {
        if (log.isInfoEnabled()) {
            String msg = formatMsg(action, img);
            log.info(msg);
        }
    }

    public void debug(Object msg) {
        if (log.isDebugEnabled()) {
            log.debug(msg);
        }
    }

    public void debug(Object action, RenderedImage img) {
        if (log.isDebugEnabled()) {
            String msg = formatMsg(action, img);
            log.debug(msg);
        }
    }

    public void fatal(Object action, RenderedImage img) {
        if (log.isFatalEnabled()) {
            String msg = formatMsg(action, img);
            log.fatal(msg);
        }
    }

    public void fatal(Object msg) {
        if (log.isFatalEnabled()) {
            log.fatal(msg);
        }
    }

    public void error(Object msg) {
        if (log.isErrorEnabled()) {
            log.error(msg);
        }
    }

    public void error(Object action, RenderedImage img) {
        if (log.isErrorEnabled()) {
            String msg = formatMsg(action, img);
            log.error(msg);
        }
    }

    public void trace(Object msg) {
        if (log.isTraceEnabled()) {
            log.trace(msg);
        }
    }

    public void trace(Object action, RenderedImage img) {
        if (log.isTraceEnabled()) {
            String msg = formatMsg(action, img);
            log.trace(msg);
        }
    }

    public void warn(Object msg) {
        if (log.isWarnEnabled()) {
            log.warn(msg);
        }
    }

    public void warn(Object action, RenderedImage img) {
        if (log.isWarnEnabled()) {
            String msg = formatMsg(action, img);
            log.warn(msg);
        }
    }
}
