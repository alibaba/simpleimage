package com.alibaba.security.simpleimage.analyze.sift;

/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
/**
 *
 * @author axman 2013-3-21 9:49:06
 */
public interface IImaging {
    
    public int getWidth();
    public int getHeight();
    
    /**
     * @param size
     * @return
     */
    public double scaleWithin(int dim);
    
    /**
     * @param converter
     * @return
     * @throws Exception 
     */
    public ImageMap toImageMap(IPixelConverter converter);
}
