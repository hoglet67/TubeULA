package com.hoglet.ulamangling;

@SuppressWarnings("serial")
public class MergeException extends Exception {

	int x0;
	int y0;
	int x1;
	int y1;
	
	public MergeException(String msg, int x0, int y0, int x1, int y1) {
		super(msg + " at " + x0 + "," + y0 + "; " + x1 + "," + y1);
		this.x0 = x0;
		this.y0 = y0;
		this.x1 = x1;
		this.y1 = y1;
	}
	
}
