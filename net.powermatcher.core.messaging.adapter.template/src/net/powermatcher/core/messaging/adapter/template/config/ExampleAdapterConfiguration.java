package net.powermatcher.core.messaging.adapter.template.config;

import net.powermatcher.core.messaging.framework.config.MessagingAdapterConfiguration;


/**
 * This interface defines the configuration properties and their default values (if any) for
 * the <code>ExampleAdapter</code> adapter.
 * <p>
 * <code>ExampleAdapter</code> supports the configuration of a single example setting.
 * The configured value of this settings is printed to the log when the adapter is initialized.
 * </p><p>
 * Unless configuring a value for a property is mandatory, a default value is defined here.<br>
 * </p><p>
 * An access method with the name of the property is defined to allow the property
 * to be exported as OSGi configuration metatype via annotations. The rule is that
 * the name of the access method must be the same as the key property, with a '.' replaced
 * by a '_' due to restrictions for method names in Java.
 * </p><p>
 * A default value is always also defined as a string literal to allow the default
 * value to be referenced in OSGi configuration meta type annotations.
 * </p>
 * @author IBM
 * @version 0.9.0
 */
public interface ExampleAdapterConfiguration extends MessagingAdapterConfiguration {

}
