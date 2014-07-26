package com.hoglet.ulamangling;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import com.hoglet.ulamangling.Pin.PinType;

public class Process {

	// Number of cells sampled as part of creating the correlation reference
	public static final int NUM_SAMPLE_CELLS = 1;
	
	// Number of cells added added to the correlation reference
	public static final int NUM_REF_CELLS = 4;
	
	// Nominal cell size in pixels
	public static final int CELL_SIZE = 39;
	
	// Exact cell width in pixels (best input estimate over the image)
	public static final double X_INTERVAL = 13.82 * 72.0 / 25.4;
	
	// Exact cell height in pixels (best input estimate over the image)
	public static final double Y_INTERVAL = 13.89 * 72.0 / 25.4;
	
	// Threshold used when searching for the next grid line in pixels
	public static final int SEARCH_THRESH = 5;
	
	// Threshold used when determining when determine connectivity to an adjacent cell
	public static final int CONNECT_THRESH = 4;

	// The X,Y coordinates of the top,left of the grid	
	public static Map<String, XY> startOffsets = new HashMap<String, XY>();
		
	// The X,Y coordinates of the top,left of the ULA cell grid	
	public static Map<String, XY> blockOffsets = new HashMap<String, XY>();

	// The X,Y coordinates of the top,left of the cell to sample for the reference
	public static Map<String, XY> sampleOffsets = new HashMap<String, XY>();

	// These values need to be manually extracted for each image
	static {
		startOffsets.put("00_00", new XY(68, 41));
		blockOffsets.put("00_00", new XY(384, 352));
		sampleOffsets.put("00_00", new XY(384, 392));
	}
			
	public static Pin[] cellPins = new Pin[] {
		new Pin(4, 1, PinType.NORMAL),
		new Pin(6, 1, PinType.NORMAL),
		new Pin(8, 1, PinType.NORMAL),
		new Pin(12, 1, PinType.NORMAL),
		new Pin(10, 3, PinType.NORMAL),
		new Pin(12, 3, PinType.NORMAL),
		new Pin(1, 4, PinType.NORMAL),
		new Pin(4, 4, PinType.NORMAL),
		new Pin(5, 4, PinType.NORMAL),
		new Pin(7, 4, PinType.NORMAL),
		new Pin(8, 4, PinType.NORMAL),
		new Pin(1, 6, PinType.NORMAL),
		new Pin(4, 7, PinType.NORMAL),
		new Pin(7, 7, PinType.CS_EMITTER_L),
		new Pin(8, 7, PinType.CS_EMITTER_R),
		new Pin(12, 7, PinType.NORMAL),
		new Pin(1, 8, PinType.NORMAL),
		new Pin(4, 8, PinType.NORMAL),
		new Pin(12, 8, PinType.NORMAL),
		new Pin(7, 9, PinType.NORMAL),
		new Pin(8, 9, PinType.CS_BASE),
		new Pin(9, 9, PinType.NORMAL),
		new Pin(12, 9, PinType.NORMAL),
		new Pin(1, 10, PinType.NORMAL),
		new Pin(4, 10, PinType.NORMAL),
		new Pin(7, 10, PinType.CS_COLLECTOR),
		new Pin(4, 11, PinType.NORMAL),
		new Pin(7, 11, PinType.NORMAL),
		new Pin(8, 11, PinType.NORMAL),
		new Pin(9, 11, PinType.NORMAL),
		new Pin(11, 12, PinType.NORMAL),
		new Pin(4, 13, PinType.NORMAL),
		new Pin(6, 13, PinType.NORMAL),
		new Pin(8, 13, PinType.NORMAL),
		new Pin(12, 13, PinType.NORMAL)
	};
	
	int blockXOffset;
	int blockYOffset;

	public Process() {
	}

	@SuppressWarnings("unused")
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

	public void convert(String name, File srcFile, File dstFile) throws IOException {

		System.out.println("# Reading image " + srcFile);
		BufferedImage image = ImageIO.read(srcFile);
		int w = image.getWidth();
		int h = image.getHeight();
		System.out.println("# Image has " + w + " x " + h + " pixels; total = " + w * h);

		System.out.println("# Converting image");
		int[][] pixels = convertTo2DWithoutUsingGetRGB(image);

		System.out.println("# Processing image");

		int[] xTotals = new int[w];
		int[] yTotals = new int[h];
		gridHistogram(pixels, xTotals, yTotals);

		int gridSize = 10; // graph only
		
		XY blockOffset = blockOffsets.get(name);		
		XY sampleOffset = sampleOffsets.get(name);		
		XY startOffset = startOffsets.get(name);

		double[] xReference = createRef(xTotals, CELL_SIZE, sampleOffset.getX(), X_INTERVAL, NUM_SAMPLE_CELLS, NUM_REF_CELLS);
		double[] yReference = createRef(yTotals, CELL_SIZE, sampleOffset.getY(), Y_INTERVAL, NUM_SAMPLE_CELLS, NUM_REF_CELLS);

		xReference = new Stats(xReference).normalize();
		yReference = new Stats(yReference).normalize();

		double[] xNormalized = new Stats(xTotals).normalize();
		double[] yNormalized = new Stats(yTotals).normalize();

		double[] xCorrelation = Stats.bruteForceCorrelation(xNormalized, xReference);
		double[] yCorrelation = Stats.bruteForceCorrelation(yNormalized, yReference);

		List<Integer> xGrid = makeGrid(w, startOffset.getX(), CELL_SIZE, xCorrelation, SEARCH_THRESH);
		List<Integer> yGrid = makeGrid(h, startOffset.getY(), CELL_SIZE, yCorrelation, SEARCH_THRESH);
		
//		dumpXGraph("x reference", xReference);
//		dumpXGraph("x normalized", xNormalized);
//		dumpXGraph("x correlation", xCorrelation);
//		dumpXGraph("x grid", xGrid, w, gridSize);
		
		int blockXOffset = getBlockOffset(xGrid, blockOffset.getX(), SEARCH_THRESH);
		int blockYOffset = getBlockOffset(yGrid, blockOffset.getY(), SEARCH_THRESH);

		 dumpXGraph("y reference", yReference);
		 dumpXGraph("y normalized", yNormalized);
		 dumpXGraph("y correlation", yCorrelation);
		 dumpXGraph("y grid", yGrid, h, gridSize);

		System.out.println("# Annotating PNG");
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

		
		for (int xi = 0; xi < xGrid.size() - 1; xi++) {
			for (int yi = 0; yi < yGrid.size() - 1; yi++) {

				int x1 = xGrid.get(xi);
				int y1 = yGrid.get(yi);
				int x2 = xGrid.get(xi + 1) - 1;
				int y2 = yGrid.get(yi + 1) - 1;

				int top = 0;
				int left = 0;
				int right = 0;
				int bottom = 0;

				for (int i = 0; i < CELL_SIZE; i++) {
					if ((pixels[y1][x1 + i] & 0xff0000) < 128) {
						top++;
					}
					if ((pixels[y2][x1 + i] & 0xff0000) < 128) {
						bottom++;
					}
					if ((pixels[y1 + i][x1] & 0xff0000) < 128) {
						left++;
					}
					if ((pixels[y1 + i][x2] & 0xff0000) < 128) {
						right++;
					}
				}

				int rgb = Pin.YELLOW;
				int z = 8;

				if (top > CONNECT_THRESH) {
					Pin.rectangle(image, x1 + (CELL_SIZE - z) / 2, y1, z, (CELL_SIZE + z) / 2, rgb);
				}
				if (bottom > CONNECT_THRESH) {
					Pin.rectangle(image, x1 + (CELL_SIZE - z) / 2, y2 - (CELL_SIZE + z) / 2, z, (CELL_SIZE + z) / 2, rgb);
				}
				if (left > CONNECT_THRESH) {
					Pin.rectangle(image, x1, y1 + (CELL_SIZE - z) / 2, (CELL_SIZE + z) / 2, z, rgb);
				}
				if (right > CONNECT_THRESH) {
					Pin.rectangle(image, x2 - (CELL_SIZE + z) / 2, y1 + (CELL_SIZE - z) / 2, (CELL_SIZE + z) / 2, z, rgb);
				}
			}
		}

		// Overlay pins
		addPins(image, blockXOffset, blockYOffset, xGrid, yGrid);

		System.out.println("# Writing PNG");
		ImageIO.write(image, "png", dstFile);

	}
	
	private int getBlockOffset(List<Integer> grid, int val, int within) {
		int offset = 0;
		for (int x : grid) {
			if (Math.abs(x - val) < 5) {
				break;
			}
			offset++;
		}
		System.out.println("# Block offset was " + offset);
		return offset;
	}

	private void addPins(BufferedImage image, int blockXOffset, int blockYOffset, List<Integer> xGrid, List<Integer> yGrid) {
		for (int cellXi = 0; cellXi < 10; cellXi++) {
			for (int cellYi = 0; cellYi < 11; cellYi++) {
				for (Pin pin : cellPins) {
					int cellX = blockXOffset + cellXi * 15;
					int cellY = blockYOffset + cellYi * 15;
					pin.plot(image, xGrid, yGrid, cellX, cellY, CELL_SIZE, Pin.GREEN);
				}
			}
		}		
	}


	private double[] createRef(int[] totals, int cellSize, int offset, double interval, int numSampleCells, int numRefCells) {
		double[] sample = new double[cellSize];
		Arrays.fill(sample, 0);
		for (int i = 0; i < numSampleCells; i++) {
			for (int j = 0; j < cellSize; j++) {
				sample[j] += totals[offset + (int) (interval * i + 0.5) + j];
			}
		}
		double[] reference = new double[(int) (interval * numRefCells + 0.5)];
		Arrays.fill(reference, 0);
		for (int i = 0; i < numRefCells; i++) {
			for (int j = 0; j < cellSize; j++) {
				reference[(int) (interval * i + 0.5) + j] = sample[j];
			}
		}
		return reference;
	}

	private List<Integer> makeGrid(int n, int offset, int cellsize, double[] correlation, int search) {
		List<Integer> grid = new ArrayList<Integer>();
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

	private void dumpXGraph(String title, List<Integer> grid, int n, int val) {
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

	@SuppressWarnings("unused")
	private void dumpXGraph(String title, int[] a) {
		System.out.println(" \" " + title);
		for (int i = 0; i < a.length; i++) {
			System.out.println(i + "\t" + a[i]);
		}
		System.out.println("\n");
	}

	private void gridHistogram(int[][] pixels, int[] xTotals, int[] yTotals) {
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

	@SuppressWarnings("unused")
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
			if (args.length != 2) {
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
			
			String name = srcFile.getName();
			name = name.substring(name.indexOf('_') + 1, name.lastIndexOf('.'));
			System.out.println("# name = " + name);

			c.convert(name, srcFile, dstFile);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
