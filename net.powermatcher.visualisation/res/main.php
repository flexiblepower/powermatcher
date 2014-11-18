<?php
include ("classes/XmlExport.php");

$xml = new XmlExport();

$str_json = file_get_contents('php://input');
$decodedJson = json_decode($str_json, true);

$xml->populateAgents($decodedJson);
$xml->exportXml();
?>