package com.hoglet.ulamangling;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class Component {
	
	public static final String TYPE_INPUT = "INPUT";
	public static final String TYPE_OUTPUT = "OUTPUT";
	public static final String TYPE_NOR = "nor";
	public static final String TYPE_LATCH = "d_latch";
	public static final String TYPE_SR = "sr_latch";

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
		inputNets.add(net);
		netlist.addComponentInput(net, this);
	}

	public void removeInput(String name, String net) {
		if (!inputs.containsKey(name)) {
			throw new RuntimeException("Component input " + name + " is missing or not connected:" + toString());
		}
		Collection<String> inputNets = inputs.get(name);		
		inputNets.remove(net);
		netlist.removeComponentInput(net, this);
	}

	
	public void addOutput(String name, String net) {
		if (outputs.containsKey(name)) {
			throw new RuntimeException("Component output " + name + " is already connected:" + toString());
		}
		outputs.put(name, net);
		netlist.addComponentOutput(net, this);
	}

	public void removeOutput(String name, String net) {
		if (!outputs.containsKey(name)) {
			throw new RuntimeException("Component output " + name + " is missing or not connected:" + toString());
		}
		outputs.remove(name);
		netlist.removeComponentOutput(net, this);
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

	public Collection<String> getInputs(String name) {
		return inputs.get(name);
	}


	public Collection<String> getInputs() {
		if (inputs.size() == 0) {
			return new TreeSet<String>();
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
			return null;
		} else if (outputs.size() > 1) {
			throw new RuntimeException("Component has multiple output nets");
		} else {
			return outputs.values().iterator().next();
		}
	}

	public Collection<String> getOutputs() {
		return outputs.values();
	}
	
	public Collection<Map.Entry<String, Collection<String>>> getNamedInputs() {
		return inputs.entrySet();
	}

	public Collection<Map.Entry<String, String>> getNamedOutputs() {
		return outputs.entrySet();
	}
	
	public String toString() {
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
				if (first) {
					first = false;
				} else {
					component += ",";
				}
				component += input.getKey() + "=>" + value;
			}
		}
		component += ");";
		return component;		
	}

	public String toVerilog() {
		boolean isPrimitive = type.equals(Component.TYPE_NOR);
		String delay = isPrimitive ? "#5 " : "";
		String component = type + " " + delay + id + "(";
		boolean first = true;
		// Outputs
		for (Map.Entry<String, String> output : outputs.entrySet()) {
			if (first) {
				first = false;
			} else {
				component += ",";
			}
			if (isPrimitive) {
				component += output.getValue();
			} else {
				component += "." + output.getKey() + "(" + output.getValue() + ")";				
			}
		}
		// Inputs
		for (Map.Entry<String, Collection<String>> input : inputs.entrySet()) {
			for (String value : input.getValue()) {
				if (first) {
					first = false;
				} else {
					component += ",";
				}
				if (isPrimitive) {
					component += value;
				} else {
					component += "." + input.getKey() + "(" + value + ")";
				}
			}
		}
		component += ");";
		return component;		
	}

	public void setNetlist(NetList netlist) {
		this.netlist = netlist;
	}

}
