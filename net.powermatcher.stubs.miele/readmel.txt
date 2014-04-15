Miele Gateway Stub
------------------
This project contains the a stub, or emulator, of the Miele@home Gateway XGW 2000.

Main class is net.powermatcher.stubs.miele.gateway.MieleGatewayStub. The program requires
a properties file that contains the definitions of the Miele appliance (current only
freezer-refrigerators and dishwashers are supported). The gateway port is also configured
using this file.

Starting:
net.powermatcher.stubs.miele.gateway.MieleGatewayStub <properties file>  
(Hint: use the launch configuration in Eclipse).

Stopping:
interrupt the process (terminate, CTRL-C).


Example URLs for retrieving information of invoking an action on the appliance:

-- Get appliance list
http://localhost:8082/homebus


-- Get appliance detail in German of refrigerator freezer of type KFN_8767 and id KFN_8767.001SIM 
http://localhost:8082/homebus/device?language=de_DE&type=KFN_8767&id=KFN_8767.001SIM

-- Get appliance detail in German of dishwasher of type DW_G1000 and id DW_G1000.004SIM 
http://localhost:8082/homebus/device?language=de_DE&type=DW_G1000&id=DW_G1000.004SIM


-- Start super cooling
http://localhost:8082/homebus/device?type=KFN_8767&id=KFN_8767.001SIM&action=startSuperCooling

-- Stop super cooling
http://localhost:8082/homebus/device?type=KFN_8767&id=KFN_8767.001SIM&action=stopSuperCooling

-- Set superfrost on for freezer
http://localhost:8082/homebus/device?type=KFN_8767&id=KFN_8767.001SIM&action=startSuperFreezing

-- Set superfrost off --
http://localhost:8082/homebus/device?type=KFN_8767&id=KFN_8767.001SIM&action=stopSuperFreezing

-- Start dishwasher
http://localhost:8082/homebus/device?type=DW_G1000&id=DW_G1000.004SIM&action=start

-- Stop dishwasher (currently not supported by stub)
http://localhost:8082/homebus/device?type=DW_G1000&id=DW_G1000.004SIM&action=stop