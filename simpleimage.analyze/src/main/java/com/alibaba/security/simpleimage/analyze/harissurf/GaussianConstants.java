package com.alibaba.security.simpleimage.analyze.harissurf;

public class GaussianConstants {
	
	/**
	 * 7 x 7 Discrete Gaussian Distribution, sigma = 2.5
	 */
	public static final double[][] Gauss25 = {
		  { 0.02546481,   0.02350698,     0.01849125,     0.01239505,     0.00708017,     0.00344629,     0.00142946 },
		  { 0.02350698,   0.02169968,     0.01706957,     0.01144208,     0.00653582,     0.00318132,     0.00131956 },
		  { 0.01849125,   0.01706957,     0.01342740,     0.00900066,     0.00514126,     0.00250252,     0.00103800 },
		  { 0.01239505,   0.01144208,     0.00900066,     0.00603332,     0.00344629,     0.00167749,     0.00069579 },
		  { 0.00708017,   0.00653582,     0.00514126,     0.00344629,     0.00196855,     0.00095820,     0.00039744 },
		  { 0.00344629,   0.00318132,     0.00250252,     0.00167749,     0.00095820,     0.00046640,     0.00019346 },
		  { 0.00142946,   0.00131956,     0.00103800,     0.00069579,     0.00039744,     0.00019346,     0.00008024 }
	};

	public static double[][] getGaussianDistribution(int sampleCount, float range, float sigma){
		double[][] distribution = new double[sampleCount][sampleCount];
		double sigmaSquared = Math.pow(sigma,2);
		double inverseTwoPiSigmaSquared = 1 / (2 * Math.PI * sigmaSquared);
		for ( int i = 0; i < sampleCount; i++ ){
			for ( int j = 0; j < sampleCount; j++ ){
				double x = (range / (sampleCount-1)) * i;
				double y = (range / (sampleCount-1)) * j;
				double power = Math.pow(x,2)/(2*sigmaSquared) + Math.pow(y,2)/(2*sigmaSquared);
				distribution[i][j] = inverseTwoPiSigmaSquared * Math.pow(Math.E,-1*power);
			}
		}
		return distribution;
	}
	
	public static void main(String args[]){
		double[][] dist = getGaussianDistribution(7,5.5F,2.5F);
		for ( double[] row : dist ){
			for ( double value : row ){
				System.out.format("%.14f,",value);
			}
			System.out.println("");
		}
	}
}
