package com.hoglet.ulamangling;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.imageio.ImageIO;

public class Process {
	

	public Process() {
	}
	
	   private static int[][] convertTo2DUsingGetRGB(BufferedImage image) {
		      int width = image.getWidth();
		      int height = image.getHeight();
		      int[][] result = new int[height][width];

		      for (int row = 0; row < height; row++) {
		         for (int col = 0; col < width; col++) {
		            result[row][col] = image.getRGB(col, row);
		         }
		      }

		      return result;
		   }

		   private static int[][] convertTo2DWithoutUsingGetRGB(BufferedImage image) {

		      final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		      final int width = image.getWidth();
		      final int height = image.getHeight();
		      final boolean hasAlphaChannel = image.getAlphaRaster() != null;

		      int[][] result = new int[height][width];
		      if (hasAlphaChannel) {
		         final int pixelLength = 4;
		         for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
		            int argb = 0;
		            argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
		            argb += ((int) pixels[pixel + 1] & 0xff); // blue
		            argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
		            argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
		            result[row][col] = argb;
		            col++;
		            if (col == width) {
		               col = 0;
		               row++;
		            }
		         }
		      } else {
		         final int pixelLength = 3;
		         for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
		            int argb = 0;
		            argb += -16777216; // 255 alpha
		            argb += ((int) pixels[pixel] & 0xff); // blue
		            argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
		            argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
		            result[row][col] = argb;
		            col++;
		            if (col == width) {
		               col = 0;
		               row++;
		            }
		         }
		      }

		      return result;
		   }
		   
	
	public void convert(File srcFile, File dstFile) throws IOException {
		
		System.out.println("# Reading image");
		BufferedImage image = ImageIO.read(srcFile);
		int w = image.getWidth();
		int h = image.getHeight();
		System.out.println("# Image has " + w + " x " + h + " pixels; total = " + w * h);
		
		
		System.out.println("# Converting image");
		int[][] pixels = convertTo2DWithoutUsingGetRGB(image);
		
		System.out.println("# Processing image");
		SortedMap<Integer, Integer> histo = new TreeMap<Integer, Integer>();

		int[] xTotals = new int[w];
		int[] yTotals = new int[h];
		gridHistogram(pixels, xTotals, yTotals);
		
		
		System.out.println("# Histo has " + histo.size() + " colours");
		
//		for (int i = 0; i < 360; i++) {
//			Integer count = histo.get(i);
//			if (count == null) {
//				count = 0;
//			}
//			System.out.println(i + "\t" + count);
//		}		
//		System.out.println(" \" x data");
		
		int cellSize = 39;
		int xOffset = 27;
		int xSampleOffset = 27;
		
		double xInterval = 13.82 * 72.0 / 25.4;
		int yOffset = 14;
		int ySampleOffset = 14;
		double yInterval = 13.89 * 72.0 / 25.4;
		
		int gridSize = 10; // graph only
		


		int numCells = 4;
		
//		double[] xReference = createRef(xTotals, cellSize, xOffset, xInterval, numCells, numCells);
//		double[] yReference = createRef(yTotals, cellSize, yOffset, yInterval, numCells, numCells);

		double[] xReference = createRef(xTotals, cellSize, xSampleOffset, xInterval, 1, numCells);
		double[] yReference = createRef(yTotals, cellSize, ySampleOffset, yInterval, 1, numCells);

		xReference = new Stats(xReference).normalize();
		yReference = new Stats(yReference).normalize();
		
		double[] xNormalized = new Stats(xTotals).normalize();
		double[] yNormalized = new Stats(yTotals).normalize();
		
		double[] xCorrelation = Stats.bruteForceCorrelation(xNormalized, xReference);
		double[] yCorrelation = Stats.bruteForceCorrelation(yNormalized, yReference);

		Set<Integer> xGrid = makeGrid(w, xOffset,  cellSize, xCorrelation, cellSize / 4);		
		Set<Integer> yGrid = makeGrid(h, yOffset,  cellSize, yCorrelation, cellSize / 3);
		

		dumpXGraph("x reference", xReference);
		dumpXGraph("x normalized", xNormalized);
		dumpXGraph("x correlation", xCorrelation);
		dumpXGraph("x grid", xGrid, w, gridSize);

//
//		dumpXGraph("y reference", yReference);
//		dumpXGraph("y normalized", yNormalized);
//		dumpXGraph("y correlation", yCorrelation);
//		dumpXGraph("y grid", yGrid, h, gridSize);

		// Overlay grid on image
		for (int x : xGrid) {
			for (int y = 0; y < h; y++) {
				image.setRGB(x, y, 0xffff0000);
			}
		}
		
		for (int y : yGrid) {
			for (int x = 0; x < w; x++) {
				image.setRGB(x, y, 0xffff0000);
			}
		}
		
		ImageIO.write(image,  "png", dstFile);

		
	}
		
	private double[] createRef(int[] totals, int cellSize, int offset, double interval, int numSampleCells, int numRefCells) {
		double[] sample = new double[cellSize];
		Arrays.fill(sample, 0);
		for (int i = 0; i < numSampleCells; i++) {
			for (int j = 0; j < cellSize; j++) {
				sample[j] += totals[offset + (int) (interval * i + 0.5) + j];
			}
		}
		double[] reference = new double[(int)(interval * numRefCells + 0.5)];
		Arrays.fill(reference, 0);
		for (int i = 0; i < numRefCells; i++) {
			for (int j = 0; j < cellSize; j++) {
				reference[(int) (interval * i + 0.5) + j] = sample[j];
			}
		}
		return reference;
	}
	

	private Set<Integer> makeGrid(int n, int offset, int cellsize, double[] correlation, int search) {
		Set<Integer> grid = new TreeSet<Integer>();
		int i = offset;
		while (i < n) {
			double best = Double.MIN_VALUE;
			int bestj = 0;			
			for (int j = -search; j <= search; j++) {
				if (i + j >= 0 && i + j < n && correlation[i + j] > best) {
					best = correlation[i + j];
					bestj = j;
				}
			}
			i += bestj;
			grid.add(i);
			i += cellsize;
		}
		
		return grid;
	}

	

	private Set<Integer> makeGrid(int n, int offset, double interval) {
		Set<Integer> grid = new TreeSet<Integer>();
		int index = 0;
		for (int i = 0; i < n; i++) {
			if (i >= offset + (int) (interval * index)) {
				grid.add(i);
				index++;
			}
		}
		return grid;
	}

	private Set<Integer> optimizeGrid1(Set<Integer> grid, double[] correlation) {
		int n = correlation.length;
		Set<Integer> newGrid = new TreeSet<Integer>();
		for (Integer i : grid) {
			if (correlation[i] > 0) {
				while (i < n - 1 && correlation[i + 1] > correlation[i]) {
					i++;
				}
				while (i > 0 && correlation[i - 1] > correlation[i]) {
					i--;
				}
			}
			newGrid.add(i);
		}
		return newGrid;
	}

	private Set<Integer> optimizeGrid2(Set<Integer> grid, double[] correlation, int search) {
		int n = correlation.length;
		Set<Integer> newGrid = new TreeSet<Integer>();
		for (Integer i : grid) {
			double best = Double.MIN_VALUE;
			int bestj = 0;
			for (int j = -search; j <= search; j++) {
				if (i + j >= 0 && i + j < n && correlation[i + j] > best) {
					best = correlation[i + j];
					bestj = j;
				}
			}
			newGrid.add(i + bestj);
		}
		return newGrid;
	}
	
	private void dumpXGraph(String title, Set<Integer> grid, int n, int val) {
		System.out.println(" \" " + title);
		for (int i = 0; i < n; i++) {
			System.out.println(i + "\t" + (grid.contains(i) ? val : 0));
		}
		System.out.println("\n");	
	}
	
	private void dumpXGraph(String title, double[] a) {
		System.out.println(" \" " + title);
		for (int i = 0; i < a.length; i++) {
			System.out.println(i + "\t" + a[i]);
		}
		System.out.println("\n");	
	}
	
	private void dumpXGraph(String title, int[] a) {
		System.out.println(" \" " + title);
		for (int i = 0; i < a.length; i++) {
			System.out.println(i + "\t" + a[i]);
		}
		System.out.println("\n");	
	}
	
	private void gridHistogram(int[][] pixels, int[]xTotals, int[]yTotals) {
		Arrays.fill(xTotals, 0);
		Arrays.fill(yTotals, 0);
		int h = yTotals.length;
		int w = xTotals.length;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int rgb = pixels[y][x];
				// metalization is 0xff0000ff
				// background is 0xffffffff
				int val = (rgb & 0xff0000) < 128 ? 1 : 0;
				xTotals[x] += val;
				yTotals[y] += val;
			}
		}
	}
	
	private void hueHistogram(int[][] pixels, Map<Integer, Integer> histo) {
		SortedMap<Integer, Integer> rgbToHue = new TreeMap<Integer, Integer>();
		for (int y = 0; y < pixels.length; y++) {
			for (int x = 0; x < pixels[y].length; x++) {
				int rgb = pixels[y][x];
				Integer hue = rgbToHue.get(rgb);
				if (hue == null) {
					int r = (rgb & 0xff0000) >> 16;
					int g = (rgb & 0x00ff00) >> 8;
					int b = (rgb & 0x0000ff);
					float[] hsv = Color.RGBtoHSB(r, g, b, null);
					hue = (int) (hsv[0] * 360.0);
					rgbToHue.put(rgb, hue);
				}
				Integer count = histo.get(hue);
				if (count == null) {
					count = 1;
				} else {
					count = count + 1;
				}
				histo.put(hue, count);
			}
		}
	}
	
	
	
	public static final void main(String[] args) {
		try {
			if (args.length != 2 ) {
				System.err.println("usage: java -jar ulamangling.jar <Source PNG> <Dst PNG> ");
				System.exit(1);
			}
			File srcFile = new File(args[0]);
			if (!srcFile.exists()) {
				System.err.println("Source File: " + srcFile + " does not exist");
				System.exit(1);
			}
			if (!srcFile.isFile()) {
				System.err.println("Source File: " + srcFile + " is not a file");
				System.exit(1);
			}
			
			File dstFile = new File(args[1]);
			
			Process c = new Process();
			c.convert(srcFile, dstFile);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
