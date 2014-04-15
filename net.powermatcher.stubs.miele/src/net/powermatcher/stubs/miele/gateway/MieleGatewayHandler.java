package net.powermatcher.stubs.miele.gateway;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.powermatcher.der.agent.miele.at.home.gateway.constants.MieleApplianceConstants;
import net.powermatcher.der.agent.miele.at.home.gateway.constants.MieleGatewayConstants;
import net.powermatcher.der.agent.miele.at.home.gateway.utils.MieleApplianceUtil;
import net.powermatcher.stubs.miele.appliance.IMieleAppliance;
import net.powermatcher.stubs.miele.appliance.IMieleDishWasherStub;
import net.powermatcher.stubs.miele.appliance.IMieleFridgeFreezerStub;
import net.powermatcher.stubs.miele.appliance.MieleFridgeFreezerStub;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 * @author IBM
 * @version 1.0.0
 */
public class MieleGatewayHandler extends AbstractHandler {

	private Map<String, IMieleAppliance> appliances = new HashMap<String, IMieleAppliance>();

	private int port;
	private Properties properties;

	/**
	 * @param port 
	 * 
	 */
	public MieleGatewayHandler(int port, Properties properties) {
		super();
		this.port = port;
		this.properties = properties;

		// Initialize the appliances
		init();
	}

	private String getActionOk(final String id, final String type, final String action) {
		StringBuffer msg = new StringBuffer();
		msg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		// msg.append("<!DOCTYPE error SYSTEM \"../dtd/action_ok_response.dtd\" >");
		msg.append("<ok>");
		msg.append(String.format("  <action>%s</action>", action));
		msg.append(String.format("  <cu-type>%s</cu-type>", type));
		msg.append(String.format("  <cu-id>%s</cu-id>", id));
		msg.append("</ok>");

		return msg.toString();
	}

	private String getActionParameterKeyword(final String action) {
		String keyword = null;

		if (action.equals(MieleApplianceConstants.APPLIANCE_ACTION_START)) {
			keyword = MieleApplianceConstants.APPLIANCE_ACTION_PARAM_START;
		}
		if (action.equals(MieleApplianceConstants.APPLIANCE_ACTION_STOP)) {
			keyword = MieleApplianceConstants.APPLIANCE_ACTION_PARAM_STOP;
		} else if (action.equals(MieleApplianceConstants.APPLIANCE_ACTION_SUPERFROST_ON)) {
			keyword = MieleApplianceConstants.APPLIANCE_ACTION_PARAM_SUPERFROST_ON;
		} else if (action.equals(MieleApplianceConstants.APPLIANCE_ACTION_SUPERFROST_OFF)) {
			keyword = MieleApplianceConstants.APPLIANCE_ACTION_PARAM_SUPERFROST_OFF;
		} else if (action.equals(MieleApplianceConstants.APPLIANCE_ACTION_SUPERCOOLING_ON)) {
			keyword = MieleApplianceConstants.APPLIANCE_ACTION_PARAM_SUPERCOOLING_ON;
		} else if (action.equals(MieleApplianceConstants.APPLIANCE_ACTION_SUPERCOOLING_OFF)) {
			keyword = MieleApplianceConstants.APPLIANCE_ACTION_PARAM_SUPERCOOLING_OFF;
		}

		return keyword;
	}

	private String getApplianceDetails(final Map<String, String[]> params) {
		String id = params.get(MieleGatewayConstants.MG_URL_PARAM_APPLIANCE_ID)[0];
		String type = params.get(MieleGatewayConstants.MG_URL_PARAM_APPLIANCE_TYPE)[0];

		String msg = null;

		// Find the appliance
		IMieleAppliance appliance = this.appliances.get(id);

		if (appliance instanceof IMieleFridgeFreezerStub) {
			msg = getFridgeFreezerDetails(params);
		} else if (appliance instanceof IMieleDishWasherStub) {
			msg = getDishWasherDetails(params);
		} else {
			return getErrorResponse(id, type, MieleGatewayConstants.ERROR_APPLIANCE_MISSING, null,
					MieleGatewayConstants.ERROR_MSG_APPLIANCE_MISSING);
		}

		return msg;
	}

	private String getApplianceSummary() {

		StringBuffer msg = new StringBuffer();
		msg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		// msg.append("<!DOCTYPE device SYSTEM \"../dtd/appliance_list.dtd\" >");
		msg.append("<DEVICES>");

		// Iterate through the appliances to create the device summary
		Set<String> keys = this.appliances.keySet();
		for (String key : keys) {
			IMieleAppliance appliance = this.appliances.get(key);
			msg.append("<device>");

			msg.append(String.format("<class>%s</class>", appliance.getClassId()));
			msg.append(String.format("<UID>%s</UID>", appliance.getId()));
			msg.append(String.format("<type>%s</type>", appliance.getType()));
			msg.append(String.format("<name>%s</name>", appliance.getName()));
			msg.append(String.format("<state>%s</state>", appliance.getState()));
			msg.append(String.format("<additionalName>%s</additionalName>", appliance.getAdditionalName()));
			msg.append(String.format("<room id=\"%s\" level=\"%s\">%s</room>", appliance.getRoomId(), appliance.getRoomLevel(),
					appliance.getRoom()));
			msg.append("<information>");
			msg.append(String.format("<key name=\"Gerätestatus\" value=\"%s\"/>",
					MieleApplianceUtil.getStateDescription(null, appliance.getState())));
			if (appliance instanceof IMieleDishWasherStub) {
				msg.append(String.format("<key name=\"Restzeit\" value=\"%s\"/>",
						((IMieleDishWasherStub) appliance).getRemainingTime()));
			}
			msg.append("</information>");
			msg.append("<actions>");
			// msg.append("<action name=\"Details\"URL=\"http://192.168.1.201/homebus/device?language=de_DE&type=DW_G1000&id=DW_G1000.6\"/>");
			msg.append("</actions>");
			msg.append("</device>");
		}
		msg.append("</DEVICES>");

		return msg.toString();
	}

	/*
	 * <device> <information> <key name="Gerät" value="Geschirrspüler"/> <key
	 * name="Gerätestatus" value="In Betrieb"/> <key name="Restzeit"
	 * value="1:12h"/> <key name="Programm" value="Stark 65°C"/> <key
	 * name="Phase" value="Vorspülen"/> </information> <actions> <action
	 * name="Stop" URL=
	 * "http://192.168.1.201/homebus/device?type=DW_G1000&id=DW_G1000.6&action=stop"
	 * /> </actions> </device>
	 */
	private String getDishWasherDetails(final Map<String, String[]> params) {
		String id = params.get("id")[0];
		System.out.println("fridge id= " + id);

		IMieleDishWasherStub dw = (IMieleDishWasherStub) this.appliances.get(id);

		// temporary
		String language = MieleGatewayConstants.LANGUAGE_GERMAN;

		// Update to the latest state
		dw.update();

		StringBuffer msg = new StringBuffer();
		if (dw != null) {

			msg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			// msg.append("<!DOCTYPE device SYSTEM \"dtd/appliance_info.dtd\" >");
			msg.append("<device>");
			msg.append("<information>");
			msg.append(String.format("<key name=\"Gerät\" value=\"%s\" />",
					MieleApplianceUtil.getApplianceClassName(language, dw.getClassId())));
			msg.append("<key name=\"Gerätestatus\" value=\"" + MieleApplianceUtil.getStateDescription(null, dw.getState())
					+ "\" />");
			// if (dw.getProgram() != null)
			// msg.append(String.format("<key name=\"Restzeit\" value=\"%s\"/>",
			// dw.getRemainingTime() ));
			// if (dw.getProgram() != null)
			// msg.append(String.format("<key name=\"Programm\" value=\"%s\"/>",
			// dw.getProgram() ));
			// if (dw.getPhase() != null)
			// msg.append(String.format("<key name=\"Phase\" value=\"%s\"/>",
			// dw.getPhase() ));

			if (dw.getState() == MieleApplianceConstants.MA_STATE_PROGRAM) {
				// Dishwasher state is 'Programmed'
				if (dw.getProgram() != null) {
					msg.append(String.format("<key name=\"%s\" value=\"%s\"/>", MieleApplianceUtil.getInformationPropertyName(
							MieleApplianceConstants.DISHWASHER_DURATION, language), (new Integer(dw.getDuration()).toString())));
				}
				if (dw.getProgram() != null) {
					msg.append(String.format(
							"<key name=\"%s\" value=\"%s\"/>",
							MieleApplianceUtil.getInformationPropertyName(MieleApplianceConstants.DISHWASHER_PROGRAM, language),
							dw.getProgram()));
				}
			} else if (dw.getState() == MieleApplianceConstants.MA_STATE_ON) {
				// Dishwasher is running
				if (dw.getProgram() != null) {
					msg.append(String.format("<key name=\"%s\" value=\"%s\"/>", MieleApplianceUtil.getInformationPropertyName(
							MieleApplianceConstants.DISHWASHER_REMAINING_TIME, language), dw.getRemainingTime()));
				}
				if (dw.getProgram() != null) {
					msg.append(String.format(
							"<key name=\"%s\" value=\"%s\"/>",
							MieleApplianceUtil.getInformationPropertyName(MieleApplianceConstants.DISHWASHER_PROGRAM, language),
							dw.getProgram()));
				}
				if (dw.getPhase() != null) {
					msg.append(String.format("<key name=\"%s\" value=\"%s\"/>",
							MieleApplianceUtil.getInformationPropertyName(MieleApplianceConstants.DISHWASHER_PHASE, language),
							dw.getPhase()));
				}
			} else if (dw.getState() == MieleApplianceConstants.MA_STATE_WAITING) {
				// Dishwasher is running
				if (dw.getProgram() != null) {
					msg.append(String.format("<key name=\"%s\" value=\"%s\"/>", MieleApplianceUtil.getInformationPropertyName(
							MieleApplianceConstants.DISHWASHER_DURATION, language), dw.getDuration()));
				}
				if (dw.getProgram() != null) {
					msg.append(String.format(
							"<key name=\"%s\" value=\"%s\"/>",
							MieleApplianceUtil.getInformationPropertyName(MieleApplianceConstants.DISHWASHER_PROGRAM, language),
							dw.getProgram()));
				}
				if (dw.getStartTime() != null) {
					msg.append(String.format("<key name=\"%s\" value=\"%s\"/>", MieleApplianceUtil.getInformationPropertyName(
							MieleApplianceConstants.DISHWASHER_START_TIME, language), dw.getStartTime()));
				}
			}

			msg.append("</information>");
			msg.append("<actions>");
			String actionKey = null;
			if (dw.getActions() != null) {
				for (String action : dw.getActions()) {
					actionKey = getActionParameterKeyword(action);
					if (actionKey != null) {
						msg.append("<action name=\"" + action + "\" URL=\"http://localhost:" + port + "/homebus/device?type="
								+ dw.getType() + "&amp;id=" + dw.getId() + "&amp;action=" + actionKey + "\"/> ");
					}
				}
			}

			msg.append("</actions>");

			msg.append("</device>");

			// Debug
			System.out.println(msg.toString());
		}

		return msg.toString();
	}

	private String getErrorResponse(final String id, final String type, final String errorType, final String action,
			final String message) {
		StringBuffer msg = new StringBuffer();
		msg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		// msg.append("<!DOCTYPE error SYSTEM \"../dtd/error.dtd\" >");
		msg.append("<error>");
		msg.append(String.format("  <error-type>%s</error-type>", errorType));
		msg.append(String.format("  <cu-type>%s</cu-type>", type));
		msg.append(String.format("  <cu-id>%s</cu-id>", id));
		msg.append(action != null ? String.format("  <action-id>%s</action-id>", action) : "");
		msg.append(String.format("  <message>%s</message>", message));
		msg.append("</error>");

		return msg.toString();
	}

	private String getFridgeFreezerDetails(final Map<String, String[]> params) {
		String id = params.get("id")[0];
		System.out.println("fridge id= " + id);

		IMieleFridgeFreezerStub fridge = (IMieleFridgeFreezerStub) this.appliances.get(id);
		StringBuffer msg = new StringBuffer();
		if (fridge != null) {

			msg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			// msg.append("<!DOCTYPE device SYSTEM \"dtd/appliance_info.dtd\" >");
			msg.append("<device>");
			msg.append("<information>");
			msg.append(String.format("<key name=\"Gerät\" value=\"%s\" />", fridge.getName()));
			msg.append("<key name=\"Gerätestatus\" value=\"" + MieleApplianceUtil.getStateDescription(null, fridge.getState())
					+ "\" />");
			msg.append("<key name=\"Kühlstatus\" value=\""
					+ MieleApplianceUtil.getStateDescription(null, fridge.getFridgeStatus()) + "\" />");
			msg.append("<key name=\"Ziel-Kühltemperatur\" value=\"" + fridge.getTargetTemperatureFridge() + " °C\" />");
			msg.append("<key name=\"Aktuelle Kühltemperatur\" value=\"" + fridge.getTemperatureFridge() + " °C\" />");
			msg.append("<key name=\"Gefrierstatus\" value=\""
					+ MieleApplianceUtil.getStateDescription(null, fridge.getFreezerStatus()) + "\" />");
			msg.append("<key name=\"Ziel-Gefriertemperatur\" value=\"" + fridge.getTargetTemperatureFreezer() + " °C\" />");
			msg.append("<key name=\"Aktuelle Gefriertemperatur\" value=\"" + fridge.getTemperatureFreezer() + " °C\" />");
			msg.append("</information>");
			msg.append("<actions>");

			String actionKey = null;
			if (fridge.getActions() != null) {
				for (String action : fridge.getActions()) {
					actionKey = getActionParameterKeyword(action);
					if (actionKey != null) {
						msg.append("<action name=\"" + action + "\" URL=\"http://localhost:" + port + "/homebus/device?type="
								+ fridge.getType() + "&amp;id=" + fridge.getId() + "&amp;action=" + actionKey + "\"/> ");
					}
				}
			}

			msg.append("</actions>");

			msg.append("</device>");

			// Debug
			System.out.println(msg.toString());
		}

		return msg.toString();
	}

	@Override
	public void handle(final String target, final Request baseRequest, final HttpServletRequest request,
			final HttpServletResponse response) throws IOException, ServletException {

		response.setContentType("text/xml;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);

		@SuppressWarnings("unchecked")
		Map<String, String[]> params = baseRequest.getParameterMap();

		// Handle the request
		if (target != null && target.equals(MieleGatewayConstants.MG_URL_HOMEBUS_TARGET)) {
			// Appliance overview is requested
			response.getWriter().println(getApplianceSummary());

			baseRequest.setHandled(true);
		} else if (target != null && target.equals(MieleGatewayConstants.MG_URL_DEVICE_TARGET)) {
			// Perform action on appliance
			if (params.keySet().contains(MieleGatewayConstants.MG_URL_PARAM_APPLIANCE_ACTION)) {
				response.getWriter().println(performDeviceAction(params));
			} else {
				// Appliance details are requested
				// response.getWriter().println(getFridgeFreezerDetails(params));
				// http://localhost:" + port + "/homebus/device?language=de_DE&type=DW_G1000&id=DW_G1000.6
				response.getWriter().println(getApplianceDetails(params));
			}

			baseRequest.setHandled(true);
		}

	}

	private void init() {
		DeviceLoader loader = new DeviceLoader(properties);
		appliances = loader.getAppliances();
	}

	
	private String performDeviceAction(final Map<String, String[]> params) {
		String id = params.get(MieleGatewayConstants.MG_URL_PARAM_APPLIANCE_ID)[0];
		String action = params.get(MieleGatewayConstants.MG_URL_PARAM_APPLIANCE_ACTION)[0];
		String type = params.get(MieleGatewayConstants.MG_URL_PARAM_APPLIANCE_TYPE)[0];

		// Find the appliance
		IMieleAppliance appliance = this.appliances.get(id);

		// Validate parameters: appliance
		if (appliance == null) {
			return getErrorResponse(id, type, MieleGatewayConstants.ERROR_APPLIANCE_MISSING, action,
					MieleGatewayConstants.ERROR_MSG_APPLIANCE_MISSING);
		}
		if (id == null || type == null || !appliance.getType().equals(type)) {
			return getErrorResponse(id, type, MieleGatewayConstants.ERROR_ACTION_INCORRECT_PARAMS, action,
					MieleGatewayConstants.ERROR_MSG_ACTION_INCORRECT_PARAMS);
		}
		if (action == null) {
			return getErrorResponse(id, type, MieleGatewayConstants.ERROR_ACTION_MISSING, action,
					MieleGatewayConstants.ERROR_MSG_ACTION_MISSING);
		}

		// Perform the action
		if (appliance instanceof IMieleFridgeFreezerStub) {
			MieleFridgeFreezerStub fridge = (MieleFridgeFreezerStub) appliance;

			if (action.equals(MieleApplianceConstants.APPLIANCE_ACTION_PARAM_SUPERFROST_ON)) {
				fridge.setSuperFrost(true);
			} else if (action.equals(MieleApplianceConstants.APPLIANCE_ACTION_PARAM_SUPERFROST_OFF)) {
				fridge.setSuperFrost(false);
			} else if (action.equals(MieleApplianceConstants.APPLIANCE_ACTION_PARAM_SUPERCOOLING_ON)) {
				fridge.setSuperCool(true);
			} else if (action.equals(MieleApplianceConstants.APPLIANCE_ACTION_PARAM_SUPERCOOLING_OFF)) {
				fridge.setSuperCool(false);
			} else {
				// Action not supported
				return getErrorResponse(id, type, MieleGatewayConstants.ERROR_ACTION_EXECUTE, action,
						MieleGatewayConstants.ERROR_MSG_NOT_SUPPORTED_BY_STUB);
			}
		} else if (appliance instanceof IMieleDishWasherStub) {
			IMieleDishWasherStub dw = (IMieleDishWasherStub) appliance;

			if (action.equalsIgnoreCase(MieleApplianceConstants.APPLIANCE_ACTION_START)) {
				dw.start();
			} else {
				// Action not supported
				return getErrorResponse(id, type, MieleGatewayConstants.ERROR_ACTION_EXECUTE, action,
						MieleGatewayConstants.ERROR_MSG_NOT_SUPPORTED_BY_STUB);
			}
		}

		return getActionOk(id, type, action);
	}

}
