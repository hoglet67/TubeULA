package com.hoglet.ulamangling;

import java.util.Arrays;

public class Stats {

	private double[] a;
	private double mean;
	private double standardDeviation;

	public Stats(int a[]) {
		this(Stats.toDouble(a));
	}

	public Stats(double a[]) {
		this.a = a;
		mean = 0;
		for (int i = 0; i < a.length; i++) {
			mean += a[i];
		}
		mean /= a.length;
		standardDeviation = 0;
		for (int i = 0; i < a.length; i++) {
			standardDeviation += (a[i] - mean) * (a[i] - mean);
		}
		standardDeviation /= a.length;
		standardDeviation = Math.sqrt(standardDeviation);
		
		System.out.println("# Mean = " + mean);
		System.out.println("# Standard Deviation = " + standardDeviation);
	}


	public double getMean() {
		return mean;
	}

	public double getStandardDeviation() {
		return standardDeviation;
	}

	
	public double[] normalize() {
		double d[] = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			d[i] = (a[i] - mean) / standardDeviation;
		}
		return d;
	}

	
	private static double[] toDouble(int[] a) {
		double d[] = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			d[i] = (double) a[i];
		}
		return d;
	}
	
    public static double[] bruteForceCorrelation(double [] x, double[] y) {
		double ac[] = new double[x.length];
        Arrays.fill(ac, 0);
        int n = x.length;
        int m = y.length;
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < m; i++) {
                ac[j] += y[i] * x[(j + i) % n];
            }
        }
        return ac;
    }
    
    public static double[] bruteForceCorrelationCentered(double [] x, double[] y) {
		double ac[] = new double[x.length];
        Arrays.fill(ac, 0);
        int n = x.length;
        int m = y.length;
        int offset = m / 2; // m odd (e.g. 9, so 4 would be the center)
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < m; i++) {
            	int xi = i + j - offset;
            	if (xi >= 0 && xi < n) {
            		ac[j] += x[xi] * y[i];
            	}
            }
        }
        return ac;
    }
}
