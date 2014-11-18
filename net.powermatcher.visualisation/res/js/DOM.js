(function () {
	
	DOM = function () {
		
		
	};
	
	DOM.width = window.innerWidth;
	DOM.height = window.innerHeight;
	DOM.resize = new Function();
	
	function fResize (e) {
		
		DOM.width = window.innerWidth;
		DOM.height = window.innerHeight;
		DOM.resize();
		
	}
	
	DOM.init = function () {
		
		window.addEventListener("resize", fResize, false);
		
	};
	
	DOM.title = function (title) {
		
		document.title = title;
		
	};
	
	// id
	
	DOM.byName = function (name) {
		
		return document.getElementById(name);
		
	};
	
	// tagname
	
	DOM.byKind = function (kind, target) {
		
		return (arguments.length > 1 ? target : document.body).getElementsByTagName(kind);
		
	};
	
	// classname
	
	DOM.byClass = function (clss, target) {
		
		return (arguments.length > 1 ? target : document.body).getElementsByClassName(clss);
		
	};
	
	// create a new element with attributes (object literal)
	// and place it in target if specified (body default)
	
	DOM.placeElement = function (kind, attributes, target) {
		
		var n = document.createElement(kind);
		
		for (var property in attributes) {
			
			n[property] = attributes[property];
			
		}
		
		(arguments.length > 2 ? target : document.body).appendChild(n);
		
		return n;
		
	};
	
	DOM.removeElement = function (element) {
		
		element.parentNode.removeChild(element);
		
	};
	
	// create a canvas element; if the viewports' scaling is set: use that pixel ratio
	// otherwise see if the device itself supports it
	// for retina and other hidpi screens
	
	DOM.placeCanvas = function (viewport) {
		
		var scale = viewport.scale ? viewport.scale : window.devicePixelRatio;
		var property = {width:viewport.width * scale, height:viewport.height * scale};
		var layer = DOM.placeElement("canvas", property, document.body).getContext("2d");
		
		layer.width = viewport.width;
		layer.height = viewport.height;
		layer.transform(scale, 0, 0, scale, 0, 0);
		
		DOM.style(layer.canvas, {"position":"absolute",
								"width":viewport.width + "px",
								"height":viewport.height + "px",
								"margin":"0",
								"padding":"0",
								"left":"0",
								"top":"0"});
		
		return layer;
		
	};
	
	DOM.placeText = function (text, target) {
		
		return (arguments.length > 1 ? target : document.body).appendChild(document.createTextNode(text));
		
	};
	
	// style a dom element
	
	DOM.style = function (target, properties) {
		
		for (var property in properties) {
			
			target.style[property] = properties[property];
			
		}
		
	};
	
	// for css classes
	
	DOM.addClass = function (target, name) {
		
		if (target.className.indexOf(" " + name) == -1) target.className += " " + name;
		
	};
	
	DOM.removeClass = function (target, name) {
		
		if (target.className.indexOf(" " + name) != -1) target.className = target.className.replace(" " + name, "");
		
	};
	
	DOM.rectangle = function (target) {
		
		var r = target.getBoundingClientRect();
		
		return {x:r.left, y:r.top, width:r.right - r.left, height:r.bottom - r.top};
		
	};
	
})();