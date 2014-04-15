package net.powermatcher.core.config.management.agent.component;


import net.powermatcher.core.config.management.agent.ConfigManagerConfiguration;
import aQute.bnd.annotation.metatype.Meta;
import aQute.bnd.annotation.metatype.Meta.OCD;
import aQute.bnd.annotation.metatype.Meta.Type;


/**
 * @author IBM
 * @version 0.9.0
 */
@OCD(name = "Configuration Management Agent")
public interface ConfigManagerComponentConfiguration extends ConfigManagerConfiguration {

	@Override
	@Meta.AD(required = false, deflt = CLUSTER_ID_DEFAULT, description = CLUSTER_ID_DESCRIPTION)
	public String cluster_id();

	@Override
	@Meta.AD(required = true, description = ID_DESCRIPTION)
	public String id();

	@Override
	@Meta.AD(required = true, deflt = CONFIGURATION_NODE_ID_DEFAULT, description = CONFIGURATION_NODE_ID_DESCRIPTION)
	public String node_id();

	@Override
	@Meta.AD(required = false, deflt = MESSAGING_ADAPTER_FACTORY_DEFAULT, description = MESSAGING_ADAPTER_FACTORY_DESCRIPTION)
	public String messaging_adapter_factory();

	@Override
	@Meta.AD(required = true, deflt = CONFIGURATION_DATA_URL_DEFAULT, description = CONFIGURATION_DATA_URL_DESCRIPTION)
	public String configuration_data_url();

	@Override
	@Meta.AD(required = false, description = CONFIGURATION_DATA_USERNAME_DESCRIPTION)
	public String configuration_data_userid();

	@Override
	@Meta.AD(required = false, type = Type.Password, description = CONFIGURATION_DATA_PASSWORD_DESCRIPTION)
	public String configuration_data_password();

	@Override
	@Meta.AD(required = true, deflt = UPDATE_INTERVAL_DEFAULT_STR, description = UPDATE_INTERVAL_DESCRIPTION)
	public int update_interval();

}
