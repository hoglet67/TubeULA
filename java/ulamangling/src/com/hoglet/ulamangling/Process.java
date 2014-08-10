package com.hoglet.ulamangling;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hoglet.ulamangling.Pin.PinType;

public class Process {

	private static final int NET_VS  = 99991;
	private static final int NET_GND = 99990;
	
	// Whether to output annotated PNG
	public static final boolean OUTPUT_ANNOTATED_PNG = true;
	
	// Nominal cell size in pixels
	public static final int CELL_SIZE = 40;

	// Locally optimize the grid over +- this many cells
	public static final int GRID_DELTA = 8;
	
	// Whether to re-center a perfectly sized cell
	public static final boolean GRID_RECENTER = true;

	// match over +- this many pixels
	public static final int MATCH_DELTA = 1;

	// Threshold used when searching for the next grid line in pixels
	public static final int SEARCH_THRESH = 3;
	
	// Threshold used when determining when determine connectivity to an
	// adjacent cell
	public static final int CONNECT_THRESH = 20;
	
	// Threshold used when determining when determine connectivity to an
	// adjacent cell
	public static final int BLANK_THRESH = 100;

	// The X,Y coordinates of the top,left of the ULA cell grid
	public static Map<String, XY> startCells = new HashMap<String, XY>();

	// The X,Y coordinates of the top,left of the ULA cell grid
	public static Map<String, XY> blockCells = new HashMap<String, XY>();

	// The X,Y coordinates of the top,left of the ULA cell grid
	public static Map<String, XY> endCells = new HashMap<String, XY>();

	// The list of IO Pins
	public static Map<String, Pin[]> ioPinLists = new HashMap<String, Pin[]>();

	public static Map<String, Cell[]> overrideLists = new HashMap<String, Cell[]>();

	
	// Attempt 6
	// These values need to be manually extracted for each image
	static {

		startCells.put("00", new XY(5, 5));
		blockCells.put("00", new XY(8, 8));
		endCells.put("00", new XY(162, 177));
		ioPinLists.put("00", new Pin[] {
				new Pin(29, 5, "PA2"),
				new Pin(45, 5, "DACK"),
				new Pin(79, 5, "PNWDS"),
				new Pin(154, 5, "PNRDS"),
				new Pin(159, 5, "PCS"),
				new Pin(5, 6, "PA1"),
				new Pin(5, 23, "PA0"),
				new Pin(5, 91, "PD7IN"),
				new Pin(5, 112, "PD7OUT"),
				new Pin(5, 143, "PD6OUT"),
				new Pin(5, 71, "PDOE"),
				new Pin(5, 177, "PDOE"),
				new Pin(34, 5, "PDOE"),
				new Pin(86, 5, "PDOE"),
				new Pin(136, 5, "PDOE"),
				new Pin(138, 5, "PDOEA"),
				new Pin(151, 5, "PDOEB")
		});
		overrideLists.put("00", new Cell[] {
		});
				
		startCells.put("10", new XY(0, 5));
		blockCells.put("10", new XY(6, 8));
		endCells.put("10", new XY(160, 177));
		ioPinLists.put("10", new Pin[] {
				new Pin(160, 5, "HRST")
		});
		overrideLists.put("10", new Cell[] {
		});

		startCells.put("20", new XY(0, 5));
		blockCells.put("20", new XY(6, 8));
		endCells.put("20", new XY(158, 177));
		ioPinLists.put("20", new Pin[] {
				new Pin(3, 5, "HRST"),
				new Pin(8, 5, "HO2"),
				new Pin(96, 5, "HCS"),
				new Pin(109, 5, "HRW"),
				new Pin(147, 5, "HA2"),
				new Pin(158, 22, "HA1"),
				new Pin(158, 53, "HA0"),
				new Pin(158, 83, "HD7IN"),
				new Pin(158, 112, "HD7OUT"),
				new Pin(158, 143, "HD6OUT"),
				new Pin(158, 176, "HD6IN"),

				new Pin(26, 5, "HDOE"),
				new Pin(65, 5, "HDOE"),
				new Pin(97, 5, "HDOE"),
				new Pin(108, 5, "HDOE"),
				new Pin(128, 5, "HDOE"),
				new Pin(158, 71, "HDOE"),
				new Pin(158, 158, "HDOE"),
				new Pin(158, 164, "HDOE"),
				new Pin(158, 177, "HDOE"),
				new Pin(24, 5, "HDOEA")
		});
		overrideLists.put("20", new Cell[] {
		});

		
		startCells.put("01", new XY(5, 0));
		blockCells.put("01", new XY(8, 6));
		endCells.put("01", new XY(162, 175));
		ioPinLists.put("01", new Pin[] {
				new Pin(5, 2, "PD6IN"),
				new Pin(5, 30, "PD5OUT"),
				new Pin(5, 106, "PD5IN"),
				new Pin(5, 121, "PD4OUT"),
				new Pin(5, 142, "PD4IN"),
				new Pin(5, 173, "PD3IN"),
				new Pin(5, 1, "PDOE"),
				new Pin(5, 161, "PDOE"),
				new Pin(5, 169, "PDOE"),
				new Pin(5, 174, "PDOE")
		});
		overrideLists.put("01", new Cell[] {
		});


		startCells.put("11", new XY(0, 0));
		blockCells.put("11", new XY(6, 6));
		endCells.put("11", new XY(160, 175));
		ioPinLists.put("11", new Pin[] {
		});
		overrideLists.put("11", new Cell[] {
		});

		
		startCells.put("21", new XY(0, 0));
		blockCells.put("21", new XY(6, 6));
		endCells.put("21", new XY(158, 175));
		ioPinLists.put("21", new Pin[] {
				new Pin(158, 23, "HD5OUT"),
				new Pin(158, 97, "HD5IN"),
				new Pin(158, 121, "HD4OUT"),
				new Pin(158, 150, "HD4IN"),
				new Pin(158, 0, "HDOE"),
				new Pin(158, 38, "HDOE"),
				new Pin(158, 45, "HDOE"),
				new Pin(158, 161, "HDOE")
		});
		overrideLists.put("21", new Cell[] {
		});

		
		startCells.put("02", new XY(5, 0));
		blockCells.put("02", new XY(8, 6));
		endCells.put("02", new XY(162, 172));
		ioPinLists.put("02", new Pin[] {
				new Pin(5, 34, "PD3OUT"),
				new Pin(5, 65, "PD3OUT"),
				new Pin(5, 76, "PD2IN"),
				new Pin(5, 127, "PD1OUT"),
				new Pin(5, 148, "PD1IN"),
				new Pin(7, 172, "PD0OUT"),
				new Pin(35, 172, "PD0IN"),
				new Pin(160, 172, "PIRQ"),
				new Pin(157, 172, "PRST"),
				new Pin(138, 172, "DRQ"),
				new Pin(5, 2, "PDOE"),
				new Pin(5, 167, "PDOE"),
				new Pin(5, 172, "PDOE")
		});
		overrideLists.put("02", new Cell[] {
		});


		startCells.put("12", new XY(0, 0));
		blockCells.put("12", new XY(6, 6));
		endCells.put("12", new XY(160, 172));
		ioPinLists.put("12", new Pin[] {
				new Pin(153, 172, "HIRQ"),
				new Pin(160, 172, "PNMI")
		});
		overrideLists.put("12", new Cell[] {
		});


		startCells.put("22", new XY(0, 0));
		blockCells.put("22", new XY(6, 6));
		endCells.put("22", new XY(158, 172));
		ioPinLists.put("22", new Pin[] {
				new Pin(158, 3, "HD3OUT"),
				new Pin(158, 32, "HD3IN"),
				new Pin(158, 65, "HD2OUT"),
				new Pin(158, 94, "HD2IN"),
				new Pin(158, 127, "HD1OUT"),
				new Pin(158, 156, "HD1IN"),
				new Pin(155, 172, "HD0OUT"),
				new Pin(119, 172, "HD0IN"),
				new Pin(158, 0, "HDOE"),
				new Pin(158, 43, "HDOE"),
				new Pin(158, 50, "HDOE"),
				new Pin(158, 105, "HDOE"),
				new Pin(158, 112, "HDOE"),
				new Pin(158, 167, "HDOE"),
				new Pin(157, 172, "HDOE")
		});
		overrideLists.put("22", new Cell[] {
		});

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

	public void extract(String name, File srcFile, File dstFile) throws IOException {

		CellMatcher matcher = new CellMatcher(CELL_SIZE + 1, 4, 5, 15, MATCH_DELTA, BLANK_THRESH, CONNECT_THRESH);

		System.out.println("# Reading image " + srcFile);
		BufferedImage image = ImageIO.read(srcFile);
		int w = image.getWidth();
		int h = image.getHeight();
		System.out.println("# Image has " + w + " x " + h + " pixels; total = " + w * h);

		XY blockCell = blockCells.get(name);
		XY startCell = startCells.get(name);
		XY endCell = endCells.get(name);
		Pin[] ioPinList = ioPinLists.get(name);
		Cell[] overrideList = overrideLists.get(name);

		System.out.println("# Converting image");
		int[][] pixels = convertTo2DWithoutUsingGetRGB(image);

		System.out.println("# Gridding image");
		double[] window = new double[] { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1 };
		double[] reference = new Stats(window).normalize();

		Cell[][] cells = initGrid(w, h, pixels, reference);
		
		// Make a copy of the original grid so it can be restored later
		// Cell[][] originalGrid = cloneCells(cells);
		
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
		addPins(name, cells, blockCell, ioPinList);

		for (int xi = startCell.getX(); xi <= endCell.getX(); xi++) {
			System.out.println((xi - startCell.getX()) + " / " + (endCell.getX() - startCell.getX()));
			for (int yi = startCell.getY(); yi <= endCell.getY(); yi++) {
				matcher.match(cells[yi][xi], pixels);
			}
		}

		fixKnownPatterns(cells, overrideList);

		fixDodgyVSSConnection(cells, blockCell);

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

		// Restore the original grid so the output looks aligned
		// Sometimes useful to comment this out for debugging
		// copyGrid(originalGrid, cellsOut8);

		System.out.println("# Writing Json");
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting().serializeNulls();
        Gson gson = builder.create();
        FileWriter writer = new FileWriter(new File("cells_"+ name + ".json"));
        gson.toJson(cellsOut8, writer);
        writer.close();
         

		
		
		if (OUTPUT_ANNOTATED_PNG) {
			System.out.println("# Annotating PNG");
			annotateImage(image, cellsOut8);
			System.out.println("# Writing PNG");
			ImageIO.write(image, "png", dstFile);
		}
	}

	private void traceNets(Cell[][] cells) {
		int net = 1;
		for (int y = 0; y < cells.length; y++) {
			for (int x = 0; x < cells[y].length; x++) {
				Cell cell = cells[y][x];
				if (cell.getNet() == 0 && cell.getType() != null && cell.getType() != PinType.NONE && cell.getConnections() > 0) {
					traceNet(cells, x, y, net++);
				}
			}
		}
	}

	private void traceNet(Cell[][] cells, int x, int y, int net) {
		Cell cell = cells[y][x];
		if (cell.getNet() > 0) {
			return;
		}
		cell.setNet(net);
		if (cell.isLeft()) {
			traceNet(cells, x - 1, y, net);
		}
		if (cell.isRight()) {
			traceNet(cells, x + 1, y, net);
		}
		if (cell.isTop()) {
			traceNet(cells, x, y - 1, net);
		}
		if (cell.isBottom()) {
			traceNet(cells, x, y + 1, net);
		}
	}
	
	private void dumpCells(Cell[][] cells, Map<Integer, String> nameMap, boolean nets) {
		for (int y = 0; y < cells.length; y++) {
			for (int x = 0; x < cells[y].length; x++) {
				Cell cell = cells[y][x];
				Pin pin = cell.getPin();
				if (pin != null) {
					System.out.print(pin);
				} else {
					System.out.print(" ");
				}
				if (nets) {
					int net = cell.getNet();
					if (cell.getNet() > 0) {
						String name = nameMap.get(net);
						if (name == null) {
							name = "" + net;
						}
						while (name.length() < 6) {
							name += " ";
						}
						System.out.print(name);
					} else {
						System.out.print("      ");
					}
				}
			}
			System.out.println();
		}
	}

	
	
	private Cell[][] initGrid(int w, int h, int[][] pixels, double[] reference) {
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
	
	@SuppressWarnings("unused")
	private void copyGrid(Cell[][] from, Cell[][] to) {
		for (int yi = 0; yi < from.length; yi++) {
			for (int xi = 0; xi < from[yi].length; xi++) {
				to[yi][xi].setX1(from[yi][xi].getX1());
				to[yi][xi].setX2(from[yi][xi].getX2());
				to[yi][xi].setY1(from[yi][xi].getY1());
				to[yi][xi].setY2(from[yi][xi].getY2());
			}			
		}
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
	
	@SuppressWarnings("unused")
	private Cell[][] cloneCells(Cell[][] from) {
		Cell[][] to = new Cell[from.length][from[0].length];
		for (int yi = 0; yi < from.length; yi++) {
			for (int xi = 0; xi < from[yi].length; xi++) {
				to[yi][xi] = new Cell(from[yi][xi]);
			}
		}
		return to;
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

	private void fixDodgyVSSConnection(Cell[][] cells, XY blockCell) {
		for (int xi = 0; xi < 10; xi++) {
			for (int yi = 0; yi < 11; yi++) {
				int cellx = blockCell.getX() + 15 * xi; 
				int celly = blockCell.getY() + 15 * yi;
				// The connection between RCS and RLB has a habbit of shorting
				// this should only be connected to VS....
				if (cells[celly + 12][cellx + 11].isBottom() && cells[celly + 13][cellx + 12].isLeft()) {
					if (cells[celly + 13][cellx + 11].isBottom()) {
						cells[celly + 13][cellx + 11].clearBottom();
						cells[celly + 13][cellx + 11].setHighlight(true);
					}
				}
			}
		}
	}
		
		

	private void fixKnownPatterns(Cell[][] cells, Cell[] overrideList) {
		for (Cell override : overrideList) {
			cells[override.getPin().getY()][override.getPin().getX()].setConnections(override.getConnections());
		}
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

	private void addPins(String name, Cell[][] cells, XY blockOffset,  Pin[] ioPinList) {
		for (int cellXi = 0; cellXi < 10; cellXi++) {
			for (int cellYi = 0; cellYi < 11; cellYi++) {
				for (Pin pin : cellPins) {
					int cellX = blockOffset.getX() + cellXi * 15;
					int cellY = blockOffset.getY() + cellYi * 15;
					cells[cellY + pin.getY()][cellX + pin.getX()].setPin(pin);
				}
			}
		}

		boolean addLeft = name.charAt(0) != '0';
		boolean addRight = name.charAt(0) != '2';
		boolean addTop = name.charAt(1) != '0';
		boolean addBottom = name.charAt(1) != '2';
		
		int left = 2;
		int top = 2;
		int right = cells[0].length - 3;
		int bottom = cells.length - 3;
		
		for (int x : generateSequence(blockOffset.getX() + 1, 10)) {
			if (addTop) {
				cells[top][x].setPin(new Pin(0, 0, PinType.LINK_T));
			}
			if (addBottom) {
				cells[bottom][x].setPin(new Pin(0, 0, PinType.LINK_B));
			}
		}

		for (int y : generateSequence(blockOffset.getY() + 1, 11)) {
			if (addLeft) {
				cells[y][left].setPin(new Pin(0, 0, PinType.LINK_L));
			}
			if (addRight) {
				cells[y][right].setPin(new Pin(0, 0, PinType.LINK_R));
			}
		}
		
		//		for (int i = 0; i < 5; i++) {
		//			addPeriperalCell(cells, horPeripheralPins, 11 + 31 * i, 0, false, false);
		//		}
		//		for (int i = 0; i < 6; i++) {
		//			addPeriperalCell(cells, verPeripheralPins, 0, 3 + 31 * i, true, false);
		//		}
		//		for (int i = 0; i < 6; i++) {
		//			cells[2][4 + 31 * i].setPin(new Pin(0, 0, PinType.NORMAL));
		//		}
		//		for (int i = 0; i < 5; i++) {
		//			cells[29 + 31 * i][2].setPin(new Pin(0, 0, PinType.NORMAL));
		//		}
		
		for (Pin pin : ioPinList) {
			cells[pin.getY()][pin.getX()].setPin(pin);
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
	
	public void netlist(String blockBase) throws IOException {
		
		// Read in all 9 blocks
		
		List<Cell[][]> blocks = new ArrayList<Cell[][]>();
		
		blocks.add(parseBlock(new File(blockBase + "_00.json")));
		blocks.add(parseBlock(new File(blockBase + "_10.json")));
		blocks.add(parseBlock(new File(blockBase + "_20.json")));
		blocks.add(parseBlock(new File(blockBase + "_01.json")));
		blocks.add(parseBlock(new File(blockBase + "_11.json")));
		blocks.add(parseBlock(new File(blockBase + "_21.json")));
		blocks.add(parseBlock(new File(blockBase + "_02.json")));
		blocks.add(parseBlock(new File(blockBase + "_12.json")));
		blocks.add(parseBlock(new File(blockBase + "_22.json")));


		// Extract the width/heights of the blocks
		int w0 = blocks.get(0)[0].length;
		int w1 = blocks.get(1)[0].length;;
		int w2 = blocks.get(2)[0].length;;
		int h0 = blocks.get(0).length;
		int h1 = blocks.get(3).length;
		int h2 = blocks.get(6).length;
		int w = w0 + w1 + w2;
		int h = h0 + h1 + h2;
		
		// Calculate the top left corner of each block in the larger array
		List<XY> blockOrigins = new ArrayList<XY>();
		blockOrigins.add(new XY(0, 0));
		blockOrigins.add(new XY(w0, 0));
		blockOrigins.add(new XY(w0 + w1,0));
		blockOrigins.add(new XY(0, h0));
		blockOrigins.add(new XY(w0, h0));
		blockOrigins.add(new XY(w0 + w1,h0));
		blockOrigins.add(new XY(0,h0 + h1));
		blockOrigins.add(new XY(w0, h0 + h1));
		blockOrigins.add(new XY(w0 + w1, h0 + h1));
		
		// Calculate the relative location of top left cell in each block
		List<XY> cellOffsets = new ArrayList<XY>();
		for (char y = '0' ; y <= '2'; y++) {
			for (char x = '0' ; x <= '2'; x++) {
				String name = "" + x + y;
				cellOffsets.add(blockCells.get(name));
			}
		}

		// Shallow copy the blocks to a single large array
		// This allows the cells to still be accessed via the original blocks
		Cell[][] array = new Cell[h][w];
		for (int i = 0; i < blocks.size(); i++) {
			copyBlock(blocks.get(i), array, blockOrigins.get(i).getX(), blockOrigins.get(i).getY());
		}

		
		labelNetsV1(blocks, blockOrigins, cellOffsets, array, w, h);
		
		Map<Integer, String> nameMap = buildNameMap(array, w, h);
		
		dumpCells(array, nameMap, true);

		// Output the components in each block
		generateNetlist(blocks, blockOrigins, cellOffsets, array);
		
	}

	private Cell[][] parseBlock(File blockFile) throws IOException {
		System.out.println("Reading " + blockFile);
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting().serializeNulls();
		Gson gson = builder.create();
		FileReader reader = new FileReader(blockFile);
		Cell[][] block = gson.fromJson(reader, Cell[][].class);
		reader.close();
		return block;
	}
	
	private void copyBlock(Cell[][] from, Cell[][] to, int x0, int y0) {
		for (int y = 0; y < from.length; y++) {
			for (int x = 0; x < from[y].length; x++) {
				to[y0 + y][x0 + x] = from[y][x];
			}
		}
	}

	private Map<Integer, String> buildNameMap(Cell[][] array, int w, int h) {
		Map<Integer, String> nameMap = new HashMap<Integer, String>();
		nameMap.put(NET_GND, "GND");
		nameMap.put(NET_VS, "VS");
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				Cell cell = array[y][x];
				if (cell.getType() == PinType.IO) {
					String name = cell.getPin().getName();
					if (nameMap.containsKey(cell.getNet())) {
						String existingName = nameMap.get(cell.getNet());
						if (!existingName.equals(name)) {
							System.err.println("Name conflict: " + name + " and " + existingName);
						}
					} else {
						nameMap.put(cell.getNet(), name);
					}
				}
			}
		}
		return nameMap;
	}
	
	private void generateNetlist(List<Cell[][]> blocks, List<XY> blockOrigins, List<XY> cellOffsets, Cell[][] array) {
		for (int i = 0; i < blocks.size(); i++) {
			System.out.println("Outputting components in block " + i);
			XY blockOrigin = blockOrigins.get(i);
			XY cellOffset = cellOffsets.get(i);
			System.out.println("Block origin = " + blockOrigin);
			System.out.println("Cell Offset = " + cellOffset);
			for (int xi = 0; xi < 10; xi++) {
				for (int yi = 0; yi < 11; yi++) {
					int cellx = blockOrigin.getX() + xi * 15 + cellOffset.getX();
					int celly = blockOrigin.getY() + yi * 15 + cellOffset.getY();

					String id = "B" + (i % 3) + (i / 3) + "_C" + Integer.toHexString(xi) + Integer.toHexString(yi) + "_";
					
					// Emitter, Base, Collector
					outputComponent(array, cellx, celly, "T1", id, new XY(7, 4), new XY(8, 4), new XY(6, 1));
					outputComponent(array, cellx, celly, "T2", id, new XY(5, 4), new XY(4, 4), new XY(6, 1));
					outputComponent(array, cellx, celly, "T3", id, new XY(4, 8), new XY(4, 7), new XY(1, 8));
					outputComponent(array, cellx, celly, "T4", id, new XY(4, 10), new XY(4, 11), new XY(1, 8));
					
					// Emitter1, Emitter2, Base, Collector
					outputComponent(array, cellx, celly, "CS", id, new XY(7, 8), new XY(9, 8), new XY(8, 9), new XY(8, 10));

					// Resistors
					outputComponent(array, cellx, celly, "RLA", id, new XY(12, 3), new XY(12, 7));
					outputComponent(array, cellx, celly, "RCS", id, new XY(12, 9), new XY(12, 13));
					outputComponent(array, cellx, celly, "RLB", id, new XY(8, 13), new XY(12, 13));
}
			}
		}
	}

	private void outputComponent(Cell[][] array, int cellx, int celly, String type, String id, XY... pins) {
		String component = type + " " + id + "(";
		boolean first = true;
		for (XY loc : pins) {
			int x = cellx + loc.getX();
			int y = celly + loc.getY();
			Cell cell = array[y][x];
			if (cell.getType() == null || cell.getType() == PinType.NONE) {
				throw new RuntimeException("Missing pin at " + x + "," + y);
			}
			if (!first) {
				component += ", ";
			}
			int net = cell.getNet();
			if (net == NET_VS) {
				component += "VS";
			} else if (net == NET_GND) {
				component += "GND";
			} else {
				component += "N" + cell.getNet();
			}
			first = false;
		}
		component += ");";
		System.out.println(component);
	}

	

	
	
	/************************************************************************
	 * START OF LABEL NETS VERSION 1
	 ************************************************************************/

	private void labelNetsV1(List<Cell[][]> blocks, List<XY> blockOrigins, List<XY> cellOffsets, Cell[][] array, int w, int h) {
		// Trace the cell connectivity nets
		traceNets(array);
		
		// List of nets to merge
		Map<Integer, Integer> merge = new TreeMap<Integer, Integer>(); 
		
		connectAdjacentBlocks(w, h, array, merge);
		
		// Add the crossunders that link between the block
		// also adds power and VCC
		addCellCrossunders(blocks, blockOrigins, cellOffsets, array, merge);

		
		// Compress the map so that:
		// 1->2 2->3 would be replaced by 1->3 and 2->3
		compressMap(merge);


		// Update the net numbers
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				Cell cell = array[y][x];
				Integer update = merge.get(cell.getNet());
				if (update != null) {
					cell.setNet(update);
				}
			}
		}
	}

	private void connectAdjacentBlocks(int w, int h, Cell[][] array, Map<Integer, Integer> merge) {
		// Add the crossunders that link between the adjacent blocks
		int countL = 0;
		int countR = 0;
		int countT = 0;
		int countB = 0;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				Cell cell = array[y][x];
				Cell target = null;
				try {
					if (cell.getType() == PinType.LINK_L) {
						countL++;
						target = array[y][x - 5];
						if (target.getType() != PinType.LINK_R) {
							throw new RuntimeException("Something went wrong at " + x + "," + y);
						}
						merge(merge, cell.getNet(), target.getNet(), x, y, x - 5, y);
					}
					if (cell.getType() == PinType.LINK_R) {
						countR++;
						target = array[y][x + 5];
						if (target.getType() != PinType.LINK_L) {
							throw new RuntimeException("Something went wrong at " + x + "," + y);
						}
					}
					if (cell.getType() == PinType.LINK_T) {
						countT++;
						target = array[y - 5][x];
						if (target.getType() != PinType.LINK_B) {
							throw new RuntimeException("Something went wrong at " + x + "," + y);
						}
						merge(merge, cell.getNet(), target.getNet(), x, y, x, y - 5);
					}
					if (cell.getType() == PinType.LINK_B) {
						countB++;
						target = array[y + 5][x];
						if (target.getType() != PinType.LINK_T) {
							throw new RuntimeException("Something went wrong at " + x + "," + y);
						}
					}
				} catch (MergeException e) {
					System.err.println(e);
				}
			}
		}
		System.out.println("countL = " + countL);
		System.out.println("countR = " + countR);
		System.out.println("countT = " + countT);
		System.out.println("countB = " + countB);
	}

	private void addCellCrossunders(List<Cell[][]> blocks, List<XY> blockOrigins, List<XY> cellOffsets, Cell[][] array,
			Map<Integer, Integer> merge) {
		for (int i = 0; i < blocks.size(); i++) {
			System.out.println("Adding crossunders to block " + i);
			XY blockOrigin = blockOrigins.get(i);
			XY cellOffset = cellOffsets.get(i);
			System.out.println("Block origin = " + blockOrigin);
			System.out.println("Cell Offset = " + cellOffset);
			for (int xi = 0; xi < 10; xi++) {
				for (int yi = 0; yi < 11; yi++) {
					int cellx = blockOrigin.getX() + xi * 15 + cellOffset.getX();
					int celly = blockOrigin.getY() + yi * 15 + cellOffset.getY();
					// Sanity check we are where we think we are....
					if (array[celly + 9][cellx + 7].getType() != PinType.CS_BASE_1) {
						throw new RuntimeException("Cell alignnment error at " + cellx + "," + celly + "; bailing....");
					}
					try {
						mergeCrossunder(merge, array, cellx, celly, 1, 4, 4, 1);
						mergeCrossunder(merge, array, cellx, celly, 1, 10, 4, 13);
						mergeCrossunder(merge, array, cellx, celly, 8, 1, 12, 1);
						// Transistor pair
						mergeCrossunder(merge, array, cellx, celly, 1, 6, 6, 1);
						mergeCrossunder(merge, array, cellx, celly, 1, 6, 10, 3);
						// Transistor pair
						mergeCrossunder(merge, array, cellx, celly, 1, 8, 6, 13);
						// Power nets
						mergePower(merge, array, cellx, celly, 12, 8, NET_VS);
						mergePower(merge, array, cellx, celly, 11, 12, NET_VS);
						mergePower(merge, array, cellx, celly, 7, 11, NET_GND);
						mergePower(merge, array, cellx, celly, 8, 11, NET_GND);
						mergePower(merge, array, cellx, celly, 9, 11, NET_GND);
					} catch (MergeException e) {
						System.err.println(e);
					}
				}
			}
		}
	}
	
	private void compressMap(Map<Integer, Integer> merge) {
		int numchanged;
		do {
			numchanged = 0;
			for (Map.Entry<Integer, Integer> entry : merge.entrySet()) {
				if (merge.containsKey(entry.getValue())) {
					merge.put(entry.getKey(), merge.get(entry.getValue()));
					numchanged++;
				}
			}
		} while (numchanged > 0);
	}
	
	private void mergeCrossunder(Map<Integer, Integer> merge, Cell[][] array, int cellx, int celly, int x0, int y0, int x1, int y1) throws MergeException {
		x0 += cellx;
		x1 += cellx;
		y0 += celly;
		y1 += celly;
		Cell cell0 = array[y0][x0];
		Cell cell1 = array[y1][x1];
		if (cell0.getType() != PinType.NORMAL) {
			throw new MergeException("Missing pin at cell0", x0, y0, -1, -1);
		}
		if (cell1.getType() != PinType.NORMAL) {
			throw new MergeException("Missing pin at cell1" , -1, -1, x1, y1);
		}
		merge(merge, cell0.getNet(), cell1.getNet(), x0, y0, x1, y1);
	}
	
	private void mergePower(Map<Integer, Integer> merge, Cell[][] array, int cellx, int celly, int x0, int y0, int powernet) throws MergeException {
		x0 += cellx;
		y0 += celly;
		Cell cell0 = array[y0][x0];
		if (cell0.getType() == null || cell0.getType() == PinType.NONE) {
			throw new MergeException("Missing power pin at cell: ", x0, y0, -1, -1);
		}
		merge(merge, cell0.getNet(), powernet, x0, y0, -1, -1);
	}
	
	private void merge(Map<Integer, Integer> merge, int net0, int net1, int x0, int y0, int x1, int y1) throws MergeException {
		// Return if either end of the underpass are unconnected, or they are already connected
		if (net0 == 0 || net1 == 0 || net0 == net1) {
			return;
		}
		// Always merge min -> max to avoid possibility of cycles
		int min = Math.min(net0, net1);
		int max = Math.max(net0, net1);
		if (min == NET_GND || min == NET_VS) {
			throw new MergeException("Short between GND and VS at ", x0, y0, x1, y1);
		}
		// Don't overwrite an existing entry, instead recurse to add it to the end of the chain
		if (merge.containsKey(min)) {
			merge(merge, merge.get(min), max, x0, y0, x1, y1); 
		} else {
			merge.put(min, max);
		}
	}

	
	
	/************************************************************************
	 * END OF OF LABEL NETS VERSION 1
	 ************************************************************************/

	
	

	
	

	public static final void main(String[] args) {
		try {
			Process c = new Process();

			if (args.length == 2 && args[0].equals("netlist")) {

				// Netlist command reads in all 9 of the cells files and outputs a netlist
				
				c.netlist(args[1]);
				
			} else if (args.length == 3 && args[0].equals("extract")) {
	
				// Extract command processes a PNG and produces a cells file and a PNG
				File srcFile = new File(args[1]);
				if (!srcFile.exists()) {
					System.err.println("Src File: " + srcFile + " does not exist");
					System.exit(1);
				}
				if (!srcFile.isFile()) {
					System.err.println("Src File: " + srcFile + " is not a file");
					System.exit(1);
				}
				File dstFile = new File(args[2]);
	
				String name = srcFile.getName();
				name = name.substring(name.indexOf('_') + 1, name.lastIndexOf('.'));
				System.out.println("# name = " + name);
	
				c.extract(name, srcFile, dstFile);
				
			} else {
				System.err.println("       java -jar ulamangling.jar netlist <Cells Base Name>");
				System.err.println("usage: java -jar ulamangling.jar extract <Src PNG> <Dst PNG> ");
				System.exit(1);
			}

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
