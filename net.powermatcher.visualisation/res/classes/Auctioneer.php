<?php
/*********************************************************
Power Tool Configuration Matcher
Author(s): Sarfaraaz (Sarfaraaz),
		   Dejan	   
*********************************************************/

	include_once("Agent.php");

	/*
		Defines the Auctioneer class
	*/
	class Auctioneer extends Agent {
		private $children;
	
		public function Auctioneer() {
			$this->children = array();
		}

		public function getChild($i) { return $this->children[$i]; }

		public function addChild($child) {
			array_push($this->children, $child);
		}

		public function getChildrenSize() {
			return count($this->children);
		}
	}
?>