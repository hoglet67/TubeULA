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
		NONE,
		NORMAL,
		CS_EMITTER_1,
		CS_EMITTER_2,
		CS_EMITTER_3,
		CS_EMITTER_4,
		CS_EMITTER_5,
		CS_EMITTER_6,
		CS_BASE_1,
		CS_BASE_2,
		CS_BASE_3,
		CS_COLLECTOR_1,
		CS_COLLECTOR_2,
		CS_COLLECTOR_3,
		CS_GND_1,
		CS_GND_2,
		CS_GND_3
	};

	private PinType type;
	
	public Pin(int x, int y, PinType type) {
		super(x, y);
		this.type = type;
	}
	
	public PinType getType() {
		return type;
	}
	
	public void plot(BufferedImage image, List<Integer> xGrid, List<Integer> yGrid, int cellX, int cellY, int w, int h, int rgb) {
		
		int px = xGrid.get(cellX) + 1;
		int py = yGrid.get(cellY) + 1;
		
		switch (type) {
		case NONE:
			break;
		case NORMAL:
		case CS_BASE_1:
		case CS_BASE_3:
			rectangle(image, px + w / 4, py + h / 4, w / 2, h / 2, rgb);
			break;
		case CS_EMITTER_1:
			rectangle(image, px + w / 4, py + h / 2, w * 3 / 4, h / 2, rgb);
			break;
		case CS_EMITTER_2:
			rectangle(image, px, py + h / 2, w / 4, h / 2, rgb);
			rectangle(image, px + w * 3 / 4, py + h / 2, w / 4, h / 2, rgb);
			break;
		case CS_EMITTER_3:
			rectangle(image, px, py + h / 2, w * 3 / 4, h / 2, rgb);
			break;
		case CS_EMITTER_4:
			rectangle(image, px + w / 4, py, w * 3 / 4, h * 3 / 4, rgb);
			break;
		case CS_EMITTER_5:
			rectangle(image, px, py, w / 4, h * 3 / 4, rgb);
			rectangle(image, px + w * 3 / 4, py, w / 4, h * 3 / 4, rgb);
			break;
		case CS_EMITTER_6:
			rectangle(image, px, py, w * 3 / 4, h * 3 / 4, rgb);
			break;
		case CS_BASE_2:
			rectangle(image, px , py + h / 4, w, h / 2, rgb);
			break;
		case CS_COLLECTOR_1:
			rectangle(image, px + w / 4 , py + h / 4, w * 4 / 4, h / 2, rgb);
			break;
		case CS_COLLECTOR_2:
			rectangle(image, px , py + h / 4, w, h / 2, rgb);
			break;
		case CS_COLLECTOR_3:
			rectangle(image, px , py + h / 4, w * 3 / 4, h / 2, rgb);
			break;
		case CS_GND_1:
			rectangle(image, px + w / 4 , py, w * 3 / 4, h * 3 / 4, rgb);
			break;
		case CS_GND_2:
			rectangle(image, px, py, w, h * 3 / 4, rgb);
			break;
		case CS_GND_3:
			rectangle(image, px , py, w * 3 / 4, h * 3 / 4, rgb);
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
