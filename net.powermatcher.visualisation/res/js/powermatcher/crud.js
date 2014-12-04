
function createNode(fpid)
{
    var url = "/system/console/configMgr/";// + fpid;
    
    generateForm(url + "[Temporary PID replaced by real PID upon save]");
    
    url = url + fpid;
    
    fillForm(url, fpid);
}

function editNode(pid, fpid) {
	
	var url = "/system/console/configMgr/" + pid;
	
	generateForm(url);
	
	fillForm(url, fpid);
}

function deleteNode(pid) {
	
	if(confirm("Are you sure you want to permanently delete\n" + pid + "?") === true){
		var url = "/system/console/configMgr/" + pid;
		
		$.post(url, {
			apply : "1",
			delete : "1"
		}, function(data) {
			if(data === "false") {
					alert("something went wrong!");
			}
			location.reload();
		});
	}
}

function generateForm(url){
	
	var  form;
	
	 dialog = $( "#dialog-form" ).dialog({
		 buttons: {
			 Ok: function() {
				 $.post( url, $("#dialog-form").serialize(), function(){location.reload(); dialog.dialog( "close" );} );
				 },
		 Cancel: function() {
		 dialog.empty();
		 dialog.dialog( "close" );
		 }
		 },
		 close: function() {
		     dialog.empty();
		     dialog.dialog( "close" );
		 }
		 });
}

function fillForm(url, fpid){
	
	$.post( url, function( data ) {

	    var tr, td, input;
		var table =	$("<table/>");
		var br = $("<br />");
		var propertylist = "";
		
		$.each(data.properties, function(key, obj) {
			
			if(propertylist.length > 0){
				propertylist = propertylist + ",";
			}
			propertylist = propertylist + key;
			
			tr = 	$("<tr/>");
			
			// title
			td = $("<td>").append(obj.name);
			tr.append(td);
			
			// input
			input = $("<input/>",{
				id : key,
				class : "inputText",
				type : "text",
				name: key,
				value : obj.value
			});
			tr.append(input);
			
			if (typeof(obj.description) != "undefined"){
				tr.append(br);
				tr.append(obj.description);
			}
			
			table.append(tr);
		});
		
		var hiddenFields = {}; // or just {}
		hiddenFields['action'] = "ajaxConfigManager";
		hiddenFields['apply'] = "true";
		hiddenFields['factoryPid'] = fpid;
		hiddenFields['propertylist'] = propertylist;

		for (var key in hiddenFields) {
			input = $("<input/>",{
				class : "inputText",
				type : "hidden",
				name: key,
				value : hiddenFields[key]
			});
			input.appendTo("#dialog-form");
		}
		table.appendTo("#dialog-form");
		});
}

/**
 * This method created a basic empty form so that the edit-buttons can be
 * created.
 */
function initializeForm(){
	// create the basic, empty dialog
	var dialog = $( "#dialog-form" ).dialog({
		autoOpen: false,
		height: 300,
		width: 350,
		modal: true
		});
	
	// setting up the edit-buttons on the nodes to open the form
		$( ".edit-button" ).button().on( "click", function() {
		    dialog.dialog( "open" );
		});
}
