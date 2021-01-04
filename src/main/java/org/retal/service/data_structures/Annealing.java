package org.retal.service.data_structures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Annealing {
	
	public static void main(String[] args) {
		final int MAX = Integer.MAX_VALUE / 2;
		int n = 4;
		int[][] m = new int[n][];
		m[0] = new int[] { MAX, 1097, 2713, 633};
		m[1] = new int[] {1097, MAX, 1687, 602};
		m[2] = new int[] {2713, 1687, MAX, 2289};
		m[3] = new int[] {633, 602, 2289, MAX};
		Annealing inst = new Annealing();
		List<Integer> routeList = new ArrayList<>();
		for(int i = 0; i < n; i++) {
			routeList.add(i);
		}
		Collections.shuffle(routeList);
		Object[] obj = routeList.toArray();
		int[] route = new int[n];
		for(int i = 0; i < n; i++) {
			route[i] = (Integer)obj[i];
		}
		System.out.println("Start");
		for(int i = 0; i < route.length; i++) {
			System.out.print(route[i] + " ");
		}
		route = inst.generateCandidate(route, 0);
		System.out.println("Generated");
		for(int i = 0; i < route.length; i++) {
			System.out.print(route[i] + " ");
		}
		//System.exit(0);
		route = inst.simulateAnnealing(m, 1000, 70, 3);
		for(int i = 0; i < route.length; i++) {
			System.out.print((route[i] + 1) + " ");
		}
	}
	
	public int[] simulateAnnealing(int[][] matr, double initialTemperature, double endTemperature, int from) {
		int n = matr.length;
		List<Integer> routeList = new ArrayList<>();
		for(int i = 0; i < n; i++) {
			routeList.add(i);
		}
		Collections.shuffle(routeList);
		int valueIndex = routeList.indexOf(from);
		routeList.set(valueIndex, routeList.get(0));
		routeList.set(0, from);
		Object[] obj = routeList.toArray();
		int[] route = new int[n];
		for(int i = 0; i < n; i++) {
			route[i] = (Integer)obj[i];
		}
		double currentTemperature = initialTemperature;
		int currentEnergy = calculateEnergy(route, matr);
		int k = 1;
		while(currentTemperature > endTemperature) {
			int[] routeCandidate = generateCandidate(route, from);
			int candidateEnergy = calculateEnergy(routeCandidate, matr);
			if(candidateEnergy < currentEnergy) {
				currentEnergy = candidateEnergy;
				route = routeCandidate;
			} else {
				double p = getTranstionProbability(candidateEnergy - currentEnergy, currentTemperature);
				if(makeTransit(p)) {
					currentEnergy = candidateEnergy;
					route = routeCandidate;
				}
			}
			currentTemperature = decreaseTemperature(initialTemperature, k);
			System.out.println("k = " + k + "; T = " + currentTemperature);
			k++;
		}
		return route;
	}
	
	private int calculateEnergy(int[] route, int[][] matr) {
		int n = route.length;
		int E = 0;
		for(int i = 0; i < n - 1; i++) {
			E += matr[i][i + 1];
		}
		return E;
	}
	
	private int[] generateCandidate(int[] previousState, int from) {
		/*int n = previousState.length;
		Random rand = new Random();
		int i, j;
		do {
			i = rand.nextInt(n);
			j = rand.nextInt(n);
		} while( i == j);
		int a, b;
		Integer[] copy;
		Integer[] prevState = new Integer[n];
		int[] newState = Arrays.copyOf(previousState, previousState.length);
		for(int k = 0; k < n; k++) {
			prevState[k] = previousState[k];
		}
		if(i < j) {
			copy = Arrays.copyOfRange(prevState, i, j);
			a = i;
			b = j;
		} else {
			copy = Arrays.copyOfRange(prevState, j, i);
			a = j;
			b = i;
		}
		List<Integer> reversed = Arrays.asList(copy);
		Collections.reverse(reversed);
		for(int k = a; k < b; k++) {
			newState[k] = reversed.get(k - a);
		}
		int val = 0;
		for(int k = 0; k < n; k++) {
			if(newState[k] == from) {
				val = newState[0];
				newState[0] = from;
				newState[k] = val;
				break;
			}
		}
		
		return newState;*/
		int n = previousState.length;
		List<Integer> state = new ArrayList<>();
		for(int i = 0; i < n ; i++) {
			state.add(previousState[i]);
		}
		Collections.shuffle(state);
		int[] newState = new int[n];
		for(int i = 0; i < n; i++) {
			newState[i] = state.get(i);
		}
		int val = 0;
		for(int i = 0; i < n; i++) {
			if(newState[i] == from) {
				val = newState[0];
				newState[0] = from;
				newState[i] = val;
				break;
			}
		}
		return newState;
	}
	
	private double getTranstionProbability(int energy, double temperature) {
		return Math.exp(-energy / temperature);
	}
	
	private boolean makeTransit(double probability) {
		return Math.random() <= probability;
	}
	
	private double decreaseTemperature(double initialTemperature, int k) {
		//return initialTemperature * 0.1 / k;
		return initialTemperature / Math.log(1 + k);
	}
}
