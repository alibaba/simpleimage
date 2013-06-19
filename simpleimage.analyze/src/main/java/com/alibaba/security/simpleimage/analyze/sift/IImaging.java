package com.alibaba.security.simpleimage.analyze.sift;

/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
/**
 * 绫?Imaging.java????版?杩帮?TODO 绫诲??版?杩?
 * @author axman 2013-3-21 涓??9:49:06
 */
public interface IImaging {
    
    public int getWidth();
    public int getHeight();
    
    /**
     * 灏哄?绌洪??????     * @param size
     * @return
     */
    public double scaleWithin(int dim);
    
    /**
     * 浠?PixelConverter???涓?釜ImageMap
     * @param converter
     * @return
     * @throws Exception 
     */
    public ImageMap toImageMap(IPixelConverter converter);
}
