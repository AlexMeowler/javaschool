<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<!doctype html>
<html lang="en">
<head>
    <title>Cargo and orders managing</title>
    <link rel="shortcut icon" href="<c:url value="static/icon.png"/>" type="image/png">
    <link rel="stylesheet" type="text/css" href="<c:url value="static/bootstrap.min.css"/>">
    <link rel="stylesheet" type="text/css" href="<c:url value="static/menu.css"/>">
    <link rel="stylesheet" type="text/css" href="<c:url value="static/main.css"/>">
    <c:if test = "${empty counter_value}"><c:set var = "counter_value" value = "0"/></c:if>
    <script>var counter = ${counter_value}</script>
    <script type="text/javascript" src = "<c:url value="static/my_js_library.js"/>"></script>
</head>
<body>
    <jsp:include page="menu.jsp"/>
    <div class="container main-body">
    <h3>Welcome, manager ${current_user_name}!</h3>
    <h3>Completed orders list:</h3>
    <table class="info-table">
        <caption hidden="true">Orders list</caption>
        <tr>
            <th scope="col">Order ID</th>
            <th scope="col">Assigned car</th>
            <th scope="col">Route</th>
            <th scope="col">Currently assigned drivers</th>
            <th scope="col">Assigned cargo</th>
        </tr>
        <c:forEach var="order" items="${ordersList}" varStatus="i">
        <tr>
            <td>${order.id}</td>
            <td>${order.completedOrderInfo.carId}</td>
            <td>${order.route}</td>
            <td>
                <c:forEach var="driver" items="${order.completedOrderInfo.driverStringToList()}">${driver}<br></c:forEach>
            </td>
            <td>
                <c:forEach var="cargo" items="${order.cargo}">
                    <c:set var = "iter" value = "${cargo.points.iterator()}"/>
                    <c:set var = "pointA" value = "${iter.next()}"/>
                    <c:set var = "pointB" value = "${iter.next()}"/>
                    <c:if test="${pointA.isLoading}">
                        <c:set var = "cityFrom" value = "${pointA.city.currentCity}"/>
                        <c:set var = "cityTo" value = "${pointB.city.currentCity}"/>
                    </c:if>
                    <c:if test="${pointB.isLoading}">
                        <c:set var = "cityFrom" value = "${pointB.city.currentCity}"/>
                        <c:set var = "cityTo" value = "${pointA.city.currentCity}"/>
                    </c:if>
                    ${cargo.id}: ${cargo.name} (To deliver from <strong>${cityFrom}</strong> to <strong>${cityTo}</strong>)<br>
                </c:forEach>
            </td>
        </tr>
        </c:forEach>
    </table>
    <div class="footer"></div>
    </div>
</body>
</html>