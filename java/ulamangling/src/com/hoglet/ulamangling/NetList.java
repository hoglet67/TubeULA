package com.hoglet.ulamangling;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class NetList {

	// Component ID -> Component
	Map<String, Component> componentMap = new TreeMap<String, Component>();
	
	// Net -> Component Driving Net
	Map<String, Component> driverMap = new TreeMap<String, Component>();
	
	public NetList() {
	}
	
	private NetList shallowCopy() {
		NetList netlist = new NetList();
		netlist.componentMap.putAll(this.componentMap);
		netlist.driverMap.putAll(this.driverMap);
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
	
	protected void addComponentOutput(String net, Component component) {
		driverMap.put(net, component);
	}
	
	public Component getDriver(String net) {
		return driverMap.get(net);
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
		for (String output : component.getOutputs()) {
			driverMap.remove(output);
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
			String output1 = gate1.getOutput();
			Collection<String> inputs1 = gate1.getInputs();
			if (inputs1.contains(output1)) {
				System.out.println("Self coupled gate " + output1);
				selfCoupledCount++;
			}
		}
		System.out.println("Found a total of " + selfCoupledCount + " self coupled gates");
	}
	
	public NetList replaceWithLatches() {
		int latchnum = 0;
		// Look for cross coupled gates
		
		NetList copy = this.shallowCopy();

		int crossCoupledCount = 0;
		for (Component gate1 : this.getAll()) {
			String output1 = gate1.getOutput();
			Collection<String> inputs1 = gate1.getInputs();
			if (inputs1.contains(output1)) {
				System.out.println("Skipping self coupled gate: " + output1);
				continue;
			}
			for (String output2 : inputs1) {
				Component gate2 = this.getDriver(output2);
				if (gate2 == null) {
					continue;
				}
				Collection<String> inputs2 = gate2.getInputs();
				if (inputs2.contains(output1)) {
					
					System.out.print("Cross coupled gate pair: "
							+ output1 + "(" + inputs1.size() + ") and "
							+ output2 + "(" + inputs2.size() + ") ");
					crossCoupledCount++;

					// See if we can trace back to a latch

					String gate = null;
					String data = null;
					for (String driver1 : inputs1) {
						if (driver1.equals(output2)) {
							continue;
						}
						Component gateDriver1 = this.getDriver(driver1);
						if (gateDriver1 == null) {
							System.err.println("No driver for " + driver1);
							continue;
						}
						Collection<String> driver1inputs = gateDriver1.getInputs();
						for (String driver2 : inputs2) {
							if (driver2.equals(output1)) {
								continue;
							}
							Component gateDriver2 = this.getDriver(driver2);
							if (gateDriver2 == null) {
								System.err.println("No driver for " + driver2);
								continue;
							}
							Collection<String> driver2inputs = gateDriver2.getInputs();
							if (driver2inputs.contains(driver1)) {
								// System.out.println("Found possible driver1: " + driver1 + "(" + driver1inputs + ")");
								// System.out.println("Found possible driver2: " + driver2 + "(" + driver2inputs + ")");
								for (String driver1input : driver1inputs) {
									if (driver2inputs.contains(driver1input)) {
										if (gate == null) {
											gate = driver1input;
										} else {
											System.err.println("Multiple clock candidates for " + driver1 + " and " + driver2);
										}
									} else {
										if (data == null) {
											data = driver1input;
										} else {
											System.err.println("Multiple data candidates for " + driver1 + " and " + driver2);

										}
									}
								}
								if (gate != null && data != null) {
									System.out.println("Found latch: gate=" + gate + "; data=" + data + "; q=" + output1 + "; nq=" + output2);
									copy.delete(gate1);
									copy.delete(gate2);
									copy.delete(gateDriver1);
									copy.delete(gateDriver2);
									Component latch = copy.createComponent("LATCH", "LATCH" + latchnum++);
									latch.addInput("D", data);
									latch.addInput("G", gate);
									latch.addOutput("Q", output1);
									latch.addOutput("NQ", output2);
								}								
							}
						}
					}
				}
			}
		}
		System.out.println("Found a total of " + crossCoupledCount + " cross coupled gates");
		System.out.println("Found a total of " + latchnum + " latches");
		return copy;
	}
	
}
