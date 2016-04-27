package com.alibaba.simpleimage.analyze.search.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.alibaba.simpleimage.analyze.search.cluster.ClusterBuilder;
import com.alibaba.simpleimage.analyze.search.cluster.Clusterable;
import com.alibaba.simpleimage.analyze.search.cluster.impl.Cluster;
import com.alibaba.simpleimage.analyze.search.util.TreeUtils;

public class KMeansTree implements VocabularyTree, Serializable {

    private static final long serialVersionUID = 1L;

    public static int         idCount          = 0;

    private int               branchFactor     = 10;
    private int               maxHeight        = 6;
    private int               treeHeight       = 0;
    private int               numWords         = 0;  // The total number of descriptors added to the tree for word
                                                     // creation
    private KMeansTreeNode    rootNode;

    // private List<KMeansTreeNode> mBreadthWiseList;//A list of all of the nodes from a depth wise search

    /**
     * This creates a new tree from the items
     * 
     * @param items
     * @param branchFactor
     * @param leafNodeCapacity
     */
    public KMeansTree(List<Clusterable> items, int branchFactor, int height, ClusterBuilder clusterer){
        this.branchFactor = branchFactor;
        maxHeight = height;
        rootNode = new KMeansTreeNode(Cluster.getMeanValue(items), items, this.branchFactor, maxHeight, 0, clusterer);
    }

    public int getBranchFactor() {
        return this.branchFactor;
    }

    public int getNumWords() {
        return this.numWords;
    }

    public List<KMeansTreeNode> getBreadthWiseList() {
        List<KMeansTreeNode> nodes = new ArrayList<KMeansTreeNode>(rootNode.getNumSubItems() + 1);
        List<KMeansTreeNode> nodesLeft = new LinkedList<KMeansTreeNode>();
        nodesLeft.add(rootNode);
        KMeansTreeNode node = nodesLeft.remove(0);
        while (node != null) {
            // TODO: Remove (but why?)
            nodes.add(node);
            if (!node.isLeafNode()) nodesLeft.addAll(node.getSubNodes());
            if (nodesLeft.size() > 0) node = nodesLeft.remove(0);
            else node = null;
        }
        return nodes;
    }

    public List<KMeansTreeNode> getLeafsList() {
        List<KMeansTreeNode> nodes = new ArrayList<KMeansTreeNode>(rootNode.getNumSubItems() + 1);
        List<KMeansTreeNode> nodesLeft = new LinkedList<KMeansTreeNode>();
        nodesLeft.add(rootNode);
        KMeansTreeNode node = nodesLeft.remove(0);
        while (node != null) {
            if (node.isLeafNode()) {
                nodes.add(node);
            } else {
                nodesLeft.addAll(node.getSubNodes());
            }
            if (nodesLeft.size() > 0) {
                node = nodesLeft.remove(0);
            } else {
                node = null;
            }
        }
        return nodes;
    }

    public List<Integer> getVisualWords(List<? extends Clusterable> imagePoints) {
        List<Integer> visual_word_list = new ArrayList<Integer>();
        for (Clusterable value : imagePoints) {
            visual_word_list.add(getVisualWord(value));
        }
        return visual_word_list;
    }

    public int getVisualWord(Clusterable point) {
        numWords += 1;
        return rootNode.getValueId(point);
    }

    /**
     * Will return the current word in a depth wise search on the tree
     */
    public List<Float> getCurrentWords() {
        return TreeUtils.getCurrentWord(rootNode, rootNode.getNumSubItems());
    }

    public void reset() {
        numWords = 0;
        rootNode.reset();
    }

    public int getTreeHeight() {
        return treeHeight;
    }

    public KMeansTreeNode getRootNode() {
        return rootNode;
    }

}
