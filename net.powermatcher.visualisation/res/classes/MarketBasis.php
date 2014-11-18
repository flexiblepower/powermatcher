<?php
/*********************************************************
Power Tool Configuration Matcher
Author(s): Sarfaraaz (Sarfaraaz),
		   Dejan	   
*********************************************************/

	include_once("Agent.php");

	/*
		Defines the MarketBasis class
	*/
	class MarketBasis extends Agent {
		private $marketRef;
		private $minimumPrice;
		private $maximumPrice;
		private $priceSteps;
		private $significance;
		
		public function getMarketRef() { return $this->marketRef; }
		public function getMinimumPrice() {	return $this->minimumPrice;	}
		public function getMaximumPrice() { return $this->maximumPrice; }
		public function getPriceSteps() { return $this->priceSteps;	}
		public function getSignificance() { return $this->significance;	}
		
		public function setMarketRef($marketRef) { $this->marketRef = $marketRef; }
		public function setMinimumPrice($minimumPrice) { $this->minimumPrice = $minimumPrice; }
		public function setMaximumPrice($maximumPrice) { $this->maximumPrice = $maximumPrice; }
		public function setPriceSteps($priceSteps) { $this->priceSteps = $priceSteps; }
		public function setSignificance($significance) { $this->significance = $significance; }
	}
?>