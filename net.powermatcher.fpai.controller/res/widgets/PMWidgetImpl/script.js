$(window).load(function() {
	w = new widget("update", 1000, function(data) {
		$("#loading").detach();
		$("p").show();
		$(".error").hide();
		$("#marketprice").text(data.marketPrice);
		$("#timestamp").text(data.timestamp);
		
		diff = data.agentTypes.length - $("#agents p").length;
		if(diff > 0) {
			for(i = 0; i < diff; i++) {
				$("#agents").append("<p><label>...</label> <span>...</span></p>");
			}
		} else {
			for(i = 0; i > diff; i--) {
				$("#agents p").last().detach();
			}
		}	
		
		labels = $("#agents p").children("label");
		spans = $("#agents p").children("span");
		for(i = 0; i < labels.length; i++) {
			labels[i].innerHTML = data.agentTypes[i];
			spans[i].innerHTML = data.demands[i];
		}
	});
	
	w.error = function(msg) {
		$("#loading").detach();
		$("p").hide();
		$(".error").show();
		$(".error").text(msg);
	}
});