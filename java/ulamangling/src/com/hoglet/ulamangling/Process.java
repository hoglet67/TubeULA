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
		startCells.put("00", new XY(5, 5));
		blockCells.put("00", new XY(8, 8));
		endCells.put  ("00", new XY(162, 177));
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

	public void convert(String name, File gridFile, File matchFile, File dstFile) throws IOException {

		CellMatcher matcher = new CellMatcher(CELL_SIZE + 1, 4, 5);

		System.out.println("# Reading image " + gridFile);
		BufferedImage image = ImageIO.read(gridFile);
		int w = image.getWidth();
		int h = image.getHeight();
		System.out.println("# Grid Image has " + w + " x " + h + " pixels; total = " + w * h);

		
		XY blockCell = blockCells.get(name); 
		int blockCellX = blockCell.getX();
		int blockCellY = blockCell.getY();
		
		XY startCell = startCells.get(name);
		XY endCell = endCells.get(name);
		
		System.out.println("# Converting image");
		int[][] pixels = convertTo2DWithoutUsingGetRGB(image);

		System.out.println("# Gridding image");

		XY startOffset = new XY(0, 0);
		XY endOffset = new XY(w - 1, h - 1);
		
		int[] xTotals = new int[w];
		int[] yTotals = new int[h];
		gridHistogram(pixels, startOffset, endOffset, xTotals, yTotals);

		double[] window = new double[] { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1 };
		double[] xReference = window;
		double[] yReference = window;

		xReference = new Stats(xReference).normalize();
		yReference = new Stats(yReference).normalize();

		double[] xNormalized = new Stats(xTotals).normalize();
		double[] yNormalized = new Stats(yTotals).normalize();

		double[] xCorrelation = Stats.bruteForceCorrelationCentered(xNormalized, xReference);
		double[] yCorrelation = Stats.bruteForceCorrelationCentered(yNormalized, yReference);

		List<Integer> xGrid = makeGrid(startOffset.getX(), endOffset.getX(), CELL_SIZE, xCorrelation, SEARCH_THRESH);
		List<Integer> yGrid = makeGrid(startOffset.getY(), endOffset.getY(), CELL_SIZE, yCorrelation, SEARCH_THRESH);

		System.out.println("# Reading image " + matchFile);
		image = ImageIO.read(matchFile);
		w = image.getWidth();
		h = image.getHeight();
		System.out.println("# Match Image has " + w + " x " + h + " pixels; total = " + w * h);

		System.out.println("# Converting image");
		pixels = convertTo2DWithoutUsingGetRGB(image);

		System.out.println("# Matching image");

		// dumpXGraph("x reference", xReference);
		// dumpXGraph("x normalized", xNormalized);
		// dumpXGraph("x correlation", xCorrelation);
		// dumpXGraph("x grid", xGrid, w, gridSize);
		// dumpXGraph("y reference", yReference);
		// dumpXGraph("y normalized", yNormalized);
		// dumpXGraph("y correlation", yCorrelation);
		// dumpXGraph("y grid", yGrid, h, gridSize);



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

		Cell[][] cells = initCells(xGrid, yGrid);

		// Overlay pins onto logical cell map
		addPins(cells, blockCellX, blockCellY);

		for (int xi = startCell.getX(); xi <= endCell.getX(); xi++) {
			System.out.println(xi + " / " + xGrid.size());
			for (int yi = startCell.getY(); yi <= endCell.getY(); yi++) {
				matcher.match(pixels, xi, yi, xGrid, yGrid, image, cells);
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
		System.out.println("# Initial DRC Connections Count = " + ret);

		ret = drcDangling(cellsOut8, null);
		System.out.println("# Initial DRC Dangle Count = " + ret);

		ret = drcBridge(cellsOut8, null);
		System.out.println("# Initial DRC Bridge Count = " + ret);


		// Results onto output image
		annotateImage(image, cellsOut8, xGrid, yGrid);

		System.out.println("# Writing PNG");
		ImageIO.write(image, "png", dstFile);

	}

	private Cell[][] initCells(List<Integer> xGrid, List<Integer> yGrid) {
		Cell[][] cells = new Cell[yGrid.size() - 1][xGrid.size() - 1];
		for (int xi = 0; xi < xGrid.size() - 1; xi++) {
			for (int yi = 0; yi < yGrid.size() - 1; yi++) {
				cells[yi][xi] = new Cell();
			}
		}
		return cells;
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
					cell.clearRight();
					cell.setTop();
					break;
				case CS_EMITTER_5:
					cell.setConnections(0);
					break;
				case CS_EMITTER_6:
					cell.clearLeft();
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

	private void annotateImage(BufferedImage image, Cell[][] cells, List<Integer> xGrid, List<Integer> yGrid) {

		int lineColour = Pin.RED;
		int z = 8;

		for (int yi = 0; yi < cells.length; yi++) {
			for (int xi = 0; xi < cells[yi].length; xi++) {

				int x1 = xGrid.get(xi);
				int x2 = xGrid.get(xi + 1);
				int y1 = yGrid.get(yi);
				int y2 = yGrid.get(yi + 1);
				int w = x2 - x1;
				int h = y2 - y1;

				Cell cell = cells[yi][xi];

				Pin pin = cell.getPin();
				if (pin != null) {
					pin.plot(image, xGrid, yGrid, xi, yi, w, h, Pin.GREEN);
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
		for (int x : generateSequence(9, 10)) {
			cells[175][x].setPin(new Pin(0, 0, PinType.NORMAL));
		}
		for (int y : generateSequence(9, 11)) {
			cells[y][160].setPin(new Pin(0, 0, PinType.NORMAL));
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
		int n = correlation.length;
		List<Integer> grid = new ArrayList<Integer>();
		int i = startOffset;
		while (i <= endOffset) {
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
		if (i - endOffset < cellsize / 2) {
			grid.add(endOffset);
		}
		return grid;
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

	private void gridHistogram(int[][] pixels, XY startOffset, XY endOffset, int[] xTotals, int[] yTotals) {
		Arrays.fill(xTotals, 0);
		Arrays.fill(yTotals, 0);
		for (int y = startOffset.getY(); y < endOffset.getY(); y++) {
			for (int x = startOffset.getX(); x < endOffset.getX(); x++) {
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
			if (args.length != 3) {
				System.err.println("usage: java -jar ulamangling.jar <Grid PNG> <Match PNG> <Dst PNG> ");
				System.exit(1);
			}
			File gridFile = new File(args[0]);
			if (!gridFile.exists()) {
				System.err.println("Grid File: " + gridFile + " does not exist");
				System.exit(1);
			}
			if (!gridFile.isFile()) {
				System.err.println("Grid File: " + gridFile + " is not a file");
				System.exit(1);
			}
			File matchFile = new File(args[1]);
			if (!matchFile.exists()) {
				System.err.println("Match File: " + matchFile + " does not exist");
				System.exit(1);
			}
			if (!matchFile.isFile()) {
				System.err.println("Match File: " + matchFile + " is not a file");
				System.exit(1);
			}

			File dstFile = new File(args[2]);

			Process c = new Process();

			String name = gridFile.getName();
			name = name.substring(name.indexOf('_') + 1, name.lastIndexOf('.'));
			System.out.println("# name = " + name);

			c.convert(name, gridFile, matchFile, dstFile);

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
					if ((y > 0) && ((cellsIn[y - 1][x].getConnections() & 4) == 0)) {
						failCount++;
						cell.setConnectionsFail(true);
						cell.setConnections(cell.getConnections() & 14);
					}
				}
				// Check connectivity with cell to right
				if ((connections & 2) > 0) {
					if ((x < w - 1) && ((cellsIn[y][x + 1].getConnections() & 8) == 0)) {
						failCount++;
						cell.setConnectionsFail(true);
						cell.setConnections(cell.getConnections() & 13);
					}
				}

				// Check connectivity with cell below
				if ((connections & 4) > 0) {
					if ((y < h - 1) && ((cellsIn[y + 1][x].getConnections() & 1) == 0)) {
						failCount++;
						cell.setConnectionsFail(true);
						cell.setConnections(cell.getConnections() & 11);
					}
				}

				// Check connectivity with cell to left
				if ((connections & 8) > 0) {
					if ((x > 0) && ((cellsIn[y][x - 1].getConnections() & 2) == 0)) {
						failCount++;
						cell.setConnectionsFail(true);
						cell.setConnections(cell.getConnections() & 7);
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

				if (cellsOut != null) {
					cellsOut[y][x] = cell1;
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

					// Fix a dangling cell that is weakly connected to from above
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

					// Fix a dangling cell that is weakly connected to from below
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

					// Fix a dangling cell that is weakly connected to from right
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
