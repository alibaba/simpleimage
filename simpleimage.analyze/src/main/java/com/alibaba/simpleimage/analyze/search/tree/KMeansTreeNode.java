package com.alibaba.simpleimage.analyze.search.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alibaba.simpleimage.analyze.search.cluster.ClusterBuilder;
import com.alibaba.simpleimage.analyze.search.cluster.Clusterable;
import com.alibaba.simpleimage.analyze.search.cluster.impl.Cluster;
import com.alibaba.simpleimage.analyze.search.util.ClusterUtils;
import com.alibaba.simpleimage.analyze.search.util.TreeUtils;

public class KMeansTreeNode implements Clusterable, Serializable {
	private static final long serialVersionUID = 1L;
	private List<KMeansTreeNode> subNodes;

	private boolean isLeafNode = false;
	private float[] center;// The center of the item
	private int height = 0;// The depth of the node from root

	private int numSubItems;// Total number of items with a path through this
								// node, or the "weight"
	private int currentItems;// The current number of items with a path through
								// this node
	private int id = -1;// The unique id for the node in the tree, AKA the
							// "word" of the tree

	public KMeansTreeNode(float[] center, List<Clusterable> items,
			int branchFactor, int maxHeight, int height, ClusterBuilder clusterBuilder) {
		// TODO: Something about this global variable
		if (height == maxHeight || items.size() < branchFactor
				|| (getMeanDist(items, center) < 0)) {
			isLeafNode = true;
			subNodes = new ArrayList<KMeansTreeNode>(0);
			id = KMeansTree.idCount++;
		}

		else {
			Clusterable[] clusters =  clusterBuilder.collect(items, branchFactor);
			subNodes = new ArrayList<KMeansTreeNode>(branchFactor);
			for (Clusterable cluster : clusters) {
			    if(cluster instanceof Cluster)
				if (((Cluster)cluster).getItems().size() > 0) {
					KMeansTreeNode node = new KMeansTreeNode(
							((Cluster)cluster).getClusterMean(), ((Cluster)cluster).getItems(),
							branchFactor, maxHeight, height + 1, clusterBuilder);
					subNodes.add(node);
				}
			}
		}
		this.height = height;
		this.center = center;
		this.numSubItems = items.size();

	}

	private float getMeanDist(List<Clusterable> items, float[] center) {
	    float sum = 0;
		for (Clusterable clusterItem : items) {
		    float dist = ClusterUtils.getEuclideanDistance(
					clusterItem.getLocation(), center);
			sum += dist;
		}
		return sum / items.size();
	}

	public boolean isLeafNode() {
		return isLeafNode;
	}

	public List<KMeansTreeNode> getSubNodes() {
		return subNodes;
	}

	public float[] getLocation() {
		return center;
	}

	public int getNumSubItems() {
		return numSubItems;
	}

	public int getHeight() {
		return height;
	}

	public int getId() {
		return id;
	}



	/**
	 * Adds a clusterable to the current vocab tree for word creation
	 */
	public int addValue(Clusterable c) {
		currentItems++;
		/*
		 * if(isLeafNode()) { return id; }
		 */
		int index = TreeUtils.findNearestNodeIndex(subNodes, c);
		if (index >= 0) {
			KMeansTreeNode node = subNodes.get(index);
			return node.addValue(c);
		}
		return id;
	}

	public int getCurrentItemCount() {
		return currentItems;
	}

	public void reset() {
		currentItems = 0;
		for (KMeansTreeNode node : subNodes) {
			node.reset();
		}
	}

	@Override
	public String toString() {
		return "KMeansTreeNode [isLeafNode=" + isLeafNode + ", center="
				+ Arrays.toString(center) + ", height=" + height
				+ ", numSubItems=" + numSubItems + ", currentItems="
				+ currentItems + ", id=" + id + "]";
	}

}