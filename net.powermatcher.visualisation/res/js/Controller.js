(function () {
	
	// Fjodor van Veen 2014
	
	// global static variables
	
	// determines whether keyboard events need to be catched
	
	if (window.attachEvent) alert("Google Chrome or Safari recommended.");
	
	isTyping = null;
	
	// settings window
	
	exportWindow = null;
	
	// background canvas for selection and connections
	
	canvas = null;
	
	// menu dom
	
	menu = null;
	
	// dom, should be json for faster access
	
	agents = null;
	
	// json
	
	agentList = null;
	
	// start dragging an agent or a selection
	
	selectingGroup = null;
	
	// invisible, used to catch native user keyboard input
	
	textInput = null;
	
	// feedback div, user feedback
	
	feedback = null;
	
	// setting object
	
	params = null;
	
	// counter for agent id
	
	id = 0;
	
	Controller = function () {
		
		DOM.init();
		DOM.resize = Render.resize;
		DOM.title("Power Configuration Designer");
		
		Mouse.init();
		Mouse.click = fClick;
		Mouse.release = fRelease;
		Mouse.move = fMove;
		
		Render.init();
		
		loadFile({}, Render.loadIcons, "icons.xml", true);
		
		Keyboard.init();
		Keyboard.keyRelease = fKeyPress;
		
		selectingGroup = false;
		agentList = [];
		isTyping = false;
		exportWindow = null;
		params = {	zoom:Render.zoom,
					fileName:"",
					exportPath:"",
					reference:0,
					min:0,
					max:0.99,
					step:100,
					significance:2};
		
		Render.createMenuButtons();
		loadFile({requestKind:"loadState"}, loadState, "database.php");
		
		var millisecondsToWait = 100;
		setTimeout(function() {
			organizeNodes(false);
		}, millisecondsToWait);
		
		
		
	};
	
	Controller.findAgent = function (id) {
		
		for (var a = 0; a < agentList.length; a++) {
			
			if (agentList[a].id == id) return agentList[a];
			
		}
		
	};
	
	function snapAgentsToGrid () {
		
		for (var a = 0; a < agentList.length; a++) {
			
			agentList[a].x = (Render.blockSize / 28) + 0.5 * Render.blockSize * Math.round(agentList[a].x / (0.5 * Render.blockSize));
			agentList[a].y = (Render.blockSize / 28) + 0.5 * Render.blockSize * Math.round(agentList[a].y / (0.5 * Render.blockSize));
			
		}
		
	}
	
	function findConnections (agent) {
		
		var connected = [];
		
		for (var d = 0; d < agentList.length; d++) {
			
			if (agentList[d].id == agent.id) continue;
			
			for (var e = 0; e < agentList[d].childBlocks.length; e++) {
				
				if (Controller.findAgent(agentList[d].childBlocks[e]).id == agent.id) {
					
					connected.push({agentIndex:d, kindIndex:e});
					
				}
				
			}
			
		}
		
		return connected;
		
	}
	
	function connectSelectedAgents (agent) {
		
		for (var c = 0; c < agentList.length; c++) {
			
			var selected = agentList[c];
			
			if (selected.selected) {
				
				for (var a = 0; a < agentList.length; a++) {
					
					if (a == c) continue;
					
					var target = agentList[a];
					
					if (target.mouse) {
						
						if (Render.displayConnectionError(selected, target, findConnections(selected))) continue;
						
						target.childBlocks.push(selected.id);
						
						break;
						
					}
					
				}
				
			}
			
		}
		
	}
	
	function deleteSelectedAgents (agent) {
		
		if (agent) agent.selected = true;
		
		for (var a = 0; a < agentList.length; a++) {
			
			var agent = agentList[a];
			
			if (agent.selected) {
				
				var connections = findConnections(agent);
				
				for (var d = 0; d < connections.length; d++) {
					
					agentList[connections[d].agentIndex].childBlocks.splice(connections[d].kindIndex, 1);
					
				}
				
				DOM.removeElement(agent);
				agentList.splice(a, 1);
				a--;
				continue;
				
			}
			
		}
		
	}
	
	function disconnectSelectedAgents (agent) {
		
		if (agent) agent.selected = true;
		
		for (var a = 0; a < agentList.length; a++) {
			
			var agent = agentList[a];
			
			if (agent.selected) {
				
				var connections = findConnections(agent);
				
				for (var d = 0; d < connections.length; d++) {
					
					agentList[connections[d].agentIndex].childBlocks.splice(connections[d].kindIndex, 1);
					
				}
				
				agent.childBlocks = [];
				
			}
			
		}
		
	}
	
	function anythingOverlaps () {
		
		for (var b = 0; b < agentList.length; b++) {
			
			var agent1 = agentList[b];
			
			for (var c = 0; c < b; c++) {
				
				var agent2 = agentList[c];
				
				if ((agent1.x - agent2.x) * (agent1.x - agent2.x) +
					(agent1.y - agent2.y) * (agent1.y - agent2.y) < Render.blockSize * Render.blockSize * 0.95) {
					
					return true;
					
				}
				
			}
			
		}
		
		return false;
		
	}
	
	function fKeyPress () {
		
		if (Keyboard.enter) {
			
			Render.initiateNameChange();
			
		}
		
		// any arrow key? move the camera and update the dom
		
		if (Keyboard.arrowKeys[0] + Keyboard.arrowKeys[1] + Keyboard.arrowKeys[2] + Keyboard.arrowKeys[3] > 0) {
			
			for (var e = 0; e < agentList.length; e++) {
				
				agentList[e].x += (Keyboard.arrowKeys[1] - Keyboard.arrowKeys[0]) * Render.blockSize * 0.5;
				agentList[e].y += (Keyboard.arrowKeys[3] - Keyboard.arrowKeys[2]) * Render.blockSize * 0.5;
				
			}
			
			Render.repositionAgents();
			
		}
		
		// entering text
		
		if (isTyping) {
			
			for (var d = 0; d < agentList.length; d++) {
				
				if (agentList[d].selected) {
					
					agentList[d].name = textInput.value;
					agentList[d].nameBox.innerHTML = agentList[d].name;
					
					DOM.addClass(agentList[d].nameBox, "typing");
					
				}
				
			}
			
			return;
			
		}
		
		if (Keyboard.back) deleteSelectedAgents();
		if (Keyboard.space) connectSelectedAgents();
		
		if (Keyboard.selectAll) {
			
			for (var b = 0; b < agentList.length; b++) {
				
				agentList[b].selected = true;
				
			}
			
		}
		
		canvas.clearRect(0, 0, DOM.width, DOM.height);
		
		fMove();
		Render.drawConnections();
		Render.repositionAgents();
		
	}
	
	function fClick () {
		
		// stop typing if you click somewhere
		
		isTyping = false;
		textInput.blur();
		
		// clear any current warnings
		
		Render.warnUser("");
		
		// gather all the buttons
		
		var menuButtons = DOM.byClass("menuButton");
		
		// select an agent?
		
		var buttonClicked = false;
		
		for (var a = 0; a < menuButtons.length; a++) {
			
			if (pointInRectangle(Mouse, DOM.rectangle(menuButtons[a]))) {
				
				// unless we spawn a new agent, prevent agent clicking
				
				buttonClicked = true;
				
				// what text is written on the button?
				
				var kind = "" + menuButtons[a].firstChild.nodeValue;
				
				if (kind == "▼" || kind == "◀" || kind == "▲" || kind == "▶") {
					
					var xTranslation = 0.5 * Render.blockSize * ((kind == "▶") - (kind == "◀"));
					var yTranslation = 0.5 * Render.blockSize * ((kind == "▼") - (kind == "▲"));
					
					for (var e = 0; e < agentList.length; e++) {
						
						agentList[e].x -= xTranslation;
						agentList[e].y -= yTranslation;
						
					}
					
					Render.repositionAgents();
					
				}
				
				if (kind == "Auctioneer" || kind == "Objective" ||
					kind == "Concentrator" || kind == "Device") {
					
					if (exportWindow) continue;
					
					var allowedToPlace = true;
					
					for (var b = 0; b < agentList.length; b++) {
						
						agentList[b].selected = false;
						agentList[b].mouse = false;
						
						if (agentList[b].kind == "Auctioneer" && kind == "Auctioneer") {
							
							allowedToPlace = false;
							Render.warnUser("You cannot have more than one auctioneer.");
							break;
							
						}
						
					}
					
					if (allowedToPlace) newAgent(Mouse.x, Mouse.y, kind);
					
					buttonClicked = false;
					
				}
				
				if (kind == "Close") {
					
					if (exportWindow) {
						
						params.fileName = DOM.byName("exportFileName").value;
						params.exportPath = DOM.byName("exportPath").value;
						params.reference = DOM.byName("exportMarket").value;
						params.min = DOM.byName("exportMin").value;
						params.max =  DOM.byName("exportMax").value;
						params.step = DOM.byName("exportSteps").value;
						params.significance = DOM.byName("exportSignificance").value;
						
						DOM.removeElement(exportWindow);
						exportWindow = null;
						
					}
					
				}
				
				if (kind == "Zoom in") Render.zoomIn();
				if (kind == "Zoom out") Render.zoomOut();
				if (kind == "Organize") organizeNodes(false);
				
				if (kind == "Save") {
					
					params.zoom = Render.zoom;
					
					loadFile({requestKind:"saveState", agents:extractInformation(), settings:params}, saveState, "database.php");
					
				}
				
				if (kind == "Load") {
					
					loadFile({requestKind:"loadState"}, loadState, "database.php");
					var millisecondsToWait = 100;
					setTimeout(function() {
						organizeNodes(false);
					}, millisecondsToWait);
					
				}
				
				if (kind == "Settings") {
					
					Render.spawnExportWindow();
					
				}
				
				if (kind == "Export") {
					
					// use hashing to check for name collisions
					// name collisions may not occur, nor empty names
					// becase these will be used in the xml to define nodes
					
					var nameHash = {};
					var exportErrorMessage = "";
					
					for (var f = 0; f < agentList.length; f++) {
						
						if (nameHash[agentList[f].name]) {
							
							exportErrorMessage = "Nodes cannot have the same name.";
							break;
							
						}
						
						nameHash[agentList[f].name] = true;
						
						if (agentList[f].name.length < 1) {
							
							exportErrorMessage = "Not all nodes have names. Please make sure every node has a name.";
							break;
							
						}
						
					}
					
					// all the settings must be present in the settings (export) window
					
					if (params.fileName.length < 1) exportErrorMessage = "Please enter a value for the file/clustername setting.";
					if (params.exportPath.length < 1) exportErrorMessage = "Please enter a value for the export path setting.";
					if (params.reference.length < 1) exportErrorMessage = "Please enter a value for the market reference setting.";
					if (params.min.length < 1) exportErrorMessage = "Please enter a value for the minimum price setting.";
					if (params.max.length < 1) exportErrorMessage = "Please enter a value for the maximum price setting.";
					if (params.step.length < 1) exportErrorMessage = "Please enter a value for the price steps setting.";
					if (params.significance.length < 1) exportErrorMessage = "Please enter a value for the significance setting.";
					
					Render.warnUser(exportErrorMessage);
					
					if (exportErrorMessage.length < 1) {
						
						params.zoom = Render.zoom;
						
						// save it in the database as well when you export it to xml
						
						loadFile({requestKind:"saveState", agents:extractInformation(), settings:params}, saveState, "database.php");
						loadFile({requestKind:"exportXML", agents:exportInformation(), settings:params}, exportState, "main.php");
						
					}
					
				}
				
			}
			
		}
		
		for (var g = 0; g < menuButtons.length; g++) {
			
			// if the user clicked on a menubutton, don't respond to any minibar buttons
			
			if (exportWindow) break;
			
			if (pointInRectangle(Mouse, DOM.rectangle(menuButtons[g]))) {
				
				var kind = "" + menuButtons[g].firstChild.nodeValue;
				
				if (kind == "rename") Render.initiateNameChange(menuButtons[g].parentNode.parentNode);
				else if (kind == "delete") deleteSelectedAgents(menuButtons[g].parentNode.parentNode);
				else if (kind == "connect") connectSelectedAgents(menuButtons[g].parentNode.parentNode);
				else if (kind == "disconnect") disconnectSelectedAgents(menuButtons[g].parentNode.parentNode);
				else if (kind == "cycle") Render.cycleSelectedAgentsIcons(menuButtons[g].parentNode.parentNode);
				
			}
			
		}
		
		selectingGroup = true;
		
		for (var c = 0; c < agentList.length; c++) {
			
			// nor respond to any agents when it comes to selection
			
			if (buttonClicked || exportWindow) break;
			
			var agent = agentList[c];
			
			agent.mx = Mouse.x - agent.x;
			agent.my = Mouse.y - agent.y;
			
			DOM.removeClass(agent.nameBox, "typing");
			
			if (agent.mouse) {
				
				// what are we going to drag? an agent or a selection?
				
				selectingGroup = false;
				
				if (Keyboard.shift) {
					
					agent.selected = !agent.selected;
					
				} else {
					
					if (!agent.selected) {
						
						for (var d = 0; d < agentList.length; d++) {
							
							agentList[d].selected = false;
							
						}
						
					}
					
					agent.selected = true;
					
				}
				
			}
			
		}
		
		// fMove();
		
	}
	
	function fRelease () {
		
		snapAgentsToGrid();
		
		// illigal placement of some agent somewhere
		// put everything back
		
		if (anythingOverlaps()) {
			
			for (var e = 0; e < agentList.length; e++) {
				
				if (!agentList[e].selected) continue;
				
				agentList[e].x = Mouse.startX - agentList[e].mx;
				agentList[e].y = Mouse.startY - agentList[e].my;
				
			}
			
			snapAgentsToGrid();
			
		}
		
		// even-odd selection with shift
		
		if (selectingGroup) {
			
			for (var d = 0; d < agentList.length; d++) {
				
				var agent = agentList[d];
				
				if (Keyboard.shift) {
					
					if (agent.mouse) agent.selected = !agent.selected;
					
				} else {
					
					agent.selected = agent.mouse;
					
				}
				
			}
			
		}
		
		selectingGroup = false;
		
		canvas.clearRect(0, 0, DOM.width, DOM.height);
		
		Render.drawConnections();
		Render.repositionAgents();
		fMove();
		
	}
	
	function fMove () {
		
		// see if the mouse is over a button
		
		var menuButtons = DOM.byClass("menuButton");
		
		for (var b = 0; b < menuButtons.length; b++) {
			
			var menuButton = menuButtons[b];
			
			if (pointInRectangle(Mouse, DOM.rectangle(menuButton))) {
				
				DOM.addClass(menuButton, "mouse");
				
			} else {
				
				DOM.removeClass(menuButton, "mouse");
				
			}
			
		}
		
		// if you're not dragging a selection, display hover effects based on the mouses' coordinate
		
		if (!selectingGroup) {
			
			for (var c = 0; c < agentList.length; c++) {
				
				var agent = agentList[c];
				
				if (pointInRectangle(Mouse, DOM.rectangle(agent))) {
					
					agent.mouse = true;
					DOM.addClass(agent, "mouse");
					
					Render.spawnAgentFunctionButtons(agent);
					
				} else {
					
					agent.mouse = false;
					DOM.removeClass(agent, "mouse");
					
					Render.deleteAgentFunctionButtons(agent);
					
				}
				
				if (agent.selected) {
					
					DOM.addClass(agent, "selection");
					
				} else {
					
					DOM.removeClass(agent, "selection");
					
				}
				
			}
			
		}
		
		// otherwise do it based on the selection rectangle
		
		if (Mouse.drag) {
			
			canvas.clearRect(0, 0, DOM.width, DOM.height);
			
			if (selectingGroup) {
				
				var target = {x:Mouse.startX, y:Mouse.startY, width:Mouse.x - Mouse.startX, height:Mouse.y - Mouse.startY};
				
				if (target.width < 0) {
					
					target.width *= -1;
					target.x -= target.width;
					
				}
				
				if (target.height < 0) {
					
					target.height *= -1;
					target.y -= target.height;
					
				}
				
				canvas.fillStyle = "#bbb";
				canvas.beginPath();
				canvas.rect(target.x, target.y, target.width, target.height);
				canvas.fill();
				
				for (var d = 0; d < agentList.length; d++) {
					
					var agent = agentList[d];
					var agentRechthoek = DOM.rectangle(agent);
					
					if (pointInRectangle({x:agent.x + 0.5 * agentRechthoek.width, y:agent.y + 0.5 * agentRechthoek.height}, target)) {
						
						agent.mouse = true;
						DOM.addClass(agent, "mouse");
						
					} else {
						
						agent.mouse = false;
						DOM.removeClass(agent, "mouse");
						
					}
					
				}
				
			} else {
				
				// display rectangles where the selected agents will snap to when you release
				
				canvas.fillStyle = "#eee";
				canvas.beginPath();
				
				for (var a = 0; a < agentList.length; a++) {
					
					var agent = agentList[a];
					
					if (agent.selected) {
						
						agent.x = Mouse.x - agent.mx;
						agent.y = Mouse.y - agent.my;
						
						canvas.rect(-(Render.blockSize / 28) + Math.round(agent.x / (0.5 * Render.blockSize)) * 0.5 * Render.blockSize,
									-(Render.blockSize / 28) + Math.round(agent.y / (0.5 * Render.blockSize)) * 0.5 * Render.blockSize,
									Render.blockSize,
									Render.blockSize);
						
					}
					
				}
				
				canvas.fill();
				
				Render.repositionAgents();
				
			}
			
			Render.drawConnections();
			
		}
		
	}
	
	// used for the database
	
	function extractInformation () {
		
		var cleanAgents = [];
		
		for (var a = 0; a < agentList.length; a++) {
			
			cleanAgents.push({	selected:agentList[a].selected,
								mouse:agentList[a].mouse,
								x:agentList[a].x,
								y:agentList[a].y,
								mx:agentList[a].mx,
								my:agentList[a].my,
								childBlocks:agentList[a].childBlocks,
								kind:agentList[a].kind,
								name:agentList[a].name,								
								clss:agentList[a].clss,
								clssName:agentList[a].clssName,
								id:agentList[a].id,
								depth:agentList[a].depth,
								treeSortValue:agentList[a].treeSortValue});
			
		}
		
		return cleanAgents;
		
	}
	
	// used for the xml export
	
	function exportInformation () {
		
		var cleanAgents = [];
		
		for (var a = 0; a < agentList.length; a++) {
			
			cleanAgents.push({	childBlocks:agentList[a].childBlocks,
								kind:agentList[a].kind,
								name:agentList[a].name,								
								clssName:agentList[a].clssName,
								id:agentList[a].id});
			
		}
		
		return cleanAgents;
		
	}
	
	// gets called after you hit export and the php echo's
	
	function exportState (i) {
		
		Render.warnUser("Exported succesfully.");
		
	}
	
	// gets called after you hit load and the php echo's
	
	function loadState (i) {
		
		if (!i) return Render.warnUser("Nothing to load or no database connection established.")
		
		i = i.split("ARRAYSPLIT");
		
		for (var a = 0; a < agentList.length; a++) {
			
			DOM.removeElement(agentList[a]);
			
		}
		
		agentList = [];
		
		var settings = JSON.parse(i[0]);
		var newAgents = JSON.parse(i[1]);
		
		params.zoom = +settings.zoom;
		params.fileName = settings.fileName;
		params.exportPath = settings.exportPath;
		params.min = settings.min;
		params.max = settings.max;
		params.step = settings.step;
		params.significance = settings.significance;
		params.reference = settings.reference;
		
		Render.zoom = params.zoom;
		Render.updateScale();
		
		for (var b = 0; b < newAgents.length; b++) {
			
			id = Math.max(newAgents[b].id + 1, id);
			
		}
		
		for (var c = 0; c < newAgents.length; c++) {
			
			var n = newAgents[c];
			
			newAgent(n.x, n.y, n.kind, {clssName:n.clssName, clss:n.clss, name:n.name, childBlocks:n.childBlocks, id:n.id});
			
		}
		
		Render.drawConnections();
		
	}
	
	// gets called after you hit save and the php echo's
	
	function saveState (i) {
		
		Render.warnUser("" + i);
		
	}
	
	// load any file, xml indicates whether the text or xml needs to be returned
	
	function loadFile (information, callback, file, xml) {
		
		var req = new XMLHttpRequest();
		
		req.open("POST", file, true);
		req.setRequestHeader("Content-type", "application/json");
		req.addEventListener("readystatechange", (function () {
			
			if (req.readyState == 4 && req.status == 200) {
				
				callback(xml ? req.responseXML : req.responseText);
				
			}
			
		}), false);
		req.send(JSON.stringify(information));
		
	}
	
	// still not perfect, fails when the depth complexity varies by more than one
	// if it fails it calls itself with messymode on, this creates a messy triangle
	// and simply sorts everything based on it's depth
	
	function organizeNodes (messyMode) {
		
		var depthArray = [];
		var highest = 0;
		
		for (var a = 0; a < agentList.length; a++) {
			
			agentList[a].depth = findAgentDepth(agentList[a]);
			
			var cons = findConnections(agentList[a]);
			
			agentList[a].treeSortValue = (cons.length > 0 ? cons[0].agentIndex : 0) - 1000 * (agentList[a].childBlocks.length > 0);
			agentList[a].x = a * 10 * Render.blockSize;
			agentList[a].y = -10 * Render.blockSize;
			
			if (agentList[a].depth > highest) highest = agentList[a].depth;
			
			
		}
		
		for (var b = 0; b < highest + 1; b++) {
			
			depthArray[b] = [];
			
		}
		
		for (var c = 0; c < agentList.length; c++) {
			
			depthArray[agentList[c].depth].push(c);
			
		}
		
		var treeCenter = Math.round(0.5 * DOM.width / Render.blockSize);
		
		for (var d = depthArray.length - 1; d > -1; d--) {
			
			depthArray[d].sort(function (a, b) {return agentList[a].treeSortValue - agentList[b].treeSortValue;});
			
			for (var e = 0; e < depthArray[d].length; e++) {
				
				var agent = agentList[depthArray[d][e]];
				
				agent.x = (e - 0.5 * depthArray[d].length * (1 - messyMode) + treeCenter) * Render.blockSize;
				agent.y = (d) * Render.blockSize;
				
				if (!messyMode) {
					
					if (agent.childBlocks.length > 0) {
						
						var x = 0;
						
						for (var g = 0; g < agent.childBlocks.length; g++) {
							
							x += Controller.findAgent(agent.childBlocks[g]).x;
							
						}
						
						x /= agent.childBlocks.length;
						
						agent.x = x;
						
					} else {
						
						for (var f = 0; f < 10; f++) {
							
							if (!anythingOverlaps()) break;
							
							agent.x += (f * 0.5 * Render.blockSize) * (f % 2 == 0 ? -1 : 1);
							
						}
						
					}
					
				}
				
			}
			
		}
		
		if (messyMode && anythingOverlaps()) Render.warnUser("Failed to organize the tree.");
		if (anythingOverlaps()) organizeNodes(true);
		
		snapAgentsToGrid();
		
		Render.repositionAgents();
		
	}
	
	// inductive depth search
	
	function findAgentDepth (agent) {
		
		var cons = findConnections(agent);
		
		if (cons.length == 0) return 0;
		
		return findAgentDepth(agentList[cons[0].agentIndex]) + 1;
		
	}
	
	// implied class, can be made a real class for better structure
	
	function newAgent (x, y, kind, other) {
		
		var agent = DOM.placeElement("div", {className:"agent"}, agents);
		
		agent.selected = false;
		agent.mouse = true;
		agent.x = x - 30;
		agent.y = y - 30;
		agent.mx = 30;
		agent.my = 30;
		agent.childBlocks = [];
		agent.kind = kind;
		agent.name = "";
		agent.clss = 0;
		agent.clssName = Render.classMap[kind][agent.clss];
		agent.id = uniqueID();
		agent.depth = 0;
		agent.treeSortValue = 0;
		
		if (arguments.length > 3) {
			
			agent.mouse = false;
			agent.selected = false;
			agent.x = x;
			agent.y = y;
			agent.mx = 0;
			agent.my = 0;
			agent.childBlocks = other.childBlocks;
			agent.name = other.name;
			agent.clss = other.clss;
			agent.clssName = other.clssName;
			agent.id = other.id;
			
		}
		
		DOM.style(agent, {"background-image":"url(" + Render.imageMap[kind][agent.clss] + ")"});
		
		var kindBox = DOM.placeElement("span", {className:"agentKind"}, agent);
		
		DOM.placeText(kind, kindBox);
		DOM.placeElement("br", {}, agent);
		
		agent.nameBox = DOM.placeElement("span", {className:"agentName"}, agent);
		
		DOM.placeText(agent.name, agent.nameBox);
		
		agentList.push(agent);
		
		Render.repositionAgents();
		
	}
	
	// nifty function
	
	function pointInRectangle (a, b) {
		
		return (a.x > b.x - 0 * b.width &&
				a.x < b.x + 1 * b.width &&
				a.y > b.y - 0 * b.height &&
				a.y < b.y + 1 * b.height);
		
	}
	
	// used to be isolated, until other classes needed to know the current id stack
	
	function uniqueID () {
		
		return id++;
		
	}
	
})();