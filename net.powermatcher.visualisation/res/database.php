<?php

// Fjodor van Veen 2014

$str_json = file_get_contents('php://input');
$javascript = json_decode($str_json);

$link = new mysqli('localhost', 'root', 'root', 'powcon');

if (mysqli_connect_errno()) echo mysqli_connect_error();

if ($javascript->requestKind === "saveState") {
	
	$query = $link->query("INSERT INTO agents (settings, agents) VALUES ('".json_encode($javascript->settings)."', '".json_encode($javascript->agents)."')");
	
	echo "Saved succesfully.";
	
}

if ($javascript->requestKind === "loadState") {
	
	$query = $link->query("SELECT * FROM agents ORDER BY id DESC LIMIT 1");
	
	while ($item = $query->fetch_object()) {
		
		echo $item->settings."ARRAYSPLIT".$item->agents;
		
	}
	
}

?>
