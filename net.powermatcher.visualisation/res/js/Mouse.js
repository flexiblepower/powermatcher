(function () {
	
	// Fjodor van Veen 2014
	
	Mouse = function () {
		
	};
	
	// offset is unused, but should be used if the application origin changes
	// start x and y are for dragging; original x and y
	
	Mouse.click = new Function();
	Mouse.release = new Function();
	Mouse.move = new Function();
	Mouse.drag = false;
	Mouse.x = -1;
	Mouse.y = -1;
	Mouse.offsetX = -1;
	Mouse.offsetY = -1;
	Mouse.startX = -1;
	Mouse.startY = -1;
	
	Mouse.init = function () {
		
		document.body.addEventListener("mousedown", fClick, false);
		document.body.addEventListener("mousemove", fMove, false);
		document.body.addEventListener("mouseup", fRelease, false);
		document.body.addEventListener("mouseout", fBlur, false);
		
	};
	
	// when you drag outside the window, release the mouse
	
	function fBlur (e) {
		
		var previousTarget = e.relatedTarget || e.toElement;
		
		if (!previousTarget || previousTarget.nodeName == "HTML") {
			
			Mouse.drag = false;
			
			canvas.clearRect(0, 0, DOM.width, DOM.height);
			
			Render.drawConnections();
			Render.repositionAgents();
			
			Mouse.release();
			
		}
			
	}
	
	function fClick (e) {
		
		if (!exportWindow) e.preventDefault();
		
		Mouse.drag = true;
		Mouse.startX = Mouse.x;
		Mouse.startY = Mouse.y;
		Mouse.click();
		
	}
	
	function fMove (e) {
		
		if (!exportWindow) e.preventDefault();
		
		Mouse.x = e.clientX - Mouse.offsetX;
		Mouse.y = e.clientY - Mouse.offsetY;
		Mouse.move();
		
	}
	
	function fRelease (e) {
		
		if (!exportWindow) e.preventDefault();
		
		Mouse.drag = false;
		Mouse.release();
		
	}
	
})();