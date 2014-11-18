<?php
/*********************************************************
Power Tool Configuration Matcher
Author(s): Sarfaraaz (Sarfaraaz),
		   Dejan	   
*********************************************************/

	/*
		Defines the base Agent class
	*/
	class Agent {
		private $id;
		private $clss;
		
		public function getID() { return $this->id;	}
		public function getClss() { return $this->clss;	}

		public function setID($id) { $this->id = $id; }
		public function setClss($clss) { $this->clss = $clss; }
	}
?>