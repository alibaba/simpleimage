/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.gif;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 类NodeVisitor.java的实现描述：TODO 类实现描述 
 * @author wendell 2011-8-8 下午09:45:26
 */
public class NodeVisitor {

    private Node root;
    
    public NodeVisitor(Node node) {
        this.root = node;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        printNode(sb, root, 0);
        
        return sb.toString();
    }
    
    private void printNode(StringBuilder sb, Node node, int level) {
        sb.append("\n");
        for(int i = 0; i < level; i++) {
            sb.append("\t");
        }
        sb.append("<");
        sb.append(node.getNodeName());
        sb.append(" ");
        NamedNodeMap attrs = node.getAttributes();
        for(int i = 0; i < attrs.getLength(); i++) {
            Node attr = attrs.item(i);
            sb.append(attr.getNodeName());
            sb.append("=");
            sb.append("\"");
            sb.append(attr.getNodeValue());
            sb.append("\"");
            sb.append(" ");
        }
        sb.append(">\n");
        NodeList children = node.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            printNode(sb, child, level + 1);
        }
        sb.append("\n");
        for(int i = 0; i < level; i++) {
            sb.append("\t");
        }
        sb.append("</");
        sb.append(node.getNodeName());
        sb.append(">");
    }
}
