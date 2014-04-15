package net.powermatcher.agent.template.config;

import net.powermatcher.agent.template.ExampleAgent3;
import net.powermatcher.core.agent.template.ExampleAgent1;
import net.powermatcher.core.agent.template.config.ExampleAgent1Configuration;
import net.powermatcher.telemetry.config.TelemetryConfiguration;



/**
 * This interface defines the configuration properties and their default values (if any) for
 * PowerMatcher agent <code>ExampleAgent3</code>, which is an extension of <code>ExampleAgent1</code>.
 * As <code>ExampleAgent3</code> does not define any new properties, this interface is empty.
 * 
 * @see ExampleAgent1
 * @see ExampleAgent3
 * 
 * @author IBM
 * @version 0.9.0
 */
public interface ExampleAgent3Configuration extends ExampleAgent1Configuration, TelemetryConfiguration {

}
