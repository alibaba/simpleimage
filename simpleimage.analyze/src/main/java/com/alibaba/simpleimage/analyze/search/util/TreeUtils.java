package com.alibaba.simpleimage.analyze.search.util;

import java.util.LinkedList;
import java.util.List;

import com.alibaba.simpleimage.analyze.search.cluster.Clusterable;
import com.alibaba.simpleimage.analyze.search.tree.KMeansTreeNode;

public class TreeUtils {
	public static int findNearestNodeIndex(List<KMeansTreeNode> nodes,Clusterable target){
	    float minDistance = Float.MAX_VALUE;
		int index = -1;
		int i = 0;
		for ( KMeansTreeNode node : nodes ){
		    float distance = ClusterUtils.getEuclideanDistance(node,target);
			if ( distance < minDistance ){
				index = i;
				minDistance = distance;
			}
			i++;
		}
		return index;
	}
	
	public static KMeansTreeNode findNearestNode(List<KMeansTreeNode> nodes, KMeansTreeNode targetNode){
		int index = findNearestNodeIndex(nodes,targetNode);
		if ( index >= 0 )
			return nodes.get(index);
		return null;
	}
	
	/**
	 * @param node
	 * @param totalNodeCount
	 * @return
	 */
	public static List<Float> getCurrentWord(KMeansTreeNode node,int totalNodeCount){
		List<Float> values = new LinkedList<Float>();
		float weight = (float)Math.log((float)node.getNumSubItems()/(float)totalNodeCount);
		float val = weight * (float)node.getCurrentItemCount();
		values.add(val);
		for ( KMeansTreeNode subNode : node.getSubNodes() ){
			values.addAll(getCurrentWord(subNode,totalNodeCount));
		}
		return values;
	}
}
