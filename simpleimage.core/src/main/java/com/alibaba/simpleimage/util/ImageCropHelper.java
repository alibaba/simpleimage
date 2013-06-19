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

import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;

import com.alibaba.simpleimage.render.CropParameter;

/**
 * 图片裁剪帮助类
 * 
 * @author willian.huw
 */
public class ImageCropHelper {

	static {
		JAIRegisterHelper.register();
	}
	protected static ImageLog log = ImageLog.getLog(ImageCropHelper.class);
	

    public static PlanarImage crop(PlanarImage src, CropParameter param) {
    	ParameterBlock pb = new ParameterBlock();
    	pb.addSource(src);
    	pb.add(param.getX());
    	pb.add(param.getY());
    	pb.add((float)param.getWidth());
    	pb.add((float)param.getHeight());
        RenderedOp op = JAI.create("crop", pb);
        return op;
    }
}
