package com.hoglet.ulamangling;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class Component {

	private NetList netlist;
	
	private String type;
	private String id;
	private Map<String, Collection<String>> inputs;
	private Map<String, String> outputs;
	
	public Component (NetList netlist, String type, String id) {
		this.netlist = netlist;
		this.type = type;
		this.id = id;
		this.inputs = new TreeMap<String, Collection<String>>();
		this.outputs = new TreeMap<String, String>();
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void addInput(String name, String net) {
		Collection<String> inputNets = inputs.get(name);
		if (inputNets == null) {
			inputNets = new TreeSet<String>();
			inputs.put(name, inputNets);
		}
//		if (inputNets.contains(net)) {
//			throw new RuntimeException("Component input " + name + " net " + net + " is already present: " + toString());
//		}
		inputNets.add(net);
	}

	public int numInputs() {
		int count = 0;
		for (Collection<String> input : inputs.values()) {
			count += input.size();
		}
		return count;
	}

	public int numOutputs() {
		return outputs.size();
	}

	public void addOutput(String name, String net) {
		if (outputs.containsKey(net)) {
			throw new RuntimeException("Component output " + name + " is already connected:" + toString());
		}
		outputs.put(name, net);
		netlist.addComponentOutput(net, this);
	}

	public Collection<String> getInputs(String name) {
		return inputs.get(name);
	}


	public Collection<String> getInputs() {
		if (inputs.size() == 0) {
			throw new RuntimeException("Component does not have any inputs");
		} else if (inputs.size() > 1) {
			throw new RuntimeException("Component has multiple input types");
		} else {
			return inputs.get(inputs.keySet().iterator().next());
		}
	}

	public String getOutput(String name) {
		return outputs.get(name);
	}

	public String getOutput() {
		if (outputs.size() == 0) {
			throw new RuntimeException("Component does not have any outputs");
		} else if (outputs.size() > 1) {
			throw new RuntimeException("Component has multiple output nets");
		} else {
			return outputs.values().iterator().next();
		}
	}

	public Collection<String> getOutputs() {
		return outputs.values();
	}
	
	
	public String toString() {
		return toStringNew();
	}
	
	public String toStringNew() {
		String component = type + " " + id + "(";
		boolean first = true;
		// Outputs
		for (Map.Entry<String, String> output : outputs.entrySet()) {
			if (first) {
				first = false;
			} else {
				component += ",";
			}
			component += output.getKey() + "=>" + output.getValue();
		}
		// Inputs
		for (Map.Entry<String, Collection<String>> input : inputs.entrySet()) {
			for (String value : input.getValue()) {
				component += ",";
				component += input.getKey() + "=>" + value;
			}
		}
		component += ");";
		return component;
		
	}

//	public String toStringOld() {
//		String component = getOutput() + " = " + type + "(";
//		boolean first = true;
//		for (String input : getInputs()) {
//			if (first) {
//				first = false;
//			} else {
//				component += ", ";
//			}
//			component += input;
//		}
//		component += ");";
//		return component;
//	}

}
