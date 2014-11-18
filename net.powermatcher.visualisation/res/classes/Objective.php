<?php
/*********************************************************
Power Tool Configuration Matcher
Author(s): Sarfaraaz (Sarfaraaz),
		   Dejan	   
*********************************************************/

	include_once("Agent.php");

	/*
		Defines the Objective class
	*/
	class Objective extends Agent {
		private $bid;
		
		public function getBid() { return $this->bid; }
		
		public function setBid($bid) { $this->bid = $bid; }
	}
?>