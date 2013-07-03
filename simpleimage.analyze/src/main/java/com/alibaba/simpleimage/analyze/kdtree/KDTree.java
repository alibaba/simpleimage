/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.analyze.kdtree;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.simpleimage.analyze.RefFloat;
import com.alibaba.simpleimage.analyze.RefInt;

/**
 * 类KDTree.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-3-22 下午4:05:00
 */
public class KDTree {

    IKDTreeDomain dr;      // 当前元素
    int           splitDim; // 子树分离元素

    KDTree        left;
    KDTree        right;

    @SuppressWarnings("rawtypes")
    public static class BestEntry implements Comparable {

        private float dist;
        private int   distSq;

        IKDTreeDomain neighbour;

        public IKDTreeDomain getNeighbour() {
            return this.neighbour;
        }

        public int getDistSq() {
            return distSq;
        }

        public void setDistSq(int distSq) {
            this.distSq = distSq;
        }

        public float getDist() {
            return dist;
        }

        public void setDist(float dist) {
            this.dist = dist;
        }

        BestEntry(IKDTreeDomain neighbour, int distSq){
            this.neighbour = neighbour;
            this.distSq = distSq;
        }

        BestEntry(IKDTreeDomain neighbour, float dist){
            this.neighbour = neighbour;
            this.dist = dist;
        }

        public int compareTo(Object o) {
            BestEntry be = (BestEntry) o;
            if (distSq < be.distSq) return -1;
            if (distSq > be.distSq) return 1;
            return 0;
        }

    }

    public static int distanceSq(IKDTreeDomain t1, IKDTreeDomain t2) {
        int distance = 0;
        for (int n = 0; n < t1.dim; ++n) {
            int dimDist = t1.descriptor[n] - t2.descriptor[n];
            distance += dimDist * dimDist;
        }
        return (distance);
    }

    static class HyperRectangle implements Cloneable {

        int[] leftTop;
        int[] rightBottom;
        int   dim;

        private HyperRectangle(int dim){
            this.dim = dim;
            leftTop = new int[dim];
            rightBottom = new int[dim];
        }

        public Object clone() {
            HyperRectangle rec = new HyperRectangle(dim);

            for (int n = 0; n < dim; ++n) {
                rec.leftTop[n] = leftTop[n];
                rec.rightBottom[n] = rightBottom[n];
            }

            return (rec);
        }

        static HyperRectangle createUniverseRectangle(int dim) {
            HyperRectangle rec = new HyperRectangle(dim);

            for (int n = 0; n < dim; ++n) {
                rec.leftTop[n] = Integer.MIN_VALUE;
                rec.rightBottom[n] = Integer.MAX_VALUE;
            }

            return (rec);
        }

        HyperRectangle splitAt(int splitDim, int splitVal) {
            if (leftTop[splitDim] >= splitVal || rightBottom[splitDim] < splitVal) {
                throw (new IllegalArgumentException("SplitAt with splitpoint outside rec"));
            }
            HyperRectangle r2 = (HyperRectangle) this.clone();
            rightBottom[splitDim] = splitVal;
            r2.leftTop[splitDim] = splitVal;
            return (r2);
        }

        boolean isIn(IKDTreeDomain target) {
            if (target.dim != dim) throw (new IllegalArgumentException("isIn dimension mismatch"));

            for (int n = 0; n < dim; ++n) {
                int targD = target.descriptor[n];

                if (targD < leftTop[n] || targD >= rightBottom[n]) return (false);
            }

            return (true);
        }

        boolean isInReach(IKDTreeDomain target, float distRad) {
            return (distance(target) < distRad);
        }

        // Return the distance from the nearest point from within the HR to
        // the target point.
        float distance(IKDTreeDomain target) {
            int closestPointN;
            int distance = 0;

            // first compute the closest point within hr to the target. if
            // this point is within reach of target, then there is an
            // intersection between the hypersphere around target with radius
            // 'dist' and this hyperrectangle.
            for (int n = 0; n < dim; ++n) {
                int tI = target.descriptor[n];
                int hrMin = leftTop[n];
                int hrMax = rightBottom[n];

                closestPointN = 0;
                if (tI <= hrMin) {
                    closestPointN = hrMin;
                } else if (tI > hrMin && tI < hrMax) {
                    closestPointN = tI;
                } else if (tI >= hrMax) {
                    closestPointN = hrMax;
                }

                int dimDist = tI - closestPointN;
                distance += dimDist * dimDist;
            }

            return (float) (Math.sqrt((float) distance));
        }
    }

    static class HREntry implements Comparable<HREntry> {

        float          dist;
        IKDTreeDomain  pivot;
        HyperRectangle rect;
        KDTree         tree;

        public float getDist() {
            return this.dist;
        }

        public IKDTreeDomain getPivot() {
            return this.pivot;
        }

        public HyperRectangle getRect() {
            return this.rect;
        }

        public KDTree getTree() {
            return this.tree;
        }

        public HREntry(HyperRectangle rect, KDTree tree, IKDTreeDomain pivot, float dist){
            this.dist = dist;
            this.pivot = pivot;
            this.tree = tree;
            this.rect = rect;
        }

        public int compareTo(HREntry o) {
            HREntry hre = (HREntry) o;
            if (dist < hre.dist) return -1;
            if (dist > hre.dist) return 1;
            return 0;
        }
    }

    /**
     * @param target
     * @param refDouble 必须从外部传入一个，且不能是缓存对象
     * @return
     */

    public IKDTreeDomain nearestNeighbour(IKDTreeDomain target, RefFloat ref) {
        HyperRectangle hr = HyperRectangle.createUniverseRectangle(target.dim);

        IKDTreeDomain nearest = nearestNeighbourI(target, hr, Float.POSITIVE_INFINITY, ref);
        // Math.sqrt(ref.val);
        return (nearest);
    }

    private IKDTreeDomain nearestNeighbourI(IKDTreeDomain target, HyperRectangle hr, float maxDistSq, RefFloat resDistSq) {
        // Console.WriteLine ("C NearestNeighbourI called");

        resDistSq.val = Float.POSITIVE_INFINITY;

        IKDTreeDomain pivot = dr;

        HyperRectangle leftHr = hr;
        HyperRectangle rightHr = leftHr.splitAt(splitDim, pivot.descriptor[splitDim]);

        HyperRectangle nearerHr, furtherHr;
        KDTree nearerKd, furtherKd;

        // step 5-7
        if (target.descriptor[splitDim] <= pivot.descriptor[splitDim]) {
            nearerKd = left;
            nearerHr = leftHr;
            furtherKd = right;
            furtherHr = rightHr;
        } else {
            nearerKd = right;
            nearerHr = rightHr;
            furtherKd = left;
            furtherHr = leftHr;
        }

        // step 8
        IKDTreeDomain nearest = null;

        RefFloat distSq = new RefFloat();
        if (nearerKd == null) {
            distSq.val = Float.POSITIVE_INFINITY;
        } else {
            nearest = nearerKd.nearestNeighbourI(target, nearerHr, maxDistSq, distSq);
        }

        // step 9
        maxDistSq = (float) Math.min(maxDistSq, distSq.val);

        // step 10
        if (furtherHr.isInReach(target, (float) Math.sqrt(maxDistSq))) {
            float ptDistSq = KDTree.distanceSq(pivot, target);
            if (ptDistSq < distSq.val) {
                // steps 10.1.1 to 10.1.3
                nearest = pivot;
                distSq.val = ptDistSq;
                maxDistSq = distSq.val;
            }

            // step 10.2
            RefFloat tempDistSq = new RefFloat();
            IKDTreeDomain tempNearest = null;
            if (furtherKd == null) {
                tempDistSq.val = Float.POSITIVE_INFINITY;
            } else {
                tempNearest = furtherKd.nearestNeighbourI(target, furtherHr, maxDistSq, tempDistSq);
            }

            // step 10.3
            if (tempDistSq.val < distSq.val) {
                nearest = tempNearest;
                distSq = tempDistSq;
            }
        }

        resDistSq = distSq;
        return (nearest);
    }

    /**
     * @param al
     * @return
     */

    public static KDTree createKDTree(List<? extends IKDTreeDomain> exset) {
        if (exset.size() == 0) return (null);
        KDTree cur = new KDTree();
        RefInt splitDim = new RefInt();
        splitDim.val = cur.splitDim;
        cur.dr = goodCandidate(exset, splitDim);
        cur.splitDim = splitDim.val;// ou ref cur.splitDim
        ArrayList<IKDTreeDomain> leftElems = new ArrayList<IKDTreeDomain>();
        ArrayList<IKDTreeDomain> rightElems = new ArrayList<IKDTreeDomain>();

        // split the exemplar set into left/right elements relative to the
        // splitting dimension
        float bound = cur.dr.descriptor[splitDim.val];
        for (IKDTreeDomain dom : exset) {
            // ignore the current element
            if (dom == cur.dr) continue;

            if (dom.descriptor[splitDim.val] <= bound) {
                leftElems.add(dom);
            } else {
                rightElems.add(dom);
            }
        }

        // recurse
        cur.left = createKDTree(leftElems);
        cur.right = createKDTree(rightElems);
        return (cur);
    }

    private static IKDTreeDomain goodCandidate(List<? extends IKDTreeDomain> exset, RefInt splitDim) {
        IKDTreeDomain first = exset.get(0);
        if (first == null) {
            throw (new java.lang.NullPointerException("Not of type IKDTreeDomain "));
        }
        int dim = first.dim;

        // initialize temporary hr search min/max values
        float[] minHr = new float[dim];
        float[] maxHr = new float[dim];
        for (int k = 0; k < dim; ++k) {
            minHr[k] = Float.POSITIVE_INFINITY;
            maxHr[k] = Float.NEGATIVE_INFINITY;
        }

        for (IKDTreeDomain dom : exset) {
            for (int k = 0; k < dim; ++k) {
                float dimE = dom.descriptor[k];
                if (dimE < minHr[k]) minHr[k] = dimE;
                if (dimE > maxHr[k]) maxHr[k] = dimE;
            }
        }

        // find the maximum range dimension
        float[] diffHr = new float[dim];
        int maxDiffDim = 0;
        float maxDiff = 0.0f;
        for (int k = 0; k < dim; ++k) {
            diffHr[k] = maxHr[k] - minHr[k];
            if (diffHr[k] > maxDiff) {
                maxDiff = diffHr[k];
                maxDiffDim = k;
            }
        }

        // the splitting dimension is maxDiffDim
        // now find a exemplar as close to the arithmetic middle as possible
        float middle = 0.0f;
        try {
            middle = (maxDiff / 2.0f) + minHr[maxDiffDim];
        } catch (Exception e) {
            System.out.println(dim);
            System.out.println(minHr.length);
        }

        IKDTreeDomain exemplar = null;
        float exemMinDiff = Float.POSITIVE_INFINITY;

        for (IKDTreeDomain dom : exset) {
            float curDiff = Math.abs(dom.descriptor[maxDiffDim] - middle);
            if (curDiff < exemMinDiff) {
                exemMinDiff = curDiff;
                exemplar = dom;
            }
        }

        // return the values
        splitDim.val = maxDiffDim;
        return (exemplar);
    }

    public ArrayList<BestEntry> nearestNeighbourListBBF(IKDTreeDomain kp, int q, int searchSteps) {
        HyperRectangle hr = HyperRectangle.createUniverseRectangle(kp.dim);

        SortedLimitedList<BestEntry> best = new SortedLimitedList<BestEntry>(q);
        SortedLimitedList<HREntry> searchHr = new SortedLimitedList<HREntry>(searchSteps);

        RefInt dummyDist = new RefInt();
        RefInt step = new RefInt();
        dummyDist.val = 0;
        step.val = searchSteps;
        nearestNeighbourListBBFI(best, q, kp, hr, Integer.MAX_VALUE, dummyDist, searchHr, step);
        for (BestEntry be : best)
            be.dist = (float) Math.sqrt(be.distSq);
        return (best);
    }

    private IKDTreeDomain nearestNeighbourListBBFI(SortedLimitedList<BestEntry> best, int q, IKDTreeDomain target,
                                                   HyperRectangle hr, int maxDistSq, RefInt resDistSq,
                                                   SortedLimitedList<HREntry> searchHr, RefInt searchSteps) {
        resDistSq.val = Integer.MAX_VALUE;

        IKDTreeDomain pivot = dr;
        best.add(new BestEntry(dr, KDTree.distanceSq(target, dr)));

        HyperRectangle leftHr = hr;
        HyperRectangle rightHr = leftHr.splitAt(splitDim, pivot.descriptor[splitDim]);

        HyperRectangle nearerHr, furtherHr;
        KDTree nearerKd, furtherKd;

        // step 5-7
        if (target.descriptor[splitDim] <= pivot.descriptor[splitDim]) {
            nearerKd = left;
            nearerHr = leftHr;
            furtherKd = right;
            furtherHr = rightHr;
        } else {
            nearerKd = right;
            nearerHr = rightHr;
            furtherKd = left;
            furtherHr = leftHr;
        }

        // step 8
        IKDTreeDomain nearest = null;
        RefInt distSq = new RefInt();

        searchHr.add(new HREntry(furtherHr, furtherKd, pivot, furtherHr.distance(target)));

        // No child, bottom reached!
        if (nearerKd == null) {
            distSq.val = Integer.MAX_VALUE;
        } else {
            nearest = nearerKd.nearestNeighbourListBBFI(best, q, target, nearerHr, maxDistSq, distSq, searchHr,
                                                        searchSteps);
        }

        // step 9
        if (best.size() >= q) {
            maxDistSq = ((BestEntry) best.get(q - 1)).getDistSq();
        } else maxDistSq = Integer.MAX_VALUE;

        if (searchHr.size() > 0) {
            HREntry hre = (HREntry) searchHr.get(0);
            searchHr.remove(0);

            furtherHr = hre.rect;
            furtherKd = hre.tree;
            pivot = hre.pivot;
        }

        // step 10
        searchSteps.val -= 1;
        if (searchSteps.val > 0 && furtherHr.isInReach(target, (float) Math.sqrt(maxDistSq))) {
            int ptDistSq = KDTree.distanceSq(pivot, target);
            if (ptDistSq < distSq.val) {
                // steps 10.1.1 to 10.1.3
                nearest = pivot;
                distSq.val = ptDistSq;
                maxDistSq = distSq.val;
            }

            // step 10.2
            RefInt tempDistSq = new RefInt();
            IKDTreeDomain tempNearest = null;
            if (furtherKd == null) {
                tempDistSq.val = Integer.MAX_VALUE;
            } else {
                tempNearest = furtherKd.nearestNeighbourListBBFI(best, q, target, furtherHr, maxDistSq, tempDistSq,
                                                                 searchHr, searchSteps);
            }

            // step 10.3
            if (tempDistSq.val < distSq.val) {
                nearest = tempNearest;
                distSq = tempDistSq;
            }
        }

        resDistSq = distSq;
        return (nearest);
    }

}

