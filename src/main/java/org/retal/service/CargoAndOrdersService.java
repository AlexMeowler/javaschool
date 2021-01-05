package org.retal.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.retal.dao.CarDAO;
import org.retal.dao.CargoDAO;
import org.retal.dao.CityDAO;
import org.retal.dao.CityDistanceDAO;
import org.retal.dao.OrderDAO;
import org.retal.dao.RoutePointDAO;
import org.retal.dao.UserDAO;
import org.retal.domain.Cargo;
import org.retal.domain.City;
import org.retal.domain.CityDistance;
import org.retal.domain.Car;
import org.retal.domain.Order;
import org.retal.domain.RoutePoint;
import org.retal.domain.User;
import org.retal.domain.UserInfo;
import org.retal.domain.enums.DriverStatus;
import org.retal.domain.enums.UserRole;
import org.retal.dto.RoutePointDTO;
import org.retal.service.data_structures.TSPTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

@Service
public class CargoAndOrdersService {
	//TODO Cargo and Order Validation
	public List<Cargo> getAllCargo() {
		return cargoDAO.readAll();
	}
	
	public List<Order> getAllOrders() {
		return orderDAO.readAll();
	}
	
	public void addNewCargo(Cargo cargo, BindingResult bindingResult) {
		//validation
		cargoDAO.add(cargo);
	}
	
	public List<RoutePoint> mapRoutePointDTOsToEntities(List<RoutePointDTO> list) {
		List<RoutePoint> entityList = new ArrayList<>();
		for(RoutePointDTO rpDTO : list) {
			RoutePoint rp = new RoutePoint();
			rp.setIsLoading(rpDTO.getIsLoading());
			rp.setCity(cityDAO.read(rpDTO.getCityName()));
			rp.setCargo(cargoDAO.read(rpDTO.getCargoId()));
			entityList.add(rp);
		}
		return entityList;
	}
	
	public void createOrderWithRoutePoints(List<RoutePoint> list) {
		//TODO validate route points
		List<CityDistance> distances = cityDistanceDAO.readAll();
		List<City> cities = cityDAO.readAll();
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		Set<RoutePoint> points = new HashSet<>(list);
		Order order = new Order();
		Object[] carAndPath = findAppropriateCarAndDriverAndCalculatePath(list, distances, cities, session);
		/*order.setCar(); //FIXME
		order.setPoints(points);
		order.setIsCompleted(false);
		Transaction transaction = session.beginTransaction();
		orderDAO.setSession(session);
		routePointDAO.setSession(session);
		orderDAO.add(order);
		transaction.commit();
		transaction = session.beginTransaction();
		for(RoutePoint rp : points) {
			rp.setOrder(order);
			routePointDAO.add(rp);
		}
		transaction.commit();
		orderDAO.setSession(null);
		routePointDAO.setSession(null);*/
		session.close();
		return ;
	}
	
	public Object[] findAppropriateCarAndDriverAndCalculatePath(List<RoutePoint> list, 
											List<CityDistance> distances, 
											List<City> cities, Session session) {
		//TODO drivers?
		log.info("Searching for cars and paths...");
		Set<City> allRoutePointCities = new HashSet<>();
		List<String> cityNames = new ArrayList<>();
		for(City city : cities) {
			cityNames.add(city.getCurrentCity());
			log.debug("Added city name " + city.getCurrentCity());
		}
		log.debug("Mapped city names");
		List<Integer> loadingCities = new ArrayList<>();
		List<Integer> unloadingCities = new ArrayList<>();
		for(RoutePoint rp : list) {
			allRoutePointCities.add(rp.getCity());
		}
		List<City> rpCities = new ArrayList<>(allRoutePointCities);
		for(RoutePoint rp : list) {
			if(rp.getIsLoading()) {
				for(RoutePoint rp2 : list) {
					if(!rp2.getIsLoading() && rp2.getCargo().getId() == rp.getCargo().getId()) {
						loadingCities.add(rpCities.indexOf(rp.getCity()));
						unloadingCities.add(rpCities.indexOf(rp2.getCity()));
					}
				}
			}
		}
		String t = "";
		for(City c : rpCities) {
			t += c.getCurrentCity() + " ";
		}
		log.debug(t);
		log.debug(loadingCities.toString());
		log.debug(unloadingCities.toString());
		Map<String, Integer> optimalRoutes = new HashMap<>();
		for(RoutePoint rp: list) {
			if(rp.getIsLoading()) {
				int loadIndex = loadingCities.indexOf(rpCities.indexOf(rp.getCity()));
				int unloadIndex = unloadingCities.indexOf(rpCities.indexOf(rp.getCity()));
				if(loadIndex == -1 || unloadIndex == -1) {
					// build matrix
					int n = cities.size();
					int[][] bigMatrix = buildMatrix(n);
					fillMatrixWithDefaultCityDistances(bigMatrix, distances, cityNames);
					log.debug("Filled big matrix");
					n = rpCities.size();
					int[][] matrix = buildMatrix(n);
					resolvePathsForMatrix(matrix, bigMatrix, cityNames, rpCities);
					log.info("Calculating optimized route (annealing algorithm)...");
					int[] annealingPaths = findOptimalPathUsingAnnealingImitation(matrix, 
							ANNEALING_START_TEMPERAURE, ANNEALING_END_TEMPERATURE, 
							rpCities.indexOf(rp.getCity()), loadingCities, 
							unloadingCities, false);
					String p = "";
					int length = 0;
					for(int x : annealingPaths) {
						p += rpCities.get(x).getCurrentCity() + " ";
					}
					length = 0;
					for(int i = 0; i < annealingPaths.length - 1; i++) {
						length += matrix[annealingPaths[i]][annealingPaths[i + 1]];
					}
					log.debug("Suggested route - " + p + "; it's length - " + length);
					optimalRoutes.put(p, length);
				} else {
					log.debug("Cycle detected");
					//optimalRoutes.put("Could not find optimal route", Integer.MAX_VALUE);
				}
			}
		}
		if(optimalRoutes.size() == 0) {
			//TODO all routes are the same length, next criteria should be the availability of car and driver
			log.debug("Did not find optimal route without cycles");
			for(RoutePoint rp: list) {
				if(rp.getIsLoading()) {
					int loadIndex = loadingCities.indexOf(rpCities.indexOf(rp.getCity()));
					int unloadIndex = unloadingCities.indexOf(rpCities.indexOf(rp.getCity()));
					if(loadIndex != -1 && unloadIndex != -1) {
						// build matrix
						int n = cities.size();
						int[][] bigMatrix = buildMatrix(n);
						fillMatrixWithDefaultCityDistances(bigMatrix, distances, cityNames);
						log.debug("Filled big matrix");
						n = rpCities.size();
						int[][] matrix = buildMatrix(n);
						resolvePathsForMatrix(matrix, bigMatrix, cityNames, rpCities);
						log.info("Calculating optimized route for cycle (annealing algorithm)...");
						int[] annealingPaths = findOptimalPathUsingAnnealingImitation(matrix, 
								ANNEALING_START_TEMPERAURE, ANNEALING_END_TEMPERATURE, 
								rpCities.indexOf(rp.getCity()), loadingCities, 
								unloadingCities, true);
						String p = "";
						int length = 0;
						for(int x : annealingPaths) {
							p += rpCities.get(x).getCurrentCity() + " ";
						}
						length = 0;
						for(int i = 0; i < annealingPaths.length - 1; i++) {
							length += matrix[annealingPaths[i]][annealingPaths[i + 1]];
						}
						log.debug("Suggested route - " + p + "; it's length - " + length);
						optimalRoutes.put(p, length);
					}
				}
			}
		}
		String shortestPath = "";
		int shortestPathLength = Integer.MAX_VALUE;
		for(Map.Entry<String, Integer> e : optimalRoutes.entrySet()) {
			if(e.getValue() < shortestPathLength) {
				shortestPath = e.getKey();
				shortestPathLength = e.getValue();
			}
		}
		String[] shortestPathCities = shortestPath.split(" ");
		float requiredCapacity = 0;
		float currentCapacity = 0;
		for(RoutePoint rp : list) {
			if(rp.getCity().getCurrentCity().equals(shortestPathCities[0]) && rp.getIsLoading()) {
				currentCapacity = currentCapacity + rp.getCargo().getMass();
			}
		}
		requiredCapacity = currentCapacity;
		log.debug("Starting capacity(kg) - " + requiredCapacity);
		for(int i = 1; i < shortestPathCities.length - 1; i++) {
			for(RoutePoint rp : list) {
				if(rp.getCity().getCurrentCity().equals(shortestPathCities[i])) {
					int sign = rp.getIsLoading() ? 1 : -1;
					currentCapacity = currentCapacity + sign * rp.getCargo().getMass();
					if(currentCapacity > requiredCapacity) {
						requiredCapacity = currentCapacity;
					}
				}
			}
		}
		requiredCapacity /= 1000;
		log.debug("Required capacity - " + requiredCapacity + " tons");
		String firstCity = shortestPath.split(" ")[0];
		Car selectedCar = null;
		//FIXME ADD DRIVER SELECTION
		for(City city : allRoutePointCities) {
			if(city.getCurrentCity().equals(firstCity)) {
				session.persist(city);
				for(Car car : city.getCars()) {
					if(car.getCapacityTons() > requiredCapacity) {
						selectedCar = car;
						break;
					}
				}
				session.detach(city);
				String debugMessage = selectedCar != null ? "Selected car " + selectedCar.toString() : "Car not selected";
				log.debug(debugMessage);
				break;
			}
		}
		shortestPath = shortestPath.replace(" ", ";");
		shortestPath = shortestPath.substring(0, shortestPath.length() - 1);
		log.debug("Selected driver, car and route - " + shortestPath + "; " + selectedCar.toString());
		return new Object[] {selectedCar, shortestPath};
	}
	
	/*public List<String> findOptimizedRoute(int[][] m, int from) {
		final int MAX = Integer.MAX_VALUE / 2;
		final int eps = 100000;
		/*int[][] m = new int[n + 1][];
		m[0] = new int[] { MAX, 20, 18, 12, 8, 0 };
		m[1] = new int[] { 5, MAX, 14, 7, 11, 0 };
		m[2] = new int[] { 12, 18, MAX, 6, 11, 0 };
		m[3] = new int[] { 11, 17, 11, MAX, 12, 0 };
		m[4] = new int[] { 5, 5, 5, 5, MAX, 0 };
		m[5] = new int[] { 0, 0, 0, 0, 0, 0 };
		n = m.length;
		int[][] matr = new int[n][n];
		for(int i = 0; i < n; i++) {
			for(int j = 0; j < n; j++) {
				matr[i][j] = m[i][j];
			}
		}
		for(int i = 0; i < n; i++) {
			if(i != from) {
				matr[i][from] = 0;
			}
		}
		Map<String, Integer> matrix = new HashMap<>();
		for(int i = 0; i < matr.length; i++) {
			for(int j = 0; j < matr[i].length; j++) {
				matrix.put(i + "," + j, matr[i][j]);
			}
		}
		printMatrix(matrix, MAX, eps);
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
				// 5
				for (int j = 0; j < n; j++) {
					for (int i = 0; i < n; i++) {
						if(matrix.get(i + "," + j) != null && matrix.get(n + "," + j) != null) {
							matrix.put(i + "," + j, matrix.get(i + "," + j) - matrix.get(n + "," + j));
						}
					}
				}
				log.debug(5);
				printMatrix(matrix, MAX, eps);
				// 6
				for (int i = 0; i < n; i++) {
					Hk += matrix.get(n + "," + i) + matrix.get(i + "," + n);
				}
				int val = currentLeave.getParent() != null ? currentLeave.getParent().getValue() : 0;
				currentLeave.setValue(val + Hk);
			}
			// 7
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
			log.debug(8);
			printMatrix(estimation, MAX, eps);
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
			log.debug(9);
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
			log.debug(10);
			printMatrix(matrix, MAX, eps);
			//11
			matrixCopy.put(maxI + "," + maxJ, MAX);
			TSPTree leaveInclude = new TSPTree(maxI + "*-" + maxJ + "*", tree.getValue() + est, matrixCopy);
			currentLeave.addChild(leaveInclude);
			//12
			result = new TSPTree("", Integer.MAX_VALUE, null);
			findNextLeave(tree);
			currentLeave = result;
			startFromPointTwo = result.getName().contains("*");
		} while(countRemainingElements(result.getMatrix()) > 4);
		log.debug("TSP: Calculation finished.Retrieving path...");
		String s = getLastElement(result.getMatrix());
		TSPTree lastElem = new TSPTree(s, result.getValue(), null);
		result.addChild(lastElem);
		depth = 0;
		getDeepestElement(tree, 0);
		log.debug("TSP: Deepest element found");
		TSPTree pointer = result;
		List<TSPTree> routes = new ArrayList<>();
		while(pointer.getParent() != null) {
			routes.add(pointer);
			pointer = pointer.getParent();
		}
		log.debug("TSP: Tree scanned");
		for(int i = 0; i < routes.size(); i++) {
			if(routes.get(i).getName().contains("*")) {
				routes.remove(i);
				i--;
			}
		}
		log.debug("TSP: Removed excluded leaves");
		String treeText = "";
		for(TSPTree t : routes) {
			treeText += t.getName() + " ";
		}
		log.debug(treeText);
		TSPTree start = null;
		for(TSPTree t : routes) {
			if(t.getName().split("-")[0].equals("" + from)) {
				start = t;
			}
		}
		log.debug(start.getName());
		List<String> path = new ArrayList<>();
		routes.remove(start);
		path.add(start.getName().split("-")[0]);
		log.debug("TSP: Beginning to reconstructing path");
		while(routes.size() != 0) {
			for(TSPTree t : routes) {
				if(start.getName().split("-")[1].equals(t.getName().split("-")[0])) {
					path.add(t.getName().split("-")[0]);
					start = t;
					break;
				}
			}
			log.debug(start.getName());
			routes.remove(start);
		}
		path.add(start.getName().split("-")[1]);
		return path;
	}

	private void printMatrix(Map<String, Integer> map, final int MAX, int eps) {
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
			String s = "";
			for (int j = 0; j < matr[i].length; j++) {
				String txt = Math.abs(matr[i][j] - MAX) < eps ? "(" + i + "," + j + ")M" : "(" + i + "," + j + ")" + matr[i][j];
				if(matr[i][j] != -1) {
					emptyLine = false;
					s += txt + " ";
				}
			}
			if(!emptyLine) {
				log.debug(s);
			}
		}
		log.debug("---------------");
	}
	
	private void findNextLeave(TSPTree root) {
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
	
	private int countRemainingElements(Map<String, Integer> map) {
		int counter = 0;
		for(Map.Entry<String, Integer> e : map.entrySet()) {
			counter = e.getValue() != null && e.getValue() != Integer.MAX_VALUE ? counter + 1 : counter;
		}
		return counter;
	}
	
	private String getLastElement(Map<String, Integer> map) {
		for(Map.Entry<String, Integer> e : map.entrySet()) {
			if(!e.getKey().contains("" + n) && e.getValue() != null) {
				return e.getKey().replace(',', '-');
			}
		}
		return null;
	}
	
	private void getDeepestElement(TSPTree root, int depth) {
		if(this.depth < depth) {
			this.depth = depth;
			result = root;
		}
		for(TSPTree t : root.getChildren()) {
			getDeepestElement(t, depth + 1);
		}
	}*/
	
	public int getShortestPath(int[][] matr, List<Integer> result, int from, int to) {
		int n = matr.length;
		final int MAX = Integer.MAX_VALUE / 2;
		/*int[][] matr = new int[6][];
		matr[0] = new int[] {MAX, 7, 9, MAX, MAX, 14};
		matr[1] = new int[] {7, MAX, 10, 15, MAX, MAX};
		matr[2] = new int[] {9, 10, MAX, 11, MAX, 2};
		matr[3] = new int[] {MAX, 15, 11, MAX, 6, MAX};
		matr[4] = new int[] {MAX, MAX, MAX, 6, MAX, 9};
		matr[5] = new int[] {14, MAX, 2, MAX, 9, MAX};*/
		int[] weights = new int[n];
		boolean[] visited = new boolean[n];
		Arrays.fill(weights, MAX);
		weights[from] = 0;
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
		log.debug("Dijkstra: visited all cities; distance is " + weights[to]);
		int current = to;
		while(current != from) {
			for(int i = 0; i < n; i++) {
				 if(weights[current] - matr[i][current] == weights[i]) {
					 result.add(current);
					 current = i;
				 }
			}
		}
		return weights[to];
	}
	
	public int[] findOptimalPathUsingAnnealingImitation(int[][] matr, double initialTemperature, 
						double endTemperature, int from, List<Integer> loadingCities, 
						List<Integer> unloadingCities, boolean cycleDetected) {
		int n = matr.length;
		//if we have cycle in our path, we need to check if its one big cycle or set of different cycles
		if(cycleDetected) {
			log.debug("Annealing: Trying to define cycle type");
			boolean[] visited = new boolean[n];
			int[] answer = new int[n + 1];
			Arrays.fill(answer, -1);
			int k = 0;
			int i = from;
			while(!visited[i]) {
				visited[i] = true;
				answer[k] = i;
				k++;
				if(loadingCities.indexOf(i) != -1) {
					i = unloadingCities.get(loadingCities.indexOf(i));
				} else {
					int index = unloadingCities.indexOf(i);
					int x = loadingCities.get(index);
					for(int j = 0; j < loadingCities.size(); j++) {
						if(loadingCities.get(j) == x && j != index) {
							i = x;
							int[] buffer = new int[answer.length + 1];
							System.arraycopy(answer, 0, buffer, 0, k);
							answer = buffer;
							visited[i] = false;
							break;
						}
					}
				}
				log.debug(Arrays.toString(answer));
			}
			boolean allVisited = true;
			for(boolean flag : visited ) {
				allVisited &= flag;
			}
			if(allVisited) {
				log.debug("Annealing: Full cycle detected - only one possible route");
				answer[answer.length - 1] = from;
				return answer;
			}
		}
		List<Integer> routeList = new ArrayList<>();
		for(int i = 0; i < n; i++) {
			routeList.add(i);
		}
		Collections.shuffle(routeList);
		int valueIndex = routeList.indexOf(from);
		routeList.set(valueIndex, routeList.get(0));
		routeList.set(0, from);
		//in case of set of cycles we have to go back to some cities to deliver cargo
		if(cycleDetected) {
			log.debug("Annealing: Set of cycles detected - editing entry candidate");
			adaptCandidateForCycleProcessing(routeList, loadingCities, unloadingCities);
			n = routeList.size();
		}
		Object[] obj = routeList.toArray();
		int[] route = new int[n];
		for(int i = 0; i < n; i++) {
			route[i] = (Integer)obj[i];
		}
		double currentTemperature = initialTemperature;
		int currentEnergy = calculateEnergy(route, matr);
		int k = 1;
		n = matr.length;
		while(currentTemperature > endTemperature) {
			int[] routeCandidate = generateCandidate(route, n, from, loadingCities, unloadingCities, cycleDetected);
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
			//log.debug("k = " + k + "; T = " + currentTemperature);
			k++;
		}
		return route;
	}
	
	private int calculateEnergy(int[] route, int[][] matr) {
		int n = route.length;
		int E = 0;
		for(int i = 0; i < n - 1; i++) {
			E += matr[route[i]][route[i + 1]];
		}
		return E;
	}
	
	private int[] generateCandidate(int[] previousState, int n, int from, List<Integer> loadingCities, 
										List<Integer> unloadingCities, boolean cycleDetected) {
		List<Integer> state = new ArrayList<>();
		for(int i = 0; i < n ; i++) {
			state.add(previousState[i]);
		}
		boolean sequenceCorrect = false;
		while(!sequenceCorrect) {
			Collections.shuffle(state);
			state.set(state.indexOf(from), state.get(0));
			state.set(0, from);
			sequenceCorrect = true;
			if(!cycleDetected) {
				for(int i = 0; i < loadingCities.size(); i++) {
					sequenceCorrect &= state.indexOf(loadingCities.get(i)) < state.indexOf(unloadingCities.get(i));
				}
			} else {
				adaptCandidateForCycleProcessing(state, loadingCities, unloadingCities);
			}
		}
		int[] newState = new int[state.size()];
		for(int i = 0; i < newState.length; i++) {
			newState[i] = state.get(i);
		}
		return newState;
	}
	
	private double getTranstionProbability(int energy, double temperature) {
		//return Math.exp(-energy / temperature);
		return 1 / (1 + Math.exp(energy / temperature));
	}
	
	private boolean makeTransit(double probability) {
		return Math.random() <= probability;
	}
	
	private double decreaseTemperature(double initialTemperature, int k) {
		//return initialTemperature * 0.1 / k;
		return initialTemperature / Math.log(1 + k);
	}
	
	private void adaptCandidateForCycleProcessing(List<Integer> routeList, List<Integer> loadingCities, 
										List<Integer> unloadingCities) {
		boolean[] loaded = new boolean[loadingCities.size()];
		boolean[] unloaded = new boolean[unloadingCities.size()];
		for(int i = 0; i < routeList.size(); i++) {
			for(int j = 0; j < loadingCities.size(); j++) {
				if(loadingCities.get(j) == routeList.get(i)) {
					loaded[j] = true;
				}
			}
			for(int j = 0; j < unloadingCities.size(); j++) {
				if(unloadingCities.get(j) == routeList.get(i) && loaded[j]) {
					unloaded[j] = true;
				}
			}
			//log.debug(Arrays.toString(loaded));
			//log.debug(Arrays.toString(unloaded));
		}
		
		List<Integer> leftCities = new ArrayList<>();
		for(int i = 0; i < unloaded.length; i++) {
			if(!unloaded[i]) {
				leftCities.add(unloadingCities.get(i));
			}
		}
		//log.debug(leftCities.toString());
		Collections.shuffle(leftCities);
		for(int i = 0; i < leftCities.size(); i++) {
			routeList.add(leftCities.get(i));
		}
		//log.debug(routeList.toString());
	}
	
	private int[][] buildMatrix(int n) {
		int[][] matrix = new int[n][n];
		for(int i = 0; i < n; i++) {
			for(int j = 0; j < n; j++) {
				matrix[i][j] = Integer.MAX_VALUE / 2;
			}
		}
		return matrix;
	}
	
	private void fillMatrixWithDefaultCityDistances(int[][] matrix, 
							List<CityDistance> distances, List<String> cityNames)  {
		for(CityDistance cd : distances) {
			int i = cityNames.indexOf(cd.getCityA());
			int j = cityNames.indexOf(cd.getCityB());
			matrix[i][j] = cd.getDistance();
			matrix[j][i] = cd.getDistance();
		}
	}
	
	private void resolvePathsForMatrix(int[][] matrix, int[][] bigMatrix, 
								List<String> cityNames, List<City> rpCities) {
		int n = matrix.length;
		for(int i = 0; i < n; i++) {
			for(int j = i + 1; j < n; j++) {
				int k = cityNames.indexOf(rpCities.get(i).getCurrentCity());
				int m = cityNames.indexOf(rpCities.get(j).getCurrentCity());
				if(bigMatrix[k][m] != Integer.MAX_VALUE / 2) {
					matrix[i][j] = bigMatrix[k][m];
					matrix[j][i] = bigMatrix[k][m];
				} else {
					List<Integer> route = new ArrayList<>();
					log.info("Trying to calulate path between " + rpCities.get(i).getCurrentCity() + " and " + rpCities.get(j).getCurrentCity());
					int value = getShortestPath(bigMatrix, route, k, m);	
					matrix[i][j] = value;
					matrix[j][i] = value;
					String path = "";
					for(Integer x : route) {
						path += x + " ";
					}
					log.debug(path);
				}
			}
		}
	}
	
	private static final int ANNEALING_START_TEMPERAURE = 1000;
	private static final int ANNEALING_END_TEMPERATURE = 75;
	private static final int MONTH_HOURS_LIMIT = 176;
	
	@Autowired
	private CargoDAO cargoDAO;
	
	@Autowired
	private CityDAO cityDAO;
	
	@Autowired
	private CityDistanceDAO cityDistanceDAO;
	
	@Autowired
	private CarDAO carDAO;
	
	@Autowired
	private OrderDAO orderDAO;
	
	@Autowired
	private UserDAO userDAO;
	
	@Autowired
	private RoutePointDAO routePointDAO;
	
	private static final Logger log = Logger.getLogger(CargoAndOrdersService.class);
}
