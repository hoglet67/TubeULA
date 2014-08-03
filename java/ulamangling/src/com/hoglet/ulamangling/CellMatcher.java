package com.hoglet.ulamangling;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.hoglet.ulamangling.Pin.PinType;

public class CellMatcher {

	private int d;
	private int c;
	private int l;
	private int csEmitter;
	private int delta;

	private List<Pattern> patterns = new ArrayList<Pattern>();
	private List<Pattern> patternsPlusPins = new ArrayList<Pattern>();
	private List<Pattern> patternsCsEmitter5 = new ArrayList<Pattern>();

	public enum Region {
		INIT, ZERO, HALF, ONE,
	};

	public CellMatcher(int dimension, int line, int corner, int csEmitter, int delta) {
		this.d = dimension;
		this.l = line;
		this.c = corner;
		this.csEmitter = csEmitter;
		this.delta = delta;
		initializePatterns();
		dumpPatterns(patterns);
		dumpPatterns(patternsPlusPins);
		dumpPatterns(patternsCsEmitter5);
	}

	private void initializePatterns() {
		Region[][] cell0 = getCell0();

		Region[][] cell1_0 = getCell1();
		Region[][] cell1_90 = rotateCell(cell1_0);
		Region[][] cell1_180 = rotateCell(cell1_90);
		Region[][] cell1_270 = rotateCell(cell1_180);

		Region[][] cell2a_0 = getCell2a();
		Region[][] cell2a_90 = rotateCell(cell2a_0);
		Region[][] cell2a_180 = rotateCell(cell2a_90);
		Region[][] cell2a_270 = rotateCell(cell2a_180);

		Region[][] cell2b_0 = getCell2b();
		Region[][] cell2b_90 = rotateCell(cell2b_0);

		Region[][] cell2c_0 = getCell2c();
		Region[][] cell2c_90 = rotateCell(cell2c_0);
		Region[][] cell2c_180 = rotateCell(cell2c_90);
		Region[][] cell2c_270 = rotateCell(cell2c_180);

		Region[][] cell3a_0 = getCell3a();
		Region[][] cell3a_90 = rotateCell(cell3a_0);
		Region[][] cell3a_180 = rotateCell(cell3a_90);
		Region[][] cell3a_270 = rotateCell(cell3a_180);

		Region[][] cell3b_0 = getCell3b();
		Region[][] cell3b_90 = rotateCell(cell3b_0);
		Region[][] cell3b_180 = rotateCell(cell3b_90);
		Region[][] cell3b_270 = rotateCell(cell3b_180);

		Region[][] cell4 = getCell4();

		patterns.add(new Pattern("cell0", cell0, 0));

		patterns.add(new Pattern("cell1_0", cell1_0, 1));
		patterns.add(new Pattern("cell1_90", cell1_90, 2));
		patterns.add(new Pattern("cell1_180", cell1_180, 4));
		patterns.add(new Pattern("cell1_270", cell1_270, 8));

		patterns.add(new Pattern("cell2a_0", cell2a_0, 3));
		patterns.add(new Pattern("cell2a_90", cell2a_90, 6));
		patterns.add(new Pattern("cell2a_180", cell2a_180, 12));
		patterns.add(new Pattern("cell2a_270", cell2a_270, 9));

		patterns.add(new Pattern("cell2b_0", cell2b_0, 5));
		patterns.add(new Pattern("cell2b_90", cell2b_90, 10));

		patterns.add(new Pattern("cell2c_0", cell2c_0, 3));
		patterns.add(new Pattern("cell2c_90", cell2c_90, 6));
		patterns.add(new Pattern("cell2c_180", cell2c_180, 12));
		patterns.add(new Pattern("cell2c_270", cell2c_270, 9));

		patterns.add(new Pattern("cell3a_0", cell3a_0, 7));
		patterns.add(new Pattern("cell3a_90", cell3a_90, 14));
		patterns.add(new Pattern("cell3a_180", cell3a_180, 13));
		patterns.add(new Pattern("cell3a_270", cell3a_270, 11));
		
		patterns.add(new Pattern("cell3b_0", cell3b_0, 7));
		patterns.add(new Pattern("cell3b_90", cell3b_90, 14));
		patterns.add(new Pattern("cell3b_180", cell3b_180, 13));
		patterns.add(new Pattern("cell3b_270", cell3b_270, 11));
		
		patterns.add(new Pattern("cell4", cell4, 15));
		
		// Deep copy as we are about to change the pattern
		patternsPlusPins = new ArrayList<Pattern>();
		for (Pattern p : patterns) {
			Pattern pPlusPins = new Pattern(p);
			pPlusPins.setName(p.getName() + "_with_pin");
			// TODO: More accurately draw the expected pin in the pattern
			rectangle(pPlusPins.getPattern(), d / 4, d / 4, d - d / 2, d - d / 2, Region.ONE);
			patternsPlusPins.add(pPlusPins);
		}
		
		
		patternsCsEmitter5 = new ArrayList<Pattern>();
		patternsCsEmitter5.add(new Pattern("getCsEmitter5Open", getCsEmitter5Open(), 0));
		patternsCsEmitter5.add(new Pattern("getCsEmitter5Closed", getCsEmitter5Closed(), 10));

	}

	public void dumpPatterns(List<Pattern> patterns) {
		for (Pattern pattern : patterns) {
			for (int y = 0; y < d; y++) {
				for (int x = 0; x < d; x++) {
					switch (pattern.getPattern()[y][x]) {
					case INIT:
						System.out.print("#");
						break;
					case ZERO:
						System.out.print(".");
						break;
					case HALF:
						System.out.print("-");
						break;
					case ONE:
						System.out.print("o");
						break;
					}
				}
				System.out.println();
			}
			System.out.println();
		}
	}

	private Region[][] getCell0() {
		Region[][] cell = new Region[d][d];
		rectangle(cell, 0, 0, d, d, Region.ZERO);
		return cell;
	}
	
	private Region[][] getCell1() {
		Region[][] cell = getCell0();
		rectangle(cell, c, 0, d - c - c, d - c, Region.ONE);
		rectangle(cell, c + l, 0, d - c - c - l - l, d - c - l, Region.HALF);
		return cell;
	}

	private Region[][] getCell2a() {
		Region[][] cell = getCell0();
		rectangle(cell, c, 0, d - c, d - c, Region.ONE);
		rectangle(cell, c + l, 0, d - c - l, d - c - l, Region.HALF);
		rectangle(cell, d - c - l, 0, c + l, c + l, Region.ONE);
		rectangle(cell, d - c, 0, c, c, Region.ZERO);
		return cell;
	}

	private Region[][] getCell2b() {
		Region[][] cell = getCell0();
		rectangle(cell, c, 0, d - c - c, d, Region.ONE);
		rectangle(cell, c + l, 0, d - c - c - l - l, d, Region.HALF);
		return cell;
	}

	private Region[][] getCell2c() {
		Region[][] cell = getCell2a();
		rectangle(cell, c, c, d - c - c, d - c - c, Region.ONE);
		rectangle(cell, c + l, c + l, d - c - c - l - l, d - c - c - l - l, Region.HALF);
		return cell;
	}

	private Region[][] getCell3a() {
		Region[][] cell = getCell0();
		rectangle(cell, c, 0, d - c, d, Region.ONE);
		rectangle(cell, c + l, 0, d - c - l, d, Region.HALF);
		rectangle(cell, d - c - l, 0, c + l, c + l, Region.ONE);
		rectangle(cell, d - c, 0, c, c, Region.ZERO);
		rectangle(cell, d - c - l, d - c - l, c + l, c + l, Region.ONE);
		rectangle(cell, d - c, d - c, c, c, Region.ZERO);
		return cell;
	}

	private Region[][] getCell3b() {
		Region[][] cell = getCell2c();
		rectangle(cell, c, d - c, d - c - c, c, Region.ONE);
		rectangle(cell, c + l, d - c, d - c - c - l - l, c, Region.HALF);
		return cell;
	}

	private Region[][] getCell4() {
		Region[][] cell = new Region[d][d];
		rectangle(cell, 0, 0, d, d, Region.HALF);
		// top left
		rectangle(cell, 0, 0, c + l, c + l, Region.ONE);
		rectangle(cell, 0, 0, c, c, Region.ZERO);
		// bottom left
		rectangle(cell, 0, d - c - l, c + l, c + l, Region.ONE);
		rectangle(cell, 0, d - c, c, c, Region.ZERO);
		// top right
		rectangle(cell, d - c - l, 0, c + l, c + l, Region.ONE);
		rectangle(cell, d - c, 0, c, c, Region.ZERO);
		// bottom right
		rectangle(cell, d - c - l, d - c - l, c + l, c + l, Region.ONE);
		rectangle(cell, d - c, d - c, c, c, Region.ZERO);
		return cell;
	}

	private Region[][] getCsEmitter5Open() {
		Region[][] cell = getCell0();
		rectangle(cell, 0, 0, csEmitter, d - c, Region.ONE);
		rectangle(cell, d - csEmitter, 0, csEmitter, d - c, Region.ONE);
		return cell;
	}
	private Region[][] getCsEmitter5Closed() {
		Region[][] cell = getCsEmitter5Open();
		rectangle(cell, csEmitter, c, d - csEmitter - csEmitter, d - c - c, Region.ONE);
		rectangle(cell, csEmitter, c + l, d - csEmitter - csEmitter, d - c - c - l - l, Region.HALF);
		return cell;
	}
	
	private Region[][] rotateCell(Region[][] in) {
		Region[][] cell = new Region[d][d];
		for (int x = 0; x < d; x++) {
			for (int y = 0; y < d; y++) {
				cell[y][x] = in[d - 1 - x][y];
			}
		}
		return cell;
	}

	private void rectangle(Region[][] cell, int x, int y, int w, int h, Region r) {
		for (int yi = y; yi < y + h; yi++) {
			for (int xi = x; xi < x + w; xi++) {
				cell[yi][xi] = r;
			}
		}
	}

	public void match(int[][] pixels, int xi, int yi, BufferedImage image, Cell[][] cells) {

		if (cells[yi][xi].getType() == PinType.CS_EMITTER_2) {
			return;
		}

		int h = pixels.length;
		int w = pixels[0].length;
		
		int x1 = cells[yi][xi].getX1();
		int x2 = cells[yi][xi].getX2();
		int y1 = cells[yi][xi].getY1();
		int y2 = cells[yi][xi].getY2();
		
		// System.out.println("x1=" + x1 + "; y1=" + y1 + "; x2=" + x2 + "; y2=" + y2);
		
		boolean log = false;
		
		Pattern bestPattern = null;
		int bestMad = Integer.MAX_VALUE;

		// If we know there is a pin in this cell, the use set of patterns that include pins
		// Note: We could be smarter here, because not all pins in the plot were blue
		boolean pinPresent = cells[yi][xi].getType() != PinType.NONE;
		
		List<Pattern> matchAgainst;
		if (cells[yi][xi].getType() == PinType.CS_EMITTER_5) {
			matchAgainst = patternsCsEmitter5;
		} else if (pinPresent) {
			matchAgainst = patternsPlusPins;
		} else {
			matchAgainst = patterns;
		}
		
		for (Pattern pattern : matchAgainst) {

			int dxMin = -delta;
			int dxMax = delta;
			int dyMin = -delta;
			int dyMax = delta;
			
			if (dxMin + x1 < 0) {
				dxMin = x1;
			}
			if (dxMax + x2 > image.getWidth()) {
				dxMax = image.getWidth() - x2;
			}
			if (dyMin + y1 < 0) {
				dyMin = y1;
			}
			if (dyMax + y2 > image.getHeight()) {
				dyMax = image.getHeight() - y2;
			}

			// System.out.println("dxmin=" + dxMin + "; dymin=" + dyMin + "; dxmax" + dxMax + "; dymax=" + dyMax);

			for (int dy = dyMin; dy <= dyMax; dy++) {
				for (int dx = dxMin; dx <= dxMax; dx++) {
					
					int countZero = 0;
					int countHalf = 0;
					int countOne = 0;
					int totalZero = 0;
					int totalHalf = 0;
					int totalOne = 0;

					for (int y = 0; y < d && y1 + dy + y < h; y++) {
						int[] row = pixels[y1 + dy + y];
						
						for (int x = 0; x < d && x1 + dx + x < w; x++) {
							int pixel = row[x1 + dx + x] & 0xffffff;
							
//							boolean isWhite = pixel == 0xffffff;
//							boolean isBlue = pixel == 0x0000ff;
//							boolean isBlack = pixel == 0x000000;
//							if (!isWhite && !isBlack && !isBlue) {
//								throw new RuntimeException("Unexpected pixel: " + Integer.toHexString(pixel));
//							}
							
							switch (pattern.getPattern()[y][x]) {
							case INIT:
								throw new RuntimeException();
							case ZERO:
								if (pixel == 0x0000ff) {
									countZero++;
								}
								break;
							case HALF:
								if (pixel  == 0x0000ff) {
									countHalf++;
								}
								totalHalf++;
								break;
							case ONE:
								if (pixel  == 0x0000ff || pixel == 0x000000) {
									countOne++;
								}
								totalOne++;
								break;
							}
						}
					}

					int mad = (totalOne - countOne) + (countZero) + Math.abs(2 * totalHalf / 15 - countHalf);

					if (log) {
						System.out.println("Pattern=" + pattern + "; dx=" + dx + "; dy=" + dy + "; CZ=" + countZero + "; CH=" + countHalf
								+ "; CO=" + countOne + "; TZ=" + totalZero + "; TH=" + totalHalf + "; TO=" + totalOne + "; mad="
								+ mad);
					}

					if (mad < bestMad) {
						bestMad = mad;
						bestPattern = pattern;
					}

				}
			}
		}

		if (log) {
			System.out.println("Matched pattern " + bestPattern);
		}

		cells[yi][xi].setConnections(bestPattern.getConnections());

		return;

	}

}
