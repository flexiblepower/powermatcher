(function () {
	
	// Fjodor van Veen 2014
	
	Render = function () {
		
	};
	
	// global settings
	// block size is the size of agents
	
	Render.blockSize = 140;
	Render.imageMap = {};
	Render.classMap = {};
	Render.zoom = 1;
	
	// zooming in and out
	
	Render.updateScale = function () {
		
		for (var a = 0; a < agentList.length; a++) {
			
			agentList[a].x /= Render.blockSize / (140 * Render.zoom);
			agentList[a].y /= Render.blockSize / (140 * Render.zoom);
			
		}
		
		Render.blockSize = 140 * Render.zoom;
		
		DOM.style(agents, {"font-size":Math.round(Render.zoom * 10) + "px"});
		
	};
	
	Render.init = function () {
		
		canvas = DOM.placeCanvas({width:DOM.width, height:DOM.height});
		agents = DOM.placeElement("div", {id:"agents"});
		menu = DOM.placeElement("div", {id:"menu"});
		textInput = DOM.placeElement("input", {type:"text", id:"textInput"});
		feedback = DOM.placeElement("span", {id:"feedback"});
		
		DOM.style(canvas.canvas, {"z-index":"-100"});
		
	};
	
	// load the icons for different agents from an xml file
	
	Render.loadIcons = function (xml) {
		
		var nodes;
		var a;
		
		xml = xml.documentElement;
		nodes = xml.getElementsByTagName("auctioneer");
		Render.imageMap["Auctioneer"] = [];
		Render.classMap["Auctioneer"] = [];
		
		for (a = 0; a < nodes.length; a++) {
			
			Render.imageMap["Auctioneer"][a] = nodes[a].getAttribute("url");
			Render.classMap["Auctioneer"][a] = nodes[a].getAttribute("class");
			
		}
		
		nodes = xml.getElementsByTagName("objective");
		Render.imageMap["Objective"] = [];
		Render.classMap["Objective"] = [];
		
		for (a = 0; a < nodes.length; a++) {
			
			Render.imageMap["Objective"][a] = nodes[a].getAttribute("url");
			Render.classMap["Objective"][a] = nodes[a].getAttribute("class");
			
		}
		
		nodes = xml.getElementsByTagName("concentrator");
		Render.imageMap["Concentrator"] = [];
		Render.classMap["Concentrator"] = [];
		
		for (a = 0; a < nodes.length; a++) {
			
			Render.imageMap["Concentrator"][a] = nodes[a].getAttribute("url");
			Render.classMap["Concentrator"][a] = nodes[a].getAttribute("class");
			
		}
		
		nodes = xml.getElementsByTagName("device");
		Render.imageMap["Device"] = [];
		Render.classMap["Device"] = [];
		
		for (a = 0; a < nodes.length; a++) {
			
			Render.imageMap["Device"][a] = nodes[a].getAttribute("url");
			Render.classMap["Device"][a] = nodes[a].getAttribute("class");
			
		}
		
	};
	
	Render.zoomIn = function () {
		
		Render.zoom *= 1.2;
		
		Render.updateScale();
		
	};
	
	Render.zoomOut = function () {
		
		Render.zoom /= 1.2;
		
		Render.updateScale();
		
	};
	
	// change the class (for xml export) and the image of an agent
	
	Render.cycleSelectedAgentsIcons = function (agent) {
		
		if (agent) agent.selected = true;
		
		for (var a = 0; a < agentList.length; a++) {
			
			var agent = agentList[a];
			
			if (agent.selected) {
				
				agent.clss++;
				
				if (agent.clss > Render.classMap[agent.kind].length - 1) agent.clss = 0;
				
				agent.clssName = Render.classMap[agent.kind][agent.clss];
				
				DOM.style(agent, {"background-image":"url(" + Render.imageMap[agent.kind][agent.clss] + ")"});
				
			}
			
		}
		
	};
	
	// default keyboard actions are disabled; until...
	
	Render.initiateNameChange = function (agent) {
		
		if (agent) agent.selected = true;
		
		isTyping = !isTyping;
		
		if (isTyping) {
			
			textInput.focus();
			textInput.value = "";
			
			for (var e = 0; e < agentList.length; e++) {
				
				if (agentList[e].selected) {
					
					textInput.value = agentList[e].name;
					
				}
				
			}
			
		} else {
			
			textInput.blur();
			
		}
		
		for (var f = 0; f < agentList.length; f++) {
			
			DOM.removeClass(agentList[f].nameBox, "typing");
			
		}
		
	};
	
	// spawn the minibar on top af an agent
	
	Render.spawnAgentFunctionButtons = function (agent) {
		
		if (DOM.byClass("miniButtonBar", agent).length > 0) return;
		
		var miniBar = DOM.placeElement("div", {className:"miniButtonBar"}, agent);
		var button;
		
		button = DOM.placeElement("button", {className:"menuButton miniFunction", id:"connect"}, miniBar);
		DOM.placeText("connect", button);
		
		button = DOM.placeElement("button", {className:"menuButton miniFunction", id:"disconnect"}, miniBar);
		DOM.placeText("disconnect", button);
		
		button = DOM.placeElement("button", {className:"menuButton miniFunction", id:"cycle"}, miniBar);
		DOM.placeText("cycle", button);
		
		button = DOM.placeElement("button", {className:"menuButton miniFunction", id:"rename"}, miniBar);
		DOM.placeText("rename", button);
		
		button = DOM.placeElement("button", {className:"menuButton miniFunction", id:"delete"}, miniBar);
		DOM.placeText("delete", button);
		
	};
	
	// kill the minibar
	
	Render.deleteAgentFunctionButtons = function (agent) {
		
		if (!DOM.byClass("miniButtonBar", agent).length > 0) return;
		
		DOM.removeElement(DOM.byClass("miniButtonBar", agent)[0]);
		
	};
	
	// not just warnings, just really status updates/user feedback
	
	Render.warnUser = function (message) {
		
		while (feedback.childNodes[0]) DOM.removeElement(feedback.childNodes[0]);
		
		DOM.placeText(message, feedback);
		
	};
	
	// the entire core interface
	
	Render.createMenuButtons = function () {
		
		var button;
		
		// agents
		
		var y = 15;
		
		var step = 6;
		
		//line 1
		button = DOM.placeElement("button", {id:"aucButton", className:"menuButton"}, menu);
		DOM.style(button, {top:y + "%", left:10 + "px"});
		DOM.placeText("Auctioneer", button);
		
		button = DOM.placeElement("button", {id:"zoomInButton", className:"menuButton"}, menu);
		DOM.style(button, {top:y + "%", right:10 + "px"});
		DOM.placeText("Zoom in", button);
		
		button = DOM.placeElement("button", {id:"panUpButton", className:"menuButton panningButton"}, menu);
		DOM.style(button, {top:y + "%", left:50 + "%"});
		DOM.placeText("▲", button);
		
		
		//line 2
		y += step;
		button = DOM.placeElement("button", {id:"objButton", className:"menuButton"}, menu);
		DOM.style(button, {top:y + "%", left:10 + "px"});
		DOM.placeText("Objective", button);
		
		button = DOM.placeElement("button", {id:"zoomOutButton", className:"menuButton"}, menu);
		DOM.style(button, {top:y + "%", right:10 + "px"});
		DOM.placeText("Zoom out", button);
		
		//line 3
		
		y += step;
		button = DOM.placeElement("button", {id:"conButton", className:"menuButton"}, menu);
		DOM.style(button, {top:y + "%", left:10 + "px"});
		DOM.placeText("Concentrator", button);
		
		button = DOM.placeElement("button", {id:"organizeButton", className:"menuButton"}, menu);
		DOM.style(button, {top:y + "%", right:10 + "px"});
		DOM.placeText("Organize", button);
		
		//line 4
		
		y += step;
		button = DOM.placeElement("button", {id:"devButton", className:"menuButton"}, menu);
		DOM.style(button, {top:y + "%", left:10 + "px"});
		DOM.placeText("Device", button);
		
		//line 5
		y += step*3;
		button = DOM.placeElement("button", {id:"panLeftButton", className:"menuButton panningButton"}, menu);
		DOM.style(button, {top:y + "%", left:10 + "px"});
		DOM.placeText("◀", button);
		
		button = DOM.placeElement("button", {id:"panRightButton", className:"menuButton panningButton"}, menu);
		DOM.style(button, {top:y + "%", right:10 + "px"});
		DOM.placeText("▶", button);
		
		//line 9
		y  = 5;
		button = DOM.placeElement("button", {id:"settingsButton", className:"menuButton"}, menu);
		DOM.style(button, {bottom:y + "%", left:10 + "px"});
		DOM.placeText("Settings", button);
		
		button = DOM.placeElement("button", {id:"panDownButton", className:"menuButton panningButton"}, menu);
		DOM.style(button, {bottom:y + "%", left:50 + "%"});
		DOM.placeText("▼", button);
		
		
		//line 8
		y += step;
		button = DOM.placeElement("button", {id:"exportButton", className:"menuButton"}, menu);
		DOM.style(button, {bottom:y + "%", left:10 + "px"});
		DOM.placeText("Export", button);
		
		
		//line 7
		y += step;
		button = DOM.placeElement("button", {id:"saveButton", className:"menuButton"}, menu);
		DOM.style(button, {bottom:y + "%", left:10 + "px"});
		DOM.placeText("Save", button);
		
		//line 6
		y += step;
		button = DOM.placeElement("button", {id:"loadButton", className:"menuButton"}, menu);
		DOM.style(button, {bottom:y + "%", left:10 + "px"});
		DOM.placeText("Load", button);

	};
	
	// the connections are drawn on a canvas behind the agent container div
	
	Render.drawConnections = function () {
		
		canvas.lineWidth = (Render.blockSize / 28);
		
		for (var e = 0; e < agentList.length; e++) {
			
			var agent1 = agentList[e];
			
			canvas.beginPath();
			
			if (agent1.childBlocks.length == 0) continue;
			
			for (var f = 0; f < agent1.childBlocks.length; f++) {
				
				var agent2 = Controller.findAgent(agent1.childBlocks[f]);
				
				canvas.strokeStyle = "#bbb";
				
				canvas.moveTo(	agent1.x + 0.5 * DOM.rectangle(agent1).width,
								agent1.y + 0.5 * DOM.rectangle(agent1).height);
				canvas.lineTo(	agent2.x + 0.5 * DOM.rectangle(agent2).width,
								agent2.y + 0.5 * DOM.rectangle(agent2).height);
				
			}
			
			canvas.stroke();
			
		}
		
	};
	
	// when the user resizes the window
	
	Render.resize = function () {
		
		DOM.removeElement(canvas.canvas);
		
		canvas = DOM.placeCanvas({width:DOM.width, height:DOM.height});
		
		DOM.style(canvas.canvas, {"z-index":"-100"});
		
		Render.drawConnections();
		
	};
	
	// settings window
	
	Render.spawnExportWindow = function () {
		
		if (exportWindow) return;
		
		exportWindow = DOM.placeElement("div", {id:"exportWindow"});
		
		DOM.placeText("File/cluster name:", exportWindow);
		DOM.placeElement("input", {id:"exportFileName", className:"param", type:"text", value:params.fileName}, exportWindow);
		
		DOM.placeText("Export path:", exportWindow);
		DOM.placeElement("input", {id:"exportPath", className:"param", type:"text", value:params.exportPath}, exportWindow);
		
		DOM.placeText("Market reference:", exportWindow);
		DOM.placeElement("input", {id:"exportMarket", className:"param", type:"number", value:params.reference, step:"1"}, exportWindow);
		
		DOM.placeText("Minimum price:", exportWindow);
		DOM.placeElement("input", {id:"exportMin", className:"param", type:"number", value:params.min, step:"1e-18"}, exportWindow);
		
		DOM.placeText("Maximum price:", exportWindow);
		DOM.placeElement("input", {id:"exportMax", className:"param", type:"number", value:params.max, step:"1e-18"}, exportWindow);
		
		DOM.placeText("Price steps:", exportWindow);
		DOM.placeElement("input", {id:"exportSteps", className:"param", type:"number", value:params.step, step:"1"}, exportWindow);
		
		DOM.placeText("Significance:", exportWindow);
		DOM.placeElement("input", {id:"exportSignificance", className:"param", type:"number", value:params.significance, step:"1"}, exportWindow);
		
		var button = DOM.placeElement("button", {id:"cancelFinalButton", className:"menuButton"}, exportWindow);
		DOM.placeText("Close", button);
		
	};
	
	// sync dom/json
	
	Render.repositionAgents = function () {
		
		for (var a = 0; a < agentList.length; a++) {
			
			DOM.style(agentList[a], {left:agentList[a].x + "px", top:agentList[a].y + DOM.topMarging + "px"});
			
		}
		
	};
	
	// used to be a lot more specific.
	// add a warning here if you want to make a new constraint
	// note: sel is selected, you're trying to connect to tar (target)
	
	Render.displayConnectionError = function (sel, tar, connectedWithSelected) {
		
		var message = "";
		
		if (connectedWithSelected.length > 0) message = "An agent cannot have more than one parent.";
		if (sel.kind == "Auctioneer") message = "You cannot bind an auctioneer to anything.";
		if (tar.kind == "Device") message = "You cannot bind anything to a device.";
		if (tar.kind == "Objective") message = "You cannot bind anything to an objective.";
		
		Render.warnUser(message);
		
		return message.length > 0;
		
	};
	
})();