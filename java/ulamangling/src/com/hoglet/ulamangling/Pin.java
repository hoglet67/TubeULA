package com.hoglet.ulamangling;

import java.awt.image.BufferedImage;

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
		RES,
		TR_EMITTER,
		TR_BASE,
		IO,
		IO_IN,
		IO_OUT,
		LINK_L,
		LINK_R,
		LINK_T,
		LINK_B,
		UNDER_1,
		UNDER_2,
		UNDER_3,
		UNDER_4,
		UNDER_5,
		UNDER_6,
		UNDER_7,
		UNDER_8,
		UNDER_9,
		UNDER_10,
		UNDER_11,
		VSS,
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

	private String name;

	public Pin(int x, int y, PinType type) {
		this(x, y, type, null);
	}

	public Pin(int x, int y, PinType type, String name) {
		super(x, y);
		this.type = type;
		this.name = name;
	}
	
	public PinType getType() {
		return type;
	}
	
	public boolean isVss() {
		return type == PinType.VSS;
	}
	
	public boolean isGnd() {
		return type == PinType.CS_GND_1 || type == PinType.CS_GND_2 ||  type == PinType.CS_GND_3; 
	}

	public boolean isLink() {
		return type == PinType.LINK_L || type == PinType.LINK_R ||
			   type == PinType.LINK_T || type == PinType.LINK_B ; 
	}

	public boolean isResistor() {
		return type == PinType.RES;
	}
	
	public boolean isTransistor() {
		return type == PinType.TR_BASE || type == PinType.TR_EMITTER ||
				type == PinType.UNDER_2 || type == PinType.UNDER_5 || type == PinType.UNDER_7 ||
				type == PinType.UNDER_8 || type == PinType.UNDER_11;
	}

	public boolean isCurrentSource() {
		return type == PinType.CS_EMITTER_1 || type == PinType.CS_EMITTER_1 || type == PinType.CS_EMITTER_1 ||
				type == PinType.CS_EMITTER_4 || type == PinType.CS_EMITTER_5 || type == PinType.CS_EMITTER_6 ||
				type == PinType.CS_BASE_1 || type == PinType.CS_BASE_2 || type == PinType.CS_BASE_3;
	}

	public void plot(BufferedImage image, int px, int py, int w, int h, int rgb) {
		
		px += 1;
		py += 1;
		
		switch (type) {
		case NONE:
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
		default:
			rectangle(image, px + w / 4, py + h / 4, w / 2, h / 2, rgb);
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		switch (type) {
		case RES:
			return "R";
		case IO:
		case IO_IN:
			return "I";
		case IO_OUT:
			return "O";
		case LINK_L:
			return "<";
		case LINK_R:
			return ">";
		case LINK_T:
			return "^";
		case LINK_B:
			return "V";
		case UNDER_1:
		case UNDER_2:
		case UNDER_3:
		case UNDER_4:
		case UNDER_5:
		case UNDER_6:
		case UNDER_7:
		case UNDER_8:
		case UNDER_9:
		case UNDER_10:
		case UNDER_11:
			return "U";
		case VSS:
			return "1";
		case TR_BASE:
		case CS_BASE_1:
		case CS_BASE_2:
		case CS_BASE_3:
			return "B";
		case TR_EMITTER:
		case CS_EMITTER_1:
		case CS_EMITTER_2:
		case CS_EMITTER_3:
		case CS_EMITTER_4:
		case CS_EMITTER_5:
		case CS_EMITTER_6:
			return "E";
		case CS_COLLECTOR_1:
		case CS_COLLECTOR_2:
		case CS_COLLECTOR_3:
			return "C";
		case CS_GND_1:
		case CS_GND_2:
		case CS_GND_3:
			return "0";
		default:
			return " ";
		}
	}
	
	

}
