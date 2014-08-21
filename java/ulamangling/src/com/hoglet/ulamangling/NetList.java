package com.hoglet.ulamangling;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
		netlist.componentMap.putAll(componentMap);
		netlist.outputMap.putAll(outputMap);
		netlist.inputMap.putAll(inputMap);
		for (Component c : getAll()) {
			c.setNetlist(netlist);
		}
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

	protected void removeComponentInput(String net, Component component) {
		Collection<Component> inputs = inputMap.get(net);
		inputs.remove(component);
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
	public void dump(File file) {
		PrintStream stream = null;
		try {
			stream = new PrintStream(file);
			dump(stream);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			stream.close();
		}
	}

	public void dump(PrintStream stream) {
		for (Component component : componentMap.values()) {
			stream.println(component);
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
		for (Component gate1 : getAll()) {
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
	
	public void pruneUnconnectedOutputs() {
		for (Component component : getAll()) {
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
				System.out.println("Removing unused output " + name + " driving " + net + " from " + component.getId());
				component.removeOutput(name, net);
			}
		}
	}	
	
	public NetList replaceWithLatches() {
		int latchnum = 0;
		// Look for cross coupled gates
		
		NetList copy = shallowCopy();

		int crossCoupledCount = 0;
		for (Component gate1 : getAll()) {
			String output1 = gate1.getOutput();
			if (gate1.getOutput() == null) {
				continue;
			}
			Collection<String> inputs1 = gate1.getInputs();
			if (inputs1.contains(output1)) {
				System.out.println("Skipping self coupled gate: " + output1);
				continue;
			}
			for (String output2 : inputs1) {
				Component gate2 = getSource(output2);
				if (gate2 == null) {
					continue;
				}
				Collection<String> inputs2 = gate2.getInputs();
				if (inputs2.contains(output1)) {
					
					System.out.println("Cross coupled gate pair: "
							+ output1 + "(" + inputs1.size() + ") and "
							+ output2 + "(" + inputs2.size() + ")");
					crossCoupledCount++;

					if (inputs1.size() > 2 || inputs2.size() > 2) {
						System.out.println("Skipping LATCH candidate with " + inputs1.size() + " and " + inputs2.size() + " input:" + output1 + " / " + output2);
						continue;
					}

					// See if we can trace back to a latch

					int latchCount = 0;
					for (String driver1 : inputs1) {
						if (driver1.equals(output2)) {
							continue;
						}
						Component gateDriver1 = getSource(driver1);
						if (gateDriver1 == null) {
							System.err.println("No driver for " + driver1);
							continue;
						}
						Collection<String> driver1inputs = gateDriver1.getInputs();
						for (String driver2 : inputs2) {
							if (driver2.equals(output1)) {
								continue;
							}
							Component gateDriver2 = getSource(driver2);
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
									
									if (gateDriver1.numInputs() > 2) {
										System.err.println("WARNING: gateDriver1 has > 2 inputs: " + gateDriver1);
									}
									if (gateDriver2.numInputs() > 2) {
										System.err.println("WARNING: gateDriver2 has > 2 inputs: " + gateDriver2);
									}
									
									
									Component latch = copy.createComponent(Component.TYPE_LATCH, "d" + latchnum++);
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

	public NetList replaceWithSR() {
		int latchnum = 0;
		NetList copy = shallowCopy();
		for (Component gate1 : getAll()) {
			if (!gate1.getType().equals(Component.TYPE_NOR)) {
				continue;
			}
			String output1 = gate1.getOutput();
			if (gate1.getOutput() == null) {
				continue;
			}
			Collection<String> inputs1 = gate1.getInputs();
			for (String output2 : inputs1) {
				// As the component is symmertical, make sure it is only added once
				if (output2.compareTo(output1) < 0) {
					continue;
				}
				Component gate2 = getSource(output2);
				if (gate2 == null) {
					continue;
				}
				if (!gate2.getType().equals(Component.TYPE_NOR)) {
					continue;
				}
				Collection<String> inputs2 = gate2.getInputs();
				
				
				if (inputs2.contains(output1)) {

					if (inputs1.size() > 2 || inputs2.size() > 2) {
						System.out.println("Skipping SR candidate with " + inputs1.size() + " and " + inputs2.size() + " input:" + output1 + " / " + output2);
						continue;
					}

					System.out.println("Cross coupled gate pair: " + output1 + "(" + inputs1.size() + ") and " + output2 + "("
							+ inputs2.size() + ")");

					copy.delete(gate1);
					copy.delete(gate2);

					Component latch = copy.createComponent(Component.TYPE_SR, "sr" + latchnum++);
					for (String input : inputs1) {
						if (!input.equals(output2)) {
							latch.addInput("R", input);
						}
					}
					for (String input : inputs2) {
						if (!input.equals(output1)) {
							latch.addInput("S", input);
						}
					}
					latch.addOutput("Q", output1);
					latch.addOutput("NQ", output2);

				}
			}
		}
		System.out.println("Found a total of " + latchnum + " set reset latches");
		return copy;
	}

	
	public List<String> traceNetForward(String net) {
		List<String> paths = new ArrayList<String>();
		Collection<String> visited = new HashSet<String>();
		traceNetForward("", net, 0, paths, visited);
		return paths;
	}

	public void traceNetForward(String cmpId, String net, int depth, List<String> paths, Collection<String> visited) {
		if (visited.contains(net)) {
			return;
		}
		visited.add(net);
		String pad = "";
		for (int i = 0; i < depth; i++) {
			pad += " ";
		}
		Collection<Component> components = inputMap.get(net);
		for (Component c : components) {
			for (String output : c.getOutputs()) {
				paths.add(pad + net + " => [" + c + "] => " + output);
				traceNetForward(c.getId(), output, depth + 1, paths, visited);
			}
		}
	}
	
	public Collection<String> getInputPins() {
		return getPins(Component.TYPE_INPUT);
	}
	
	public Collection<String> getOutputPins() {
		return getPins(Component.TYPE_OUTPUT);
	}

	private Collection<String> getPins(String type) {
		Collection<String> pins = new TreeSet<String>();
		for (Component c : componentMap.values()) {
			if (c.getType().equals(type)) {
				pins.add(c.getId());
			}
		}
		return pins;
	}

	public void toVerilog(File file, String moduleName) {

		PrintStream stream = null;
		try {
			Collection<String> inputPins = getInputPins();
			Collection<String> outputPins = getOutputPins();
			Collection<String> allPins = new TreeSet<String>();
			allPins.addAll(inputPins);
			allPins.addAll(outputPins);
			stream = new PrintStream(file);
			stream.println("module " + moduleName + "(");
			boolean first = true;
			for (String pin : allPins) {
				if (first) {
					stream.println("      " + pin);
					first = false;
				} else {
					stream.println("    , " + pin);
				}
			}
			stream.println(");");

			stream.println("// Inputs");
			for (String pin : inputPins) {
				stream.println("input " + pin + ";");
			}

			stream.println("// Outputs");
			for (String pin : outputPins) {
				stream.println("output " + pin + ";");
			}

			stream.println("// Wires");
			for (String net : outputMap.keySet()) {
				if (!allPins.contains(net)) {
					stream.println("wire " + net + ";");
				}
			}
			
			for (Component c : componentMap.values()) {
				if (c.getType().equals("INPUT") || c.getType().equals("OUTPUT")) {
					continue;
				}
				stream.println(c.toVerilog());
			}

			stream.println("endmodule");

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			stream.close();
		}

	}
	
	
	public void renameNet(String from, String to) {
		for (Component c : getAll()) {
			for (Map.Entry<String, Collection<String>> input : c.getNamedInputs()) {
				String name = input.getKey();
				Collection<String> nets = input.getValue();
				if (nets.contains(from)) {
					c.removeInput(name, from);
					c.addInput(name, to);
				}
			}
			for (Map.Entry<String, String> output : c.getNamedOutputs()) {
				String name = output.getKey();
				String net = output.getValue();
				if (net.equals(from)) {
					c.removeOutput(name, from);
					c.addOutput(name, to);
				}
			}			
		}
	}
}
