jsPlumb.ready(function() {

	// TODO here
	var color = "lightgreen";

	var instance = jsPlumb.getInstance({
		// notice the 'curviness' argument to this Bezier curve. the
		// curves on this page are far smoother
		// than the curves on the first canvas, which use the default
		// curviness value.
		Connector : [ "Bezier", {
			curviness : 50
		} ],
		DragOptions : {
			cursor : "pointer",
			zIndex : 2000
		},
		PaintStyle : {
			strokeStyle : color,
			lineWidth : 2
		},
		EndpointStyle : {
			radius : 9,
			fillStyle : color
		},
		HoverPaintStyle : {
			strokeStyle : "#ec9f2e"
		},
		EndpointHoverStyle : {
			fillStyle : "#ec9f2e"
		},
		ConnectionOverlays : [ [ "Label", {
			label : "Data",
			id : "label",
			cssClass : "aLabel"
		} ], ],
		Container : "container"
	});

	// suspend drawing and initialise.
	instance.doWhileSuspended(function() {

		$.post("data", function(data) {

			var jobject = data;

			// creating the node-divs
			createNodes(jobject.agents);

			// declare some common values:
			var arrowCommon = {
				foldback : 0.7,
				fillStyle : color,
				width : 14
			},
			// use three-arg spec to create two different arrows with the
			// common values:
			overlays = [ [ "Arrow", {
				location : 0.7
			}, arrowCommon ], [ "Arrow", {
				location : 0.3,
				direction : -1
			}, arrowCommon ] ];

			// TODO only endpoints where needed
			var windows = jsPlumb.getSelector(".window");

			$.each(windows, function() {
				instance.addEndpoint(this, {
					uuid : this.getAttribute("id") + "-bottom",
					anchor : "Bottom",
					maxConnections : -1
				});

				instance.addEndpoint(this, {
					uuid : this.getAttribute("id") + "-top",
					anchor : "Top",
					maxConnections : -1
				});
			})

			// Connect all nodes
			$.each(jobject.connections, function(idx, obj) {
				instance.connect({
					uuids : [ obj.matcherRole + "-bottom",
							obj.agentRole + "-top" ],
					overlays : overlays
				});
			});

			instance.draggable(windows);
		});
	});
	// Code that has be be executed when label is clicked
	instance.bind("click", function(conn, originalEvent) {
		alert("Getting latest bid and price. Or whatever");
	});

	jsPlumb.fire("dataLoaded", instance);
});

// creating the node-divs
function createNodes(agentArray) {

	$.each(agentArray, function(idx, obj) {

		// creating the div
		$("<div/>", {
			id : obj.agentId,
			class : "window",
		}).append("<b>" + obj.fpid + "</b><br />" + obj.agentId).
		// inserting a span in the div
		append($("<span/>", {
			class : "ui-icon ui-icon-pencil",
			click : function() {
				// TODO call json
				alert("get json for " + obj.pid);
			},
			hover : "hover",
		})).
		// putting the div in the container
		appendTo("#container");

	});
}
