<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"><%@page
	language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<% String messageContents = (String)request.getAttribute("sensorevent.client.jmstest.message"); %>
<html>
<head>
   <title>Publish Telemetry Message Success</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>

<body>
<h2>Message sent successfully.</h2>
<br>
	<fieldset>
		<legend>Message Content</legend>
		<textarea rows="20" cols="120" name="messageTextArea" readonly><%= messageContents %></textarea>
	</fieldset>
	<form action="selectmessage"><input type="submit" value="New message">
	</form>
</body>
</body>
</html>