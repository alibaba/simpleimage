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

/**
 * TODO Comment of ImageFormat
 * @author wendell
 *
 */
public enum ImageFormat {
    JPEG, TIFF, PNG, BMP, GIF, ICO, RAW, PSD, UNKNOWN;
    
    public static ImageFormat getImageFormat(String suffix){
        if("JPEG".equalsIgnoreCase(suffix)){
            return JPEG;
        }else if("JPG".equalsIgnoreCase(suffix)){
            return JPEG;
        }else if("BMP".equalsIgnoreCase(suffix)){
            return BMP;
        }else if("GIF".equalsIgnoreCase(suffix)){
            return GIF;
        }else if("PNG".equalsIgnoreCase(suffix)){
            return PNG;
        }else if("TIFF".equalsIgnoreCase(suffix)){
            return TIFF;
        }else if("TFF".equalsIgnoreCase(suffix)){
            return TIFF;
        }else{
            return UNKNOWN;
        }
    }
    
    public static String getDesc(ImageFormat format){
        if(JPEG == format){
            return "JPEG";
        }else if(BMP == format){
            return "BMP";
        }else if(GIF == format){
            return "GIF";
        }else if(PNG == format){
            return "PNG";
        }else if(TIFF == format){
            return "TIFF";
        }else{
            return "UNKNOWN";
        }
    }
    
    public String getDesc() {
        return ImageFormat.getDesc(this);
    }
}
