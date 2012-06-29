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

import javax.imageio.metadata.IIOMetadataNode;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 类NodeUtils.java的实现描述：TODO 类实现描述 
 * @author wendell 2011-8-29 下午03:13:23
 */
public class NodeUtils {
    
    /**
     * Currently only use by GIFStreamMetadata and GIFImageMetadata
     * @param node
     * @return
     */
    public static Node cloneNode(Node node) {
        if(node == null) {
            return null;
        }
        
        IIOMetadataNode newNode = new IIOMetadataNode(node.getNodeName());
        //clone user object
        if(node instanceof IIOMetadataNode) {
            IIOMetadataNode iioNode = (IIOMetadataNode)node;
            Object obj = iioNode.getUserObject();
            if(obj instanceof byte[]) {
                byte[] copyBytes = ((byte[])obj).clone();
                newNode.setUserObject(copyBytes);
            }
        }
        
        //clone attributes
        NamedNodeMap attrs = node.getAttributes();
        for(int i = 0; i < attrs.getLength(); i++ ) {
            Node attr = attrs.item(i);
            newNode.setAttribute(attr.getNodeName(), attr.getNodeValue());
        }
        
        //clone children
        NodeList children = node.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            newNode.appendChild(cloneNode(child));
        }
        
        return newNode;
    }
    
    public static Node getChild(Node parent, String nodeName) {
        if(nodeName == null) {
            return null;
        }
        
        NodeList children = parent.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if(nodeName.equalsIgnoreCase(child.getNodeName())) {
                return child;
            }
        }
        
        return null;
    }
    
    public static void removeChild(Node parent, String nodeName) {
        if(nodeName == null) {
            return ;
        }
        
        NodeList children = parent.getChildNodes();
        for(int j = 0; j < children.getLength(); j++) {
            Node child = children.item(j);
            if(nodeName.equalsIgnoreCase(child.getNodeName())) {
                parent.removeChild(child);
                return ;
            }
        }
    }
    
    public static void setAttrValue(Node node, String attrName, String value) {
        if(attrName == null) {
            return ;
        }
        
        NamedNodeMap attrs = node.getAttributes();
        for(int i = 0; i < attrs.getLength(); i++) {
            Node attr = attrs.item(i);
            if(attrName.equalsIgnoreCase(attr.getNodeName())) {
                attr.setNodeValue(value);
                return;
            }
        }
    }
    
    public static void setAttrValue(Node node, String attrName, int value) {
        setAttrValue(node, attrName, String.valueOf(value));
    }
    
    public static void setAttrValue(Node node, String attrName, double value) {
        setAttrValue(node, attrName, String.valueOf(value));
    }
    
    public static String getAttr(Node node, String attrName) {
        if(attrName == null) {
            return null;
        }
        
        NamedNodeMap attributes = node.getAttributes();
        for(int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            if(attrName.equalsIgnoreCase(attr.getNodeName())) {
                return attr.getNodeValue();
            }
        }
        
        return null;
    }
    
    public static int getIntAttr(Node node, String attrName) {
        String sValue = getAttr(node, attrName);
        if(sValue == null) {
            return Integer.MIN_VALUE;
        }
        
        return Integer.parseInt(sValue);
    }
    
    public static double getDoubleAttr(Node node, String attrName) {
        String sValue = getAttr(node, attrName);
        if(sValue == null) {
            return Double.NaN;
        }
        
        return Double.parseDouble(sValue);
    }
}
