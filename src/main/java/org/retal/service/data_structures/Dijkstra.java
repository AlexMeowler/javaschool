package org.retal.service.data_structures;

import java.util.Arrays;

public class Dijkstra {
	
	public static void main(String[] args) {
		int n = 6;
		final int MAX = Integer.MAX_VALUE / 2;
		int[][] matr = new int[6][];
		matr[0] = new int[] {MAX, 7, 9, MAX, MAX, 14};
		matr[1] = new int[] {7, MAX, 10, 15, MAX, MAX};
		matr[2] = new int[] {9, 10, MAX, 11, MAX, 2};
		matr[3] = new int[] {MAX, 15, 11, MAX, 6, MAX};
		matr[4] = new int[] {MAX, MAX, MAX, 6, MAX, 9};
		matr[5] = new int[] {14, MAX, 2, MAX, 9, MAX};
		int[] weights = new int[n];
		boolean[] visited = new boolean[n];
		Arrays.fill(weights, MAX);
		weights[0] = 0;
		boolean allVisited = false;
		while(!allVisited) {
			int min = MAX;
			int current = 0;
			for(int i = 0; i < n; i++) {
				if(!visited[i] && weights[i] < min) {
					min = weights[i];
					current = i;
				}
			}
			for(int i = 1; i < n; i++) {
				if(matr[current][i] != MAX) {
					weights[i] = Math.min(weights[i], weights[current] + matr[current][i]);
				}
			}
			visited[current] = true;
			allVisited = true;
			for(boolean flag : visited) {
				allVisited &= flag;
			}
		}
		for(int i = 0; i < n; i++) {
			System.out.println(weights[i] + " ");
		}
	}
}
