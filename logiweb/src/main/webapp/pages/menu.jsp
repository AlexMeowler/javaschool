<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<nav class = "navbar navbar-expand-lg fixed-top">
		<a class="navbar-brand" href="<c:url value="/home"/>">Home</a>
		<sec:authorize access = "isAuthenticated()">
		<c:url value="/logout" var = "logoutURL"/>
		<form:form style="margin-block-end: 0" id="logout" action="${logoutURL}" method="POST">
				<a class = "navbar-brand" href="javascript:;" onclick="document.getElementById('logout').submit();">Log out</a>
		</form:form>
		</sec:authorize>
		<button class = "navbar-toggler" type = "button" data-toggle = "collapse" data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
			<span class="navbar-toggler-icon"></span>
		</button>
		<div id="navbarSupportedContent" class="collapse navbar-collapse">
			<ul class = "navbar-nav mr-4">
				<!-- role based links display -->
				<sec:authorize access = "hasAuthority('ADMIN')">
				<li class="navitem"><a class = "nav-link" href="<c:url value="/adminPage"/>">Administrator page</a></li>
				</sec:authorize>
				<sec:authorize access = "hasAnyAuthority('MANAGER', 'ADMIN')">
				<li class="navitem"><a class = "nav-link" href="<c:url value="/managerPage"/>">Manager page</a></li>
				<li class="navitem"><a class = "nav-link" href="<c:url value="/cargoAndOrders"/>">Cargo &amp; Orders</a></li>
				</sec:authorize>
				<sec:authorize access = "hasAuthority('DRIVER')">
				<li class="navitem"><a class = "nav-link" href="<c:url value="/driverPage"/>">Driver page</a></li>
				</sec:authorize>
			</ul>
		</div>
</nav>