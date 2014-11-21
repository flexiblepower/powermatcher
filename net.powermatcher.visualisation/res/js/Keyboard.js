(function () {
	
	// Fjodor van Veen 2014
	
	Keyboard = function () {
		
		
		
	};
	
	Keyboard.back = false;
	Keyboard.shift = false;
	Keyboard.space = false;
	Keyboard.tab = false;
	Keyboard.enter = false;
	Keyboard.selectAll = false;
	Keyboard.arrowKeys = [0, 0, 0, 0];
	Keyboard.keyPress = new Function();
	Keyboard.keyRelease = new Function();
	
	Keyboard.init = function () {
		
		document.body.addEventListener("keydown", fPress, false);
		document.body.addEventListener("keyup", fRelease, false);
		
	}
	
	function fPress (e) {
		
		var i = e.keyCode;
		
		if ((i == 91 || i == 93 || e.metaKey) && !isTyping) return;
		
		if (!isTyping && !exportWindow) e.preventDefault();
		
		if (i == 16) Keyboard.shift = !isTyping;
		if (i == 9) Keyboard.tab = !isTyping;
		if (i == 8 || i == 46) Keyboard.back = !isTyping;
		if (i == 32) Keyboard.space = !isTyping;
		if (i == 13) Keyboard.enter = true;
		if (i == 65) Keyboard.selectAll = !isTyping;
		if (i == 37) Keyboard.arrowKeys[0] = 1;
		if (i == 39) Keyboard.arrowKeys[1] = 1;
		if (i == 38) Keyboard.arrowKeys[2] = 1;
		if (i == 40) Keyboard.arrowKeys[3] = 1;
		
		Keyboard.keyPress();
		
	}
	
	function fRelease (e) {
		
		var i = e.keyCode;
		
		if ((i == 91 || i == 93 || e.metaKey) && !isTyping) return;
		
		if (!isTyping && !exportWindow) e.preventDefault();
		
		Keyboard.keyRelease();
		
		if (i == 16) Keyboard.shift = false;
		if (i == 9) Keyboard.tab = false;
		if (i == 8 || i == 46) Keyboard.back = false;
		if (i == 32) Keyboard.space = false;
		if (i == 13) Keyboard.enter = false;
		if (i == 65) Keyboard.selectAll = false;
		if (i == 37) Keyboard.arrowKeys[0] = 0;
		if (i == 39) Keyboard.arrowKeys[1] = 0;
		if (i == 38) Keyboard.arrowKeys[2] = 0;
		if (i == 40) Keyboard.arrowKeys[3] = 0;
		
	}
	
})();