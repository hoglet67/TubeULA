package com.hoglet.ulamangling;

import com.hoglet.ulamangling.Pin.PinType;

public class Cell {

	private static final int C_TOP = 1;
	private static final int C_RIGHT = 2;
	private static final int C_BOTTOM = 4;
	private static final int C_LEFT = 8;
	private static final int C_ALL = C_TOP + C_RIGHT + C_BOTTOM + C_LEFT;

	private int connections;
	private Pin pin;
	private boolean connectionsFail;
	private boolean dangleFail;
	private boolean bridgeFail;
	private boolean highlight;
	private int x1;
	private int y1;
	private int x2;
	private int y2;
	

	public Cell() {
		this.connections = 0;
		this.pin = null;
		this.connectionsFail = false;
		this.dangleFail = false;
		this.bridgeFail = false;
		this.highlight = false;
	}

	public Cell(Cell from) {
		this.connections = from.connections;
		this.pin = from.pin;
		this.connectionsFail = from.connectionsFail;
		this.dangleFail = from.dangleFail;
		this.bridgeFail = from.bridgeFail;
		this.highlight = from.highlight;
		this.x1 = from.x1;
		this.x2 = from.x2;
		this.y1 = from.y1;
		this.y2 = from.y2;
	}

	public int getConnections() {
		return connections;
	}

	public void setConnections(int connections) {
		this.connections = connections;
	}

	public Pin getPin() {
		return pin;
	}

	public void setPin(Pin pin) {
		this.pin = pin;
	}

	public boolean isConnectionsFail() {
		return connectionsFail;
	}

	public void setConnectionsFail(boolean connectionsFail) {
		this.connectionsFail = connectionsFail;
	}

	public boolean isDangleFail() {
		return dangleFail;
	}

	public void setDangleFail(boolean dangleFail) {
		this.highlight |= dangleFail;
		this.dangleFail = dangleFail;
	}

	public boolean isBridgeFail() {
		return bridgeFail;
	}

	public void setBridgeFail(boolean bridgeFail) {
		this.highlight |= bridgeFail;
		this.bridgeFail = bridgeFail;
	}

	public PinType getType() {
		if (pin == null) {
			return PinType.NONE;
		} else {
			return pin.getType();
		}
	}

	public boolean isEnd() {
		return connections == C_TOP || connections == C_RIGHT || connections == C_BOTTOM || connections == C_LEFT;
	}

	public boolean isTop() {
		return (connections & C_TOP) > 0;
	}

	public void setTop() {
		connections |= C_TOP;
	}

	public void clearTop() {
		connections &= (C_ALL - C_TOP);
	}

	public boolean isRight() {
		return (connections & C_RIGHT) > 0;
	}

	public void setRight() {
		connections |= C_RIGHT;
	}

	public void clearRight() {
		connections &= (C_ALL - C_RIGHT);
	}

	public boolean isBottom() {
		return (connections & C_BOTTOM) > 0;
	}

	public void setBottom() {
		connections |= C_BOTTOM;
	}

	public void clearBottom() {
		connections &= (C_ALL - C_BOTTOM);
	}

	public boolean isLeft() {
		return (connections & C_LEFT) > 0;
	}

	public void setLeft() {
		connections |= C_LEFT;
	}

	public void clearLeft() {
		connections &= (C_ALL - C_LEFT);
	}

	public boolean isHighlight() {
		return highlight;
	}

	public int getX1() {
		return x1;
	}

	public void setX1(int x1) {
		this.x1 = x1;
	}

	public int getY1() {
		return y1;
	}

	public void setY1(int y1) {
		this.y1 = y1;
	}

	public int getX2() {
		return x2;
	}

	public void setX2(int x2) {
		this.x2 = x2;
	}

	public int getY2() {
		return y2;
	}

	public void setY2(int y2) {
		this.y2 = y2;
	}

}
