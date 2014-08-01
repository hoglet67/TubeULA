package com.hoglet.ulamangling;

import com.hoglet.ulamangling.CellMatcher.Region;

public class Pattern {

	private Region[][] pattern;
	private int connections;

	public Pattern(Region[][] pattern, int connections) {
		this.pattern = pattern;
		this.connections = connections;
	}

	public Region[][] getPattern() {
		return pattern;
	}

	public void setPattern(Region[][] pattern) {
		this.pattern = pattern;
	}

	public int getConnections() {
		return connections;
	}

	public void setConnections(int connections) {
		this.connections = connections;
	}

}
