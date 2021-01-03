package org.retal.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.retal.domain.Cargo;
import org.retal.domain.City;
import org.retal.domain.CityDistance;
import org.retal.domain.Car;
import org.retal.domain.Order;
import org.retal.domain.RoutePoint;
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
		Object[] carAndPath = findAppropriateCarAndCalculatePath(list, distances, cities, session);
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
	
	public Object[] findAppropriateCarAndCalculatePath(List<RoutePoint> list, List<CityDistance> distances, List<City> cities, Session session) {
		log.info("Searching for cars and paths...");
		Set<City> allRoutePointCities = new HashSet<>();
		List<String> cityNames = new ArrayList<>();
		for(City city : cities) {
			cityNames.add(city.getCurrentCity());
			log.debug("Added city name " + city.getCurrentCity());
		}
		log.debug("Mapped city names");
		for(RoutePoint rp : list) {
			allRoutePointCities.add(rp.getCity());
		}
		for(RoutePoint rp: list) {
			if(rp.getIsLoading()) {
				// build matrix
				log.debug("Trying to build matrix");
				int n = distances.size();
				int[][] bigMatrix = new int[n][n];
				for(int i = 0; i < n; i++) {
					for(int j = 0; j < n; j++) {
						bigMatrix[i][j] = Integer.MAX_VALUE / 2;
					}
				}
				log.debug("Built base of big matrix");
				for(CityDistance cd : distances) {
					int i = cityNames.indexOf(cd.getCityA());
					int j = cityNames.indexOf(cd.getCityB());
					bigMatrix[i][j] = cd.getDistance();
					bigMatrix[j][i] = cd.getDistance();
				}
				log.debug("Filled big matrix");
				List<City> rpCities = new ArrayList<>(allRoutePointCities);
				n = rpCities.size();
				int[][] matrix = new int[n][n];
				for(int i = 0; i < n; i++) {
					for(int j = 0; j < n; j++) {
						matrix[i][j] = Integer.MAX_VALUE / 2;
					}
				}
				log.debug("Built base of small matrix, n = " + n);
				for(int i = 0; i < n; i++) {
					for(int j = i + 1; j < n; j++) {
						int k = cityNames.indexOf(rpCities.get(i).getCurrentCity());
						int m = cityNames.indexOf(rpCities.get(j).getCurrentCity());
						log.debug("k = " + k + "; m = " + m);
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
				for(int i = 0; i < n; i++) {
					String s = "";
					for(int j = 0; j < n; j++) {
						s += matrix[i][j] + " ";
					}
					log.debug(s);
				}
				log.info("Calculating optimized route...");
				List<String> paths = findOptimizedRoute(matrix, rpCities.indexOf(rp.getCity()));
				String p = "";
				for(String s : paths) {
					p += s + " ";
				}
				log.debug(p);
			}
		}
		Set<City> startCities = new HashSet<>();
		for(RoutePoint rp : list) {
			if(rp.getIsLoading()) {
				startCities.add(rp.getCity());
			}
		}
		for(City city : allRoutePointCities) {
			session.persist(city);
			for(Car car : city.getCars()) {
				log.info(car.toString());
			}
			session.detach(city);
		}
		return null;
	}
	
	public List<String> findOptimizedRoute(int[][] m, int from) {
		final int MAX = Integer.MAX_VALUE / 2;
		final int eps = 100000;
		/*int[][] m = new int[n + 1][];
		m[0] = new int[] { MAX, 20, 18, 12, 8, 0 };
		m[1] = new int[] { 5, MAX, 14, 7, 11, 0 };
		m[2] = new int[] { 12, 18, MAX, 6, 11, 0 };
		m[3] = new int[] { 11, 17, 11, MAX, 12, 0 };
		m[4] = new int[] { 5, 5, 5, 5, MAX, 0 };
		m[5] = new int[] { 0, 0, 0, 0, 0, 0 };*/
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
			//11
			matrixCopy.put(maxI + "," + maxJ, MAX);
			TSPTree leaveInclude = new TSPTree(maxI + "*-" + maxJ + "*", tree.getValue() + est, matrixCopy);
			currentLeave.addChild(leaveInclude);
			//12
			result = new TSPTree("", Integer.MAX_VALUE, null);
			findNextLeave(tree);
			currentLeave = result;
			startFromPointTwo = result.getName().contains("*");
		} while(countNotNullElements(result.getMatrix()) != 4);
		log.debug("Calculation finished");
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
		for(int i = 0; i < routes.size(); i++) {
			if(routes.get(i).getName().contains("*")) {
				routes.remove(i);
				i--;
			}
		}
		TSPTree start = null;
		for(TSPTree t : routes) {
			if(t.getName().split("-")[0].equals("0")) {
				start = t;
			}
		}
		List<String> path = new ArrayList<>();
		routes.remove(start);
		path.add(start.getName().split("-")[0]);
		while(routes.size() != 0) {
			for(TSPTree t : routes) {
				if(start.getName().split("-")[1].equals(t.getName().split("-")[0])) {
					path.add(t.getName().split("-")[0]);
					start = t;
					break;
				}
			}
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
	
	private int countNotNullElements(Map<String, Integer> map) {
		int counter = 0;
		for(Map.Entry<String, Integer> e : map.entrySet()) {
			counter = e.getValue() != null ? counter + 1 : counter;
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
	}
	
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
	
	private TSPTree result;
	private int depth;
	private int MAX_CITIES = 50;
	private int n;
	
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
	private RoutePointDAO routePointDAO;
	
	private static final Logger log = Logger.getLogger(CargoAndOrdersService.class);
}
