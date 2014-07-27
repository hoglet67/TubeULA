package com.hoglet.ulamangling;

import java.awt.image.BufferedImage;
import java.util.List;

public class Pin extends XY {
	
	public static final int BLACK   = 0xff000000;
	public static final int BLUE    = 0xff0000ff;
	public static final int GREEN   = 0xff00ff00;
	public static final int RED     = 0xffff0000;
	public static final int CYAN    = 0xff00ffff;
	public static final int YELLOW  = 0xffffff00;
	public static final int MAGENTA = 0xffff00ff;
	public static final int WHITE   = 0xffffffff;
	
	public enum PinType {
		NORMAL,
		CS_EMITTER_L,
		CS_EMITTER_R,
		CS_BASE,
		CS_COLLECTOR
	};

	private PinType type;
	
	public Pin(int x, int y, PinType type) {
		super(x, y);
		this.type = type;
	}
	
	public void plot(BufferedImage image, List<Integer> xGrid, List<Integer> yGrid, int cellX, int cellY, int cellsize, int rgb) {
		
		int px = xGrid.get(cellX + getX()) + 1;
		int py = yGrid.get(cellY + getY()) + 1;
		
		switch (type) {
		case NORMAL:
			rectangle(image, px + cellsize / 4, py + cellsize / 4, cellsize / 2, cellsize / 2, rgb);
			break;
		case CS_EMITTER_L:
			rectangle(image, px + cellsize / 4, py + cellsize / 2, cellsize, cellsize, rgb);
			break;
		case CS_EMITTER_R:
			rectangle(image, px + cellsize * 3 / 4, py + cellsize / 2, cellsize, cellsize, rgb);
			break;
		case CS_BASE:
			rectangle(image, px , py + cellsize / 4, cellsize, cellsize / 2, rgb);
			break;
		case CS_COLLECTOR:
			rectangle(image, px + cellsize / 4 , py + cellsize / 4, cellsize * 5 / 2, cellsize / 2, rgb);
			break;
			
		}		
	}

	public static void rectangle(BufferedImage image, int x, int y, int w, int h, int rgb) {
		rectangle(image, x, y, w, h, rgb, false);
	}

	public static void rectangle(BufferedImage image, int x, int y, int w, int h, int rgb, boolean and) {
		for (int xi = x; xi < x + w; xi++) {
			for (int yi = y; yi < y + h; yi++) {
				try {
					int val = and ? (image.getRGB(xi, yi) & rgb) : rgb;
					image.setRGB(xi, yi, val);
				} catch (Throwable e) {
					System.out.println(" x = " + xi + "; y = " + yi);
					throw new RuntimeException(e);
				}
			}
		}
	}

	
	

}
