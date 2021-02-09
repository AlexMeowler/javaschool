package org.retal.logiweb.service.logic;

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
import org.retal.logiweb.config.spring.app.hibernate.HibernateSessionFactory;
import org.retal.logiweb.dao.CityDAO;
import org.retal.logiweb.dao.CityDistanceDAO;
import org.retal.logiweb.dao.OrderDAO;
import org.retal.logiweb.dao.OrderRouteProgressionDAO;
import org.retal.logiweb.dao.RoutePointDAO;
import org.retal.logiweb.dao.UserDAO;
import org.retal.logiweb.domain.entity.Car;
import org.retal.logiweb.domain.entity.Cargo;
import org.retal.logiweb.domain.entity.City;
import org.retal.logiweb.domain.entity.CityDistance;
import org.retal.logiweb.domain.entity.Order;
import org.retal.logiweb.domain.entity.OrderRouteProgression;
import org.retal.logiweb.domain.entity.RoutePoint;
import org.retal.logiweb.domain.entity.SessionInfo;
import org.retal.logiweb.domain.entity.User;
import org.retal.logiweb.domain.entity.UserInfo;
import org.retal.logiweb.domain.enums.CargoStatus;
import org.retal.logiweb.domain.enums.DriverStatus;
import org.retal.logiweb.domain.enums.UserRole;
import org.retal.logiweb.dto.RoutePointDTO;
import org.retal.logiweb.dto.RoutePointListWrapper;
import org.retal.logiweb.service.jms.NotificationSender;
import org.retal.logiweb.service.validators.RoutePointsValidator;
import org.retal.table.jms.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;

/**
 * Service, containing business-logic methods regarding
 * {@linkplain org.retal.logiweb.domain.entity.Cargo Cargo} and
 * {@linkplain org.retal.logiweb.domain.entity.Order Order} with
 * {@linkplain org.retal.logiweb.domain.entity.RoutePoint RoutePoint} entities.
 * 
 * @author Alexander Retivov
 *
 */
@Service
public class OrderService {

  private static final int ANNEALING_START_TEMPERAURE = 1000;
  private static final int ANNEALING_END_TEMPERATURE = 76;
  private static final String PATH_DELIMETER = "->";
  public static final int MONTH_HOURS_LIMIT = 176;
  public static final int AVERAGE_CAR_SPEED = 80;

  private final CityDAO cityDAO;

  private final CityDistanceDAO cityDistanceDAO;

  private final OrderDAO orderDAO;

  private final UserDAO userDAO;

  private final OrderRouteProgressionDAO orderRouteProgressionDAO;

  private final CarService carService;

  private CargoService cargoService;

  private final RoutePointDAO routePointDAO;

  private final Validator routePointsValidator;

  private final SessionInfo sessionInfo;

  private final NotificationSender sender;

  private Calendar calendarForSimulation = null;

  private static final Logger log = Logger.getLogger(OrderService.class);

  /**
   * Creates an instance of this class using constructor-based dependency injection.
   */
  @Autowired
  public OrderService(CityDAO cityDAO, CityDistanceDAO cityDistanceDAO, OrderDAO orderDAO,
      UserDAO userDAO, OrderRouteProgressionDAO orderRouteProgressionDAO, CarService carService,
      RoutePointDAO routePointDAO, RoutePointsValidator routePointsValidator,
      SessionInfo sessionInfo, NotificationSender sender) {
    this.cityDAO = cityDAO;
    this.cityDistanceDAO = cityDistanceDAO;
    this.orderDAO = orderDAO;
    this.userDAO = userDAO;
    this.orderRouteProgressionDAO = orderRouteProgressionDAO;
    this.carService = carService;
    this.routePointDAO = routePointDAO;
    this.routePointsValidator = routePointsValidator;
    this.sessionInfo = sessionInfo;
    this.sender = sender;
  }

  public List<Order> getAllOrders() {
    return orderDAO.readAll();
  }

  public Order getOrder(Integer primaryKey) {
    return orderDAO.read(primaryKey);
  }

  @Autowired
  public void setCargoService(CargoService cargoService) {
    this.cargoService = cargoService;
  }

  public void setCalendarForSimulation(Calendar calendarForSimulation) {
    this.calendarForSimulation = calendarForSimulation;
  }

  /**
   * Checks if order has been started by drivers. Order is considered started if both statements
   * below are true:<br>
   * 1) it is not completed 2) car assigned has already moved from starting city or cargo has been
   * already loaded to that car
   * 
   * @param order order to check
   * @return true if order has been started, false otherwise
   */
  public boolean isOrderStarted(Order order) {
    boolean cargoChanged = false;
    for (Cargo cargo : order.getCargo()) {
      cargoChanged |= !cargo.getStatus().equalsIgnoreCase(CargoStatus.PREPARED.toString());
    }
    return !order.getIsCompleted() && (!order.getCar().getLocation().getCurrentCity()
        .equals(order.getRoute().split(Order.ROUTE_DELIMETER)[0]) || cargoChanged);
  }

  /**
   * Attempts to change order's assigned car to another.
   * 
   * @param data input query matching pattern "A_B" where A is car's registration ID and B is order
   *        ID
   * @return null if car was changed successfully, error message otherwise
   */
  public String tryToChangeOrderCar(String data) {
    Car car = null;
    Order order = null;
    String errorMessage = "";
    try {
      log.debug(data);
      String[] input = data.split("_");
      Integer id = Integer.parseInt(input[0]);
      log.debug(id);
      order = getOrder(id);
      car = carService.getCar(input[1]);
    } catch (Exception e) {
      errorMessage = "Invalid argument, please don't try to change page code.";
    }
    if (car == null || order == null || order.getIsCompleted()) {
      errorMessage = "Invalid argument, please don't try to change page code.";
    }
    if (errorMessage.isEmpty()) {
      order.setCar(car);
      updateOrder(order);
      sender.send(NotificationType.ORDERS_UPDATE);
      sender.send(NotificationType.CARS_UPDATE);
      return null;
    } else {
      log.warn("Hacking attempt by changing page code");
      return errorMessage;
    }
  }

  /**
   * Updates order in database.
   * 
   * @param order order to be updated
   */
  public void updateOrder(Order order) {
    Session session = HibernateSessionFactory.getSessionFactory().openSession();
    orderDAO.setSession(session);
    Transaction transaction = session.beginTransaction();
    orderDAO.update(order);
    session.flush();
    transaction.commit();
    orderDAO.setSession(null);
    session.close();
    sender.send(NotificationType.ORDERS_UPDATE);
  }

  /**
   * Checks order for completion. If order is completed, this method will unassign car and all
   * drivers from this order and mark it as completed.
   * 
   * @param order order to be checked
   * @return true if order is completed, false otherwise
   */
  public boolean checkOrderForCompletion(Order order) {
    boolean isCompleted = true;
    order = orderDAO.read(order.getId());
    for (Cargo c : order.getCargo()) {
      log.debug(c.getStatus());
      isCompleted &= c.getStatus().equalsIgnoreCase(CargoStatus.UNLOADED.toString());
    }
    if (isCompleted) {
      for (UserInfo driverInfo : order.getDriverInfo()) {
        log.debug(driverInfo.getUser().toString());
        driverInfo.setOrder(null);
        driverInfo.setCar(null);
        driverInfo.setHoursDrived(null);
        userDAO.update(driverInfo.getUser());
      }
      sender.send(NotificationType.DRIVERS_UPDATE);
      orderRouteProgressionDAO.delete(order.getOrderRouteProgression());
      order.setIsCompleted(isCompleted);
      order.setCar(null);
      updateOrder(order);
      sender.send(NotificationType.CARS_UPDATE);
      sessionInfo.refreshUser();
    }
    return isCompleted;
  }

  /**
   * Maps list of {@linkplain org.retal.logiweb.dto.RoutePointDTO RoutePointDTOs} to list of
   * {@linkplain org.retal.logiweb.domain.entity.RoutePoint RoutePoints}.
   * 
   * @param list of {@linkplain org.retal.logiweb.dto.RoutePointDTO RoutePointDTOs} to be mapped
   * @return list of {@linkplain org.retal.logiweb.domain.entity.RoutePoint RoutePoints}
   */
  public List<RoutePoint> mapRoutePointDTOsToEntities(List<RoutePointDTO> list) {
    List<RoutePoint> entityList = new ArrayList<>();
    for (RoutePointDTO rpDTO : list) {
      RoutePoint rp = new RoutePoint(cityDAO.read(rpDTO.getCityName()), rpDTO.getIsLoading(),
          cargoService.getCargo(rpDTO.getCargoId()), null);
      entityList.add(rp);
    }
    return entityList;
  }

  /**
   * Validates {@linkplain org.retal.logiweb.domain.entity.RoutePoint RoutePoint} input and if no
   * errors found, attempts to calculate optimal path and assign car and drivers according to that
   * path. If path, car and drivers are selected, then
   * {@linkplain org.retal.logiweb.domain.entity.Order Order} is created and saved to database.
   * 
   * @param wrapper {@linkplain org.retal.logiweb.dto.RoutePointListWrapper wrapper} for list of
   *        route points to be validated and used for order creation
   * @param bindingResult object for storing validation result
   */
  public void createOrderAndRoutePoints(RoutePointListWrapper wrapper,
      BindingResult bindingResult) {
    routePointsValidator.validate(wrapper, bindingResult);
    if (!bindingResult.hasErrors()) {
      List<RoutePoint> list = mapRoutePointDTOsToEntities(wrapper.getList());
      log.info("Mapped " + list.size() + " DTOs to entities");
      List<CityDistance> distances = cityDistanceDAO.readAll();
      List<City> cities = cityDAO.readAll();
      Set<RoutePoint> points = new HashSet<>(list);
      Object[] carAndDriversAndPath =
          findAppropriateCarAndDriversAndCalculatePath(list, distances, cities);
      Car selectedCar = (Car) carAndDriversAndPath[0];
      @SuppressWarnings("unchecked")
      List<User> drivers = (List<User>) carAndDriversAndPath[1];
      String route = (String) carAndDriversAndPath[2];
      Float requiredCapacity = (Float) carAndDriversAndPath[3];
      // i don't know if this will ever be triggered because if route hadn't been calculated
      // then algorithm would have been in an infinite loop
      if (route == null || route.isEmpty()) {
        bindingResult.reject("globalRoute", "Could not calculate route.");
      } else {
        if (selectedCar == null) {
          bindingResult.reject("globalCar",
              "Could not select car. Please make sure there is" + " working car in "
                  + route.split(Order.ROUTE_DELIMETER)[0] + " which has capacity of "
                  + requiredCapacity + " tons.");
        }
        if (drivers == null) {
          bindingResult.reject("globalDrivers",
              "Could not select drivers. Please make sure there are"
                  + " available drivers on route " + route.replace(Order.ROUTE_DELIMETER, "->")
                  + ".");
        }
      }
      if (!bindingResult.hasErrors()) {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        Order order = new Order(false, selectedCar, points, route, requiredCapacity,
            selectedCar.getShiftLength());
        Transaction transaction = session.beginTransaction();
        orderDAO.setSession(session);
        orderDAO.add(order);
        transaction.commit();
        transaction = session.beginTransaction();
        for (User driver : drivers) {
          driver.getUserInfo().setOrder(order);
          userDAO.update(driver);
        }
        sender.send(NotificationType.DRIVERS_UPDATE);
        routePointDAO.setSession(session);
        for (RoutePoint rp : points) {
          rp.setOrder(order);
          routePointDAO.add(rp);
        }
        transaction.commit();
        orderDAO.setSession(null);
        routePointDAO.setSession(null);
        session.close();
        OrderRouteProgression orderRouteProgression = new OrderRouteProgression(0, order);
        orderRouteProgressionDAO.add(orderRouteProgression);
        sender.send(NotificationType.ORDERS_UPDATE);
        sender.send(NotificationType.CARS_UPDATE);
      }
    }
  }

  /**
   * Calculates optimal path and attempts to select car and drivers to complete the order. Optimal
   * path calculation is basically solving the "Traveling Salesman Problem" with additional
   * restrictions, such as:<br>
   * City of unloading for cargo X can not be visited before city of loading for cargo X; <br>
   * Only loading cities can be starting points; <br>
   * Avoid starting from city cycles (that means that city X is city both for loading and unloading
   * come cargo) at all costs;<br>
   * In common case we have open version of TSP (no need to return back to starting city), if not
   * specified directly. <br>
   * All restrictions above are based on three reasons: 1) to avoid infinite loops during
   * calculation 2) to make found routes applicable to the cause 3) to save extra time (the guesses
   * are hypothetical and not proved mathematically)
   * 
   * @see <a href="https://en.wikipedia.org/wiki/Traveling_salesman_problem">Traveling salesman
   *      problem</a>
   * @param list list of {@linkplain org.retal.logiweb.domain.entity.RoutePoint RoutePoint} entities
   * @param distances list of all {@linkplain org.retal.logiweb.domain.entity.CityDistance
   *        CityDistance} entities from database
   * @param cities list of all {@linkplain org.retal.logiweb.domain.entity.City City} entities.
   * @return Object array of size 3: Object[0] is selected
   *         {@linkplain org.retal.logiweb.domain.entity.Car Car}, Object[1] is
   *         List<{@linkplain org.retal.logiweb.domain.entity.User User} (selected drivers),
   *         Object[3] is String (shortest path, cities are divided by ";"), Object[4] is Float
   *         (required capacity, used in showing error message). Path and required capacity are
   *         never null.
   */
  public Object[] findAppropriateCarAndDriversAndCalculatePath(List<RoutePoint> list,
      List<CityDistance> distances, List<City> cities) {
    log.info("Searching for cars and paths...");
    Set<City> allRoutePointCities = new HashSet<>();
    List<String> cityNames =
        cities.stream().map(c -> c.getCurrentCity()).collect(Collectors.toList());
    log.debug("Mapped city names");
    List<Integer> loadingCities = new ArrayList<>();
    List<Integer> unloadingCities = new ArrayList<>();
    // adding all unique cities to set
    for (RoutePoint rp : list) {
      allRoutePointCities.add(rp.getCity());
    }
    List<City> rpCities = new ArrayList<>(allRoutePointCities);
    // filling two lists with city indexes for loading and unloading cargo indexed i
    for (RoutePoint rp : list) {
      if (rp.getIsLoading()) {
        for (RoutePoint rp2 : list) {
          if (!rp2.getIsLoading() && rp2.getCargo().getId() == rp.getCargo().getId()) {
            loadingCities.add(rpCities.indexOf(rp.getCity()));
            unloadingCities.add(rpCities.indexOf(rp2.getCity()));
          }
        }
      }
    }
    String debugStatus = "";
    for (City c : rpCities) {
      debugStatus += c.getCurrentCity() + " ";
    }
    log.debug(debugStatus);
    log.debug(loadingCities.toString());
    log.debug(unloadingCities.toString());
    // maps for calculating optimal route for all cities which can be start points
    Map<String, Integer> optimalRoutes = new HashMap<>();
    Map<String, int[][]> optimalRoutesMatrixes = new HashMap<>();
    for (RoutePoint rp : list) {
      // only loading cities can be starting points to save time(it is my heuristic hypothesis)
      if (rp.getIsLoading()) {
        log.debug(rp.getCity().getCurrentCity());
        // getting load and unload indexes, if current city is only loading/unloading
        // then we can calculate optimal route starting from this city
        // otherwise it is a "cycle" and we skip it
        int loadIndex = loadingCities.indexOf(rpCities.indexOf(rp.getCity()));
        int unloadIndex = unloadingCities.indexOf(rpCities.indexOf(rp.getCity()));
        if (loadIndex == -1 || unloadIndex == -1) {
          // build matrix for traveling between cities
          int n = cities.size();
          int[][] bigMatrix = buildMatrix(n);
          fillMatrixWithDefaultCityDistances(bigMatrix, distances, cityNames);
          log.debug("Filled big matrix");
          n = rpCities.size();
          int[][] matrix = buildMatrix(n);
          // calculate paths for cities which are not connected directly in real life.
          // this is used to make our graph full (i.e. all cities are connected to each other).
          resolvePathsForMatrix(matrix, bigMatrix, cityNames, rpCities);
          log.info("Calculating optimized route (annealing algorithm)...");
          // simulated annealing algorithm is used for calculating path
          int[] annealing = findOptimalPathUsingAnnealingImitation(matrix,
              ANNEALING_START_TEMPERAURE, ANNEALING_END_TEMPERATURE, rpCities.indexOf(rp.getCity()),
              loadingCities, unloadingCities, false);
          String route = "";
          int length = 0;
          for (int path : annealing) {
            route += rpCities.get(path).getCurrentCity() + PATH_DELIMETER;
          }
          length = 0;
          for (int i = 0; i < annealing.length - 1; i++) {
            length += matrix[annealing[i]][annealing[i + 1]];
          }
          log.debug("Suggested route - " + route + "; it's length - " + length);
          optimalRoutes.put(route, length);
          optimalRoutesMatrixes.put(route, matrix);
        } else {
          log.debug("Cycle detected");
        }
      }
    }
    // if we have full cycle (i.e. each city is both city of loading and unloading come cargo)
    // for example:
    // cargo 1 - load in A; drop in B
    // cargo 2 - load in B; drop in A
    // in this case we don't care about restrictions above, so we try to find optimal path
    // starting from each city
    // but technically they will be all the same due to loop
    if (optimalRoutes.size() == 0) {
      log.debug("Did not find optimal route without cycles");
      for (RoutePoint rp : list) {
        if (rp.getIsLoading()) {
          int loadIndex = loadingCities.indexOf(rpCities.indexOf(rp.getCity()));
          int unloadIndex = unloadingCities.indexOf(rpCities.indexOf(rp.getCity()));
          if (loadIndex != -1 && unloadIndex != -1) {
            // build matrix for traveling between cities
            int n = cities.size();
            int[][] bigMatrix = buildMatrix(n);
            fillMatrixWithDefaultCityDistances(bigMatrix, distances, cityNames);
            log.debug("Filled big matrix");
            n = rpCities.size();
            int[][] matrix = buildMatrix(n);
            // calculate paths for cities which are not connected directly in real life
            // this is used to make our graph full (i.e. all cities are connected to each other)
            resolvePathsForMatrix(matrix, bigMatrix, cityNames, rpCities);
            log.info("Calculating optimized route for cycle (annealing algorithm)...");
            int[] annealingPaths = findOptimalPathUsingAnnealingImitation(matrix,
                ANNEALING_START_TEMPERAURE, ANNEALING_END_TEMPERATURE,
                rpCities.indexOf(rp.getCity()), loadingCities, unloadingCities, true);
            StringBuilder builder = new StringBuilder();
            int length = 0;
            for (int x : annealingPaths) {
              builder.append(rpCities.get(x).getCurrentCity() + PATH_DELIMETER);
            }
            String path = builder.toString();
            length = 0;
            for (int i = 0; i < annealingPaths.length - 1; i++) {
              length += matrix[annealingPaths[i]][annealingPaths[i + 1]];
            }
            log.debug("Suggested route - " + path + "; it's length - " + length);
            optimalRoutes.put(path, length);
            optimalRoutesMatrixes.put(path, matrix);
          }
        }
      }
    }
    // now we have to find the shortest optimal route
    String shortestPath = "";
    int shortestPathLength = Integer.MAX_VALUE;
    int[][] shortestPathMatrix = null;
    for (Map.Entry<String, Integer> e : optimalRoutes.entrySet()) {
      if (e.getValue() < shortestPathLength) {
        shortestPath = e.getKey();
        shortestPathLength = e.getValue();
        shortestPathMatrix = optimalRoutesMatrixes.get(shortestPath);
      }
    }
    // calculating required capacity when following path
    String[] shortestPathCities = shortestPath.split(PATH_DELIMETER);
    float requiredCapacity = 0;
    float currentCapacity = 0;
    for (RoutePoint rp : list) {
      if (rp.getCity().getCurrentCity().equals(shortestPathCities[0]) && rp.getIsLoading()) {
        currentCapacity = currentCapacity + rp.getCargo().getMass();
      }
    }
    requiredCapacity = currentCapacity;
    log.debug("Starting capacity(kg) - " + requiredCapacity);
    for (int i = 1; i < shortestPathCities.length - 1; i++) {
      for (RoutePoint rp : list) {
        if (rp.getCity().getCurrentCity().equals(shortestPathCities[i])) {
          int sign = rp.getIsLoading() ? 1 : -1;
          currentCapacity = currentCapacity + sign * rp.getCargo().getMass();
          if (currentCapacity > requiredCapacity) {
            requiredCapacity = currentCapacity;
          }
        }
      }
    }
    requiredCapacity /= 1000;
    log.debug("Required capacity - " + requiredCapacity + " tons");
    String firstCity = shortestPath.split(PATH_DELIMETER)[0];
    // calculate drivers for all possible cars
    List<Car> selectedCarList = new ArrayList<>();
    List<List<User>> selectedDriversList = new ArrayList<>();
    for (City city : rpCities) {
      if (city.getCurrentCity().equals(firstCity)) {
        for (Car car : city.getCars()) {
          List<User> drivers =
              tryToAssignDriversForOrder(shortestPath, shortestPathMatrix, rpCities, car);
          if (car.getOrder() == null && car.getIsWorking()
              && car.getCapacityTons() >= requiredCapacity && drivers != null) {
            selectedCarList.add(car);
            selectedDriversList.add(drivers);
            break;
          }
        }
        break;
      }
    }
    // selecting car with minimal fitting capacity
    log.debug("Matching cars: " + selectedCarList.toString());
    Car selectedCar = selectedCarList.stream()
        .min((a, b) -> (int) (a.getCapacityTons() - b.getCapacityTons())).orElse(null);
    List<User> selectedDrivers = null;
    if (selectedCar != null) {
      selectedDrivers = selectedDriversList.get(selectedCarList.indexOf(selectedCar));
    }
    shortestPath = shortestPath.replace(PATH_DELIMETER, Order.ROUTE_DELIMETER);
    shortestPath = shortestPath.substring(0, shortestPath.length() - 1);
    String driversMessage = selectedDrivers != null ? selectedDrivers.toString() : "null";
    String carMessage = selectedCar != null ? selectedCar.toString() : "null";
    log.debug("Selected drivers, car and route - " + driversMessage + "; " + carMessage + "; "
        + shortestPath);
    return new Object[] {selectedCar, selectedDrivers, shortestPath, requiredCapacity};
  }

  /**
   * Calculates distance between two cities.
   * 
   * @param cityA first city
   * @param cityB second city
   * @return distance between cityA and cityB
   */
  public int lengthBetweenTwoCities(String cityA, String cityB) {
    List<City> cities = cityDAO.readAll();
    List<String> cityNames =
        cities.stream().map(c -> c.getCurrentCity()).collect(Collectors.toList());
    int n = cities.size();
    int[][] bigMatrix = buildMatrix(n);
    List<CityDistance> distances = cityDistanceDAO.readAll();
    fillMatrixWithDefaultCityDistances(bigMatrix, distances, cityNames);
    List<City> inputCities =
        Stream.of(cityDAO.read(cityA), cityDAO.read(cityB)).collect(Collectors.toList());
    n = inputCities.size();
    int[][] matrix = buildMatrix(n);
    resolvePathsForMatrix(matrix, bigMatrix, cityNames, inputCities);
    return matrix[0][1];
  }

  /**
   * Implementation of Dijkstra algorithm to find shortest path between to vertices in graph. Used
   * for calculating paths between cities which are not connected directly.
   * 
   * @param matr matrix of distances between cities
   * @param result list which will store shortest path indexes
   * @param from index of city to start from
   * @param to index of city to end at
   * @return distance length, path is stored in <b>result</b> variable
   */
  private int getShortestPath(int[][] matr, List<Integer> result, int from, int to) {
    int n = matr.length;
    final int MAX = Integer.MAX_VALUE / 2;
    int[] weights = new int[n];
    boolean[] visited = new boolean[n];
    Arrays.fill(weights, MAX);
    weights[from] = 0;
    boolean allVisited = false;
    while (!allVisited) {
      int min = MAX;
      int current = 0;
      for (int i = 0; i < n; i++) {
        if (!visited[i] && weights[i] < min) {
          min = weights[i];
          current = i;
        }
      }
      for (int i = 1; i < n; i++) {
        if (matr[current][i] != MAX) {
          weights[i] = Math.min(weights[i], weights[current] + matr[current][i]);
        }
      }
      visited[current] = true;
      allVisited = true;
      for (boolean flag : visited) {
        allVisited &= flag;
      }
    }
    log.debug("Dijkstra: visited all cities; distance is " + weights[to]);
    int current = to;
    while (current != from) {
      for (int i = 0; i < n; i++) {
        if (weights[current] - matr[i][current] == weights[i]) {
          result.add(current);
          current = i;
        }
      }
    }
    return weights[to];
  }

  /**
   * Simulated annealing algorithm implementation for solving the traveling salesman problem. This
   * is modified version of algorithm based on some restrictions stated
   * {@linkplain #createOrderAndRoutePoints(RoutePointListWrapper, BindingResult) here}
   * 
   * @see <a href="https://en.wikipedia.org/wiki/Simulated_annealing">Simulated annealing
   *      algorithm</a>
   * @param matr matrix of distances between cities
   * @param initialTemperature temperature at which annealing starts
   * @param endTemperature temperature at which annealing ends
   * @param from city index to start from
   * @param loadingCities cities of loading cargo
   * @param unloadingCities cities of unloading cargo
   * @param cycleDetected flag if suggested path has cycles
   * @return array of city indexes containing optimal path
   */
  private int[] findOptimalPathUsingAnnealingImitation(int[][] matr, double initialTemperature,
      double endTemperature, int from, List<Integer> loadingCities, List<Integer> unloadingCities,
      boolean cycleDetected) {
    int n = matr.length;
    // lack of cycle for starting city doesn't guarantee there are no cycles
    // for example 3-1-4-5-1 : oops
    if (!cycleDetected) {
      log.debug("Annealing: pre-checking for possible cycle");
      boolean[] visited = new boolean[n];
      int i = from;
      while (!visited[i] && loadingCities.indexOf(i) != -1) {
        visited[i] = true;
        if (loadingCities.indexOf(i) != -1) {
          i = unloadingCities.get(loadingCities.indexOf(i));
          if (loadingCities.indexOf(i) == -1) {
            int counter = 0;
            for (int j = 0; j < visited.length; j++) {
              counter = !visited[j] ? counter + 1 : counter;
              /*
               * if (!visited[j]) { counter++; }
               */
            }
            if (counter > 1) {
              visited[i] = true;
              for (int j = 0; j < visited.length; j++) {
                if (!visited[j]) {
                  i = j;
                  break;
                }
              }
            }
          }
        }
      }
      if (visited[i]) {
        log.debug("Possible cycle detected");
        cycleDetected = true;
      }
    }
    // if we have cycle in our path, we need to check if its one big cycle or set of different
    // cycles
    if (cycleDetected) {
      log.debug("Annealing: Trying to define cycle type");
      boolean[] visited = new boolean[n];
      int[] answer = new int[n + 1];
      Arrays.fill(answer, -1);
      int k = 0;
      int i = from;
      while (!visited[i]) {
        visited[i] = true;
        answer[k] = i;
        k++;
        if (loadingCities.indexOf(i) != -1) {
          i = unloadingCities.get(loadingCities.indexOf(i));
        } else {
          int index = unloadingCities.indexOf(i);
          int x = loadingCities.get(index);
          for (int j = 0; j < loadingCities.size(); j++) {
            if (loadingCities.get(j) == x && j != index) {
              i = x;
              int[] buffer = new int[answer.length + 1];
              System.arraycopy(answer, 0, buffer, 0, k);
              answer = buffer;
              visited[i] = false;
              break;
            }
          }
        }
        // log.debug(Arrays.toString(answer));
      }
      boolean allVisited = true;
      for (boolean flag : visited) {
        allVisited &= flag;
      }
      if (allVisited) {
        log.debug("Annealing: Full cycle detected - only one possible route");
        answer[answer.length - 1] = i;
        return answer;
      }
    }
    List<Integer> routeList = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      routeList.add(i);
    }
    Collections.shuffle(routeList);
    int valueIndex = routeList.indexOf(from);
    routeList.set(valueIndex, routeList.get(0));
    routeList.set(0, from);
    // in case of set of cycles we have to go back to some cities to deliver cargo
    if (cycleDetected) {
      log.debug("Annealing: Set of cycles detected - editing entry candidate");
      adaptCandidateForCycleProcessing(routeList, loadingCities, unloadingCities);
      n = routeList.size();
    }
    Object[] obj = routeList.toArray();
    int[] route = new int[n];
    for (int i = 0; i < n; i++) {
      route[i] = (Integer) obj[i];
    }
    double currentTemperature = initialTemperature;
    int currentEnergy = calculateEnergy(route, matr);
    int k = 1;
    n = matr.length;
    log.debug("Annealing: annealing");
    while (currentTemperature > endTemperature) {
      int[] routeCandidate =
          generateCandidate(route, n, from, loadingCities, unloadingCities, cycleDetected);
      int candidateEnergy = calculateEnergy(routeCandidate, matr);
      if (candidateEnergy < currentEnergy) {
        currentEnergy = candidateEnergy;
        route = routeCandidate;
      } else {
        double p = getTranstionProbability(candidateEnergy - currentEnergy, currentTemperature);
        if (makeTransit(p)) {
          currentEnergy = candidateEnergy;
          route = routeCandidate;
        }
      }
      currentTemperature = decreaseTemperature(initialTemperature, k);
      // log.debug("k = " + k + "; T = " + currentTemperature);
      k++;
    }
    log.debug("Annealing ended");
    return route;
  }

  /**
   * Utility method to calculate "energy" of current route.
   * 
   * @param route route for calculating energy
   * @param matr matrix of distances between cities
   * @return energy amount
   */
  private int calculateEnergy(int[] route, int[][] matr) {
    int n = route.length;
    int energy = 0;
    for (int i = 0; i < n - 1; i++) {
      energy += matr[route[i]][route[i + 1]];
    }
    return energy;
  }

  /**
   * Utility method for generating new candidate based on previous state.
   * 
   * @param previousState previous state for generating new state
   * @param n state length
   * @param from city index to start from
   * @param loadingCities list of loading cities
   * @param unloadingCities list of unloading cities
   * @param cycleDetected flag which indicates if cycle detected. Used to alter generation
   *        conditions for cycled case.
   * @return new route state
   */
  private int[] generateCandidate(int[] previousState, int n, int from, List<Integer> loadingCities,
      List<Integer> unloadingCities, boolean cycleDetected) {
    List<Integer> state = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      state.add(previousState[i]);
    }
    boolean sequenceCorrect = false;
    while (!sequenceCorrect) {
      Collections.shuffle(state);
      state.set(state.indexOf(from), state.get(0));
      state.set(0, from);
      sequenceCorrect = true;
      if (!cycleDetected) {
        for (int i = 0; i < loadingCities.size(); i++) {
          sequenceCorrect &=
              state.indexOf(loadingCities.get(i)) < state.indexOf(unloadingCities.get(i));
        }
      } else {
        adaptCandidateForCycleProcessing(state, loadingCities, unloadingCities);
      }
    }
    int[] newState = new int[state.size()];
    for (int i = 0; i < newState.length; i++) {
      newState[i] = state.get(i);
    }
    return newState;
  }

  /**
   * Calculates probability of changing state to generated (even if its energy is higher than
   * current).
   * 
   * @param energy current state energy
   * @param temperature current temperature
   * @return probability of state change
   */
  private double getTranstionProbability(int energy, double temperature) {
    return 1 / (1 + Math.exp(energy / temperature));
  }

  /**
   * Decides whether to change state or not, based on given probability.
   * 
   * @param probability probability of transit
   * @return true if state should be changed, else otherwise
   */
  private boolean makeTransit(double probability) {
    return Math.random() <= probability;
  }

  /**
   * Utility function to decrease current temperature.
   * 
   * @param initialTemperature staring temperature
   * @param k iteration counter
   * @return decreased temperature
   */
  private double decreaseTemperature(double initialTemperature, int k) {
    return initialTemperature / Math.log(1.0 + k);
  }

  /**
   * Utility method, required for solving cycles when generating candidate.
   * 
   * @param routeList route list
   * @param loadingCities list of loading cities
   * @param unloadingCities list of unloading cities
   */
  private void adaptCandidateForCycleProcessing(List<Integer> routeList,
      List<Integer> loadingCities, List<Integer> unloadingCities) {
    boolean[] loaded = new boolean[loadingCities.size()];
    boolean[] unloaded = new boolean[unloadingCities.size()];
    for (int i = 0; i < routeList.size(); i++) {
      for (int j = 0; j < loadingCities.size(); j++) {
        if (loadingCities.get(j).equals(routeList.get(i))) {
          loaded[j] = true;
        }
      }
      for (int j = 0; j < unloadingCities.size(); j++) {
        if (unloadingCities.get(j).equals(routeList.get(i)) && loaded[j]) {
          unloaded[j] = true;
        }
      }
    }
    List<Integer> leftCities = new ArrayList<>();
    for (int i = 0; i < unloaded.length; i++) {
      if (!unloaded[i]) {
        leftCities.add(unloadingCities.get(i));
      }
    }
    Collections.shuffle(leftCities);
    for (int i = 0; i < leftCities.size(); i++) {
      routeList.add(leftCities.get(i));
    }
  }

  /**
   * Creates and fills matrix.
   * 
   * @param n matrix size
   * @return matrix filled with {@linkplain Integer#MAX_VALUE Integer.MAX_VALUE} / 2
   */
  private int[][] buildMatrix(int n) {
    int[][] matrix = new int[n][n];
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        matrix[i][j] = Integer.MAX_VALUE / 2;
      }
    }
    return matrix;
  }

  /**
   * Fills given matrix with given city distances.
   * 
   * @param matrix matrix to fill
   * @param distances list of distances between cities
   * @param cityNames list of city names
   */
  private void fillMatrixWithDefaultCityDistances(int[][] matrix, List<CityDistance> distances,
      List<String> cityNames) {
    for (CityDistance cd : distances) {
      int i = cityNames.indexOf(cd.getCityA());
      int j = cityNames.indexOf(cd.getCityB());
      matrix[i][j] = cd.getDistance();
      matrix[j][i] = cd.getDistance();
    }
  }

  /**
   * Fills matrix with calculated path between cities which are not connected directly.
   * 
   * @param matrix matrix of selected cities
   * @param bigMatrix matrix of distances between cities
   * @param cityNames list of city names
   * @param rpCities list of route point cities
   */
  private void resolvePathsForMatrix(int[][] matrix, int[][] bigMatrix, List<String> cityNames,
      List<City> rpCities) {
    int n = matrix.length;
    for (int i = 0; i < n; i++) {
      for (int j = i + 1; j < n; j++) {
        int k = cityNames.indexOf(rpCities.get(i).getCurrentCity());
        int m = cityNames.indexOf(rpCities.get(j).getCurrentCity());
        if (bigMatrix[k][m] != Integer.MAX_VALUE / 2) {
          matrix[i][j] = bigMatrix[k][m];
          matrix[j][i] = bigMatrix[k][m];
        } else {
          List<Integer> route = new ArrayList<>();
          log.info("Trying to calulate path between " + rpCities.get(i).getCurrentCity() + " and "
              + rpCities.get(j).getCurrentCity());
          int value = getShortestPath(bigMatrix, route, k, m);
          matrix[i][j] = value;
          matrix[j][i] = value;
          StringBuilder builder = new StringBuilder();
          for (Integer x : route) {
            builder.append(x + PATH_DELIMETER);
          }
          String path = builder.toString();
          log.debug(path);
        }
      }
    }
  }

  /**
   * Method which will assign drivers for given path and car. The best solution is counted as one
   * driver for whole path.
   * 
   * @param path calculated path
   * @param matrix matrix of distances between cities
   * @param rpCities route points cities
   * @param selectedCar selected cars
   * @param session active session for persisting entities with lazy fetch type
   * @return list of drivers available for assigning to order
   */
  private List<User> tryToAssignDriversForOrder(String path, int[][] matrix, List<City> rpCities,
      Car selectedCar) {
    /*
     * if (selectedCar == null) { return null; }
     */
    List<String> cityNames =
        rpCities.stream().map(c -> c.getCurrentCity()).collect(Collectors.toList());
    log.debug(cityNames.toString());
    String[] cities = path.split(PATH_DELIMETER);
    final int n = cities.length;
    int[] distances = new int[n - 1];
    // list of capable drivers from each city on route
    List<List<User>> drivers = new ArrayList<>();
    for (int i = 0; i < n - 1; i++) {
      int indexCurrentCity = cityNames.indexOf(cities[i]);
      int indexNextCity = cityNames.indexOf(cities[i + 1]);
      distances[i] = matrix[indexCurrentCity][indexNextCity];
      // adding drivers
      List<User> cityDrivers = rpCities.get(indexCurrentCity).getUserInfos().stream()
          .map(ui -> ui.getUser()).filter(this::isDriverCapable)
          .sorted((a, b) -> a.getId() - b.getId()).collect(Collectors.toList());
      drivers.add(i, cityDrivers);
    }
    log.debug(drivers.toString());
    // counter for list of drivers for each city
    int[] counters = new int[n - 1];
    int currentCityIndex = 0;
    int selectedDriverCity = 0;
    int hoursAtDriving = 0;
    List<User> driversChain = new ArrayList<>();
    List<Calendar> calendarChain = new ArrayList<>();
    User driver;
    if (calendarForSimulation == null) {
      calendarForSimulation = new GregorianCalendar();
    }
    calendarChain.add(calendarForSimulation);
    while (currentCityIndex != n - 1 && counters[0] < drivers.get(0).size()) {
      log.debug(driversChain.toString());
      if (counters[selectedDriverCity] < drivers.get(selectedDriverCity).size()) {
        log.debug("Selecting driver");
        driver = drivers.get(selectedDriverCity).get(counters[selectedDriverCity]);
      } else {
        log.debug("Deleting last entry from chain");
        currentCityIndex--;
        selectedDriverCity = currentCityIndex;
        driversChain.remove(driversChain.size() - 1);
        calendarChain.remove(calendarChain.size() - 1);
        calendarForSimulation = calendarChain.get(calendarChain.size() - 1);
        counters[selectedDriverCity]++;
        for (int i = selectedDriverCity + 1; i < counters.length; i++) {
          counters[i] = 0;
        }
        continue;
      }
      int hoursToNextCity =
          (int) Math.round((double) distances[currentCityIndex] / AVERAGE_CAR_SPEED);
      log.debug(hoursToNextCity);
      hoursAtDriving += hoursToNextCity;
      log.debug(hoursAtDriving);
      Calendar copy = (Calendar) calendarForSimulation.clone();
      copy.add(Calendar.HOUR_OF_DAY, hoursToNextCity);
      int hoursWorked = driver.getUserInfo().getHoursWorked();
      if (copy.get(Calendar.MONTH) != calendarForSimulation.get(Calendar.MONTH)) {
        hoursWorked = copy.get(Calendar.HOUR_OF_DAY);
      }
      calendarForSimulation = copy;
      boolean hasTime = hoursWorked + hoursAtDriving <= MONTH_HOURS_LIMIT;
      boolean isCarShiftLengthCapacityReached = hoursAtDriving > selectedCar.getShiftLength();
      log.debug(hasTime + ";" + isCarShiftLengthCapacityReached);
      if (hasTime && !isCarShiftLengthCapacityReached) {
        log.debug("Adding driver to chain");
        currentCityIndex++;
        driversChain.add(driver);
        calendarChain.add(calendarForSimulation);
      } else {
        hoursAtDriving = 0;
        if (selectedDriverCity != currentCityIndex) {
          log.debug("City has changed");
          selectedDriverCity = currentCityIndex;
        } else {
          log.debug("City hasn't changed, switching to next driver");
          counters[selectedDriverCity]++;
        }
      }
    }
    calendarForSimulation = null;
    return currentCityIndex == n - 1 ? driversChain : null;
  }

  /**
   * Checks if driver capable of driving (i.e. driver is on shift, has no assigned order and user is
   * driver)
   * 
   * @param user user to check
   * @return true if user can be used for driving for given order, false otherwise
   */
  private boolean isDriverCapable(User user) {
    boolean isDriver = user.getRole().equalsIgnoreCase(UserRole.DRIVER.toString());
    boolean isOnShift =
        user.getUserInfo().getStatus().equalsIgnoreCase(DriverStatus.ON_SHIFT.toString());
    boolean hasAssignedOrder = user.getUserInfo().getOrder() != null;
    return isDriver && isOnShift && !hasAssignedOrder;
  }
}
