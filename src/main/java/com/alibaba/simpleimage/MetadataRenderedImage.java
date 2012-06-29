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
package com.alibaba.simpleimage;

import org.w3c.dom.Node;

/**
 * TODO Comment of MetaRenderedImage
 * 
 * @author wendell
 */
public abstract class MetadataRenderedImage {
    public static final String GIF_IMAGE_METADATA_NAME  = "javax_imageio_gif_image_1.0";
    public static final String GIF_STREAM_METADATA_NAME = "javax_imageio_gif_stream_1.0";
    
    // Used by JPEG
    protected int   quality;
    protected int[] horizontalSamplingFactors = new int[] { 1, 1, 1, 1 };
    protected int[] verticalSamplingFactors   = new int[] { 1, 1, 1, 1 };
    
    // Used by all image types, only used by gif currently
    protected Node[] metadatas;

    // Use by all image types, only used by gif currently
    protected Node streamMetadata;
    protected ImageFormat format;
    
    /**
     * 
     */
    public MetadataRenderedImage() {
        super();
    }

    public void setImageFormat(ImageFormat format){
        this.format = format;
    }
    
    public ImageFormat getImageFormat(){
        return this.format;
    }
    
    public Node getStreamMetadata() {
        return streamMetadata;
    }
    
    public void setStreamMetadata(Node streamMetadata) {
        this.streamMetadata = streamMetadata;
    }
    
    public Node getMetadata(int index){
        return metadatas[index];
    }
    
    public Node getMetadata(){
        return metadatas[0];
    }
    
    public Node[] getMetadatas(){
        return metadatas;
    }
    
    public void setMetadata(int index, Node metadata){
        metadatas[index] = metadata;
    }
    
    public void setMetadatas(Node[] metadatas){
        this.metadatas = metadatas;
    }
    
    public void setMetadata(Node metadata){
        if(metadatas == null){
            metadatas = new Node[1];
        }
        
        metadatas[0] = metadata;
    }
    
    public void setHorizontalSamplingFactor(int component, int subsample) {
        horizontalSamplingFactors[component] = subsample;
    }

    public int getHorizontalSamplingFactor(int component) {
        return horizontalSamplingFactors[component];
    }

    public int getHorizontalSubsampling(int component){
        int subsampling = horizontalSamplingFactors[component] == 1 ? 2 : 1;
        
        return subsampling;
    }
    
    public void setVerticalSamplingFactor(int component, int subsample) {
        verticalSamplingFactors[component] = subsample;
    }

    public int getVerticalSamplingFactor(int component) {
        return verticalSamplingFactors[component];
    }
    
    public int getVerticalSubsampling(int component){
        int subsampling = verticalSamplingFactors[component] == 1 ? 2 : 1;
        
        return subsampling;
    }

    public int getQuality() {
        return quality;
    }
}
