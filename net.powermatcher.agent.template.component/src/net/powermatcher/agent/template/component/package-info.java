/**
 * Example project for developing PowerMatcher agents for OSGi that use features beyond the core.
 * <p>
 * The <code>net.powermatcher.agent.template.component</code> project makes the PowerMatcher agent in the
 * <code>net.powermatcher.agent.template</code> available as a configurable component in the OSGi Service Component
 * Model. The template project implements an agents, that is exported as an OSGi components by this project.
 * <ul>
 * <li><code><b>ExampleAgent3</b></code><br>An extension of <code>ExampleAgent1</code> that also publishes an update count as telemetry measurement data.</li>
 * </ul> 
 * </p>
 * <p>
 * The agent requires two classes to enable it as configurable component in the OSGi model: 
 * <ul>
 * <li><code><b>Component class</b></code><br>Declares a managed service (singleton) or managed factory service (many) component with its lifecycle methods.</li>
 * <li><code><b>ComponentConfiguration class</b><br>Declares the configuration properties of the agents as OSGi metatype Object Class Definitions.</code></li>
 * </ul>
 * The aQute.bnd.annotations translate the component and OCD declarations into the required OSGI-INF xml declaration in the bundle.
 * </p>
 *
 * @since 0.7
 */
package net.powermatcher.agent.template.component;