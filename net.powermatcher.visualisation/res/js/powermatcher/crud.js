function editNode(pid) {
	alert("I'm going to edit " + pid);
}

function deleteNode(pid) {
	
	if(confirm("Are you sure you want to permanently delete\n" + pid + "?") == true){
		var url = "/system/console/configMgr/" + pid;
		
		$.post(url, {
			apply : "1",
			delete : "1"
		}, function(data) {
			if(data === "false"){
					alert("something went wrong!");
			}
			location.reload();
		});
	}
}