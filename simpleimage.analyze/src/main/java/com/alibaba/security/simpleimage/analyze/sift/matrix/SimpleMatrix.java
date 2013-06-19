/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.security.simpleimage.analyze.sift.matrix;

/**
 * 类SimpleMatrix.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-3-25 下午4:41:17
 */
// A very simple 2-dimensional Matrix class providing some basic operations.
public class SimpleMatrix implements Cloneable {

    public double[][] values;
    int               xDim, yDim;

    public SimpleMatrix(int yDim, int xDim){
        this.xDim = xDim;
        this.yDim = yDim;
        values = new double[yDim][xDim];
    }

    public Object clone() {
        SimpleMatrix cp = new SimpleMatrix(yDim, xDim);
        for (int y = 0; y < yDim; ++y)
            for (int x = 0; x < xDim; ++x)
                cp.values[y][x] = values[y][x];
        return (cp);
    }

    static public SimpleMatrix multiply(SimpleMatrix m1, SimpleMatrix m2) {
        if (m1.xDim != m2.xDim) {
            throw (new IllegalArgumentException("Matrixes cannot be multiplied, dimension mismatch"));
        }

        // vanilla!
        SimpleMatrix res = new SimpleMatrix(m1.yDim, m2.xDim);
        for (int y = 0; y < m1.yDim; ++y) {
            for (int x = 0; x < m2.yDim; ++x) {
                for (int k = 0; k < m2.yDim; ++k)
                    res.values[y][x] += m1.values[y][k] * m2.values[k][x];
            }
        }
        return (res);
    }

    // 矩阵的点乘
    public double dot(SimpleMatrix m) {
        if (yDim != m.yDim || xDim != 1 || m.xDim != 1) {
            throw (new IllegalArgumentException("Dotproduct only possible for two equal n x 1 matrices"));
        }
        double sum = 0.0;

        for (int y = 0; y < yDim; ++y)
            sum += values[y][0] * m.values[y][0];
        return (sum);
    }

    public void negate() {
        for (int y = 0; y < yDim; ++y) {
            for (int x = 0; x < xDim; ++x) {
                values[y][x] = -values[y][x];
            }
        }
    }

    public void inverse() {
        if (xDim != yDim) throw (new IllegalArgumentException("Matrix x dimension != y dimension"));

        // Shipley-Coleman inversion, from
        // http://www.geocities.com/SiliconValley/Lab/4223/fault/ach03.html
        int dim = xDim;
        for (int k = 0; k < dim; ++k) {
            values[k][k] = -1.0 / values[k][k];
            for (int i = 0; i < dim; ++i) {
                if (i != k) values[i][k] *= values[k][k];
            }

            for (int i = 0; i < dim; ++i) {
                if (i != k) {
                    for (int j = 0; j < dim; ++j) {
                        if (j != k) values[i][j] += values[i][k] * values[k][j];
                    }
                }
            }

            for (int i = 0; i < dim; ++i) {
                if (i != k) values[k][i] *= values[k][k];
            }

        }

        for (int i = 0; i < dim; ++i) {
            for (int j = 0; j < dim; ++j)
                values[i][j] = -values[i][j];
        }
    }

    // The vector 'vec' is used both for input/output purposes. As input, it
    // contains the vector v, and after this method finishes it contains x,
    // the solution in the formula
    // this * x = v
    // This matrix might get row-swapped, too.
    // 高斯（gauss）主元素消去法
    public void solveLinear(SimpleMatrix vec) {
        if (xDim != yDim || yDim != vec.yDim) throw (new IllegalArgumentException(
                                                                                  "Matrix not quadratic or vector dimension mismatch"));

        // Gaussian Elimination Algorithm, as described by
        // "Numerical Methods - A Software Approach", R.L. Johnston

        // Forward elimination with partial pivoting
        for (int y = 0; y < (yDim - 1); ++y) {

            // Searching for the largest pivot (to get "multipliers < 1.0 to
            // minimize round-off errors")
            int yMaxIndex = y;
            double yMaxValue = Math.abs(values[y][y]);
            // 找列中最大的那个元素
            for (int py = y; py < yDim; ++py) {
                if (Math.abs(values[py][y]) > yMaxValue) {
                    yMaxValue = Math.abs(values[py][y]);
                    yMaxIndex = py;
                }
            }

            // if a larger row has been found, swap with the current one
            swapRow(y, yMaxIndex);
            vec.swapRow(y, yMaxIndex);

            // Now do the elimination left of the diagonal
            // 化成上三角阵
            for (int py = y + 1; py < yDim; ++py) {
                // always <= 1.0
                double elimMul = values[py][y] / values[y][y];

                for (int x = 0; x < xDim; ++x)
                    values[py][x] -= elimMul * values[y][x];

                // FIXME: do we really need this?
                vec.values[py][0] -= elimMul * vec.values[y][0];
            }
        }

        // Back substitution
        // 求解放入vec中
        // 从这里我们还可以看出，参数是类的对象，等于是传入类的指针
        for (int y = yDim - 1; y >= 0; --y) {
            double solY = vec.values[y][0];

            for (int x = xDim - 1; x > y; --x)
                solY -= values[y][x] * vec.values[x][0];

            vec.values[y][0] = solY / values[y][y];
        }
    }

    // Swap two rows r1, r2
    private void swapRow(int r1, int r2) {
        if (r1 == r2) return;

        for (int x = 0; x < xDim; ++x) {
            double temp = values[r1][x];
            values[r1][x] = values[r2][x];
            values[r2][x] = temp;
        }
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("( ");
        for (int y = 0; y < yDim; ++y) {
            if (y > 0) str.append("\n  ");

            for (int x = 0; x < xDim; ++x) {
                if (x > 0) str.append("  ");

                str.append(values[y][x]);
            }
        }
        str.append(" )");

        return (str.toString());
    }
}
