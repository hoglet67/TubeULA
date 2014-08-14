package com.hoglet.ulamangling;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hoglet.ulamangling.Pin.PinType;

public class Process {

	private static final int NET_VSS  = 1;
	private static final int NET_GND = 2;

	private static final String NAME_VSS  = "VSS";
	private static final String NAME_GND = "GND";

	private static final String[] POWER_NETS = new String[] { NAME_GND, NAME_VSS };
	private static final int NET_NORMAL = 3;
	
	private static final String TYPE_TR = "TR";
	private static final String TYPE_CS = "CS";
	private static final String TYPE_RL = "RL";
	private static final String TYPE_RCS = "RCS";
	
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

	// The X,Y coordinates of the top,left of the ULA cell grid
	public static Map<String, XY> startCells = new HashMap<String, XY>();

	// The X,Y coordinates of the top,left of the ULA cell grid
	public static Map<String, XY> blockCells = new HashMap<String, XY>();

	// The X,Y coordinates of the top,left of the ULA cell grid
	public static Map<String, XY> endCells = new HashMap<String, XY>();

	// The list of IO Pins
	public static Map<String, Pin[]> ioPinLists = new HashMap<String, Pin[]>();

	public static Map<PinType, XY[]> underpassMap = new HashMap<Pin.PinType, XY[]>(); 
	
	static {
		
		underpassMap.put(PinType.UNDER_1, new XY[] { new XY(-3, 3) });
		underpassMap.put(PinType.UNDER_2, new XY[] { new XY(-5, 5), new XY(4, 2) });
		underpassMap.put(PinType.UNDER_3, new XY[] { new XY(4, 0) });
		underpassMap.put(PinType.UNDER_4, new XY[] { new XY(-4, 0) });
		underpassMap.put(PinType.UNDER_5, new XY[] { new XY(-4, -2), new XY(-9, 3) });
		underpassMap.put(PinType.UNDER_6, new XY[] { new XY(3, -3) });
		underpassMap.put(PinType.UNDER_7, new XY[] { new XY(5, -5), new XY(9, -3) });
		underpassMap.put(PinType.UNDER_8, new XY[] { new XY(5, 5) });
		underpassMap.put(PinType.UNDER_9, new XY[] { new XY(3, 3) });
		underpassMap.put(PinType.UNDER_10, new XY[] { new XY(-3, -3) });
		underpassMap.put(PinType.UNDER_11, new XY[] { new XY(-5, -5) });
		underpassMap.put(PinType.LINK_L, new XY[] { new XY(-5, 0) });
		underpassMap.put(PinType.LINK_R, new XY[] { new XY(5, 0) });
		underpassMap.put(PinType.LINK_T, new XY[] { new XY(0, -5) });
		underpassMap.put(PinType.LINK_B, new XY[] { new XY(0, 5) });
		

		startCells.put("00", new XY(5, 5));
		blockCells.put("00", new XY(8, 8));
		endCells.put("00", new XY(162, 177));
		ioPinLists.put("00", new Pin[] {
				new Pin(29, 5, PinType.IO_IN, "PA2"),
				new Pin(45, 5, PinType.IO_IN, "DACK"),
				new Pin(79, 5, PinType.IO_IN, "PNWDS"),
				new Pin(154, 5, PinType.IO_IN, "PNRDS"),
				new Pin(159, 5, PinType.IO_IN, "PCS"),
				new Pin(5, 6, PinType.IO_IN, "PA1"),
				new Pin(5, 23, PinType.IO_IN, "PA0"),
				new Pin(5, 91, PinType.IO_IN, "PD7IN"),
				new Pin(5, 112, PinType.IO_OUT, "PD7OUT"),
				new Pin(5, 143, PinType.IO_OUT, "PD6OUT"),
				new Pin(5, 71, PinType.IO_OUT, "PDOE"),
				new Pin(5, 177, PinType.IO_OUT, "PDOE"),
				new Pin(34, 5, PinType.IO_OUT, "PDOE"),
				new Pin(86, 5, PinType.IO_OUT, "PDOE"),
				new Pin(136, 5, PinType.IO_OUT, "PDOE"),
				new Pin(138, 5, PinType.IO_OUT, "PDOEA"),
				new Pin(151, 5, PinType.IO_OUT, "PDOEB")
		});
				
		startCells.put("10", new XY(0, 5));
		blockCells.put("10", new XY(6, 8));
		endCells.put("10", new XY(160, 177));
		ioPinLists.put("10", new Pin[] {
				new Pin(160, 5, PinType.IO_IN, "HRST")
		});

		startCells.put("20", new XY(0, 5));
		blockCells.put("20", new XY(6, 8));
		endCells.put("20", new XY(158, 177));
		ioPinLists.put("20", new Pin[] {
				new Pin(3, 5, PinType.IO_IN, "HRST"),
				new Pin(8, 5, PinType.IO_IN, "HO2"),
				new Pin(96, 5, PinType.IO_IN, "HCS"),
				new Pin(109, 5, PinType.IO_IN, "HRW"),
				new Pin(147, 5, PinType.IO_IN, "HA2"),
				new Pin(158, 22, PinType.IO_IN, "HA1"),
				new Pin(158, 53, PinType.IO_IN, "HA0"),
				new Pin(158, 83, PinType.IO_IN, "HD7IN"),
				new Pin(158, 112, PinType.IO_OUT, "HD7OUT"),
				new Pin(158, 143, PinType.IO_OUT, "HD6OUT"),
				new Pin(158, 176, PinType.IO_IN, "HD6IN"),

				new Pin(26, 5, PinType.IO_OUT, "HDOE"),
				new Pin(65, 5, PinType.IO_OUT, "HDOE"),
				new Pin(97, 5, PinType.IO_OUT, "HDOE"),
				new Pin(108, 5, PinType.IO_OUT, "HDOE"),
				new Pin(128, 5, PinType.IO_OUT, "HDOE"),
				new Pin(158, 71, PinType.IO_OUT, "HDOE"),
				new Pin(158, 158, PinType.IO_OUT, "HDOE"),
				new Pin(158, 164, PinType.IO_OUT, "HDOE"),
				new Pin(158, 177, PinType.IO_OUT, "HDOE"),
				new Pin(24, 5, PinType.IO_OUT, "HDOEA")
		});
		
		startCells.put("01", new XY(5, 0));
		blockCells.put("01", new XY(8, 6));
		endCells.put("01", new XY(162, 175));
		ioPinLists.put("01", new Pin[] {
				new Pin(5, 2, PinType.IO_IN, "PD6IN"),
				new Pin(5, 30, PinType.IO_OUT, "PD5OUT"),
				new Pin(5, 106, PinType.IO_IN, "PD5IN"),
				new Pin(5, 121, PinType.IO_OUT, "PD4OUT"),
				new Pin(5, 142, PinType.IO_IN, "PD4IN"),
				new Pin(5, 173, PinType.IO_IN, "PD3IN"),
				new Pin(5, 1, PinType.IO_OUT, "PDOE"),
				new Pin(5, 161, PinType.IO_OUT, "PDOE"),
				new Pin(5, 169, PinType.IO_OUT, "PDOE"),
				new Pin(5, 174, PinType.IO_OUT, "PDOE")
		});

		startCells.put("11", new XY(0, 0));
		blockCells.put("11", new XY(6, 6));
		endCells.put("11", new XY(160, 175));
		ioPinLists.put("11", new Pin[] {
		});

		startCells.put("21", new XY(0, 0));
		blockCells.put("21", new XY(6, 6));
		endCells.put("21", new XY(158, 175));
		ioPinLists.put("21", new Pin[] {
				new Pin(158, 23, PinType.IO_OUT, "HD5OUT"),
				new Pin(158, 97, PinType.IO_IN, "HD5IN"),
				new Pin(158, 121, PinType.IO_OUT, "HD4OUT"),
				new Pin(158, 150, PinType.IO_IN, "HD4IN"),
				new Pin(158, 0, PinType.IO_OUT, "HDOE"),
				new Pin(158, 38, PinType.IO_OUT, "HDOE"),
				new Pin(158, 45, PinType.IO_OUT, "HDOE"),
				new Pin(158, 161, PinType.IO_OUT, "HDOE")
		});
		
		startCells.put("02", new XY(5, 0));
		blockCells.put("02", new XY(8, 6));
		endCells.put("02", new XY(162, 172));
		ioPinLists.put("02", new Pin[] {
				new Pin(5, 34, PinType.IO_OUT, "PD3OUT"),
				new Pin(5, 65, PinType.IO_OUT, "PD2OUT"),
				new Pin(5, 76, PinType.IO_IN, "PD2IN"),
				new Pin(5, 127, PinType.IO_OUT, "PD1OUT"),
				new Pin(5, 148, PinType.IO_IN, "PD1IN"),
				new Pin(7, 172, PinType.IO_OUT, "PD0OUT"),
				new Pin(35, 172, PinType.IO_IN, "PD0IN"),
				new Pin(160, 172, PinType.IO_OUT, "PIRQ"),
				new Pin(157, 172, PinType.IO_OUT, "PRST"),
				new Pin(138, 172, PinType.IO_OUT, "DRQ"),
				new Pin(5, 2, PinType.IO_OUT, "PDOE"),
				new Pin(5, 167, PinType.IO_OUT, "PDOE"),
				new Pin(5, 172, PinType.IO_OUT, "PDOE")
		});

		startCells.put("12", new XY(0, 0));
		blockCells.put("12", new XY(6, 6));
		endCells.put("12", new XY(160, 172));
		ioPinLists.put("12", new Pin[] {
				new Pin(153, 172, PinType.IO_OUT, "HIRQ"),
				new Pin(160, 172, PinType.IO_OUT, "PNMI")
		});

		startCells.put("22", new XY(0, 0));
		blockCells.put("22", new XY(6, 6));
		endCells.put("22", new XY(158, 172));
		ioPinLists.put("22", new Pin[] {
				new Pin(158, 3, PinType.IO_OUT, "HD3OUT"),
				new Pin(158, 32, PinType.IO_IN, "HD3IN"),
				new Pin(158, 65, PinType.IO_OUT, "HD2OUT"),
				new Pin(158, 94, PinType.IO_IN, "HD2IN"),
				new Pin(158, 127, PinType.IO_OUT, "HD1OUT"),
				new Pin(158, 156, PinType.IO_IN, "HD1IN"),
				new Pin(155, 172, PinType.IO_OUT, "HD0OUT"),
				new Pin(119, 172, PinType.IO_IN, "HD0IN"),
				new Pin(158, 0, PinType.IO_OUT, "HDOE"),
				new Pin(158, 43, PinType.IO_OUT, "HDOE"),
				new Pin(158, 50, PinType.IO_OUT, "HDOE"),
				new Pin(158, 105, PinType.IO_OUT, "HDOE"),
				new Pin(158, 112, PinType.IO_OUT, "HDOE"),
				new Pin(158, 167, PinType.IO_OUT, "HDOE"),
				new Pin(157, 172, PinType.IO_OUT, "HDOE")
		});
	}

	public static Pin[] cellPins = new Pin[] {
		new Pin(4, 1, PinType.UNDER_1), 
		new Pin(6, 1, PinType.UNDER_2),
		new Pin(8, 1, PinType.UNDER_3), 
		new Pin(12, 1, PinType.UNDER_4), 
		new Pin(10, 3, PinType.UNDER_5),
		new Pin(12, 3, PinType.RES), 
		new Pin(1, 4, PinType.UNDER_6), 
		new Pin(4, 4, PinType.TR_BASE),
		new Pin(5, 4, PinType.TR_EMITTER), 
		new Pin(7, 4, PinType.TR_EMITTER), 
		new Pin(8, 4, PinType.TR_BASE),
		new Pin(1, 6, PinType.UNDER_7), 
		new Pin(4, 7, PinType.TR_BASE), 
		new Pin(7, 7, PinType.CS_EMITTER_1),
		new Pin(8, 7, PinType.CS_EMITTER_2), 
		new Pin(9, 7, PinType.CS_EMITTER_3), 
		new Pin(7, 8, PinType.CS_EMITTER_4),
		new Pin(8, 8, PinType.CS_EMITTER_5), 
		new Pin(9, 8, PinType.CS_EMITTER_6), 
		new Pin(12, 7, PinType.RES),
		new Pin(1, 8, PinType.UNDER_8), 
		new Pin(4, 8, PinType.TR_EMITTER), 
		new Pin(12, 8, PinType.VSS),
		new Pin(7, 9, PinType.CS_BASE_1), 
		new Pin(8, 9, PinType.CS_BASE_2), 
		new Pin(9, 9, PinType.CS_BASE_3),
		new Pin(12, 9, PinType.RES), 
		new Pin(1, 10, PinType.UNDER_9), 
		new Pin(4, 10, PinType.TR_EMITTER),
		new Pin(7, 10, PinType.CS_COLLECTOR_1), 
		new Pin(8, 10, PinType.CS_COLLECTOR_2), 
		new Pin(9, 10, PinType.CS_COLLECTOR_3),
		new Pin(4, 11, PinType.TR_BASE), 
		new Pin(7, 11, PinType.CS_GND_1), 
		new Pin(8, 11, PinType.CS_GND_2),
		new Pin(9, 11, PinType.CS_GND_3), 
		new Pin(11, 12, PinType.VSS), 
		new Pin(4, 13, PinType.UNDER_10),
		new Pin(6, 13, PinType.UNDER_11), 
		new Pin(8, 13, PinType.RES), 
		new Pin(12, 13, PinType.RES)
	};


	// Maps Integer Net Number to Net Name
	private Map<Integer, String> numberToNameMap;
	
	// Maps Net Name to List<Pin> where pin is <Type>_<ID>_<Num>
	private Map<String, Collection<String>> nameToPinMap;
	
	// Maps Component Pin to Net Name
	private Map<String, String> pinToNameMap;

	// Contains names of each type of component
	private Map<String, Collection<String>> componentMap;
	

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
	
	/****************************************************************
	 * START OF EXTRACT CODE
	 ****************************************************************/

	public void extract(String name, File srcFile, File dstFile) throws IOException {

		CellMatcher matcher = new CellMatcher(CELL_SIZE + 1, 4, 5, 15, MATCH_DELTA);

		System.out.println("# Reading image " + srcFile);
		BufferedImage image = ImageIO.read(srcFile);
		int w = image.getWidth();
		int h = image.getHeight();
		System.out.println("# Image has " + w + " x " + h + " pixels; total = " + w * h);

		XY blockCell = blockCells.get(name);
		XY startCell = startCells.get(name);
		XY endCell = endCells.get(name);
		Pin[] ioPinList = ioPinLists.get(name);

		System.out.println("# Converting image");
		int[][] pixels = convertTo2DWithoutUsingGetRGB(image);

		System.out.println("# Gridding image");
		double[] window = new double[] { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1 };
		double[] reference = new Stats(window).normalize();

		Cell[][] cells = initGrid(w, h, pixels, reference);
		
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

		fixKnownPatterns(cells);

		fixDodgyVSSConnection(cells, blockCell);
		
		
		fixExtractionErrors(cells, name);

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

		Cell[][] cellsOut9 = new Cell[cells.length][cells[0].length];
		ret = drcConnections(cellsOut8, cellsOut9);
		System.out.println("# Final DRC Connections Count = " + ret);

		Cell[][] cellsOut10 = new Cell[cells.length][cells[0].length];
		ret = drcDangling(cellsOut9, cellsOut10);
		System.out.println("# Final DRC Dangle Count = " + ret);

		Cell[][] cellsOut11 = new Cell[cells.length][cells[0].length];
		ret = drcBridge(cellsOut10, cellsOut11);
		System.out.println("# Final DRC Bridge Count = " + ret);

		Cell[][] cellsLast = cellsOut11;
		
		// It's necessary to do this again, because one of out heuristics
		// fixDodgyVSSConnections introduces a few errors
		fixExtractionErrors(cellsLast, name);

		System.out.println("# Writing Json");
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting().serializeNulls();
        Gson gson = builder.create();
        FileWriter writer = new FileWriter(new File("cells_"+ name + ".json"));
        gson.toJson(cellsLast, writer);
        writer.close();
		
		if (OUTPUT_ANNOTATED_PNG) {
			System.out.println("# Annotating PNG");
			annotateImage(image, cellsLast);
			System.out.println("# Writing PNG");
			ImageIO.write(image, "png", dstFile);
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

	private void fixDodgyVSSConnection(Cell[][] cells, XY blockCell) {
		for (int xi = 0; xi < 10; xi++) {
			for (int yi = 0; yi < 11; yi++) {
				int cellx = blockCell.getX() + 15 * xi; 
				int celly = blockCell.getY() + 15 * yi;
				// The connection between RCS and RLB has a habit of shorting
				// this should only be connected to VS....
				if ((cells[celly + 12][cellx + 11].isBottom() && !cells[celly + 12][cellx + 11].isTop()) || 
					(cells[celly + 13][cellx + 12].isLeft() && !cells[celly + 13][cellx + 12].isRight())) {
					cells[celly + 12][cellx + 11].setBottom();
					cells[celly + 13][cellx + 12].setLeft();
					cells[celly + 13][cellx + 11].setTop();
					cells[celly + 13][cellx + 11].setRight();
					if (cells[celly + 13][cellx + 11].isBottom()) {
						cells[celly + 13][cellx + 11].clearBottom();
						cells[celly + 13][cellx + 11].setHighlight(true);
					}
				}
			}
		}
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

				if (cell.isDangleFail()) {
					Pin.rectangle(image, x1, y1, w, h, Pin.MAGENTA, true);
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
	
	private void fixExtractionErrors(Cell[][] cells, String name) {

		
		
		XY[] csShorts = new XY[] {};
				
		if (name.equals("00")) {
			// Shorts near links between cells, causing Mis Matched emitters and
			// collectors
			cells[131][160].setConnections(8);
			cells[131][161].setConnections(5);
		}

		if (name.equals("10")) {
			// block 10
			csShorts = new XY[] {
				new XY(0, 0),
				new XY(0, 1),
				new XY(0, 3),
				new XY(0, 6),
				new XY(0, 9),
				new XY(3, 0),
				new XY(3, 1),
				new XY(3, 2),
				new XY(3, 4),
				new XY(3, 5),
				new XY(3, 8),
				new XY(3, 9),
				new XY(3, 10),
				new XY(6, 0),
				new XY(6, 1),
				new XY(6, 3),
				new XY(6, 10)
			};

			// Fix shorts to VSS
			cells[35][152].setConnections(0);
			cells[36][152].setConnections(6);
			cells[37][152].setConnections(5);
			cells[38][152].setConnections(5);
			cells[96][153].setConnections(8);
			cells[97][153].setConnections(10);
			cells[52][39].setConnections(15);
			cells[53][39].setConnections(5);
			cells[54][39].setConnections(3);
			// Connected to something unexpected
			cells[119][73].setConnections(10);
			cells[120][73].setConnections(4);
			// Self Coupled Gates
			cells[146][15].setConnections(5);
			cells[146][16].setConnections(2);
			// Shorts near links between cells, causing Mis Matched emitters and
			// collectors
			cells[125][1].setConnections(5);
			cells[125][2].setConnections(2);
			// Last few things causing Mis Matched emitters and collectors
			cells[144][59].setConnections(1);
			cells[144][60].setConnections(5);
		}

		if (name.equals("20")) {
			// Danling nets
			cells[127][93].setConnections(0);
			cells[127][94].setConnections(5);
			cells[127][96].setConnections(0);
			cells[127][97].setConnections(5);
			cells[128][96].setConnections(6);
			cells[128][97].setConnections(9);
		}

		if (name.equals("01")) {
			// Fix emitter not connected to current source
			cells[119][14].setConnections(7);
			cells[119][15].setConnections(11);
			// Last few things causing Mis Matched emitters and collectors
			cells[7][94].setConnections(5);
			cells[7][95].setConnections(2);
		}

		if (name.equals("11")) {
			csShorts = new XY[] {
				new XY(3, 5),
				new XY(3, 10),
				new XY(4, 0),
				new XY(6, 0)
			};
			// Last few things causing Mis Matched emitters and collectors
			cells[82][54].setConnections(5);
			cells[82][55].setConnections(1);
		}

		if (name.equals("21")) {
			// Shorts between current sources
			cells[117][133].setConnections(3);
			cells[118][133].setConnections(4);
		}

		if (name.equals("02")) {
			// Fix emitter not connected to current source
			cells[134][14].setConnections(7);
			cells[134][15].setConnections(11);
			// Fix shorts to VSS
			cells[108][48].setConnections(5);
			cells[108][49].setConnections(4);
			cells[108][93].setConnections(5);
			cells[108][94].setConnections(4);
			cells[14][50].setConnections(1);
			cells[15][50].setConnections(8);
			cells[74][110].setConnections(1);
			cells[75][110].setConnections(8);
			// Shorts near links between cells, causing Mis Matched emitters and
			// collectors
			cells[33][160].setConnections(4);
			cells[33][161].setConnections(5);
			cells[129][160].setConnections(8);
			cells[129][161].setConnections(5);
		}

		if (name.equals("12")) {
			csShorts = // block 12
			new XY[] {
					new XY(0, 1),
					new XY(0, 2),
					new XY(0, 4),
					new XY(0, 5),
					new XY(0, 6),
					new XY(0, 7),

					new XY(3, 0),
					new XY(3, 1),
					new XY(3, 2),
					new XY(3, 3),
					new XY(3, 4),
					new XY(3, 6),
					new XY(3, 7),
					new XY(3, 8),
					new XY(3, 10),
					
					new XY(6, 1),
					new XY(6, 7),
			};

			// Fix shorts to VSS
			cells[123][121].setConnections(5);
			cells[123][122].setConnections(4);
			// Self Coupled Gates
			cells[84][16].setConnections(2);
			cells[85][16].setConnections(10);
			cells[84][46].setConnections(2);
			cells[85][46].setConnections(10);
			cells[84][76].setConnections(2);
			cells[85][76].setConnections(10);
			cells[84][106].setConnections(2);
			cells[85][106].setConnections(10);
			// Shorts between current sources
			cells[14][37].setConnections(3);
			cells[14][38].setConnections(4);
			// Non Standard Shorts between current sources
			cells[27][148].setConnections(3);
			cells[28][148].setConnections(1);

		}
		
		if (name.equals("22")) {
			// From original overrides list
			cells[54][152].setConnections(10);
			// Fix shorts to VSS
			cells[93][92].setConnections(0);
			cells[94][92].setConnections(0);
			cells[95][92].setConnections(10);
			cells[93][107].setConnections(0);
			cells[94][107].setConnections(0);
			cells[95][107].setConnections(10);
			cells[138][92].setConnections(0);
			cells[139][92].setConnections(0);
			cells[140][92].setConnections(10);
			cells[138][107].setConnections(0);
			cells[139][107].setConnections(0);
			cells[140][107].setConnections(10);
			cells[108][136].setConnections(5);
			cells[108][137].setConnections(4);
			// Shorts between current sources
			cells[12][58].setConnections(3);
			cells[13][58].setConnections(4);
			cells[119][43].setConnections(9);
			cells[119][44].setConnections(0);
			cells[119][45].setConnections(1);
			cells[32][9].setConnections(5);
			cells[32][10].setConnections(4);
		}
		
		XY origin = blockCells.get(name);
		for (XY loc : csShorts) {
			cells[origin.getY() + 15 * loc.getY() + 8][origin.getX() + 15 * loc.getX() + 7].clearRight();
			cells[origin.getY() + 15 * loc.getY() + 8][origin.getX() + 15 * loc.getX() + 8].clearLeft();
			cells[origin.getY() + 15 * loc.getY() + 8][origin.getX() + 15 * loc.getX() + 8].clearRight();
			cells[origin.getY() + 15 * loc.getY() + 8][origin.getX() + 15 * loc.getX() + 9].clearLeft();
		}
	}

	/****************************************************************
	 * END OF EXTRACT CODE
	 ****************************************************************/
	
	/****************************************************************
	 * START OF NETLIST CODE
	 ****************************************************************/
	
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

		// Trace the connections, taking account of underpasses, power and ground
		traceConnections(array);

		// Build mapping of external pin name to internal net numbers
		numberToNameMap = buildNameMap(array);

		// Add the transistors/resistors/current sources in each block into the 
		//		nameToPinMap
		//		pinToNameMap
		//		componentMap
		generateTransistorNetlist(blocks, blockOrigins, cellOffsets, array);
		// dumpCells(array, true);
		
		// Transform the transistors level netlist to gates
		NetList netlist = transformToGates();
		
		// Add IO Pins
		Collection<String> inputPins = getInputPins();
		Collection<String> outputPins = getOutputPins();
		addIOPins(netlist, inputPins, outputPins);

		// Output for debugging
		System.out.println("**** Gate Level Netlist ****");
		netlist.dumpStats();
		netlist.dump();

		// Sanity check we have no gates that feedback to themselves
		netlist.checkFromSelfCoupledGates();

		// Refine the netlist by recognising latches
		netlist = netlist.replaceWithLatches();

		// Refine the netlist by recognising latches
		netlist = netlist.replaceWithSR();

		// Prune any output pins that don't drive anything (e.g. unused latch outputs)
		netlist = netlist.pruneUnconnectedOutputs();
		
		System.out.println("**** Latch Level Netlist ****");
		netlist.dumpStats();
		netlist.dump();

		for (String net : inputPins) {
			System.out.println("**** Tracing Path from " + net + " ****");
			List<String> paths = netlist.traceNetForward(net);
			System.out.println("**** found " + paths.size() + " paths");
			for (String path : paths) {
				System.out.println(path);
			}
		}
	}

	private Cell[][] parseBlock(File blockFile) throws IOException {
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

	private void traceConnections(Cell[][] array) {
		tracePowerConnections(array);
		validatePowerConnections(array);
		traceNormalConnections(array);
	}

	private void traceNormalConnections(Cell[][] array) {
		int net = NET_NORMAL;
		for (int y = 0; y < array.length; y++) {
			for (int x = 0; x < array[y].length; x++) {
				Cell cell = array[y][x];
				// Start at a connected pin that hasn't been labeled with a net
				if (cell.getNet() == 0 && cell.getType() != null && cell.getType() != PinType.NONE && cell.getConnections() > 0) {
					traceConnections(array, x, y, net++);
				}
			}
		}
	}

	private void validatePowerConnections(Cell[][] array) {
		for (int y = 0; y < array.length; y++) {
			for (int x = 0; x < array[y].length; x++) {
				Cell cell = array[y][x];
				if (cell.getPin() != null && cell.getPin().isLink()) {
					// If either GND or VSS in on any of the array block links, this is most likely incorrect
					if (cell.getNet() == NET_GND) {
						System.err.println("Unexpected GND on link at " + x + "," + y);
					}
					if (cell.getNet() == NET_VSS) {
						System.err.println("Unexpected VSS on link at " + x + "," + y);
					}
				}
				if (cell.getPin() != null && cell.getPin().isTransistor()) {
					if (cell.getNet() == NET_GND) {
						System.err.println("Unexpected GND on transistor at " + x + "," + y);
					}
					if (cell.getNet() == NET_VSS) {
						System.err.println("Unexpected VSS on transistor at " + x + "," + y);
					}					
				}
				if (cell.getPin() != null && cell.getPin().isCurrentSource()) {
					if (cell.getNet() == NET_GND) {
						System.err.println("Unexpected GND on current source at " + x + "," + y);
					}
					if (cell.getNet() == NET_VSS) {
						System.err.println("Unexpected VSS on current source at " + x + "," + y);
					}					
				}
				if (cell.getPin() != null && cell.getPin().isResistor()) {
					if (cell.getNet() == NET_GND) {
						System.err.println("Unexpected GND on resistor at " + x + "," + y);
					}
				}
			}
		}
	}

	private void tracePowerConnections(Cell[][] array) {
		for (int y = 0; y < array.length; y++) {
			for (int x = 0; x < array[y].length; x++) {
				Cell cell = array[y][x];
				if (cell.getPin() != null && cell.getPin().isGnd()) {
					traceConnections(array, x, y, NET_GND);
				}
				if (cell.getPin() != null && cell.getPin().isVss()) {
					traceConnections(array, x, y, NET_VSS);
				}
			}
		}
	}

	private void traceConnections(Cell[][] cells, int x, int y, int net) {
		Cell cell = cells[y][x];
		if (cell.getNet() > 0) {
			return;
		}
		cell.setNet(net);
		if (cell.isLeft()) {
			traceConnections(cells, x - 1, y, net);
		}
		if (cell.isRight()) {
			traceConnections(cells, x + 1, y, net);
		}
		if (cell.isTop()) {
			traceConnections(cells, x, y - 1, net);
		}
		if (cell.isBottom()) {
			traceConnections(cells, x, y + 1, net);
		}
		XY[] connections = underpassMap.get(cell.getType());
		if (connections != null) {
			for (XY connection : connections) {
				traceConnections(cells, x + connection.getX(), y + connection.getY(), net);
			}
		}
	}
	
	private Map<Integer, String> buildNameMap(Cell[][] array) {
		Map<Integer, String> nameMap = new TreeMap<Integer, String>();
		nameMap.put(NET_GND, NAME_GND);
		nameMap.put(NET_VSS, NAME_VSS);
		for (int y = 0; y < array.length; y++) {
			for (int x = 0; x < array[y].length; x++) {
				Cell cell = array[y][x];
				// TODO IO_IN is legacy, remove once cell maps have been rebuilt
				if (cell.getType() == PinType.IO  || cell.getType() == PinType.IO_IN || cell.getType() == PinType.IO_OUT) {
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
	
	private void generateTransistorNetlist(List<Cell[][]> blocks, List<XY> blockOrigins, List<XY> cellOffsets, Cell[][] array) {
		nameToPinMap = new HashMap<String, Collection<String>>();
		pinToNameMap = new HashMap<String, String>();
		componentMap = new HashMap<String, Collection<String>>();
		for (int i = 0; i < blocks.size(); i++) {
			XY blockOrigin = blockOrigins.get(i);
			XY cellOffset = cellOffsets.get(i);
			for (int xi = 0; xi < 10; xi++) {
				for (int yi = 0; yi < 11; yi++) {
					int cellx = blockOrigin.getX() + xi * 15 + cellOffset.getX();
					int celly = blockOrigin.getY() + yi * 15 + cellOffset.getY();

					String id = "B" + (i % 3) + (i / 3) + "_C" + Integer.toHexString(xi) + Integer.toHexString(yi);
					
					// Emitter, Base, Collector
					addComponent(array, cellx, celly, TYPE_TR, id + "_T1", new XY(7, 4), new XY(8, 4), new XY(6, 1));
					addComponent(array, cellx, celly, TYPE_TR, id + "_T2", new XY(5, 4), new XY(4, 4), new XY(6, 1));
					addComponent(array, cellx, celly, TYPE_TR, id + "_T3", new XY(4, 8), new XY(4, 7), new XY(1, 8));
					addComponent(array, cellx, celly, TYPE_TR, id + "_T4", new XY(4, 10), new XY(4, 11), new XY(1, 8));
					
					// Emitter1, Emitter2, Base, Collector
					addComponent(array, cellx, celly, TYPE_CS, id, new XY(7, 8), new XY(9, 8), new XY(8, 9), new XY(8, 10));

					// Resistors
					addComponent(array, cellx, celly, TYPE_RL, id + "_RLA", new XY(12, 3), new XY(12, 7));
					addComponent(array, cellx, celly, TYPE_RCS, id + "_RCS", new XY(12, 9), new XY(12, 13));
					addComponent(array, cellx, celly, TYPE_RL, id + "_RLB", new XY(8, 13), new XY(12, 13));
				}
			}
		}
	}

	private void addComponent(Cell[][] array, int cellx, int celly, String type, String id, XY... pins) {
		String component = type + " " + id + "(";
		boolean first = true;

		// First, make sure all pins are properly used
		boolean allUsed = true;
		for (int i = 0; i < pins.length; i++) {
			XY loc = pins[i];
			int x = cellx + loc.getX();
			int y = celly + loc.getY();
			Cell cell = array[y][x];
			int net = cell.getNet();
			allUsed &= net > 0;
		}

		for (int i = 0; i < pins.length; i++) {
			XY loc = pins[i];
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

			if (type.equals(TYPE_TR) || (type.equals(TYPE_CS) && i < 2)) {
				if (net == NET_VSS) {
					System.err.println("Component " + type + " " + id + " has an unexpected short to VSS on pin " + i);
				}
			}

			if ((!type.equals(TYPE_CS) || i == 2)) {
				if (net == NET_GND) {
					System.err.println("Component " + type + " " + id + " has an unexpected short to GND on pin " + i);
				}
			}

			String netName = numberToNameMap.get(net);
			if (netName == null) {
				netName = "N" + net;
			}
			component += netName;
			first = false;

			if (allUsed) {
				// Add the component
				Collection<String> componentList = componentMap.get(type);
				if (componentList == null) {
					// First time this type of component has been encountered
					componentList = new TreeSet<String>();
					componentMap.put(type, componentList);
				}
				componentList.add(id);
	
				// Add the pin -> netName to the first map
				String pin = type + "_" + id + "_" + i;
				pinToNameMap.put(pin, netName);
	
				// Append the pin to the collection in the netName -> pin maps
				Collection<String> pinList = nameToPinMap.get(netName);
				if (pinList == null) {
					// First time this net has been encountered
					pinList = new TreeSet<String>();
					nameToPinMap.put(netName, pinList);
				}
				pinList.add(pin);
			}
		}
		component += ");";

		if (allUsed) {
			System.out.println(component);
		} else {
			System.out.println("# " + component);
		}
	}
	
	private NetList transformToGates() {
		System.err.println(nameToPinMap.size() + " nets");
		System.err.println(pinToNameMap.size() + " pins");
		for (Map.Entry<String, Collection<String>> entry : componentMap.entrySet()) {
			System.err.println(entry.getValue().size() + " " + entry.getKey() + " components");
		}

		NetList netlist = new NetList();

		// Iterate through the transistors
		for (String trid : componentMap.get(TYPE_TR)) {

			// Lookup the nets that the transistor is connected to
			String emitterNet = pinToNameMap.get(TYPE_TR + "_" + trid + "_0");
			String baseNet = pinToNameMap.get(TYPE_TR + "_" + trid + "_1");
			String collectorNet = pinToNameMap.get(TYPE_TR + "_" + trid + "_2");

			boolean shorted = false;
			for (String power : POWER_NETS) {
				if (emitterNet.equals(power) || baseNet.equals(power) || collectorNet.equals(power)) {
					System.err.println("Skipping transistor " + trid + " because it has a pin shorted to " + power);
					shorted = true;
				}
			}
			if (shorted) {
				continue;
			}

			String id = "G" + collectorNet;
			Component gate  = netlist.get(id);
			if (gate == null) {
				gate = netlist.createComponent("NOR", id);
				gate.addOutput("O", collectorNet);
			}
			gate.addInput("I", baseNet);
			
			
			// The rest of the code is flagging possible transistor netlist errors
			
			// Follow the emitter connections
			Collection<String> emitterConnections = nameToPinMap.get(emitterNet);

			// Count the current sources, and warn if there are none
			int csCount = 0;
			Collection<String> linkedEmitters = new TreeSet<String>();
			for (String connection : emitterConnections) {
				if (connection.startsWith(TYPE_TR)) {
					if (connection.endsWith("0")) {
						linkedEmitters.add(connection.substring(0, connection.length() - 2));
					} else {
						System.err.println("Warning: Transistor " + trid + " emitter connected to the wrong pin of another transistor: " + connection);
					}
				} else if (connection.startsWith(TYPE_CS)) {
					if (!connection.endsWith("0") &&  !connection.endsWith("1")) {
						System.err.println("Warning: Transistor " + trid + " emitter connected to the wrong pin of current source: " + connection);
					}
					csCount++;
				} else {
					System.err.println("Warning: Transistor " + trid + " emitter connected to something unexpected: " + connection);
				}
			}
			if (csCount == 0) {
				System.err.println("Warning: Transistor " + trid + " emitter not connected to any current sources");
			}			

			// Follow the collector connections
			Collection<String> collectorConnections = nameToPinMap.get(collectorNet);
			
			// Count the load resistors
			int rlCount = 0;
			Collection<String> linkedCollectors = new TreeSet<String>();
			for (String connection : collectorConnections) {
				if (connection.startsWith(TYPE_TR)) {
					if (connection.endsWith("0")) {
						// Emitter
						System.err.println("Warning: Transistor " + trid + " collector connected to the emitter of another transistor: " + connection);
					} else if (connection.endsWith("2")) {
						linkedCollectors.add(connection.substring(0, connection.length() - 2));
					}
				} else if (connection.startsWith(TYPE_RL) || connection.startsWith(TYPE_RCS)) {
					rlCount++;
				} else {
					System.err.println("Warning: Transistor " + trid + " collector connected to something unexpected: " + connection);
				}
			}
			if (rlCount == 0) {
				System.err.println("Warning: Transistor " + trid + " collector not connected to any current sources");
			}
			
			if (linkedCollectors.size() != linkedEmitters.size()) {
				System.out.println("Different numbers of emitters and collectors linked: " + linkedEmitters + " and " + linkedCollectors);
			} else if (!linkedCollectors.equals(linkedEmitters)) {
				System.out.println("Different emitters and collectors linked: " + linkedEmitters + " and " + linkedCollectors);
			}
		}
		
		return netlist;

	}

	private Collection<String> getInputPins() {
		Collection<String> inputs = new TreeSet<String>();
		for (Pin pins[] : ioPinLists.values()) {
			for (Pin pin : pins) {
				if (pin.getType() == PinType.IO_IN) {
					inputs.add(pin.getName());
				}
			}
		}
		return inputs;
		
	}

	private Collection<String> getOutputPins() {
		Collection<String> outputs = new TreeSet<String>();
		for (Pin pins[] : ioPinLists.values()) {
			for (Pin pin : pins) {
				if (pin.getType() == PinType.IO_OUT) {
					outputs.add(pin.getName());
				}
			}
		}
		return outputs;
	}

	private void addIOPins(NetList netlist, Collection<String> inputs, Collection<String> outputs) {
		for (Pin pins[] : ioPinLists.values()) {
			for (Pin pin : pins) {
				if (pin.getType() == PinType.IO_IN) {
					inputs.add(pin.getName());
				}
				if (pin.getType() == PinType.IO_OUT) {
					outputs.add(pin.getName());
				}
			}
		}
		for (String input : inputs) {
			Component component = netlist.createComponent("INPUT", input);
			component.addOutput("O", input);
		}
		for (String output : outputs) {
			Component component = netlist.createComponent("OUTPUT", output);
			component.addInput("I", output);
		}
	}
	
	@SuppressWarnings("unused")
	private void dumpCells(Cell[][] cells, boolean nets) {
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
						String name = numberToNameMap.get(net);
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
	
	/****************************************************************
	 * END OF NETLIST CODE
	 ****************************************************************/

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
