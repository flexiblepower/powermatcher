package net.powermatcher.stubs.miele.gateway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import net.powermatcher.der.agent.miele.at.home.gateway.constants.MieleApplianceConstants;
import net.powermatcher.der.agent.miele.at.home.gateway.utils.MieleApplianceUtil;
import net.powermatcher.stubs.miele.appliance.IMieleAppliance;
import net.powermatcher.stubs.miele.appliance.IMieleDishWasherStub;
import net.powermatcher.stubs.miele.appliance.IMieleFridgeFreezerStub;
import net.powermatcher.stubs.miele.appliance.MieleDishWasherStub;
import net.powermatcher.stubs.miele.appliance.MieleFridgeFreezerStub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author IBM
 *
 */
public class DeviceLoader {
	
	/**
	 * Define the logger (Logger) field.
	 */
	private final static Logger logger = LoggerFactory.getLogger(DeviceLoader.class);
	
	private static final String ADDITIONALNAME_PROPERTY = "additionalname";

	private static final String ROOM_LEVEL_PROPERTY = "room_level";

	private static final String ROOM_ID_PROPERTY = "room_id";

	private static final String ROOM_NAME_PROPERTY = "room_name";

	private static final String APPLIANCE_STATE_PROPERTY = "state";

	private static final String APPLIANCE_CLASS_PROPERTY = "class";

	private static final String APPLIANCE_TYPE_PROPERTY = "type";

	private static final String APPLIANCE_ID_PROPERTY = "appliance_id";

	private static final String DISHWASHER_TYPE = "dishwasher";

	private static final String REFRIGERATOR_TYPE = "refrigerator";

	private static final String ID_LIST_PROPERTY = "ids";

	private static final String PROPERTY_LIST_DELIMITER = ";, ";

	private static final String TYPE_LIST_PROPERTY = "types";
	
	private final static String DEVICE_PROPERTY_PREFIX = "device";

	
	/**
	 * Map containing the appliances which can be retrieved by their id.
	 */
	private Map<String, IMieleAppliance> appliances = new HashMap<String, IMieleAppliance>();
	
	/**
	 * Property file containing the appliance properties.
	 */
	private Properties properties;
	
	
	
	public DeviceLoader(Properties properties) {
		super();
		this.properties = properties;
		init();
	}


	
	/**
	 * @return the appliances
	 */
	public Map<String, IMieleAppliance> getAppliances() {
		return appliances;
	}



	private void init() {
		List<String> deviceList = getDeviceList(properties.getProperty(DEVICE_PROPERTY_PREFIX + '.' + TYPE_LIST_PROPERTY));
		for (String type : deviceList) {
			processDeviceType(type);
		}
	}
	
	private List<String> getDeviceList(String deviceListString) {
		List<String> devices = new ArrayList<String>();
		
		StringTokenizer st = new StringTokenizer(deviceListString, PROPERTY_LIST_DELIMITER);
		while(st.hasMoreTokens()) {
			String val = st.nextToken();
			devices.add(val.trim());
		} 
		
		return devices;
	}
	
	private List<String> getDeviceIds(String deviceType) {
		List<String> ids = new ArrayList<String>();
		
		String idString = properties.getProperty(DEVICE_PROPERTY_PREFIX + '.' + deviceType + '.' + ID_LIST_PROPERTY);
		StringTokenizer st = new StringTokenizer(idString, PROPERTY_LIST_DELIMITER);
		while(st.hasMoreTokens()) {
			String val = st.nextToken();
			ids.add(val.trim());
		} 
		
		return ids;
	}
	
	
	private void processDeviceType(String type) {
		List<String> ids = getDeviceIds(type);
		
		for (String id : ids) {
			if (type.equals(REFRIGERATOR_TYPE)) {
				processRefrigerator(id, type);
			}
			else if (type.equals(DISHWASHER_TYPE)) {
				processDishwasher(id, type);
			}
			else {
				logger.error("Type " + type + " not supported");
			}
		}

	}

	private void processRefrigerator(String id, String type) {
		IMieleFridgeFreezerStub fridge = (MieleFridgeFreezerStub) setGenericDeviceProperties(new MieleFridgeFreezerStub(), id, type);
		
		this.appliances.put(fridge.getId(), fridge);
	}

	
	private void processDishwasher(String id, String type) {
		
		// Add DishWasher. Initial start will be 'OFF'
		IMieleDishWasherStub dw = (MieleDishWasherStub) setGenericDeviceProperties(new MieleDishWasherStub(), id, type);
		
		dw.setSmartGridOn(new Boolean(getDeviceProperty(type, id, "smartgridon")));

		// Start will switch on the DW and change state to 'ON'
		dw.start();

		// Program the DW
		dw.setProgram(getDeviceProperty(type, id, "program"));
		dw.setStartTime(getDeviceProperty(type, id, "starttime"));
		dw.setState(MieleApplianceConstants.MA_STATE_WAITING);

		this.appliances.put(dw.getId(), dw);

	}

	private IMieleAppliance setGenericDeviceProperties(IMieleAppliance device, String id, String type) {
		device.setId( getDeviceProperty(type, id, APPLIANCE_ID_PROPERTY));
		device.setType(getDeviceProperty(type, id, APPLIANCE_TYPE_PROPERTY));
		device.setClassId(new Integer(getDeviceProperty(type, id, APPLIANCE_CLASS_PROPERTY)));
		device.setState(new Integer(getDeviceProperty(type, id, APPLIANCE_STATE_PROPERTY))); 
		device.setName(MieleApplianceUtil.getApplianceClassName(null, device.getClassId()));
		device.setRoom(getDeviceProperty(type, id, ROOM_NAME_PROPERTY));
		device.setRoomId(getDeviceProperty(type, id, ROOM_ID_PROPERTY));
		device.setRoomLevel(getDeviceProperty(type, id, ROOM_LEVEL_PROPERTY));
		device.setAdditionalName(getDeviceProperty(type, id, ADDITIONALNAME_PROPERTY));
		
		return device;
	}
	
	
	private String getDeviceProperty(String type, String id, String key) {
		return properties.getProperty(DEVICE_PROPERTY_PREFIX + '.' + type + '.' + id + '.' + key);
	}
}
