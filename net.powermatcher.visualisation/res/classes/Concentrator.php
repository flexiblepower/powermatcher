<?php
/*********************************************************
Power Tool Configuration Matcher
Author(s): Sarfaraaz (Sarfaraaz),
		   Dejan	   
*********************************************************/

	include_once("Agent.php");

	/*
		Defines the Concentrator class
	*/
	class Concentrator extends Agent {
		private $children;
		private $childList;
		
		public function Concentrator() {
			$this->children = array();
			$this->childList = array();
		}

		public function getChild($i) { return $this->children[$i]; }
		public function getChildList() { return $this->childList; }
		
		public function getChildListContent() {
			$list = "";
			for ($i = 0; $i < count($this->childList); $i++) {
				$list .= $this->childList[$i];
			}

			return $list;
		}
		
		public function addChild($child) {
			array_push($this->children, $child);
		}

		public function addToChildList($id) {
			array_push($this->childList, $id);
		}

		public function getChildrenSize() {
			return count($this->children);
		}
	}
?>