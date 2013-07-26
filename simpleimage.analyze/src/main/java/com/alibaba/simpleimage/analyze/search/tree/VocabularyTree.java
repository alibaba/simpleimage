package com.alibaba.simpleimage.analyze.search.tree;

import java.util.List;

import com.alibaba.simpleimage.analyze.search.cluster.Clusterable;

public interface VocabularyTree {
	public List<Float> getCurrentWords();

	public List<Integer> addImage(List<? extends Clusterable> imagePoint);

	public void reset();
}
