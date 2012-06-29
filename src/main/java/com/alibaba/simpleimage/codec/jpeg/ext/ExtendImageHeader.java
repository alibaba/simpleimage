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
package com.alibaba.simpleimage.codec.jpeg.ext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author wendell
 */
public class ExtendImageHeader {

    private boolean             sawJFIFMarker;
    private boolean             sawJFXXMarker;
    private boolean             sawAdobeMarker;
    private int                 JFIFMajorVersion;
    private int                 JFIFMinorVersion;
    private int                 densityUnit;
    private int                 XDensity;
    private int                 YDensity;
    private int                 AdobeTransform;
    private boolean             existProfile    = false;
    private byte[]              profileData;

    // use for extend property
    private Map<String, Object> extraProperties = new HashMap<String, Object>();

    public void addExtraProperty(String name, Object value) {
        extraProperties.put(name, value);
    }

    public Object getExtraProperty(String name) {
        return extraProperties.get(name);
    }

    public Set<String> getExtraPropertyNames() {
        return extraProperties.keySet();
    }

    public void removeProperty(String name) {
        extraProperties.remove(name);
    }

    /**
     * @return the sawJFIFMarker
     */
    public boolean isSawJFIFMarker() {
        return sawJFIFMarker;
    }

    /**
     * @param sawJFIFMarker the sawJFIFMarker to set
     */
    public void setSawJFIFMarker(boolean sawJFIFMarker) {
        this.sawJFIFMarker = sawJFIFMarker;
    }

    /**
     * @return the sawAdobeMarker
     */
    public boolean isSawAdobeMarker() {
        return sawAdobeMarker;
    }

    /**
     * @param sawAdobeMarker the sawAdobeMarker to set
     */
    public void setSawAdobeMarker(boolean sawAdobeMarker) {
        this.sawAdobeMarker = sawAdobeMarker;
    }

    /**
     * @return the jFIFMajorVersion
     */
    public int getJFIFMajorVersion() {
        return JFIFMajorVersion;
    }

    /**
     * @param jFIFMajorVersion the jFIFMajorVersion to set
     */
    public void setJFIFMajorVersion(int jFIFMajorVersion) {
        JFIFMajorVersion = jFIFMajorVersion;
    }

    /**
     * @return the jFIFMinorVersion
     */
    public int getJFIFMinorVersion() {
        return JFIFMinorVersion;
    }

    /**
     * @param jFIFMinorVersion the jFIFMinorVersion to set
     */
    public void setJFIFMinorVersion(int jFIFMinorVersion) {
        JFIFMinorVersion = jFIFMinorVersion;
    }

    /**
     * @return the densityUnit
     */
    public int getDensityUnit() {
        return densityUnit;
    }

    /**
     * @param densityUnit the densityUnit to set
     */
    public void setDensityUnit(int densityUnit) {
        this.densityUnit = densityUnit;
    }

    /**
     * @return the xDensity
     */
    public int getXDensity() {
        return XDensity;
    }

    /**
     * @param xDensity the xDensity to set
     */
    public void setXDensity(int xDensity) {
        XDensity = xDensity;
    }

    /**
     * @return the yDensity
     */
    public int getYDensity() {
        return YDensity;
    }

    /**
     * @param yDensity the yDensity to set
     */
    public void setYDensity(int yDensity) {
        YDensity = yDensity;
    }

    /**
     * @return the adobeTransform
     */
    public int getAdobeTransform() {
        return AdobeTransform;
    }

    /**
     * @param adobeTransform the adobeTransform to set
     */
    public void setAdobeTransform(int adobeTransform) {
        AdobeTransform = adobeTransform;
    }

    /**
     * @return the sawJFXXMarker
     */
    public boolean isSawJFXXMarker() {
        return sawJFXXMarker;
    }

    /**
     * @param sawJFXXMarker the sawJFXXMarker to set
     */
    public void setSawJFXXMarker(boolean sawJFXXMarker) {
        this.sawJFXXMarker = sawJFXXMarker;
    }

    /**
     * @return the existProfile
     */
    public boolean isExistProfile() {
        return existProfile;
    }

    /**
     * @param existProfile the existProfile to set
     */
    public void setExistProfile(boolean existProfile) {
        this.existProfile = existProfile;
    }

    /**
     * @param profileData the profileData to set
     */
    public void setProfileData(byte[] profileData) {
        this.profileData = profileData;
    }

    public byte[] getProfileData() {
        return this.profileData;
    }
}
