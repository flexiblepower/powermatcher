<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@page isErrorPage="true"
	language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<% String errorMessage = (String)request.getAttribute("config.web.errormsg"); %>
<% String exceptionMsg = (String)request.getAttribute("config.web.exception"); %>
<html>
<head>
   <title>Publish Telemetry Message Error</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>

<body>
<h2>Failed to send JMS message</h2>

	<fieldset>
		<legend>Error message</legend>
		<%= errorMessage %>
	</fieldset>
	<fieldset>
		<legend>Exception</legend>
		<%= exceptionMsg %>
	</fieldset>
	<form action="selectnode"><input type="submit" value="Try again">
	</form>
</body>
</html>
