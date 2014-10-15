/**
 * <p>
 * The <code>net.powermatcher.core.agent.template.component</code> project makes the PowerMatcher agents in the
 * <code>net.powermatcher.core.agent.template</code> available as configurable components in the OSGi Service Component
 * Model. The template project implements two different agents, which are exported as three OSGi components by
 * this project.
 * <ul>
 * <li><code><b>ExampleAgent1</b></code><br>An agent that publishes a step-shaped bid for a configurable demand and price value.</li>
 * <li><code><b>ExampleAgent2</b></code><br>An extension of <code>ExampleAgent1</code> that implements a control and notification interface for integration
 * of this agent with an adapter.</li>
 * </ul> 
 * </p>
 * <p>
 * Each agent requires two classes to enable it as configurable component in the OSGi model: 
 * <ul>
 * <li><code><b>Component class</b></code><br>Declares a managed service (singleton) or managed factory service (many) component with its lifecycle methods.</li>
 * <li><code><b>ComponentConfiguration class</b><br>Declares the configuration properties of the agents as OSGi metatype Object Class Definitions.</code></li>
 * </ul>
 * The aQute.bnd.annotations translate the component and OCD declarations into the required OSGI-INF xml declaration in the bundle.
 * </p>
 *
 * @since 0.7
 */
package net.powermatcher.core.agent.template.component;