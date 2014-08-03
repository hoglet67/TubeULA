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

	// Nominal cell size in pixels
	public static final int CELL_SIZE = 40;

	// Locally optimize the grid over +- this many cells
	public static final int GRID_DELTA = 8;
	
	// Whether to re-center a perfectly sized cell
	public static final boolean GRID_RECENTER = true;

	// match over +- this many pixels
	public static final int MATCH_DELTA = 0;

	// Threshold used when searching for the next grid line in pixels
	public static final int SEARCH_THRESH = 3;

	// The X,Y coordinates of the top,left of the ULA cell grid
	public static Map<String, XY> startCells = new HashMap<String, XY>();

	// The X,Y coordinates of the top,left of the ULA cell grid
	public static Map<String, XY> blockCells = new HashMap<String, XY>();

	// The X,Y coordinates of the top,left of the ULA cell grid
	public static Map<String, XY> endCells = new HashMap<String, XY>();

	// Attempt 4
	// These values need to be manually extracted for each image
	static {
		startCells.put("00", new XY(0, 0));
		blockCells.put("00", new XY(8, 8));
		endCells.put("00", new XY(162, 177));
	}

	public static Pin[] cellPins = new Pin[] { new Pin(4, 1, PinType.NORMAL), new Pin(6, 1, PinType.NORMAL),
			new Pin(8, 1, PinType.NORMAL), new Pin(12, 1, PinType.NORMAL), new Pin(10, 3, PinType.NORMAL),
			new Pin(12, 3, PinType.NORMAL), new Pin(1, 4, PinType.NORMAL), new Pin(4, 4, PinType.NORMAL),
			new Pin(5, 4, PinType.NORMAL), new Pin(7, 4, PinType.NORMAL), new Pin(8, 4, PinType.NORMAL),
			new Pin(1, 6, PinType.NORMAL), new Pin(4, 7, PinType.NORMAL), new Pin(7, 7, PinType.CS_EMITTER_1),
			new Pin(8, 7, PinType.CS_EMITTER_2), new Pin(9, 7, PinType.CS_EMITTER_3), new Pin(7, 8, PinType.CS_EMITTER_4),
			new Pin(8, 8, PinType.CS_EMITTER_5), new Pin(9, 8, PinType.CS_EMITTER_6), new Pin(12, 7, PinType.NORMAL),
			new Pin(1, 8, PinType.NORMAL), new Pin(4, 8, PinType.NORMAL), new Pin(12, 8, PinType.NORMAL),
			new Pin(7, 9, PinType.CS_BASE_1), new Pin(8, 9, PinType.CS_BASE_2), new Pin(9, 9, PinType.CS_BASE_3),
			new Pin(12, 9, PinType.NORMAL), new Pin(1, 10, PinType.NORMAL), new Pin(4, 10, PinType.NORMAL),
			new Pin(7, 10, PinType.CS_COLLECTOR_1), new Pin(8, 10, PinType.CS_COLLECTOR_2), new Pin(9, 10, PinType.CS_COLLECTOR_3),
			new Pin(4, 11, PinType.NORMAL), new Pin(7, 11, PinType.CS_GND_1), new Pin(8, 11, PinType.CS_GND_2),
			new Pin(9, 11, PinType.CS_GND_3), new Pin(11, 12, PinType.NORMAL), new Pin(4, 13, PinType.NORMAL),
			new Pin(6, 13, PinType.NORMAL), new Pin(8, 13, PinType.NORMAL), new Pin(12, 13, PinType.NORMAL) };

	
	public static Pin[] horPeripheralPins = new Pin[] {
		new Pin(1, 2, PinType.NORMAL),

		new Pin(2, 2, PinType.NORMAL),
		new Pin(3, 2, PinType.NORMAL),
		new Pin(4, 2, PinType.NORMAL),
		new Pin(5, 2, PinType.NORMAL),
		new Pin(7, 2, PinType.HPAD_GND_1),
		new Pin(8, 2, PinType.HPAD_GND_2),
		new Pin(9, 2, PinType.HPAD_GND_2),
		new Pin(10, 2, PinType.HPAD_GND_2),
		new Pin(11, 2, PinType.HPAD_GND_2),
		new Pin(12, 2, PinType.HPAD_GND_3),
		new Pin(14, 2, PinType.NORMAL),
		new Pin(15, 2, PinType.NORMAL),
		new Pin(16, 2, PinType.NORMAL),
		new Pin(17, 2, PinType.NORMAL),


		new Pin(2, 3, PinType.NORMAL),
		new Pin(3, 3, PinType.NORMAL),
		new Pin(4, 3, PinType.NORMAL),
		new Pin(5, 3, PinType.NORMAL),
		new Pin(7, 3, PinType.HPAD_GND_4),
		new Pin(8, 3, PinType.HPAD_GND_5),
		new Pin(9, 3, PinType.HPAD_GND_5),
		new Pin(10, 3, PinType.HPAD_GND_5),
		new Pin(11, 3, PinType.HPAD_GND_5),
		new Pin(12, 3, PinType.HPAD_GND_6),
		new Pin(14, 3, PinType.NORMAL),
		new Pin(15, 3, PinType.NORMAL),
		new Pin(16, 3, PinType.NORMAL),
		new Pin(17, 3, PinType.NORMAL),
		
		new Pin(18, 2, PinType.NORMAL)

	};
	
	public static Pin[] verPeripheralPins = new Pin[] {
		new Pin(1, 2, PinType.NORMAL),

		new Pin(2, 2, PinType.NORMAL),
		new Pin(3, 2, PinType.NORMAL),
		new Pin(4, 2, PinType.NORMAL),
		new Pin(5, 2, PinType.NORMAL),
		new Pin(7, 2, PinType.VPAD_GND_1),
		new Pin(8, 2, PinType.VPAD_GND_2),
		new Pin(9, 2, PinType.VPAD_GND_2),
		new Pin(10, 2, PinType.VPAD_GND_2),
		new Pin(11, 2, PinType.VPAD_GND_2),
		new Pin(12, 2, PinType.VPAD_GND_3),
		new Pin(14, 2, PinType.NORMAL),
		new Pin(15, 2, PinType.NORMAL),
		new Pin(16, 2, PinType.NORMAL),
		new Pin(17, 2, PinType.NORMAL),


		new Pin(2, 3, PinType.NORMAL),
		new Pin(3, 3, PinType.NORMAL),
		new Pin(4, 3, PinType.NORMAL),
		new Pin(5, 3, PinType.NORMAL),
		new Pin(7, 3, PinType.VPAD_GND_4),
		new Pin(8, 3, PinType.VPAD_GND_5),
		new Pin(9, 3, PinType.VPAD_GND_5),
		new Pin(10, 3, PinType.VPAD_GND_5),
		new Pin(11, 3, PinType.VPAD_GND_5),
		new Pin(12, 3, PinType.VPAD_GND_6),
		new Pin(14, 3, PinType.NORMAL),
		new Pin(15, 3, PinType.NORMAL),
		new Pin(16, 3, PinType.NORMAL),
		new Pin(17, 3, PinType.NORMAL),
		
		new Pin(18, 2, PinType.NORMAL)

	};
	
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

		CellMatcher matcher = new CellMatcher(CELL_SIZE + 1, 4, 5, 15, MATCH_DELTA);

		System.out.println("# Reading image " + srcFile);
		BufferedImage image = ImageIO.read(srcFile);
		int w = image.getWidth();
		int h = image.getHeight();
		System.out.println("# Image has " + w + " x " + h + " pixels; total = " + w * h);

		XY blockCell = blockCells.get(name);
		int blockCellX = blockCell.getX();
		int blockCellY = blockCell.getY();

		XY startCell = startCells.get(name);
		XY endCell = endCells.get(name);

		System.out.println("# Converting image");
		int[][] pixels = convertTo2DWithoutUsingGetRGB(image);

		System.out.println("# Gridding image");
		double[] window = new double[] { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1 };
		double[] reference = new Stats(window).normalize();

		Cell[][] cells = initializeGrid(w, h, pixels, reference);
		optimizeGrid(cells, pixels, CELL_SIZE, SEARCH_THRESH, reference, GRID_DELTA, GRID_RECENTER);

		System.out.println("# Matching image");

		// dumpXGraph("x reference", xReference);
		// dumpXGraph("x normalized", xNormalized);
		// dumpXGraph("x correlation", xCorrelation);
		// dumpXGraph("x grid", xGrid, w, gridSize);
		// dumpXGraph("y reference", yReference);
		// dumpXGraph("y normalized", yNormalized);
		// dumpXGraph("y correlation", yCorrelation);
		// dumpXGraph("y grid", yGrid, h, gridSize);

		// Overlay pins onto logical cell map
		addPins(cells, blockCellX, blockCellY);

		for (int xi = startCell.getX(); xi <= endCell.getX(); xi++) {
			System.out.println((xi - startCell.getX()) + " / " + (endCell.getX() - startCell.getX()));
			for (int yi = startCell.getY(); yi <= endCell.getY(); yi++) {
				matcher.match(pixels, xi, yi, image, cells);
			}
		}

		fixKnownPatterns(cells);

		int ret;

		Cell[][] cellsOut1 = new Cell[cells.length][cells[0].length];
		ret = drcConnections(cells, cellsOut1);
		System.out.println("# Initial DRC Connections Count = " + ret);

		Cell[][] cellsOut2 = new Cell[cells.length][cells[0].length];
		ret = drcDangling(cellsOut1, cellsOut2);
		System.out.println("# Initial DRC Dangle Count = " + ret);

		Cell[][] cellsOut3 = new Cell[cells.length][cells[0].length];
		ret = drcBridge(cellsOut2, cellsOut3);
		System.out.println("# Initial DRC Bridge Count = " + ret);

		Cell[][] cellsOut4 = new Cell[cells.length][cells[0].length];
		ret = fixDanglingPairs(cellsOut3, cellsOut4);
		System.out.println("# Fixed Dangling Pairs corrected = " + ret);

		Cell[][] cellsOut5 = new Cell[cells.length][cells[0].length];
		ret = fixDanglingWeaklyConnected(cellsOut4, cells, cellsOut5);
		System.out.println("# Fixed Dangling Weakly Connected corrected = " + ret);

		Cell[][] cellsOut6 = new Cell[cells.length][cells[0].length];
		ret = fixDanglingPins(cellsOut5, cellsOut6);
		System.out.println("# Fixed Dangling Pins corrected = " + ret);

		Cell[][] cellsOut7 = new Cell[cells.length][cells[0].length];
		ret = fixDanglingIsolated(cellsOut6, cellsOut7);
		System.out.println("# Fixed Dangling Isolated corrected = " + ret);

		Cell[][] cellsOut8 = new Cell[cells.length][cells[0].length];
		ret = fixBridgedPairs(cellsOut7, cellsOut8);
		System.out.println("# Fixed Bridged Pairs corrected = " + ret);

		ret = drcConnections(cellsOut8, null);
		System.out.println("# Final DRC Connections Count = " + ret);

		ret = drcDangling(cellsOut8, null);
		System.out.println("# Final DRC Dangle Count = " + ret);

		ret = drcBridge(cellsOut8, null);
		System.out.println("# Final DRC Bridge Count = " + ret);

		// Results onto output image
		System.out.println("# Annotating PNG");
		annotateImage(image, cellsOut8);

		System.out.println("# Writing PNG");
		ImageIO.write(image, "png", dstFile);

	}

	private Cell[][] initializeGrid(int w, int h, int[][] pixels, double[] reference) {
		int[] xTotals = new int[w];
		int[] yTotals = new int[h];
		gridHistogram(pixels, 0, 0, w, h, xTotals, yTotals);
		double[] xNormalized = new Stats(xTotals).normalize();
		double[] yNormalized = new Stats(yTotals).normalize();
		double[] xCorrelation = Stats.bruteForceCorrelationCentered(xNormalized, reference);
		double[] yCorrelation = Stats.bruteForceCorrelationCentered(yNormalized, reference);
		List<Integer> xGrid = makeGrid(0, w, CELL_SIZE, xCorrelation, SEARCH_THRESH);
		List<Integer> yGrid = makeGrid(0, h, CELL_SIZE, yCorrelation, SEARCH_THRESH);
		Cell[][] cells = initCells(xGrid.size() - 1, yGrid.size() - 1);
		for (int yi = 0; yi < cells.length; yi++) {
			for (int xi = 0; xi < cells[yi].length; xi++) {
				cells[yi][xi].setX1(xGrid.get(xi));
				cells[yi][xi].setX2(xGrid.get(xi + 1));
				cells[yi][xi].setY1(yGrid.get(yi));
				cells[yi][xi].setY2(yGrid.get(yi + 1));
			}
		}
		return cells;
	}

	private Cell[][] initCells(int w, int h) {
		Cell[][] cells = new Cell[h][w];
		for (int xi = 0; xi < w; xi++) {
			for (int yi = 0; yi < h; yi++) {
				cells[yi][xi] = new Cell();
			}
		}
		return cells;
	}
	
	// Optimize the grid by correlating locally over a smaller window (e.g. 16x16 cells)
	private void optimizeGrid(Cell[][] cells, int[][] pixels, int cellsize, int search, double[] reference, int delta, boolean recenter) {
		int heightInCells = cells.length;
		int widthInCells = cells[0].length;
		int heightInPixels = pixels.length;
		int widthInPixels = pixels[0].length;
		
		// Reduce the complexity by reducing pixels to cells in one dimension
		// xCells is <vertical cells> x <horizontal pixels>
		// yCells is <horizontal cells> x <vertical pixels> 
		int[][] xCells = new int[heightInCells][widthInPixels];
		int[][] yCells = new int[widthInCells][heightInPixels];
		for (int yi = 0; yi < heightInCells; yi++) {
			for (int xi = 0; xi < widthInCells; xi++) {
				for (int y = yi * cellsize; y < (yi  + 1) * cellsize; y++) {
					for (int x = xi * cellsize; x < (xi + 1) * cellsize; x++) {
						int rgb = pixels[y][x];
						int val = (rgb & 0xffffff) == 0x0000ff ? 1 : 0;
						xCells[yi][x] += val;
						yCells[xi][y] += val;
					}
				}
			}
		}
		
		int[] xTotals = new int[2 * delta * cellsize];
		int[] yTotals = new int[2 * delta * cellsize];

		for (int yi = 0; yi < heightInCells; yi++) {
			for (int xi = 0; xi < widthInCells; xi++) {
				
				Cell cell = cells[yi][xi];
				
				// Top Left corner of the window to correlate over, ideally centred on the cell
				int cellx1 = findStart(xi, delta, 0, widthInCells);
				int celly1 = findStart(yi, delta, 0, heightInCells);
				int pixelx1 = cellx1 * cellsize;
				int pixely1 = celly1 * cellsize;
				
				// Bottom Right corner of the window to correlate over, ideally centred on the cell
				int cellx2 = cellx1 + 2 * delta;
				int celly2 = celly1 + 2 * delta;
								
				totalCells(xCells, celly1, celly2, pixelx1, xTotals);
				totalCells(yCells, cellx1, cellx2, pixely1, yTotals);

				double[] xNormalized = new Stats(xTotals).normalize();
				double[] yNormalized = new Stats(yTotals).normalize();
				
				// Correlate these with the reference
				double[] xCorrelation = Stats.bruteForceCorrelationCentered(xNormalized, reference);
				double[] yCorrelation = Stats.bruteForceCorrelationCentered(yNormalized, reference);
							
				int x1 = cell.getX1();
				int x2 = cell.getX2();
				int y1 = cell.getY1();
				int y2 = cell.getY2();
				
				// Search for a local maximum within += search pixels
				x1 = pixelx1 + searchForOptimum(x1 - pixelx1, xCorrelation, search);
				x2 = pixelx1 + searchForOptimum(x2 - pixelx1, xCorrelation, search);
				y1 = pixely1 + searchForOptimum(y1 - pixely1, yCorrelation, search);
				y2 = pixely1 + searchForOptimum(y2 - pixely1, yCorrelation, search);

				// Place a perfect cell on the centre
				if (recenter) {
					x1 = (x1 + x2 - cellsize) / 2;
					y1 = (y1 + y2 - cellsize) / 2;
					x2 = x1 + cellsize;
					y2 = y1 + cellsize;
				}
				
				// Sanity check these
				if (x1 < 0) {
					//System.out.println("x1 out of range (0 .." + (widthInPixels - 1) + ":" + x1);
					x1 = 0;
				}
				if (y1 < 0) {
					//System.out.println("y1 out of range (0 .." + (heightInPixels - 1) + ":" + y1);
					y1 = 0;
				}
				if (x2 >= widthInPixels - 1) {
					//System.out.println("x2 out of range (0 .." + (widthInPixels - 1) + ":" + x2);
					x2 = widthInPixels - 1;
				}
				if (y2 > heightInPixels - 1) {
					//System.out.println("y2 out of range (0 .." + (heightInPixels - 1) + ":" + y2);
					y2 = heightInPixels - 1;
				}

				// Update the cell
				cell.setX1(x1);
				cell.setX2(x2);
				cell.setY1(y1);
				cell.setY2(y2);
				
			}
		}
	}
	
	
	private void totalCells(int[][] cells, int startCell, int endCell, int startPixel, int[] totals) {
		for (int i = 0; i < totals.length; i++) {
			int total = 0;
			for (int cell = startCell; cell < endCell; cell++) {
				total += cells[cell][startPixel + i];
			}
			totals[i] = total;
		}
	}
	
	private int findStart(int i, int delta, int min, int max) {
		int start = i - delta;
		if (start < min) {
			start = min;
		}
		if (start + 2 * delta > max) {
			start = max - 2 * delta;
		}
		return start;
	}

	private void fixKnownPatterns(Cell[][] cells) {
		for (int yi = 0; yi < cells.length; yi++) {
			for (int xi = 0; xi < cells[yi].length; xi++) {

				Cell cell = cells[yi][xi];

				switch (cell.getType()) {
				case CS_EMITTER_1:
					cell.clearRight();
					cell.setBottom();
					break;
				case CS_EMITTER_2:
					cell.setConnections(0);
					break;
				case CS_EMITTER_3:
					cell.clearLeft();
					cell.setBottom();
					break;
				case CS_EMITTER_4:
					// Detect the case where the two emitters are joined, and
					// join it properly
					if (cells[yi][xi + 1].isLeft()) {
						cell.setRight();
					} else {
						cell.clearRight();
					}
					cell.setTop();
					break;
				case CS_EMITTER_5:
					cell.clearTop();
					cell.clearBottom();
					break;
				case CS_EMITTER_6:
					// Detect the case where the two emitters are joined, and
					// join it properly
					if (cells[yi][xi - 1].isRight()) {
						cell.setLeft();
					} else {
						cell.clearLeft();
					}
					cell.setTop();
					break;
				case CS_BASE_1:
					cell.setRight();
					break;
				case CS_BASE_2:
					cell.setRight();
					cell.setLeft();
					break;
				case CS_BASE_3:
					cell.setLeft();
					break;
				case CS_COLLECTOR_1:
					cell.setRight();
					cell.setBottom();
					break;
				case CS_COLLECTOR_2:
					cell.setRight();
					cell.setLeft();
					cell.clearBottom();
					break;
				case CS_COLLECTOR_3:
					cell.setLeft();
					cell.setBottom();
					break;
				case CS_GND_1:
					cell.setRight();
					cell.setTop();
					break;
				case CS_GND_2:
					cell.setRight();
					cell.setLeft();
					cell.clearTop();
					break;
				case CS_GND_3:
					cell.setLeft();
					cell.setTop();
					break;
				case HPAD_GND_1:
					cell.setRight();
					cell.setBottom();
					break;
				case HPAD_GND_2:
					cell.setRight();
					cell.setLeft();
					break;
				case HPAD_GND_3:
					cell.setLeft();
					cell.setBottom();
					break;
				case HPAD_GND_4:
					cell.setRight();
					cell.setTop();
					break;
				case HPAD_GND_5:
					cell.setRight();
					cell.setLeft();
					break;
				case HPAD_GND_6:
					cell.setLeft();
					cell.setTop();
					break;
				case VPAD_GND_1:
					cell.setRight();
					cell.setBottom();
					break;
				case VPAD_GND_2:
					cell.setTop();
					cell.setBottom();
					break;
				case VPAD_GND_3:
					cell.setRight();
					cell.setTop();
					break;
				case VPAD_GND_4:
					cell.setLeft();
					cell.setBottom();
					break;
				case VPAD_GND_5:
					cell.setTop();
					cell.setBottom();
					break;
				case VPAD_GND_6:
					cell.setLeft();
					cell.setTop();
					break;
				default:
				}

			}
		}
	}

	private void annotateImage(BufferedImage image, Cell[][] cells) {

		int lineColour = Pin.RED;
		int z = 8;

		for (int yi = 0; yi < cells.length; yi++) {
			for (int xi = 0; xi < cells[yi].length; xi++) {

				int x1 = cells[yi][xi].getX1();
				int x2 = cells[yi][xi].getX2();
				int y1 = cells[yi][xi].getY1();
				int y2 = cells[yi][xi].getY2();
				int w = x2 - x1;
				int h = y2 - y1;

				// System.out.println("x1=" + x1 + "; y1=" + y1 + "; x2=" + x2 + "; y2=" + y2 + "; w=" + w + "; h=" + h);
				
				// Overlay grid on image
				for (int y = 0; y < h; y++) {
					image.setRGB(x1, y1 + y, 0xffff0000);
					image.setRGB(x2, y1 + y, 0xffff0000);
				}
				for (int x = 0; x < w; x++) {
					image.setRGB(x1 + x, y1, 0xffff0000);
					image.setRGB(x1 + x, y2, 0xffff0000);
				}

				Cell cell = cells[yi][xi];

				Pin pin = cell.getPin();
				if (pin != null) {
					pin.plot(image, x1, y1, w, h, Pin.GREEN);
				}

				int connections = cell.getConnections();

				// top
				if ((connections & 1) > 0) {
					Pin.rectangle(image, x1 + (w - z) / 2 + 1, y1 + 1, z, (h + z) / 2, lineColour, false);
				}
				// right
				if ((connections & 2) > 0) {
					Pin.rectangle(image, x2 - (w + z) / 2 + 1, y1 + (h - z) / 2 + 1, (w + z) / 2, z, lineColour, false);
				}
				// bottom
				if ((connections & 4) > 0) {
					Pin.rectangle(image, x1 + (w - z) / 2 + 1, y2 - (h + z) / 2 + 1, z, (h + z) / 2, lineColour, false);
				}
				// left
				if ((connections & 8) > 0) {
					Pin.rectangle(image, x1 + 1, y1 + (h - z) / 2 + 1, (w + z) / 2, z, lineColour, false);
				}

				// // DRC - Connections
				// if (cell.isConnectionsFail()) {
				// Pin.rectangle(image, x1 + (w - z) / 2 + 1, y1 + (h - z) / 2 +
				// 1, z, z, Pin.YELLOW, false);
				// }

				// DRC - flag cells than need manual checking
				if (cell.isHighlight()) {
					Pin.rectangle(image, x1, y1, w, h, Pin.YELLOW, true);
				}

			}
		}
	}

	private void addPins(Cell[][] cells, int blockXOffset, int blockYOffset) {
		for (int cellXi = 0; cellXi < 10; cellXi++) {
			for (int cellYi = 0; cellYi < 11; cellYi++) {
				for (Pin pin : cellPins) {
					int cellX = blockXOffset + cellXi * 15;
					int cellY = blockYOffset + cellYi * 15;
					cells[cellY + pin.getY()][cellX + pin.getX()].setPin(pin);
				}
			}
		}
		// TODO Need to generalise this
		for (int x : generateSequence(9, 10)) {
			cells[175][x].setPin(new Pin(0, 0, PinType.NORMAL));
		}
		for (int y : generateSequence(9, 11)) {
			cells[y][160].setPin(new Pin(0, 0, PinType.NORMAL));
		}
		for (int i = 0; i < 5; i++) {
			addPeriperalCell(cells, horPeripheralPins, 11 + 31 * i, 0, false, false);
		}
		for (int i = 0; i < 6; i++) {
			addPeriperalCell(cells, verPeripheralPins, 0, 3 + 31 * i, true, false);
		}
		for (int i = 0; i < 6; i++) {
			cells[2][4 + 31 * i].setPin(new Pin(0, 0, PinType.NORMAL));
		}
		for (int i = 0; i < 5; i++) {
			cells[29 + 31 * i][2].setPin(new Pin(0, 0, PinType.NORMAL));
		}
	}

	private void addPeriperalCell(Cell[][] cells, Pin[] pins, int xi, int yi, boolean rotated, boolean mirrored) {
		for (Pin pin : pins) {
			int x = rotated ? pin.getY() : pin.getX();
			int y = rotated ? pin.getX() : pin.getY();
			if (mirrored) {
				x = -x;
				y = -y;
			}
			cells[yi + y][xi + x].setPin(pin);
		}
	}
	private int[] generateSequence(int offset, int n) {
		int[] sequence = new int[n * 6];
		int i = 0;
		int p = offset;
		for (int j = 0; j < n; j++) {
			sequence[i++] = p;
			p += 2;
			sequence[i++] = p;
			p += 2;
			sequence[i++] = p;
			p += 3;
			sequence[i++] = p;
			p += 2;
			sequence[i++] = p;
			p += 2;
			sequence[i++] = p;
			p += 4;
		}
		return sequence;
	}

	private List<Integer> makeGrid(int startOffset, int endOffset, int cellsize, double[] correlation, int search) {
		List<Integer> grid = new ArrayList<Integer>();
		int i = startOffset;
		while (i < endOffset) {
			i = searchForOptimum(i, correlation, search);
			grid.add(i);
			i += cellsize;
		}
		if (i - endOffset < cellsize / 2) {
			grid.add(endOffset - 1);
		}
		return grid;
	}
	
	private int searchForOptimum(int i, double[] correlation, int search) {
		int n = correlation.length;
		double best = Double.MIN_VALUE;
		int bestj = 0;
		for (int j = -search; j <= search; j++) {
			if (i + j >= 0 && i + j < n && correlation[i + j] > best) {
				best = correlation[i + j];
				bestj = j;
			}
		}
		return i + bestj;
	}

	@SuppressWarnings("unused")
	private void dumpXGraph(String title, List<Integer> grid, int n, int val) {
		System.out.println(" \" " + title);
		for (int i = 0; i < n; i++) {
			System.out.println(i + "\t" + (grid.contains(i) ? val : 0));
		}
		System.out.println("\n");
	}

	@SuppressWarnings("unused")
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

	private void gridHistogram(int[][] pixels, int x1, int y1, int x2, int y2, int[] xTotals, int[] yTotals) {
		Arrays.fill(xTotals, 0);
		Arrays.fill(yTotals, 0);
		for (int y = 0; y < y2 - y1; y++) {
			for (int x = 0; x < x2 - x1; x++) {
				int rgb = pixels[y1 + y][x1 + x];
				// black is 0xff000000 -- ignore
				// blue metalization is 0xff0000ff -- use this colour only
				// white background is 0xffffffff -- ignore
				int val = (rgb & 0xffffff) == 0x0000ff ? 1 : 0;
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
				System.err.println("usage: java -jar ulamangling.jar <Src PNG> <Dst PNG> ");
				System.exit(1);
			}
			File srcFile = new File(args[0]);
			if (!srcFile.exists()) {
				System.err.println("Src File: " + srcFile + " does not exist");
				System.exit(1);
			}
			if (!srcFile.isFile()) {
				System.err.println("Src File: " + srcFile + " is not a file");
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

	private int drcConnections(Cell[][] cellsIn, Cell[][] cellsOut) {
		int failCount = 0;

		int h = cellsIn.length;
		int w = cellsIn[0].length;

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				Cell cell = new Cell(cellsIn[y][x]);
				if (cellsOut != null) {
					cellsOut[y][x] = cell;
				}

				int connections = cellsIn[y][x].getConnections();
				// Check connectivity with cell above
				if ((connections & 1) > 0) {
					if ((y == 0) || ((cellsIn[y - 1][x].getConnections() & 4) == 0)) {
						failCount++;
						cell.setConnectionsFail(true);
						cell.clearTop();
					}
				}
				// Check connectivity with cell to right
				if ((connections & 2) > 0) {
					if ((x == w - 1) || ((cellsIn[y][x + 1].getConnections() & 8) == 0)) {
						failCount++;
						cell.setConnectionsFail(true);
						cell.clearRight();
					}
				}

				// Check connectivity with cell below
				if ((connections & 4) > 0) {
					if ((y == h - 1) || ((cellsIn[y + 1][x].getConnections() & 1) == 0)) {
						failCount++;
						cell.setConnectionsFail(true);
						cell.clearBottom();
					}
				}

				// Check connectivity with cell to left
				if ((connections & 8) > 0) {
					if ((x == 0) || ((cellsIn[y][x - 1].getConnections() & 2) == 0)) {
						failCount++;
						cell.setConnectionsFail(true);
						cell.clearLeft();
					}
				}
			}
		}
		return failCount;

	}

	private int drcDangling(Cell[][] cellsIn, Cell[][] cellsOut) {
		int failCount = 0;

		int h = cellsIn.length;
		int w = cellsIn[0].length;

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				Cell cell = new Cell(cellsIn[y][x]);
				if (cell.isEnd() && cell.getType() == PinType.NONE) {
					cell.setDangleFail(true);
					failCount++;
				}
				if (cellsOut != null) {
					cellsOut[y][x] = cell;
				}
			}
		}
		return failCount;
	}

	private int drcBridge(Cell[][] cellsIn, Cell[][] cellsOut) {
		int failCount = 0;

		int h = cellsIn.length;
		int w = cellsIn[0].length;

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				Cell cell1 = new Cell(cellsIn[y][x]);
				if (cellsOut != null) {
					cellsOut[y][x] = cell1;
				}
				if (cell1.getType().ordinal() < PinType.HPAD_GND_1.ordinal()) {
					int c1 = cell1.getConnections();
					// Check for a bridge between two vertical traces
					if (x < w - 1) {
						Cell cell2 = cellsIn[y][x + 1];
						int c2 = cell2.getConnections();
						if ((c1 == 7 || c1 == 15) && (c2 == 13 || c2 == 15)) {
							cell1.setBridgeFail(true);
							cell2.setBridgeFail(true);
							failCount++;
						}
					}
					// Check for a bridge between two vertical traces
					if (y < h - 1) {
						Cell cell2 = cellsIn[y + 1][x];
						int c2 = cell2.getConnections();
						if ((c1 == 14 || c1 == 15) && (c2 == 11 || c2 == 15)) {
							cell1.setBridgeFail(true);
							cell2.setBridgeFail(true);
							failCount++;
						}
					}
				}
			}
		}
		return failCount;
	}

	private int fixDanglingPins(Cell[][] cellsIn, Cell[][] cellsOut) {

		int fixed = 0;

		int h = cellsIn.length;
		int w = cellsIn[0].length;

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				cellsOut[y][x] = new Cell(cellsIn[y][x]);
			}
		}

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {

				Cell cell1 = cellsIn[y][x];
				Cell cell1out = cellsOut[y][x];

				if (cell1.isDangleFail()) {

					// Fix a dangling cell that has an pad above
					if (y > 0 && !cell1.isTop()) {
						Cell cell2 = cellsIn[y - 1][x];
						Cell cell2out = cellsOut[y - 1][x];
						if (cell2.getType() != PinType.NONE) {
							cell1out.setTop();
							cell2out.setBottom();
							cell1out.setDangleFail(false);
							cell2out.setDangleFail(false);
							fixed++;
							continue;
						}
					}

					// Fix a dangling cell that has an pad below
					if (y < h - 1 && !cell1.isBottom()) {
						Cell cell2 = cellsIn[y + 1][x];
						Cell cell2out = cellsOut[y + 1][x];
						if (cell2.getType() != PinType.NONE) {
							cell1out.setBottom();
							cell2out.setTop();
							cell1out.setDangleFail(false);
							cell2out.setDangleFail(false);
							fixed++;
							continue;
						}
					}

					// Fix a dangling cell that has an pad left
					if (x > 0 && !cell1.isLeft()) {
						Cell cell2 = cellsIn[y][x - 1];
						Cell cell2out = cellsOut[y][x - 1];
						if (cell2.getType() != PinType.NONE) {
							cell1out.setLeft();
							cell2out.setRight();
							cell1out.setDangleFail(false);
							cell2out.setDangleFail(false);
							fixed++;
							continue;
						}
					}

					// Fix a dangling cell that has an pad right
					if (x < w - 1 && !cell1.isRight()) {
						Cell cell2 = cellsIn[y][x + 1];
						Cell cell2out = cellsOut[y][x + 1];
						if (cell2.getType() != PinType.NONE) {
							cell1out.setRight();
							cell2out.setLeft();
							cell1out.setDangleFail(false);
							cell2out.setDangleFail(false);
							fixed++;
							continue;
						}
					}
				}
			}
		}
		return fixed;
	}

	private int fixDanglingWeaklyConnected(Cell[][] cellsIn, Cell[][] cellsRef, Cell[][] cellsOut) {

		int fixed = 0;

		int h = cellsIn.length;
		int w = cellsIn[0].length;

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				cellsOut[y][x] = new Cell(cellsIn[y][x]);
			}
		}

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {

				Cell cell1 = cellsIn[y][x];
				Cell cell1out = cellsOut[y][x];

				if (cell1.isDangleFail()) {

					// Fix a dangling cell that is weakly connected to from
					// above
					if (y > 0 && !cell1.isTop()) {
						Cell cell2 = cellsRef[y - 1][x];
						Cell cell2out = cellsOut[y - 1][x];
						if (cell2.isBottom()) {
							cell1out.setTop();
							cell2out.setBottom();
							cell1out.setDangleFail(false);
							cell2out.setDangleFail(false);
							fixed++;
							continue;
						}
					}

					// Fix a dangling cell that is weakly connected to from
					// below
					if (y < h - 1 && !cell1.isBottom()) {
						Cell cell2 = cellsRef[y + 1][x];
						Cell cell2out = cellsOut[y + 1][x];
						if (cell2.isTop()) {
							cell1out.setBottom();
							cell2out.setTop();
							cell1out.setDangleFail(false);
							cell2out.setDangleFail(false);
							fixed++;
							continue;
						}
					}

					// Fix a dangling cell that is weakly connected to from left
					if (x > 0 && !cell1.isLeft()) {
						Cell cell2 = cellsRef[y][x - 1];
						Cell cell2out = cellsOut[y][x - 1];
						if (cell2.isRight()) {
							cell1out.setLeft();
							cell2out.setRight();
							cell1out.setDangleFail(false);
							cell2out.setDangleFail(false);
							fixed++;
							continue;
						}
					}

					// Fix a dangling cell that is weakly connected to from
					// right
					if (x < w - 1 && !cell1.isRight()) {
						Cell cell2 = cellsRef[y][x + 1];
						Cell cell2out = cellsOut[y][x + 1];
						if (cell2.isLeft()) {
							cell1out.setRight();
							cell2out.setLeft();
							cell1out.setDangleFail(false);
							cell2out.setDangleFail(false);
							fixed++;
							continue;
						}
					}
				}
			}
		}
		return fixed;
	}

	private int fixDanglingPairs(Cell[][] cellsIn, Cell[][] cellsOut) {

		int fixed = 0;

		int h = cellsIn.length;
		int w = cellsIn[0].length;

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				cellsOut[y][x] = new Cell(cellsIn[y][x]);
			}
		}

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {

				Cell cell1 = cellsIn[y][x];
				Cell cell1out = cellsOut[y][x];

				if (cell1.isDangleFail()) {

					// Fix a dangling cell that has a dangling cell to the right
					if (x < w - 1) {
						Cell cell2 = cellsIn[y][x + 1];
						Cell cell2out = cellsOut[y][x + 1];
						if (cell2.isDangleFail()) {
							if (cell1.isRight() && cell2.isLeft()) {
								cell1out.clearRight();
								cell2out.clearLeft();
							} else {
								cell1out.setRight();
								cell2out.setLeft();
							}
							cell1out.setDangleFail(false);
							cell2out.setDangleFail(false);
							fixed++;
							continue;
						}
					}

					// Fix a dangling cell that has a dangling cell to the below
					if (y < h - 1) {
						Cell cell2 = cellsIn[y + 1][x];
						Cell cell2out = cellsOut[y + 1][x];
						if (cell2.isDangleFail()) {
							if (cell1.isBottom() && cell2.isTop()) {
								cell1out.clearBottom();
								cell2out.clearTop();
							} else {
								cell1out.setBottom();
								cell2out.setTop();
							}
							cell1out.setDangleFail(false);
							cell2out.setDangleFail(false);
							fixed++;
							continue;
						}
					}
				}
			}
		}
		return fixed;
	}

	private int fixDanglingIsolated(Cell[][] cellsIn, Cell[][] cellsOut) {

		int fixed = 0;

		int h = cellsIn.length;
		int w = cellsIn[0].length;

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				cellsOut[y][x] = new Cell(cellsIn[y][x]);
			}
		}

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {

				Cell cell1 = cellsIn[y][x];
				Cell cell1out = cellsOut[y][x];

				if (cell1.isDangleFail()) {

					// Fix a dangling cell that is connected above
					if (y > 0 && cell1.isTop()) {
						Cell cell2out = cellsOut[y - 1][x];
						cell1out.clearTop();
						cell2out.clearBottom();
						cell1out.setDangleFail(false);
						fixed++;
						continue;
					}

					// Fix a dangling cell that is connected below
					if (y < h - 1 && cell1.isBottom()) {
						Cell cell2out = cellsOut[y + 1][x];
						cell1out.clearBottom();
						cell2out.clearTop();
						cell1out.setDangleFail(false);
						fixed++;
						continue;
					}

					// Fix a dangling cell that is connected left
					if (x > 0 && cell1.isLeft()) {
						Cell cell2out = cellsOut[y][x - 1];
						cell1out.clearLeft();
						cell2out.clearRight();
						cell1out.setDangleFail(false);
						fixed++;
						continue;
					}

					// Fix a dangling cell that is connected right
					if (x < w - 1 && cell1.isRight()) {
						Cell cell2out = cellsOut[y][x + 1];
						cell1out.clearRight();
						cell2out.clearLeft();
						cell1out.setDangleFail(false);
						fixed++;
						continue;
					}
				}
			}
		}
		return fixed;
	}

	private int fixBridgedPairs(Cell[][] cellsIn, Cell[][] cellsOut) {

		int fixed = 0;

		int h = cellsIn.length;
		int w = cellsIn[0].length;

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				cellsOut[y][x] = new Cell(cellsIn[y][x]);
			}
		}

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {

				Cell cell1 = cellsIn[y][x];
				Cell cell1out = cellsOut[y][x];

				if (cell1.isBridgeFail()) {

					// Fix a dangling cell that has a dangling cell to the right
					if (x < w - 1) {
						Cell cell2 = cellsIn[y][x + 1];
						Cell cell2out = cellsOut[y][x + 1];
						if (cell2.isBridgeFail()) {
							cell1out.clearRight();
							cell2out.clearLeft();
							cell1out.setDangleFail(false);
							cell2out.setDangleFail(false);
							fixed++;
							continue;
						}
					}

					// Fix a dangling cell that has a dangling cell to the below
					if (y < h - 1) {
						Cell cell2 = cellsIn[y + 1][x];
						Cell cell2out = cellsOut[y + 1][x];
						if (cell2.isBridgeFail()) {
							cell1out.clearBottom();
							cell2out.clearTop();
							cell1out.setDangleFail(false);
							cell2out.setDangleFail(false);
							fixed++;
							continue;
						}
					}
				}
			}
		}
		return fixed;
	}

}
