<%@ page import = "org.retal.model.*, org.retal.dao.*" %>
<html>
<body>
<h2>Hello World!</h2>
<h2>
	<%
		UserDAO crud = new UserDAO();
		User test = crud.read(1);
		out.print(test.toString());
	%>
</h2>
</body>
</html>
