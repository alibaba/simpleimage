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
package com.alibaba.simpleimage.jai.scale;

import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderableImage;

import javax.media.jai.CRIFImpl;
import javax.media.jai.ImageLayout;

import com.sun.media.jai.opimage.RIFUtil;

/**
 * TODO Comment of LanczosCRIF
 * 
 * @author wendell
 */
public class LanczosCRIF extends CRIFImpl {

    public LanczosCRIF() {
        super("LanczosScale");
    }

    /*
     * (non-Javadoc)
     * @see
     * javax.media.jai.CRIFImpl#create(java.awt.image.renderable.ParameterBlock,
     * java.awt.RenderingHints)
     */
    @SuppressWarnings("deprecation")
    @Override
    public RenderedImage create(ParameterBlock paramBlock, RenderingHints renderHints) {
        ImageLayout layout = RIFUtil.getImageLayoutHint(renderHints);

        RenderedImage source = paramBlock.getRenderedSource(0);
        double scaleX = paramBlock.getDoubleParameter(0);
        double scaleY = paramBlock.getDoubleParameter(1);

        // Check and see if we are scaling by 1.0 in both x and y and no
        // translations. If so return the source directly.  
        if (scaleX == 1.0 && scaleY == 1.0) {
            return source;
        }

        return new LanczosOpImage(source, layout, renderHints, scaleX, scaleY);
    }

    public Rectangle2D getBounds2D(ParameterBlock paramBlock) {

        RenderableImage source = paramBlock.getRenderableSource(0);

        double scaleX = paramBlock.getDoubleParameter(0);
        double scaleY = paramBlock.getDoubleParameter(1);

        // Get the source dimensions
        float x0 = (float) source.getMinX();
        float y0 = (float) source.getMinY();
        float w = (float) source.getWidth();
        float h = (float) source.getHeight();

        // Forward map the source using x0, y0, w and h
        float d_x0 = (float) (x0 * scaleX);
        float d_y0 = (float) (y0 * scaleY);
        float d_w = (float) (w * scaleX);
        float d_h = (float) (h * scaleY);

        return new Rectangle2D.Float(d_x0, d_y0, d_w, d_h);
    }
}
