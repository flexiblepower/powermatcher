package net.powermatcher.core.scheduler.component;


import net.powermatcher.core.object.config.ConnectableObjectConfiguration;
import net.powermatcher.core.scheduler.config.SchedulerAdapterFactoryConfiguration;
import aQute.bnd.annotation.metatype.Meta;
import aQute.bnd.annotation.metatype.Meta.OCD;

/**
 * @author IBM
 * @version 0.9.0
 */
@OCD(name = "PowerMatcher Time Adapter Factory")
public interface TimeAdapterFactoryComponentConfiguration extends SchedulerAdapterFactoryConfiguration {

	@Override
	@Meta.AD(required = false, deflt = CLUSTER_ID_DEFAULT, description = CLUSTER_ID_DESCRIPTION)
	public String cluster_id();

	@Override
	@Meta.AD(required = true, deflt = ConnectableObjectConfiguration.TIME_ADAPTER_FACTORY_DEFAULT, description = ID_DESCRIPTION)
	public String id();

}
