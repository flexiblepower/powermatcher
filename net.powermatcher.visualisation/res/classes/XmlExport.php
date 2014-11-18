<?php
/*********************************************************
Power Tool Configuration Matcher
Author(s): Sarfaraaz (Sarfaraaz),
		   Dejan	   
*********************************************************/

include("classes/Objects.php");

/*
	Defines the XmlExport class
*/
class XmlExport {
	private $agents = array();
	private $cluster;
	
	/*
		Populates the agents array by parsing the passed json string
		$json: the passed in decoded json string
	*/
	function populateAgents($json) {
		$marketBasis;
		$auctioneer;
		$concentrators = array();

		$auctioneerChildren = "";
		$concentratorChildren = "";

		$this->cluster =$json['settings']['fileName'];

		$marketBasis = new MarketBasis();
		$marketBasis->setMarketRef($json['settings']['reference']);
		$marketBasis->setMinimumPrice($json['settings']['min']);
		$marketBasis->setMaximumPrice($json['settings']['max']);
		$marketBasis->setPriceSteps($json['settings']['step']);
		$marketBasis->setSignificance($json['settings']['significance']);

		/*
			Loop through each agent object in the decoded json string and check whether
			they're specific types and handle that accordingly
		*/
		foreach ($json["agents"] as $child) {
			if ($child['kind'] == "Auctioneer") {
				$auctioneer = new Auctioneer();
				$auctioneer->setId($child['name']);
				$auctioneer->setClss($child['clssName']);

				for ($i = 0; $i < count($child["childBlocks"]); $i++) {
					$auctioneerChildren .= $child["childBlocks"][$i] . ",";
				}
			}

			if ($child['kind'] == "Concentrator") {
				$childArr = explode(",", $auctioneerChildren);

				$concentrator = new Concentrator();
				$concentrator->setId($child['name']);
				$concentrator->setClss($child['clssName']);

				for ($i = 0; $i < count($child["childBlocks"]); $i++) {
					$concentrator->addToChildList($child["childBlocks"][$i]);
				}

				array_push($concentrators, $concentrator);

				for ($i = 0; $i < count($childArr) - 1; $i++) {
					if ($child['id'] == $childArr[$i]) {
						$auctioneer->addChild($concentrator);
					}
				}

				foreach ($concentrators as $c) {
					if (strpos($c->getChildListContent(), $child['id']) !== false) {
						$c->addChild($concentrator);
					}
				}
			}

			if ($child['kind'] == "Objective") {
				$childArr = explode(",", $auctioneerChildren);

				$agent = new Objective();
				$agent->setId($child['name']);
				$agent->setClss($child['clssName']);
				$agent->setBid(5);

				for ($i = 0; $i < count($childArr) - 1; $i++) {
					if ($child['id'] == $childArr[$i]) {
						$auctioneer->addChild($agent);
					}
				}

				foreach ($concentrators as $c) {
					if (strpos($c->getChildListContent(), $child['id']) !== false) {
						$c->addChild($agent);
					}
				}
			}

			if ($child['kind'] == "Device") {
				$agent = new Device();
				$agent->setId($child['name']);
				$agent->setClss($child['clssName']);

				for ($i = 0; $i < count($childArr) - 1; $i++) {
					if ($child['id'] == $childArr[$i]) {
						$auctioneer->addChild($agent);
					}
				}

				foreach ($concentrators as $c) {
					if (strpos($c->getChildListContent(), $child['id']) !== false) {
						$c->addChild($agent);
					}
				}
			}
		}

		array_push($this->agents, $marketBasis);
		array_push($this->agents, $auctioneer);

		foreach ($concentrators as $c) {
			array_push($this->agents, $c);
		}
	}

	/*
		Exports the xml
	*/
	function exportXml() {	
		$data = "";
		date_default_timezone_set('Europe/Amsterdam');
		$currentDate = date('Y-m-d');

		//xml definition
		$data .= "<?xml version='1.0' encoding='UTF-8'?>\n";
		$data .= "<nodeconfig id='sample_node' name='Sample Core node' description='PowerMatcher VPP cluster Head End v002' date='". $currentDate ."'>\n";

		$data .= "\t<configuration type='group' id='root'>\n";

		//defaults
		$data .= "\t\t<property name='update.interval' value='5' type='Integer' />\n";
		$data .= "\t\t<property name='time.adapter.factory' value='timeAdapterFactory' type='String' />\n";
		$data .= "\t\t<property name='scheduler.adapter.factory' value='schedulerAdapterFactory' type='String' />\n";
		$data .= "\t\t<property name='messaging.adapter.factory' value='mqttv3ConnectionFactory' type='String' />\n";
		$data .= "\t\t<property name='agent.adapter.factory' value='agentProtocolAdapterFactory' type='String' />\n";
		$data .= "\t\t<property name='matcher.adapter.factory' value='matcherProtocolAdapterFactory' type='String' />\n";
		$data .= "\t\t<property name='logging.adapter.factory' value='directLoggingAdapterFactory' type='String' />\n\n";

		//connectivity
		$data .= "\t\t<configuration type='factory' cluster='" . $this->cluster . "' pid='net.powermatcher.core.messaging.mqttv3.Mqttv3ConnectionFactory' id='mqttv3Connection'>\n";
		$data .= "\t\t\t<property name='id' value='mqttv3ConnectionFactory' type='String' />\n";
		$data .= "\t\t</configuration>\n\n";

		$data .= "\t\t<configuration type='factory' cluster='" . $this->cluster . "' pid='net.powermatcher.core.messaging.protocol.adapter.MatcherProtocolAdapterFactory' id='matcherProtocolAdapter'>\n";
		$data .= "\t\t\t<property name='id' value='matcherProtocolAdapterFactory' type='String' />\n";
		$data .= "\t\t\t<property name='messaging.protocol' value='INTERNAL_v1' type='String' />\n";
		$data .= "\t\t\t<property name='bid.topic.suffix' value='UpdateBid' type='String' />\n";
		$data .= "\t\t\t<property name='price.info.topic.suffix' value='UpdatePriceInfo' type='String' />\n";
		$data .= "\t\t</configuration>\n\n";

		$data .= "\t\t<configuration type='factory' cluster='" . $this->cluster . "' pid='net.powermatcher.core.messaging.protocol.adapter.AgentProtocolAdapterFactory' id='agentProtocolAdapter'>\n";
		$data .= "\t\t\t<property name='id' value='agentProtocolAdapterFactory' type='String' />\n";
		$data .= "\t\t\t<property name='messaging.protocol' value='INTERNAL_v1' type='String' />\n";
		$data .= "\t\t\t<property name='price.info.topic.suffix' value='UpdatePriceInfo' type='String' />\n";
		$data .= "\t\t\t<property name='bid.topic.suffix' value='UpdateBid' type='String' />\n";
		$data .= "\t\t</configuration>\n\n";

		//scheduling
		$data .= "\t\t<configuration type='factory' cluster='" . $this->cluster . "' pid='net.powermatcher.core.scheduler.TimeAdapterFactory' id='timeAdapter'>\n";
		$data .= "\t\t\t<property name='id' value='timeAdapterFactory' type='String' />\n";
		$data .= "\t\t</configuration>\n\n";

		$data .= "\t\t<configuration type='factory' cluster='" . $this->cluster . "' pid='net.powermatcher.core.scheduler.SchedulerAdapterFactory' id='schedulerAdapter'>\n";
		$data .= "\t\t\t<property name='id' value='schedulerAdapterFactory' type='String' />\n";
		$data .= "\t\t</configuration>\n\n";

		//agents
		$data .= "\t\t<configuration type='group' id='agents'>\n\n";

		foreach ($this->agents as $agent) {
			if ($agent instanceof MarketBasis) {
				$data .= "\t\t\t<configuration type='factory' cluster='" . $this->cluster . "' pid='net.powermatcher.core.agent.marketbasis.adapter.MarketBasisAdapterFactory' id='marketBasisAdapter'>\n";
				$data .= "\t\t\t\t<property name='id' value='marketBasisAdapterFactory' type='String' />\n";
				$data .= "\t\t\t\t<property name='market.ref' value='" . $agent->getMarketRef() . "' type='Integer' />\n";
				$data .= "\t\t\t\t<property name='minimum.price' value='". $agent->getMinimumPrice() . "' type='Double' />\n";
				$data .= "\t\t\t\t<property name='maximum.price' value='" . $agent->getMaximumPrice() . "' type='Double' />\n";
				$data .= "\t\t\t\t<property name='price.steps' value='" . $agent->getPriceSteps() . "' type='Integer' />\n";
				$data .= "\t\t\t\t<property name='significance' value='" . $agent->getSignificance() . "' type='Integer' />\n";
				$data .= "\t\t\t</configuration>\n\n";
			}
			
			if($agent instanceof Auctioneer) {
				$data .= "\t\t\t<configuration type='factory' cluster='" . $this->cluster . "' pid='net.powermatcher.core.agent.auctioneer.Auctioneer' id='" . $agent->getId() . "'>\n";
				$data .= "\t\t\t\t<property name='id' value='" . $agent->getClss() . "' type='String' />\n";
				$data .= "\t\t\t\t<property name='agent.adapter.factory' value='marketBasisAdapterFactory' type='String' />\n";
				$data .= "\t\t\t</configuration>\n\n";

				if ($agent->getChildrenSize() > 0) {
					$data .= "\t\t\t<configuration type='group' id='" . $agent->getId() . "-children'>\n";
					$data .= "\t\t\t\t<property name='matcher.id' value='" . $agent->getClss() . "' type='String' />\n";
					
					for ($i = 0; $i < $agent->getChildrenSize(); $i++) {
						if ($agent->getChild($i) instanceof Objective) {
							$data .= "\t\t\t\t\t<configuration type='factory' cluster='" . $this->cluster . "' pid='net.powermatcher.core.agent.objective.ObjectiveAgent' id='" . $agent->getChild($i)->getId() . "'>\n";
							$data .= "\t\t\t\t\t\t<property name='id' value='" . $agent->getChild($i)->getClss() . "' type='String' />\n";
							$data .= "\t\t\t\t\t\t<property name='objective.bid' value='" . $agent->getChild($i)->getBid() . "' type='String' />\n";
							$data .= "\t\t\t\t\t</configuration>\n";
						}

						if ($agent->getChild($i) instanceof Device) {
							$data .= "\t\t\t\t\t<configuration type='factory' cluster='" . $this->cluster . "' pid='net.powermatcher.core.agent.test.TestAgent' id='" . $agent->getChild($i)->getId() . "'>\n";
							$data .= "\t\t\t\t\t\t<property name='id' value='" . $agent->getChild($i)->getClss() . "' type='String' />\n";
							$data .= "\t\t\t\t\t</configuration>\n";
						}

						if($agent->getChild($i) instanceof Concentrator) {
							$data .= "\t\t\t\t\t<configuration type='factory' cluster='" . $this->cluster . "' pid='net.powermatcher.core.agent.concentrator.Concentrator' id='" . $agent->getChild($i)->getId() . "'>\n";
							$data .= "\t\t\t\t\t\t<property name='id' value='" . $agent->getChild($i)->getClss() . "' type='String' />\n";
							$data .= "\t\t\t\t\t</configuration>\n";
						}
					}

					$data .= "\t\t\t</configuration>\n\n";
				}
			}

			if($agent instanceof Concentrator) {
				$data .= "\t\t\t<configuration type='group' id='" . $agent->getId() . "-children'>\n";
				$data .= "\t\t\t\t<property name='matcher.id' value='" . $agent->getClss() . "' type='String' />\n";
				
				for ($y = 0; $y < $agent->getChildrenSize(); $y++) {
					if ($agent->getChild($y) instanceof Device) {
						$data .= "\t\t\t\t\t<configuration type='factory' cluster='" . $this->cluster . "' pid='net.powermatcher.core.agent.test.TestAgent' id='" . $agent->getChild($y)->getId() . "'>\n";
						$data .= "\t\t\t\t\t\t<property name='id' value='" . $agent->getChild($y)->getClss() . "' type='String' />\n";
						$data .= "\t\t\t\t\t</configuration>\n";
					}

					if ($agent->getChild($y) instanceof Objective) {
						$data .= "\t\t\t\t\t<configuration type='factory' cluster='" . $this->cluster . "' pid='net.powermatcher.core.agent.objective.ObjectiveAgent' id='" . $agent->getChild($y)->getId() . "'>\n";
						$data .= "\t\t\t\t\t\t<property name='id' value='" . $agent->getChild($y)->getClss() . "' type='String' />\n";
						$data .= "\t\t\t\t\t\t<property name='objective.bid' value='" . $agent->getChild($y)->getBid() . "' type='String' />\n";
						$data .= "\t\t\t\t\t</configuration>\n";
					}


					if($agent->getChild($y) instanceof Concentrator) {
						$data .= "\t\t\t\t\t<configuration type='factory' cluster='" . $this->cluster . "' pid='net.powermatcher.core.agent.concentrator.Concentrator' id='" . $agent->getChild($y)->getId() . "'>\n";
						$data .= "\t\t\t\t\t\t<property name='id' value='" . $agent->getChild($y)->getClss() . "' type='String' />\n";
						$data .= "\t\t\t\t\t</configuration>\n";
					}
				}

				$data .= "\t\t\t</configuration>\n\n";
			}
		}

		$data .= "\t\t</configuration>\n";
		$data .= "\t</configuration>\n";
		$data .= "</nodeconfig>";

		file_put_contents($this->cluster . ".xml", $data);
	}
}
?>