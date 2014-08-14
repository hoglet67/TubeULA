package com.hoglet.ulamangling;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class NetList {

	// Component ID -> Component
	Map<String, Component> componentMap = new TreeMap<String, Component>();
	
	// Net -> Component that have the net as an input
	Map<String, Collection<Component>> inputMap = new TreeMap<String, Collection<Component>>();

	// Net -> Component Driving Net
	Map<String, Component> outputMap = new TreeMap<String, Component>();

	public NetList() {
	}
	
	private NetList shallowCopy() {
		NetList netlist = new NetList();
		netlist.componentMap.putAll(this.componentMap);
		netlist.outputMap.putAll(this.outputMap);
		netlist.inputMap.putAll(this.inputMap);
		return netlist;
	}
	
	public Component createComponent(String type, String id) {
		if (componentMap.containsKey(id)) {
			throw new RuntimeException("Netlist already contains " + id);
		}
		Component component = new Component(this, type, id);
		componentMap.put(id, component);
		return component;
	}

	protected void addComponentInput(String net, Component component) {
		Collection<Component> inputs = inputMap.get(net);
		if (inputs == null) {
			inputs = new HashSet<Component>();
			inputMap.put(net, inputs);
		}
		inputs.add(component);
	}

	protected void addComponentOutput(String net, Component component) {
		outputMap.put(net, component);
	}

	protected void removeComponentOutput(String net, Component component) {
		outputMap.remove(net);
	}

	public Component getSource(String net) {
		return outputMap.get(net);
	}
	
	public Collection<Component> getDestinations(String net) {
		return inputMap.get(net);
	}
	
	public Component get(String id) {
		return componentMap.get(id);
	}
	
	public Collection<Component> getAll() {
		return componentMap.values();
	}
	
	public void delete(String id) {
		delete(componentMap.get(id));
	}
	
	public void delete(Component component) {
		for (String input : component.getInputs()) {
			inputMap.get(input).remove(component);
		}
		for (String output : component.getOutputs()) {
			outputMap.remove(output);
		}
		componentMap.remove(component.getId());
	}


	/**
	 * Output the components in the netlist
	 */
	public void dump() {
		for (Component component : componentMap.values()) {
			System.out.println(component);
		}
	}

	
	/**
	 * Output the distribution of components in the netlist
	 */
	public void dumpStats() {
		Map<String, Integer> dist = new TreeMap<String, Integer>();
		for (Component component : componentMap.values()) {
			String key = component.getType() + "_" + component.numInputs() + "_" + component.numOutputs();
			Integer count = dist.get(key);
			if (count == null) {
				count = 0;
			}
			count++;
			dist.put(key, count);
		}
		for (Map.Entry<String, Integer> entry : dist.entrySet()) {
			System.out.println(entry.getKey() + "\t" + entry.getValue());
		}
	}
	
	
	public void checkFromSelfCoupledGates() {
		// Look for gates that feed back to themselves
		int selfCoupledCount= 0;
		for (Component gate1 : this.getAll()) {
			if (gate1.getOutput() == null) {
				continue;
			}
			String output1 = gate1.getOutput();
			Collection<String> inputs1 = gate1.getInputs();
			if (inputs1.contains(output1)) {
				System.out.println("Self coupled gate " + output1);
				selfCoupledCount++;
			}
		}
		System.out.println("Found a total of " + selfCoupledCount + " self coupled gates");
	}
	
	public NetList pruneUnconnectedOutputs() {
		NetList copy = this.shallowCopy();
		for (Component component : this.getAll()) {
			Map<String, String> toRemove = new HashMap<String, String>();
			for (Map.Entry<String, String> entry : component.getNamedOutputs()) {
				String name = entry.getKey();
				String net = entry.getValue();
				Collection<Component> inputs = inputMap.get(net);
				if (inputs == null || inputs.size() == 0) {
					toRemove.put(name, net);
				}
			}
			// Delay actual deletion to avoid a concurrent modification exception
			for (Map.Entry<String, String> entry : toRemove.entrySet()) {
				String name = entry.getKey();
				String net = entry.getValue();
				System.out.println("Removing unused output " + name + " from " + component.getId());
				component.removeOutput(name, net);
			}
		}
		return copy;
	}	
	
	public NetList replaceWithLatches() {
		int latchnum = 0;
		// Look for cross coupled gates
		
		NetList copy = this.shallowCopy();

		int crossCoupledCount = 0;
		for (Component gate1 : this.getAll()) {
			String output1 = gate1.getOutput();
			if (gate1.getOutput() == null) {
				continue;
			}
			Collection<String> inputs1 = gate1.getInputs();
			if (inputs1.contains(output1)) {
				System.out.println("Skipping self coupled gate: " + output1);
				continue;
			}
			if (inputs1.size() > 2) {
				System.out.println("Skipping gate with > 2 inputs: " + output1);
				continue;
			}
			for (String output2 : inputs1) {
				Component gate2 = this.getSource(output2);
				if (gate2 == null) {
					continue;
				}
				Collection<String> inputs2 = gate2.getInputs();
				if (inputs2.size() > 2) {
					System.out.println("Skipping gate with > 2 inputs: " + output1);
					continue;
				}
				if (inputs2.contains(output1)) {
					
					System.out.println("Cross coupled gate pair: "
							+ output1 + "(" + inputs1.size() + ") and "
							+ output2 + "(" + inputs2.size() + ")");
					crossCoupledCount++;

					// See if we can trace back to a latch

					int latchCount = 0;
					for (String driver1 : inputs1) {
						if (driver1.equals(output2)) {
							continue;
						}
						Component gateDriver1 = this.getSource(driver1);
						if (gateDriver1 == null) {
							System.err.println("No driver for " + driver1);
							continue;
						}
						Collection<String> driver1inputs = gateDriver1.getInputs();
						for (String driver2 : inputs2) {
							if (driver2.equals(output1)) {
								continue;
							}
							Component gateDriver2 = this.getSource(driver2);
							if (gateDriver2 == null) {
								System.err.println("No driver for " + driver2);
								continue;
							}
							Collection<String> driver2inputs = gateDriver2.getInputs();
							if (driver2inputs.contains(driver1)) {
								// System.out.println("Found possible driver1: " + driver1 + "(" + driver1inputs + ")");
								// System.out.println("Found possible driver2: " + driver2 + "(" + driver2inputs + ")");
								String enable = null;
								Collection<String> data = new TreeSet<String>();
								for (String driver1input : driver1inputs) {
									if (driver2inputs.contains(driver1input)) {
										if (enable == null) {
											enable = driver1input;
										} else {
											System.err.println("Multiple clock candidates for " + driver1 + " and " + driver2);
										}
									} else {
										data.add(driver1input);
									}
								}
								if (enable != null && data.size() > 0) {
									latchCount++;
									System.out.println("Found latch: gate=" + enable + "; data=" + data + "; q=" + output1 + "; nq=" + output2);
									copy.delete(gate1);
									copy.delete(gate2);
									copy.delete(gateDriver1);
									copy.delete(gateDriver2);
									
									if (gate1.numInputs() > 2) {
										System.err.println("WARNING: gate1 has > 2 inputs: " + gate1);
									}
									if (gate2.numInputs() > 2) {
										System.err.println("WARNING: gate2 has > 2 inputs: " + gate2);
									}
									if (gateDriver1.numInputs() > 2) {
										System.err.println("WARNING: gateDriver1 has > 2 inputs: " + gateDriver1);
									}
									if (gateDriver2.numInputs() > 2) {
										System.err.println("WARNING: gateDriver2 has > 2 inputs: " + gateDriver2);
									}
									
									
									Component latch = copy.createComponent("LATCH", "LATCH" + latchnum++);
									for (String d : data) {
										latch.addInput("D", d);
									}
									latch.addInput("EN", enable);
									latch.addOutput("Q", output1);
									latch.addOutput("NQ", output2);
								}
							}
						}
					}
					if (latchCount == 0) {
						System.out.println("No latch");
					} else if (latchCount == 1) {
						System.out.println();
					} else {
						throw new RuntimeException("Multiple latches matched, bailing:" + latchCount);
					}
				}
			}
		}
		System.out.println("Found a total of " + crossCoupledCount / 2 + " cross coupled gates");
		System.out.println("Found a total of " + latchnum + " latches");
		return copy;
	}
	
}
