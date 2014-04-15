<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"><%@page import="java.io.BufferedReader"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="java.io.InputStream"%>
<%@page import="java.io.IOException"%>
<%@page import="net.powermatcher.server.telemetry.tasks.test.TelemetryPublisherServlet"%>
<%@page import="net.powermatcher.server.telemetry.tasks.test.TelemetryPublisherServlet"%>
<%@page
	language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%!

	private String getMessageText(String name) throws IOException {
		String resourceName = "/WEB-INF/resources/TelemetrySchema-" + name + ".xml";
		String text = null;
		
		InputStream is = getServletContext().getResourceAsStream(resourceName);
		if (is != null) {			
			InputStreamReader isr = new InputStreamReader(is, "UTF-8");
			BufferedReader reader = new BufferedReader(isr);
			
			final char[] buffer = new char[0x10000];
			StringBuffer strbuf  = new StringBuffer();
			int read;
			do {
				read = reader.read(buffer, 0, buffer.length);
				if (read > 0) {
					strbuf.append(buffer, 0, read);
				}
			} while (read >= 0);
			text = strbuf.toString();
		}
		
		return text;
	}

%>
<html>
<head>
<%
	String messageTypes[] = { "Alert", "Control", "Measurement", "Request", "Response", "Status", "Topology1", "Topology2" };
%>
<title>Select and Publish Telemetry Message</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>
<body>
	<form action="sendstatus" method="get">
		<fieldset>			
			<legend>Select Status message template</legend>
			<select id="messageSelect" onchange="this.form.elements['messageText'].value = this.form.elements['messageSelect'].value; ">
			 <option value="none">Select template</option>  
			    <%	for (int i=0; i < messageTypes.length; i++) {  %>
      				<option value='<%=getMessageText(messageTypes[i])%>'><%=messageTypes[i]%></option>    
      			<% } %>
			</select>
			<input type="submit" value="Publish Status Message" >
		</fieldset>	
		<fieldset>
			<legend>Enter message text</legend>
			<textarea name="messageText" rows="20" cols="120" name="messageTextArea"></textarea>
		</fieldset>
	</form>
</body>
</html>