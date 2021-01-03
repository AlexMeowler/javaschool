package org.retal.service.data_structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class AlgoTest {

	public static void main(String[] args) {
		final int MAX = Integer.MAX_VALUE / 2;
		final int eps = 100000;
		int[][] m = new int[n + 1][];
		m[0] = new int[] { MAX, 20, 18, 12, 8, 0 };
		m[1] = new int[] { 5, MAX, 14, 7, 11, 0 };
		m[2] = new int[] { 12, 18, MAX, 6, 11, 0 };
		m[3] = new int[] { 11, 17, 11, MAX, 12, 0 };
		m[4] = new int[] { 5, 5, 5, 5, MAX, 0 };
		m[5] = new int[] { 0, 0, 0, 0, 0, 0 };
		Map<String, Integer> matrix = new HashMap<>();
		for(int i = 0; i < m.length; i++) {
			for(int j = 0; j < m[i].length; j++) {
				matrix.put(i + "," + j, m[i][j]);
			}
		}
		TSPTree tree = new TSPTree("root", 0, matrix);
		TSPTree currentLeave = tree;
		boolean startFromPointTwo = true;
		do {
			matrix = currentLeave.getMatrix();
			int Hk = 0;
			if(startFromPointTwo) {
				// 2
				for (int i = 0; i < n; i++) {
					int min = Integer.MAX_VALUE;
					for (int j = 0; j < n; j++) {
						if (matrix.get(i + "," + j) != null && matrix.get(i + "," + j) < min) {
							min = matrix.get(i + "," + j);
						}
					}
					matrix.put(i + "," + n, min);
				}
				// 3
				for (int i = 0; i < n; i++) {
					for (int j = 0; j < n; j++) {
						if(matrix.get(i + "," + j) != null && matrix.get(i + "," + n) != null) {
							matrix.put(i + "," + j, matrix.get(i + "," + j) - matrix.get(i + "," + n));
						}
					}
				}
				System.out.println(3);
				printMatrix(matrix, MAX, eps);
				// 4
				for (int j = 0; j < n; j++) {
					int min = Integer.MAX_VALUE;
					for (int i = 0; i < n; i++) {
						if (matrix.get(i + "," + j) != null && matrix.get(i + "," + j) < min) {
							min = matrix.get(i + "," + j);
						}
					}
					matrix.put(n + "," + j, min);
				}
				System.out.println(4);
				printMatrix(matrix, MAX, eps);
				// 5
				for (int j = 0; j < n; j++) {
					for (int i = 0; i < n; i++) {
						if(matrix.get(i + "," + j) != null && matrix.get(n + "," + j) != null) {
							matrix.put(i + "," + j, matrix.get(i + "," + j) - matrix.get(n + "," + j));
						}
					}
				}
				System.out.println(5);
				printMatrix(matrix, MAX, eps);
				// 6
				for (int i = 0; i < n; i++) {
					Hk += matrix.get(n + "," + i) + matrix.get(i + "," + n);
				}
				int val = currentLeave.getParent() != null ? currentLeave.getParent().getValue() : 0;
				currentLeave.setValue(val + Hk);
				System.out.println(6);
				System.out.println("Hk = " + (val + Hk));
			}
			// 7
			System.out.println("before 7");
			printMatrix(matrix, MAX, eps);
			Map<String, Integer> estimation = new HashMap<>();
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (matrix.get(i + "," + j) != null && matrix.get(i + "," + j) == 0) {
						int minI = Integer.MAX_VALUE;
						int minJ = Integer.MAX_VALUE;
						for (int k = 0; k < n; k++) {
							if (matrix.get(i + "," + k) != null && matrix.get(i + "," + k) < minI && k != j) {
								minI = matrix.get(i + "," + k);
							}
							if (matrix.get(k + "," + j) != null && matrix.get(k + "," + j) < minJ && k != i) {
								minJ = matrix.get(k + "," + j);
							}
						}
						int x = minI != Integer.MAX_VALUE && minJ != Integer.MAX_VALUE ? minI + minJ : 0;
						estimation.put(i + "," + j, x);
					}
				}
			}
			System.out.println(7);
			printMatrix(estimation, MAX, eps);
			// 8
			int maxI = Integer.MAX_VALUE;
			int maxJ = Integer.MAX_VALUE;
			int est = Integer.MIN_VALUE;
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (matrix.get(i + "," + j) != null && matrix.get(i + "," + j) == 0) {
						if (estimation.get(i + "," + j) > est) {
							maxI = i;
							maxJ = j;
							est = estimation.get(i + "," + j);
						}
					}
				}
			}
			System.out.println(8);
			System.out.println("max estimation = [" + maxI + "," + maxJ + "]=" + est);
			// 9
			currentLeave.setMatrix(matrix);
			Map<String, Integer> matrixCopy = new HashMap<>();
			if(matrix.get(maxJ + "," + maxI) != null) {
				matrix.put(maxJ + "," + maxI, MAX);
			}
			for (int i = 0; i <= n; i++) {
				if (i != maxI) {
					for (int j = 0; j <= n; j++) {
						if (j != maxJ) {
							matrixCopy.put(i + "," + j, matrix.get(i + "," + j));
						}
					}
				}
			}
			matrixCopy.remove(maxI + "," + n);
			matrixCopy.remove(n + "," + maxJ);
			TSPTree leaveExclude = new TSPTree(maxI + "-" + maxJ, est, matrixCopy);
			currentLeave.addChild(leaveExclude);
			System.out.println(9);
			printMatrix(matrixCopy, MAX, eps);
			// 10
			matrixCopy = matrix;
			matrix = leaveExclude.getMatrix();
			for (int i = 0; i < n; i++) {
				int min = Integer.MAX_VALUE;
				for (int j = 0; j < n; j++) {
					if(matrix.get(i + "," + j) != null && matrix.get(i + "," + j) < min){
						min = matrix.get(i + "," + j);
					}
				}
				if(min != Integer.MAX_VALUE) {
					matrix.put(i + "," + n, min);
				}
			}
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if(matrix.get(i + "," + j) != null && matrix.get(i + "," + n) != null) {
						matrix.put(i + "," + j, matrix.get(i + "," + j) - matrix.get(i + "," + n));
					}
				}
			}
			for (int j = 0; j < n; j++) {
				int min = Integer.MAX_VALUE;
				for (int i = 0; i < n; i++) {
					if (matrix.get(i + "," + j) != null && matrix.get(i + "," + j) < min) {
						min = matrix.get(i + "," + j);
					}
				}
				if(min != Integer.MAX_VALUE) {
					matrix.put(n + "," + j, min);
				}
			}
			for (int j = 0; j < n; j++) {
				for (int i = 0; i < n; i++) {
					if(matrix.get(i + "," + j) != null && matrix.get(n + "," + j) != null) {
						matrix.put(i + "," + j, matrix.get(i + "," + j) - matrix.get(n + "," + j));
					}
				}
			}
			Hk = 0;
			for (int i = 0; i < n; i++) {
				if(matrix.get(n + "," + i) != null) {
					Hk += matrix.get(n + "," + i);
				}
				if(matrix.get(i + "," + n) != null) {
					Hk += matrix.get(i + "," + n);
				}
			}
			leaveExclude.setValue(leaveExclude.getParent().getValue() + Hk);
			System.out.println(10);
			System.out.println("Hk = " + leaveExclude.getValue());
			printMatrix(matrix, MAX, eps);
			//11
			matrixCopy.put(maxI + "," + maxJ, MAX);
			TSPTree leaveInclude = new TSPTree(maxI + "*-" + maxJ + "*", tree.getValue() + est, matrixCopy);
			currentLeave.addChild(leaveInclude);
			System.out.println(11);
			printMatrix(matrixCopy, MAX, eps);
			//12
			result = new TSPTree("", Integer.MAX_VALUE, null);
			findNextLeave(tree);
			System.out.println(result.getName());
			printMatrix(result.getMatrix(), MAX, eps);
			currentLeave = result;
			startFromPointTwo = result.getName().contains("*");
			//scan.nextLine();
		} while(countNotNullElements(result.getMatrix()) != 4);
		String s = getLastElement(result.getMatrix());
		TSPTree lastElem = new TSPTree(s, result.getValue(), null);
		result.addChild(lastElem);
		depth = 0;
		getDeepestElement(tree, 0);
		TSPTree pointer = result;
		List<TSPTree> routes = new ArrayList<>();
		while(pointer.getParent() != null) {
			routes.add(pointer);
			pointer = pointer.getParent();
		}
		for(TSPTree t : routes) {
			System.out.print(t.getName() + " ");
		}
	}

	private static void printMatrix(Map<String, Integer> map, final int MAX, int eps) {
		int[][] matr = new int[MAX_CITIES][MAX_CITIES];
		for(int i = 0; i < MAX_CITIES; i++) {
			for(int j = 0; j < MAX_CITIES; j++) {
				matr[i][j] = -1;
			}
		}
		for(Map.Entry<String, Integer> e : map.entrySet()) {
			String[] strs = e.getKey().split(",");
			int[] index = {Integer.parseInt(strs[0]), Integer.parseInt(strs[1])};
			if(e.getValue() != null) {
				matr[index[0]][index[1]] = e.getValue();
			}
		}
		boolean emptyLine;
		for (int i = 0; i < matr.length; i++) {
			emptyLine = true;
			for (int j = 0; j < matr[i].length; j++) {
				String txt = Math.abs(matr[i][j] - MAX) < eps ? "(" + i + "," + j + ")M" : "(" + i + "," + j + ")" + matr[i][j];
				if(matr[i][j] != -1) {
					emptyLine = false;
					System.out.print(txt + " ");
				}
			}
			if(!emptyLine) {
				System.out.println();
			}
		}
		System.out.println("---------------");
	}
	
	private static void findNextLeave(TSPTree root) {
		if(root.hasChildren()) {
			for(TSPTree t : root.getChildren()) {
				findNextLeave(t);
			}
		} else {
			if(root.getValue() < result.getValue()) {
				result = root;
			}
		}
	}
	
	private static void printTree(TSPTree root) {
		System.out.println(root.getName() + "=" + root.getValue());
		for(TSPTree t : root.getChildren()) {
			printTree(t);
		}
	}
	
	private static int countNotNullElements(Map<String, Integer> map) {
		int counter = 0;
		for(Map.Entry<String, Integer> e : map.entrySet()) {
			counter = e.getValue() != null ? counter + 1 : counter;
		}
		return counter;
	}
	
	private static String getLastElement(Map<String, Integer> map) {
		for(Map.Entry<String, Integer> e : map.entrySet()) {
			if(!e.getKey().contains("" + n) && e.getValue() != null) {
				return e.getKey().replace(',', '-');
			}
		}
		return null;
	}
	
	private static void getDeepestElement(TSPTree root, int depth) {
		if(AlgoTest.depth < depth) {
			AlgoTest.depth = depth;
			result = root;
		}
		for(TSPTree t : root.getChildren()) {
			getDeepestElement(t, depth + 1);
		}
	}
	
	private static TSPTree result;
	private static int depth;
	private static final int MAX_CITIES = 100;
	private static final int n = 5;
}
