package net.powermatcher.server.config.xml.writer;

import java.util.List;
import java.util.Set;

import net.powermatcher.server.config.jpa.entity.Configuration;
import net.powermatcher.server.config.jpa.entity.Nodeconfiguration;
import net.powermatcher.server.config.jpa.entity.Property;


/**
 * @author IBM
 *
 * Helper class for debugging.
 * 
 */
public class NodeConfigurationHtmlWriter {

	public String getConfigHtmlDoc(Configuration config) {
		String response;
		response = "<html> <head> <title>Tutorial: HelloWorld</title> </head> <body>";
		response += getConfigHtml(config);
		response += "</body> </html>";
		return response;
	}
	
	public String getNodeConfigHtmlDoc(Nodeconfiguration nodeconfig) {
		String html = null;
	
		html = "<html> <head> <title>Tutorial: HelloWorld</title> </head> <body>"  
				+ "<p> <u>Node Configuration :</u><br/>" + " node id:"
				+ nodeconfig.getNodeid() + "<br/>" + " name:"
				+ nodeconfig.getName() + "<br/>" + " description:"
				+ nodeconfig.getDescription() + "<br/>" + " updated:"
				+ nodeconfig.getUpdated() + "<br/>";

		html += "<u>Children:</u><br/>";

		List<Configuration> children = nodeconfig.getConfigurationList();
		if (children != null) {
			for (Configuration c : children) {
				html += "- clusterId=" + c.getId().getClusterId()
						+ " configId=" + c.getId().getConfigId() + "<br/>";
			}
		} else {
			html += "no children.<br/>";
		}
		html += "</p> ";

		html += "<hr />";
		
		// Process children
		if (children != null) {
			for ( Configuration c : children) {
				html += getConfigHtml(c);
			}
		}
		
		html += "</body> </html>";
		
		return html;

	}
	
	
	private String getConfigHtml(Configuration config) {
		String hasParent = "unknown";
		if (config != null) {
			if (config.getParent() != null)
				hasParent = "clusterId="
						+ config.getParent().getId().getClusterId()
						+ " configId="
						+ config.getParent().getId().getConfigId() + "<br/>";
			else
				hasParent = "NO";
		}
		String configHtml = "";

		if (config != null) {
			configHtml = "<p> <u>Configuration:</u><br/>" + "<b> pid:</b>"
					+ config.getPid() + "<br/>" + "<b> cluster id</b>:"
					+ config.getId().getClusterId() + "<br/>"
					+ "<b> config id:</b>" + config.getId().getConfigId()
					+ "<br/>" + "<b> type:</b>" + config.getType() + "<br/>"
					+ "<b> template:</b>" + config.isTemplate() + "<br/>"
					+ "<b> parent:</b>" + hasParent + "<br/>";

			configHtml += "Properties:<br/>" + "-----------------<br/>";

			Set<Property> props = config.getProperties();
			if (props != null) {
				for (Property p : props) {
					configHtml += p.getId().getName() + "=" + p.getValue()
							+ "<br/>";
				}
			} else {
				configHtml += "no properties.<br/>";
			}

			configHtml += "Children:<br/>" + "-----------------<br/>";
			Set<Configuration> children = config.getConfigurations();
			if (children.size() != 0) {
				for (Configuration c : children) {
					configHtml += "- clusterId=" + c.getId().getClusterId()
							+ " configId=" + c.getId().getConfigId() + "<br/>";
				}
			} else {
				configHtml += "no children.<br/>";
			}
			configHtml += "</p> ";
		} else {
			configHtml = "Configuration: null<br/>";
		}

		configHtml += "<hr />";

		// Process children
		Set<Configuration> children = config.getConfigurations();
		if (children != null) {
			for (Configuration c : children) {
				configHtml += getConfigHtml(c);
			}
		}

		return configHtml;
	}
}
