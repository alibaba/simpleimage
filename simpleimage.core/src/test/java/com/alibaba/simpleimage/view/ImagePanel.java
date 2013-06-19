/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.alibaba.simpleimage.ImageWrapper;

/**
 * 类ImagePanel.java的实现描述：TODO 类实现描述 
 * @author wendell 2011-7-21 下午04:52:11
 */
public class ImagePanel extends JPanel {
    /**
     * 
     */
    private static final long serialVersionUID = 8942241691324980880L;
    
    BufferedImage image;
    
    public static void show(ImageWrapper imageWrapper) {
        JFrame frame = new JFrame();
        frame.setContentPane(new ImagePanel(imageWrapper.getAsBufferedImage()));
        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        while(frame.isVisible()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    ImagePanel(BufferedImage image) {
        this.image = image;
        this.setBackground(Color.RED);
    }
    
    public void paint(Graphics g) {
        super.paint(g);
        g.drawImage(image, 0, 0, null);
    }
}
