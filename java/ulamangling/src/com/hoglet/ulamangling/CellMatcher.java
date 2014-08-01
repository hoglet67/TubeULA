package com.hoglet.ulamangling;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class CellMatcher {

	private int d;
	private int c;
	private int l;

	private List<Pattern> patterns = new ArrayList<Pattern>();

	public enum Region {
		INIT, ZERO, HALF, ONE,
	};

	public CellMatcher(int dimension, int line, int corner) {
		this.d = dimension;
		this.l = line;
		this.c = corner;
		initializePatterns();
		dumpPatterns();
	}

	private void initializePatterns() {
		Region[][] cell0a = getCell0a();
		Region[][] cell0b = getCell0b();

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

		patterns.add(new Pattern(cell0a, 0));
		patterns.add(new Pattern(cell0b, 0));

		patterns.add(new Pattern(cell1_0, 1));
		patterns.add(new Pattern(cell1_90, 2));
		patterns.add(new Pattern(cell1_180, 4));
		patterns.add(new Pattern(cell1_270, 8));

		patterns.add(new Pattern(cell2a_0, 3));
		patterns.add(new Pattern(cell2a_90, 6));
		patterns.add(new Pattern(cell2a_180, 12));
		patterns.add(new Pattern(cell2a_270, 9));

		patterns.add(new Pattern(cell2b_0, 5));
		patterns.add(new Pattern(cell2b_90, 10));

		patterns.add(new Pattern(cell2c_0, 3));
		patterns.add(new Pattern(cell2c_90, 6));
		patterns.add(new Pattern(cell2c_180, 12));
		patterns.add(new Pattern(cell2c_270, 9));

		patterns.add(new Pattern(cell3a_0, 7));
		patterns.add(new Pattern(cell3a_90, 14));
		patterns.add(new Pattern(cell3a_180, 13));
		patterns.add(new Pattern(cell3a_270, 11));
		
		patterns.add(new Pattern(cell3b_0, 7));
		patterns.add(new Pattern(cell3b_90, 14));
		patterns.add(new Pattern(cell3b_180, 13));
		patterns.add(new Pattern(cell3b_270, 11));
		
		patterns.add(new Pattern(cell4, 15));
	}

	public void dumpPatterns() {
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

	private Region[][] getCell0a() {
		Region[][] cell = new Region[d][d];
		rectangle(cell, 0, 0, d, d, Region.ZERO);
		return cell;
	}

	private Region[][] getCell0b() {
		Region[][] cell = getCell0a();
		rectangle(cell, d / 4, d / 4, d - d / 2, d - d / 2, Region.ONE);
		return cell;
	}
	
	private Region[][] getCell1() {
		Region[][] cell = getCell0a();
		rectangle(cell, c, 0, d - c - c, d - c, Region.ONE);
		rectangle(cell, c + l, 0, d - c - c - l - l, d - c - l, Region.HALF);
		return cell;
	}

	private Region[][] getCell2a() {
		Region[][] cell = getCell0a();
		rectangle(cell, c, 0, d - c, d - c, Region.ONE);
		rectangle(cell, c + l, 0, d - c - l, d - c - l, Region.HALF);
		rectangle(cell, d - c - l, 0, c + l, c + l, Region.ONE);
		rectangle(cell, d - c, 0, c, c, Region.ZERO);
		return cell;
	}

	private Region[][] getCell2b() {
		Region[][] cell = getCell0a();
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
		Region[][] cell = getCell0a();
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

	public int match(int[][] pixels, int x1, int y1, int x2, int y2, BufferedImage image, boolean log) {

		Pattern bestPattern = null;
		int bestMad = Integer.MAX_VALUE;

		int bdx = 0;
		int bdy = 0;
		
		for (Pattern pattern : patterns) {

			int delta = 3;
			int dxMin = -delta;
			int dxMax = delta;
			int dyMin = -delta;
			int dyMax = delta;
			
			if (dxMin + x1 < 0) {
				dxMin = x1;
			}
			if (dxMax + x2 >= image.getWidth()) {
				dxMax = image.getWidth() - x2 - 1;
			}
			if (dyMin + y1 < 0) {
				dyMin = y1;
			}
			if (dyMax + y2 >= image.getHeight()) {
				dyMax = image.getHeight() - y2 - 1;
			}

			
			for (int dy = dyMin; dy <= dyMax; dy++) {
				for (int dx = dxMin; dx <= dxMax; dx++) {
					
					int countZero = 0;
					int countHalf = 0;
					int countOne = 0;
					int totalZero = 0;
					int totalHalf = 0;
					int totalOne = 0;

					for (int y = 0; y < d; y++) {
						for (int x = 0; x < d; x++) {
							// white = 0xffffff
							// blue = 0xff0000
							// black = 0x000000
							int pixel = (pixels[y1 + dy + y][x1 + dx + x] & 0xffffff);
							boolean isWhite = pixel == 0xffffff;
							boolean isBlue = pixel  == 0x0000ff;
							boolean isBlack = pixel == 0x000000;
							
							if (!isWhite && !isBlack && !isBlue) {
								throw new RuntimeException("Unexpected pixel: " + Integer.toHexString(pixel));
							}
							switch (pattern.getPattern()[y][x]) {
							case INIT:
								throw new RuntimeException();
							case ZERO:
								if (isBlue) {
									countZero++;
								}
								break;
							case HALF:
								if (isBlue) {
									countHalf++;
								}
								totalHalf++;
								break;
							case ONE:
								if (isBlue || isBlack) {
									countOne++;
								}
								totalOne++;
								break;
							}
						}
					}

					int mad = (totalOne - countOne) + (countZero) + Math.abs(2 * totalHalf / 15 - countHalf);

					if (log) {
						System.out.println("Pattern=" + pattern.getConnections() + "; CZ=" + countZero + "; CH=" + countHalf
								+ "; CO=" + countOne + "; TZ=" + totalZero + "; TH=" + totalHalf + "; TO=" + totalOne + "; mad="
								+ mad);
					}

					if (mad < bestMad) {
						bestMad = mad;
						bestPattern = pattern;
						bdx = dx;
						bdy = dy;
					}

				}
			}
		}

		if (log) {
			System.out.println("Matched pattern " + bestPattern);
		}

		int rgb = Pin.RED;
		int z = 8;
		int cellsize = d - 1;

		bdx = 0;
		bdy = 0;
		
		int connections = bestPattern.getConnections();
		// top
		if ((connections & 1) > 0) {
			Pin.rectangle(image, x1 + bdx + (cellsize - z) / 2 + 1, y1 + bdy + 1, z, (cellsize + z) / 2, rgb, false);
		}
		// right
		if ((connections & 2) > 0) {
			Pin.rectangle(image, x2 + bdx - (cellsize + z) / 2 + 1, y1 + bdy + (cellsize - z) / 2 + 1, (cellsize + z) / 2, z, rgb, false);
		}
		// bottom
		if ((connections & 4) > 0) {
			Pin.rectangle(image, x1 + bdx + (cellsize - z) / 2 + 1, y2 + bdy - (cellsize + z) / 2 + 1, z, (cellsize + z) / 2, rgb, false);
		}
		// left
		if ((connections & 8) > 0) {
			Pin.rectangle(image, x1 + bdx + 1, y1 + bdy + (cellsize - z) / 2 + 1, (cellsize + z) / 2, z, rgb, false);
		}

		return connections;

	}

	public static final void main(String[] args) {
		CellMatcher matcher = new CellMatcher(41, 4, 4);
		matcher.dumpPatterns();
	}

}
