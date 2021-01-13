package org.retal.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
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
import org.retal.domain.SessionInfo;
import org.retal.domain.User;
import org.retal.domain.UserInfo;
import org.retal.domain.enums.CargoStatus;
import org.retal.domain.enums.DriverStatus;
import org.retal.domain.enums.UserRole;
import org.retal.dto.RoutePointDTO;
import org.retal.dto.RoutePointListWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;

@Service
public class CargoAndOrdersService {

	public List<Cargo> getAllCargo() {
		return cargoDAO.readAll();
	}
	
	public List<Order> getAllOrders() {
		return orderDAO.readAll();
	}
	
	public Order getOrder(Integer primaryKey) {
		return orderDAO.read(primaryKey);
	}
	
	public void updateOrder(Order order) {
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		orderDAO.setSession(session);
		orderDAO.update(order);
		orderDAO.setSession(null);
		session.flush();
		transaction.commit();
		session.close();
	}
	
	public void addNewCargo(Cargo cargo, BindingResult bindingResult, String weight) {
		try {
			Integer weightInt = Integer.parseInt(weight);
			cargo.setMass(weightInt);
		} catch (NumberFormatException e) {
			bindingResult.reject("mass", "Cargo weight length must be non-negative integer");
		}
		cargoValidator.validate(cargo, bindingResult);
		if(!bindingResult.hasErrors()) {
			cargoDAO.add(cargo);
		}
	}
	
	public boolean updateCargoAndCheckOrderCompletion(Integer id, BindingResult bindingResult) {
		Cargo cargo = cargoDAO.read(id);
		User user = sessionInfo.getCurrentUser();
		Order order = orderDAO.read(user.getUserInfo().getOrder().getId());
		if(cargo == null) {
			bindingResult.reject("globalCargo", "Cargo not found");
			log.warn("Cargo not found");
		} else {
			if(!order.getCargo().contains(cargo)) {
				bindingResult.reject("globalCargo", "Attempt to change unassigned to your current"
											+ " order cargo. Please don't try to cahnge page code");
				log.warn("Attempt to access unassigned cargo");
			}
		}
		if(!bindingResult.hasErrors()) {
			CargoStatus status = CargoStatus.valueOf(cargo.getStatus().toUpperCase());
			switch(status) {
			case PREPARED:
				status = CargoStatus.LOADED;
				break;
			case LOADED:
				status = CargoStatus.UNLOADED;
				break;
			case UNLOADED:
			default:
				break;
			}
			String newStatus = status.toString().toLowerCase();
			log.info("Cargo id=" + cargo.getId() + ": changed status from " + cargo.getStatus() + " to " + newStatus);
			cargo.setStatus(newStatus);
			cargoDAO.update(cargo);
			return checkOrderForCompletion(order);
		}
		return false;
	}
	
	private boolean checkOrderForCompletion(Order order) {
		boolean isCompleted = true;
		order = orderDAO.read(order.getId());
		for(Cargo c : order.getCargo()) {
			log.debug(c.getStatus());
			isCompleted &= c.getStatus().equalsIgnoreCase(CargoStatus.UNLOADED.toString());
		}
		if(isCompleted) {
			for(UserInfo driverInfo : order.getDriverInfo()) {
				log.debug(driverInfo.getUser().toString());
				driverInfo.setOrder(null);
				driverInfo.setCar(null);
				driverInfo.setHoursDrived(null);
				userDAO.update(driverInfo.getUser());
			}
			order.setIsCompleted(isCompleted);
			order.setCar(null);
			order.setRequiredShiftLength(null);
			Session session = HibernateSessionFactory.getSessionFactory().openSession();
			Transaction transaction = session.beginTransaction();
			orderDAO.setSession(session);
			orderDAO.update(order);
			orderDAO.setSession(null);
			session.flush();
			transaction.commit();
			session.close();
			sessionInfo.refreshUser();
		}
		return isCompleted;
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
	
	public void createOrderWithRoutePoints(RoutePointListWrapper wrapper, BindingResult bindingResult) {
		routePointsValidator.validate(wrapper, bindingResult);
		if(!bindingResult.hasErrors()) {
			List<RoutePoint> list = mapRoutePointDTOsToEntities(wrapper.getList());
			log.info("Mapped " + list.size() + " DTOs to entities");
			List<CityDistance> distances = cityDistanceDAO.readAll();
			List<City> cities = cityDAO.readAll();
			Session session = HibernateSessionFactory.getSessionFactory().openSession();
			Set<RoutePoint> points = new HashSet<>(list);
			Object[] carAndDriversAndPath = findAppropriateCarAndDriversAndCalculatePath(list, distances, cities, session);
			session.close();
			Car selectedCar = (Car)carAndDriversAndPath[0];
			@SuppressWarnings("unchecked")
			List<User> drivers = (List<User>)carAndDriversAndPath[1];
			String route = (String)carAndDriversAndPath[2];
			Float requiredCapacity = (Float)carAndDriversAndPath[3];
			// i don't know if this will ever be triggered because if way hadn't been calculated
			// then algorithm would have been in an infinite loop
			if(route == null || route.isEmpty()) {
				bindingResult.reject("globalRoute", "Could not calculate route.");
			} else {
				if(selectedCar == null) {
					bindingResult.reject("globalCar", "Could not select car. Please make sure there is"
							+ " working car in " + route.split(";")[0] + " which has capacity of "
							+ requiredCapacity + " tons.");
				}
				if(drivers == null) {
					bindingResult.reject("globalDrivers", "Could not select drivers. Please make sure there are"
							+ " available drivers on route " + route.replace(";", "->") + ".");
				}
			}	
			if(!bindingResult.hasErrors()) {
				session = HibernateSessionFactory.getSessionFactory().openSession();
				Order order = new Order();
				order.setCar(selectedCar);
				order.setPoints(points);
				order.setRoute(route);
				order.setIsCompleted(false);
				order.setRequiredCapacity(requiredCapacity);
				Transaction transaction = session.beginTransaction();
				orderDAO.setSession(session);
				orderDAO.add(order);
				transaction.commit();
				transaction = session.beginTransaction();
				for(User driver : drivers) {
					driver.getUserInfo().setOrder(order);
					userDAO.update(driver);
				}
				routePointDAO.setSession(session);
				for(RoutePoint rp : points) {
					rp.setOrder(order);
					routePointDAO.add(rp);
				}
				transaction.commit();
				orderDAO.setSession(null);
				routePointDAO.setSession(null);
				session.close();
			}
		}
	}
	
	public Object[] findAppropriateCarAndDriversAndCalculatePath(List<RoutePoint> list, 
											List<CityDistance> distances, 
											List<City> cities, Session session) {
		//TODO code refactoring
		log.info("Searching for cars and paths...");
		Set<City> allRoutePointCities = new HashSet<>();
		List<String> cityNames = cities.stream().map(c -> c.getCurrentCity()).collect(Collectors.toList());
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
		Map<String, int[][]> optimalRoutesMatrixes = new HashMap<>();
		for(RoutePoint rp: list) {
			if(rp.getIsLoading()) {
				log.debug(rp.getCity().getCurrentCity());
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
					optimalRoutesMatrixes.put(p, matrix);
				} else {
					log.debug("Cycle detected");
					//optimalRoutes.put("Could not find optimal route", Integer.MAX_VALUE);
				}
			}
		}
		if(optimalRoutes.size() == 0) {
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
						optimalRoutesMatrixes.put(p, matrix);
					}
				}
			}
		}
		String shortestPath = "";
		int shortestPathLength = Integer.MAX_VALUE;
		int[][] shortestPathMatrix = null;
		for(Map.Entry<String, Integer> e : optimalRoutes.entrySet()) {
			if(e.getValue() < shortestPathLength) {
				shortestPath = e.getKey();
				shortestPathLength = e.getValue();
				shortestPathMatrix = optimalRoutesMatrixes.get(shortestPath);
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
		//Car and drivers selection
		List<Car> selectedCarList = new ArrayList<>();
		List<List<User>> selectedDriversList = new ArrayList<>();
		for(City city : rpCities) {
			if(city.getCurrentCity().equals(firstCity)) {
				session.persist(city);
				for(Car car : city.getCars()) {
					List<User> drivers = tryToAssignDriversForOrder(shortestPath, shortestPathMatrix, rpCities, car, session);
					if(car.getOrder() == null &&  car.getIsWorking() && car.getCapacityTons() >= requiredCapacity && drivers != null) {
						selectedCarList.add(car);
						selectedDriversList.add(drivers);
						break;
					}
				}
				session.detach(city);
				break;
			}
		}
		log.debug("Matching cars: " + selectedCarList.toString());
		Car selectedCar = selectedCarList.stream().min((a, b) -> (int)(a.getCapacityTons() - b.getCapacityTons())).orElse(null);
		List<User> selectedDrivers = null;
		if(selectedCar != null) {
			selectedDrivers = selectedDriversList.get(selectedCarList.indexOf(selectedCar));
		}
		shortestPath = shortestPath.replace(" ", ";");
		shortestPath = shortestPath.substring(0, shortestPath.length() - 1);
		String driversMessage = selectedDrivers != null ? selectedDrivers.toString() : "null";
		String carMessage = selectedCar != null ? selectedCar.toString() : "null";
		log.debug("Selected drivers, car and route - " + driversMessage + "; " + carMessage + "; " + shortestPath);
		return new Object[] {selectedCar, selectedDrivers, shortestPath, requiredCapacity};
	}
	
	public int lengthBetweenTwoCities(String cityA, String cityB) {
		List<City> cities = cityDAO.readAll();
		List<String> cityNames = cities.stream().map(c -> c.getCurrentCity()).collect(Collectors.toList());
		int n = cities.size();
		int[][] bigMatrix = buildMatrix(n);
		List<CityDistance> distances = cityDistanceDAO.readAll();
		fillMatrixWithDefaultCityDistances(bigMatrix, distances, cityNames);
		List<City> inputCities = Stream.of(cityDAO.read(cityA), cityDAO.read(cityB)).collect(Collectors.toList());
		n = inputCities.size();
		int[][] matrix = buildMatrix(n);
		resolvePathsForMatrix(matrix, bigMatrix, cityNames, inputCities);
		return matrix[0][1];
	}
	
	private int getShortestPath(int[][] matr, List<Integer> result, int from, int to) {
		int n = matr.length;
		final int MAX = Integer.MAX_VALUE / 2;
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
		//lack of cycle for starting city doesn't guarantee there are no cycles
		//for example 3-1-4-5-1 : oops
		if(!cycleDetected) {
			log.debug("Annealing: pre-checking for possible cycle");
			boolean[] visited = new boolean[n];
			int[] answer = new int[n + 1];
			Arrays.fill(answer, -1);
			int k = 0;
			int i = from;
			while(!visited[i] && loadingCities.indexOf(i) != -1) {
				visited[i] = true;
				answer[k] = i;
				k++;
				if(loadingCities.indexOf(i) != -1) {
					i = unloadingCities.get(loadingCities.indexOf(i));
				}
			}
			if(visited[i]) {
				log.debug("Possible cycle detected");
				cycleDetected = true;
			}
		}
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
				//log.debug(Arrays.toString(answer));
			}
			boolean allVisited = true;
			for(boolean flag : visited) {
				allVisited &= flag;
			}
			if(allVisited) {
				log.debug("Annealing: Full cycle detected - only one possible route");
				answer[answer.length - 1] = i;
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
	
	private List<User> tryToAssignDriversForOrder(String path, int[][] matrix, List<City> rpCities, Car selectedCar, Session session) {
		if(selectedCar == null) {
			return null;
		}
		// delimeter is ' '
		final String delimeter = " ";
		List<String> cityNames = rpCities.stream().map(c -> c.getCurrentCity()).collect(Collectors.toList());
		log.debug(cityNames.toString());
		String[] cities = path.split(delimeter);
		final int n = cities.length;
		int[] distances = new int[n - 1];
		//list of capable drivers from each city on route 
		List<List<User>> drivers = new ArrayList<List<User>>(n - 1);
		for(int i = 0; i < n - 1; i++) {
			int indexCurrentCity = cityNames.indexOf(cities[i]);
			int indexNextCity = cityNames.indexOf(cities[i + 1]);
			distances[i] = matrix[indexCurrentCity][indexNextCity];
			//adding drivers
			session.persist(rpCities.get(indexCurrentCity));
			List<User> cityDrivers = rpCities.get(indexCurrentCity).getUserInfos().stream()
					.map(ui -> ui.getUser()).filter(this::isDriverCapable)
					.collect(Collectors.toList());
			session.detach(rpCities.get(indexCurrentCity));
			drivers.add(i, cityDrivers);
		}
		log.debug(drivers.toString());
		//counter for list of drivers for each city
		int[] counters = new int[n - 1];
		int currentCityIndex = 0;
		int selectedDriverCity = 0;
		int hoursAtDriving = 0;
		List<User> driversChain = new ArrayList<>();
		List<Calendar> calendarChain = new ArrayList<>();
		User driver;
		Calendar calendar = new GregorianCalendar();
		while(currentCityIndex != n - 1 && counters[0] < drivers.get(0).size()) {
			log.debug(driversChain.toString());
			if(counters[selectedDriverCity] < drivers.get(selectedDriverCity).size()) {
				driver = drivers.get(selectedDriverCity).get(counters[selectedDriverCity]);
			} else {
				for(int i = 0; i < drivers.size(); i++) {
					if(drivers.get(i).contains(driversChain.get(driversChain.size() - 1))) {
						selectedDriverCity = i;
						currentCityIndex = i;
						break;
					}
				}
				driversChain.remove(driversChain.size() - 1);
				calendarChain.remove(calendarChain.size() - 1);
				calendar = calendarChain.get(calendarChain.size() - 1);
				counters[selectedDriverCity]++;
				for(int i = selectedDriverCity + 1; i < counters.length; i++) {
					counters[i] = 0;
				}
				continue;
			}
			int hoursToNextCity = (int)Math.round((double)distances[currentCityIndex] / AVERAGE_CAR_SPEED);
			log.debug(hoursToNextCity);
			hoursAtDriving += hoursToNextCity;
			log.debug(hoursAtDriving);
			Calendar copy = (Calendar) calendar.clone();
			copy.add(Calendar.HOUR_OF_DAY, hoursToNextCity);
			int hoursWorked = driver.getUserInfo().getHoursWorked();
			if(copy.get(Calendar.MONTH) != calendar.get(Calendar.MONTH)) {
				hoursWorked = hoursToNextCity - (24 - calendar.get(Calendar.HOUR_OF_DAY));
			}
			calendar = copy;
			boolean hasTime = hoursWorked + hoursAtDriving <= MONTH_HOURS_LIMIT;
			boolean isCarShiftLengthCapacityReached = hoursAtDriving >= selectedCar.getShiftLength();
			log.debug(hasTime + ";" + isCarShiftLengthCapacityReached);
			if(hasTime && !isCarShiftLengthCapacityReached) {
				currentCityIndex++;
				driversChain.add(driver);
				calendarChain.add(calendar);
			} else {
				hoursAtDriving = 0;
				if(selectedDriverCity != currentCityIndex) {
					selectedDriverCity = currentCityIndex;
				} else {
					counters[selectedDriverCity]++;
				}
			}
		}
		return currentCityIndex == n - 1 ? driversChain : null;
	}
	
	private boolean isDriverCapable(User user) {
		boolean isDriver = user.getRole().equalsIgnoreCase(UserRole.DRIVER.toString());
		boolean isOnShift = user.getUserInfo().getStatus().equalsIgnoreCase(DriverStatus.ON_SHIFT.toString());
		boolean hasAssignedOrder = user.getUserInfo().getOrder() != null;
		//TODO month checking
		return isDriver && isOnShift && !hasAssignedOrder;
	}
	
	private static final int ANNEALING_START_TEMPERAURE = 1000;
	private static final int ANNEALING_END_TEMPERATURE = 76;
	private static final int MONTH_HOURS_LIMIT = 176;
	public static final int AVERAGE_CAR_SPEED = 80;
	
	@Autowired
	private CargoDAO cargoDAO;
	
	@Autowired
	private CityDAO cityDAO;
	
	@Autowired
	private CityDistanceDAO cityDistanceDAO;
	
	@Autowired
	private OrderDAO orderDAO;
	
	@Autowired
	private UserDAO userDAO;
	
	@Autowired
	private RoutePointDAO routePointDAO;
	
	@Autowired
	@Qualifier("cargoValidator")
	private Validator cargoValidator;
	
	@Autowired
	@Qualifier("routePointsValidator")
	private Validator routePointsValidator;
	
	@Autowired
	private SessionInfo sessionInfo;
	
	private static final Logger log = Logger.getLogger(CargoAndOrdersService.class);
}
