package org.retal.service.data_structures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TSPTree {
	
	public TSPTree() {
		children = new ArrayList<>();
		parent = null;
		m = null;
	}
	
	public TSPTree(String name, int value, Map<String, Integer> matrix) {
		this.name = name;
		this.value = value;
		children = new ArrayList<>();
		parent = null;
		if(matrix != null) {
			setMatrix(matrix);
		}
	}
	
	public String getName() {
		return name;
	}
	
	public int getValue() {
		return value;
	}
	
	public List<TSPTree> getChildren() {
		return children;
	}
	
	public TSPTree getParent() {
		return parent;
	}
	
	public void addChild(TSPTree t) {
		children.add(t);
		t.parent = this;
	}
	
	public void setValue(int v) {
		value = v;
	}
	
	public Map<String, Integer> getMatrix() {
		Map<String, Integer> matrix = new HashMap<>();
		for(Map.Entry<String, Integer> e : m.entrySet()) {
			matrix.put(e.getKey(), e.getValue());
		}
		return matrix;
	}
	
	public void setMatrix(Map<String, Integer> matrix) {
		m = new HashMap<>();
		for(Map.Entry<String, Integer> e : matrix.entrySet()) {
			m.put(e.getKey(), e.getValue());
		}
	}
	
	public boolean hasChildren() {
		return children.size() > 0;
	}
	
	private String name;
	private int value;
	private List<TSPTree> children;
	private TSPTree parent;
	private Map<String, Integer> m;
}
