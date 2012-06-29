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

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import com.alibaba.simpleimage.render.DrawTextItem;
import com.alibaba.simpleimage.render.DrawTextParameter;
import com.alibaba.simpleimage.render.WatermarkParameter;

/**
 * JAI图片处理辅助类
 * 
 * @author xianmao.hexm 2007-12-13 下午02:34:51
 */
public class ImageDrawHelper {

    static {
        JAIRegisterHelper.register();
    }

    private static ImageLog log = ImageLog.getLog(ImageDrawHelper.class);

    public static void drawText(BufferedImage src, DrawTextParameter dp) {
        if (dp == null || dp.getTextInfo() == null || dp.getTextInfo().size() == 0) {
            return;
        }

        int width = src.getWidth();
        int height = src.getHeight();

        Graphics2D graphics = src.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        try {
            for (DrawTextItem item : dp.getTextInfo()) {
                if (item != null) {
                    item.drawText(graphics, width, height);
                }
            }
        } finally {
            if (graphics != null) {
                graphics.dispose();
            }

            log.debug("After drawText", src);
        }
    }

    public static BufferedImage drawWatermark(BufferedImage src, WatermarkParameter param) {
        if (param == null) {
            return src;
        }

        if (param.getX() > src.getWidth() || param.getY() > src.getHeight()) {
            throw new IllegalArgumentException("Watermark's coordinate(" + param.getX() + ", " + param.getY()
                                               + ") exceed " + "the dimension of background image(" + src.getWidth()
                                               + ", " + src.getHeight() + ")");
        }

        Graphics2D graphics = src.createGraphics();
        Composite oldComposite = graphics.getComposite();
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, param.getAlpha()));
        try {
            graphics.drawImage(param.getWatermark().getAsBufferedImage(), null, param.getX(), param.getY());
        } finally {
            if (graphics != null) {
                graphics.setComposite(oldComposite);
                graphics.dispose();
            }
        }

        return src;
    }
}
