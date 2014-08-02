package com.hoglet.ulamangling;

import java.util.Arrays;

import com.hoglet.ulamangling.CellMatcher.Region;

public class Pattern {

	private Region[][] pattern;
	private int connections;

	public Pattern(Pattern from) {
		this.pattern = new Region[from.pattern.length][];
		for (int i = 0; i < from.pattern.length; i++) {
			this.pattern[i] = Arrays.copyOf(from.pattern[i], from.pattern[i].length);
		}
		this.connections = from.connections;
	}

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
