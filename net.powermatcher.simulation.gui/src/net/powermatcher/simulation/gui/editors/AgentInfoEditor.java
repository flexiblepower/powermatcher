package net.powermatcher.simulation.gui.editors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.powermatcher.core.agent.framework.config.AgentConfiguration;
import net.powermatcher.core.object.config.IdentifiableObjectConfiguration;
import net.powermatcher.simulation.configuration.DataDescriptor;
import net.powermatcher.simulation.configuration.DataDescriptor.DataType;
import net.powermatcher.simulation.configuration.DataSinkDescriptor;
import net.powermatcher.simulation.configuration.NodeDescriptor;
import net.powermatcher.simulation.engine.SimulationCycleListener;
import net.powermatcher.simulation.gui.Application;
import net.powermatcher.simulation.gui.ApplicationSimulationControl;
import net.powermatcher.simulation.gui.GUIUtils;
import net.powermatcher.simulation.telemetry.metadata.MeasurementTelemetryDefinition;
import net.powermatcher.simulation.telemetry.metadata.ProviderDefinition;
import net.powermatcher.simulation.telemetry.metadata.StatusTelemetryDefinition;
import net.powermatcher.simulation.telemetry.metadata.TelemetryDefinition;
import net.powermatcher.simulation.telemetry.metadata.TelemetryMetaData;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.ObjectClassDefinition;

// TODO rename, not only for agents
public class AgentInfoEditor extends EditorPart {
	public static final String ID = AgentInfoEditor.class.getName().toLowerCase();

	public SimulationStateListener listener = new SimulationStateListener() {
		
		@Override
		public void simulationStopped() {
			// TODO Auto-generated method stub
			optionsPart.simulationStopped();
			loggingPart.simulationStopped();
			optionsPart.getControl().setEnabled(true);
			loggingPart.getControl().setEnabled(true);
		}

		@Override
		public void simulationStarted() {
			// TODO Auto-generated method stub
			optionsPart.simulationStarted();
			loggingPart.simulationStarted();
			optionsPart.getControl().setEnabled(false);
			loggingPart.getControl().setEnabled(false);
		}
	};
	private NodeDescriptor node;
	private ObjectClassDefinition configurationDefinition;

	private TelemetryMetaData telemetryData;

	private LoggingPart loggingPart;
	private OptionsPart optionsPart;

	@Override
	public void createPartControl(Composite parent) {
		
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		parent.setLayout(gridLayout);

		String nodeTypeName = GUIUtils.getInstance().getDisplayableName(this.node.getFactoryPid());

		Label typeLabel = new Label(parent, SWT.NONE);
		typeLabel.setText(nodeTypeName + " configuration");

		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		layoutData.heightHint = 20;
		typeLabel.setLayoutData(layoutData);

		optionsPart = new OptionsPart(parent);
		layoutData = new GridData(GridData.FILL_BOTH);
		optionsPart.getControl().setLayoutData(layoutData);
		//optionsPart.getControl().setEnabled(false);
		
		loggingPart = new LoggingPart(parent);
		layoutData = new GridData(GridData.FILL_BOTH);
		loggingPart.getControl().setLayoutData(layoutData);
		//loggingPart.getControl().setEnabled(false);
		
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		if (input instanceof AgentEditorInput) {
			AgentEditorInput agentEditorInput = (AgentEditorInput) input;
			this.configurationDefinition = agentEditorInput.getConfigurationDefinition();
			this.telemetryData = agentEditorInput.getTelemetryMetaData();
			this.node = agentEditorInput.getNodeDescriptor();
		} else {
			throw new IllegalArgumentException("Input not an instance of AgentEditorInput!");
		}

		setSite(site);
		setInput(input);
		setPartName(input.getName());
		ApplicationSimulationControl control = ((ApplicationSimulationControl) Application.getInstance().getSimulationControl());
		control.addSimulationStateListener(listener);
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void setFocus() {
		// maybe the user changed the datasinks in another editor
		loggingPart.refresh();
	}

	private class OptionsPart implements SelectionListener, ModifyListener {
		private final Map<String, Widget> fields = new HashMap<String, Widget>();
		private Group group;

		public OptionsPart(Composite parent) {
			group = new Group(parent, SWT.NONE);
			

			group.setText("Component configuration");
			group.setLayout(new GridLayout(2, true));

			if (configurationDefinition != null) {
				for (AttributeDefinition ad : configurationDefinition
						.getAttributeDefinitions(ObjectClassDefinition.ALL)) {
					String id = ad.getID();

					// skip over cluster and matcher id properties
					if (id.equals(IdentifiableObjectConfiguration.CLUSTER_ID_PROPERTY)
							|| id.equals(AgentConfiguration.PARENT_MATCHER_ID_PROPERTY)) {
						continue;
					}

					// skip over factory properties
					String lastWord = id.substring(id.lastIndexOf(".") + 1, id.length());
					if (lastWord.equalsIgnoreCase("factory")) {
						continue;
					}

					// skip over applianceId
					if (id.equals("applianceId")) {
						continue;
					}

					// skip over applianceId
					if (id.equals("enabled")) {
						continue;
					}

					// if(lastWord.equalsIgnoreCase("level")){
					// System.out.println("configuration Changed");
					// this.agent.changeConfigurationParameter(id,
					// ad.getDefaultValue());
					// }

					// ad the field to the group for the attribute definition
					addField(ad, group);
				}
			}
		}

		public void simulationStarted() {
			// TODO Auto-generated method stub
			group.setEnabled(false);
			Collection<Widget> values = fields.values();
			Iterator<Widget> iterator = values.iterator();
			while(iterator.hasNext()){
				Widget widget = iterator.next();
				if(widget instanceof Text){
					((Text)widget).setEnabled(false);
				} else if(widget instanceof Spinner){
					((Spinner)widget).setEnabled(false);
				} else if(widget instanceof Button){
					((Button)widget).setEnabled(false);
				} else if(widget instanceof Combo){
					((Combo)widget).setEnabled(false);
				}

			}
			
		}

		public void simulationStopped() {
			// TODO Auto-generated method stub
			group.setEnabled(true);
			Collection<Widget> values = fields.values();
			Iterator<Widget> iterator = values.iterator();
			while(iterator.hasNext()){
				Widget widget = iterator.next();
				if(widget instanceof Text){
					((Text)widget).setEnabled(true);
				} else if(widget instanceof Spinner){
					((Spinner)widget).setEnabled(true);
				} else if(widget instanceof Button){
					((Button)widget).setEnabled(true);
				} else if(widget instanceof Combo){
					((Combo)widget).setEnabled(true);
				}

			}
		}

		public Control getControl() {
			return group;
		}

		private void addField(AttributeDefinition attribute, Composite group) {
			createFieldLabel(attribute, group);

			String value = getValue(attribute);

			if (attribute.getOptionValues() != null && attribute.getOptionValues().length > 0) {
				this.fields.put(attribute.getID(), createComboField(attribute, group, value));
			} else {
				this.fields.put(attribute.getID(), createField(attribute, group, value));
			}
		}

		private void createFieldLabel(AttributeDefinition attribute, Composite group) {
			String labelText = attribute.getID().replace('.', ' ');
			labelText = Character.toUpperCase(labelText.charAt(0)) + labelText.substring(1);

			StringBuilder spacedLabelText = new StringBuilder();
			spacedLabelText.append(labelText.charAt(0));

			char[] chars = new char[labelText.length()];
			labelText.getChars(0, labelText.length(), chars, 0);

			for (int i = 1; i < chars.length; i++) {
				char c = chars[i];

				if (Character.isUpperCase(c) && chars[i - 1] != ' ') {
					c = Character.toLowerCase(c);
					spacedLabelText.append(' ');
				}

				spacedLabelText.append(c);
			}

			labelText = spacedLabelText.toString();

			Label label = new Label(group, SWT.NONE);
			label.setText(labelText);

			if (attribute.getDescription() != null) {
				label.setToolTipText(attribute.getDescription());
			}
		}

		private String getValue(AttributeDefinition attribute) {
			HashMap<String, Object> configuration = node.getConfiguration();
			if (configuration.containsKey(attribute.getID())) {
				return String.valueOf(configuration.get(attribute.getID()));
			} else if (attribute.getDefaultValue() != null && attribute.getDefaultValue().length > 0) {
				return attribute.getDefaultValue()[0];
			} else {
				return "";
			}
		}

		private Widget createComboField(AttributeDefinition ad, Composite parent, String value) {
			Combo combo = new Combo(parent, SWT.READ_ONLY);
			combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			combo.addModifyListener(this);
	

			String[] optionLabels = ad.getOptionLabels();
			String[] optionValues = ad.getOptionValues();
			for (int i = 0; i < optionLabels.length; i++) {
				combo.add(optionLabels[i]);

				if (optionValues[i].equals(value)) {
					combo.select(i);
				}
			}

			return combo;
		}

		private Widget createField(AttributeDefinition ad, Composite parent, String value) {
			switch (ad.getType()) {
			case AttributeDefinition.BOOLEAN:
				Button checkbox = new Button(parent, SWT.CHECK);
				checkbox.setSelection(Boolean.valueOf(value));
				checkbox.addSelectionListener(this);
				return checkbox;
			case AttributeDefinition.BYTE:
			case AttributeDefinition.INTEGER:
			case AttributeDefinition.LONG:
				Spinner spinner = new Spinner(parent, SWT.BORDER);
				spinner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				spinner.setMinimum(0);
				spinner.setSelection(Integer.valueOf(value));
				spinner.setMaximum(1000);
				spinner.setIncrement(1);
				spinner.setPageIncrement(100);
				spinner.addModifyListener(this);
				return spinner;
			case AttributeDefinition.STRING:
			default:
				Text text = new Text(parent, SWT.BORDER);
				text.setText(value);
				text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				text.addModifyListener(this);
				return text;
			}
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// not interesting
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			this.saveChange(e);
		}

		@Override
		public void modifyText(ModifyEvent e) {
			this.saveChange(e);
		}

		private void saveChange(TypedEvent e) {
			Object source = e.getSource();

			String key = findKey(source);
			if (key == null) {
				// TODO log error
				return;
			}

			String value = getValue(source, key);
			if (value != null) {
				node.changeConfigurationParameter(key, value);

				final IStatusLineManager statusLineManager = getEditorSite().getActionBars().getStatusLineManager();
				statusLineManager.setMessage("Settings saved");
				getEditorSite().getShell().getDisplay().timerExec(500, new Runnable() {
					public void run() {
						statusLineManager.setMessage(null);
					}
				});
			}
			// TODO else{ log error }
		}

		private AttributeDefinition findAttributeDefinitionByKey(String key) {
			AttributeDefinition cur = null;
			for (AttributeDefinition ad : configurationDefinition.getAttributeDefinitions(ObjectClassDefinition.ALL)) {
				if (ad.getID().equals(key)) {
					cur = ad;
				}
			}
			return cur;
		}

		private String getValue(Object source, String key) {
			if (source instanceof Text) {
				return ((Text) source).getText();
			} else if (source instanceof Spinner) {
				return ((Spinner) source).getText();
			} else if (source instanceof Button) {
				return String.valueOf(((Button) source).getSelection());
			} else if (source instanceof Combo) {
				AttributeDefinition definition = findAttributeDefinitionByKey(key);
				if (definition == null) {
					return null;
				}

				int selectionIndex = ((Combo) source).getSelectionIndex();
				return definition.getOptionValues()[selectionIndex];
			} else {
				return null;
			}
		}

		private String findKey(Object source) {
			for (Entry<String, Widget> entry : this.fields.entrySet()) {
				if (entry.getValue().equals(source)) {
					return entry.getKey();
				}
			}

			return null;
		}
	}

	private class LoggingPart {

		private Group group;
		private Table loggingTable;

		private final ArrayList<TableEditor> dataSinkCheckboxes = new ArrayList<TableEditor>();
		private final SelectionListener dataSinkCheckboxListener = new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				Button widget = (Button) e.widget;
				DataSinkDescriptor sink = (DataSinkDescriptor) widget.getData("dataSink");
				DataDescriptor descriptor = (DataDescriptor) widget.getData("dataDescriptor");
				if (widget.getSelection()) { // checkbox is selected
					sink.getDataDescriptors().add(descriptor);
				} else { // checkbox is deselected
					sink.getDataDescriptors().remove(descriptor);
				}
			}
		};

		public LoggingPart(Composite parent) {
			group = new Group(parent, SWT.NONE);
			group.setText("Logging configuration");
			group.setLayout(new GridLayout(1, false));

			this.loggingTable = new Table(group, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
			this.loggingTable.setLinesVisible(true);
			this.loggingTable.setHeaderVisible(true);

			GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
			data.heightHint = 200;
			this.loggingTable.setLayoutData(data);

			if (telemetryData == null) {
				Label errorLabel = new Label(group, SWT.NONE);
				errorLabel.setForeground(new Color(null, 255, 128, 0));
				errorLabel.setText("Could not load telemetry metadata! The simulation tool will not be\n"
						+ "able to log or visualize data from this component until a telemetry\n"
						+ "metadata XML file is added to the bundle.");
			}

			this.refresh();
		}

		public void simulationStarted() {
			// TODO Auto-generated method stub
			group.setEnabled(false);
			loggingTable.setEnabled(false);
		}

		public void simulationStopped() {
			// TODO Auto-generated method stub
			group.setEnabled(true);
			loggingTable.setEnabled(true);
		}

		public Control getControl() {
			return group;
		}

		private void refresh() {
			// Clear everything
			for (TableEditor e : this.dataSinkCheckboxes) {
				e.getEditor().dispose();
				e.dispose();
			}
			this.dataSinkCheckboxes.clear();
			this.loggingTable.removeAll();
			while (this.loggingTable.getColumnCount() > 0) {
				this.loggingTable.getColumns()[0].dispose();
			}

			// Create columns
			new TableColumn(this.loggingTable, SWT.NONE).setText("Property");
			for (DataSinkDescriptor desc : node.getScenarioDescriptor().getDataSinks()) {
				new TableColumn(this.loggingTable, SWT.NONE).setText(desc.getSinkId());
			}

			// Add items in table
			TableItem priceItem = new TableItem(this.loggingTable, SWT.NONE);
			priceItem.setText("Price");
			createDataSinkCheckboxes(priceItem, new DataDescriptor(node.getId(), node.getClusterId(), DataType.PRICE));

			TableItem bidItem = new TableItem(this.loggingTable, SWT.NONE);
			bidItem.setText("Bid");
			createDataSinkCheckboxes(bidItem, new DataDescriptor(node.getId(), node.getClusterId(), DataType.BID));

			if (telemetryData != null) {
				ProviderDefinition[] providerDefinitions = telemetryData.getProviderDefinitions();
				for (ProviderDefinition pd : providerDefinitions) {
					for (TelemetryDefinition td : pd.getTelemetryDefinitions()) {
						String description = td.getDescription();
						DataDescriptor dataDescriptor = null;
						/*
						 * if (td instanceof AlertTelemetryDefinition) {
						 * dataDescriptor = new
						 * DataDescriptor(this.agent.getId(),
						 * this.agent.getClusterId(), DataType.TELEMETRY_ALERT,
						 * td.getKey()); } else if (td instanceof
						 * ControlTelemetryDefinition) { description =
						 * description.concat(" (" +
						 * ((ControlTelemetryDefinition) td).getUnit() + ')');
						 * dataDescriptor = new
						 * DataDescriptor(this.agent.getId(),
						 * this.agent.getClusterId(), DataType.TELEMTRY_CONTROL,
						 * td.getKey()); } else
						 */if (td instanceof MeasurementTelemetryDefinition) {
							description = description.concat(" (" + ((MeasurementTelemetryDefinition) td).getUnit()
									+ ')');
							dataDescriptor = new DataDescriptor(node.getId(), node.getClusterId(),
									DataType.TELEMETRY_MEASUREMENT, td.getKey());
						} else if (td instanceof StatusTelemetryDefinition) {
							dataDescriptor = new DataDescriptor(node.getId(), node.getClusterId(),
									DataType.TELEMETRY_STATUS, td.getKey());
						}
						TableItem item = new TableItem(this.loggingTable, SWT.NONE);
						item.setText(description);
						createDataSinkCheckboxes(item, dataDescriptor);
					}
				}
			}

			// Resize columns
			this.loggingTable.getColumn(0).setWidth(200);
			for (int i = 1; i < this.loggingTable.getColumnCount(); i++) {
				this.loggingTable.getColumn(i).pack();
			}
		}

		/**
		 * Create the checkboxes for an item in the logging table
		 * 
		 * @param item
		 * @param dataDescriptor
		 */
		private void createDataSinkCheckboxes(TableItem item, DataDescriptor dataDescriptor) {
			int i = 0;

			for (DataSinkDescriptor sink : node.getScenarioDescriptor().getDataSinks()) {
				TableEditor editor = new TableEditor(this.loggingTable);
				this.dataSinkCheckboxes.add(editor);
				Button button = new Button(this.loggingTable, SWT.CHECK);
				button.setData("dataDescriptor", dataDescriptor);
				button.setData("dataSink", sink);
				button.addSelectionListener(this.dataSinkCheckboxListener);
				editor.grabHorizontal = true;
				editor.setEditor(button, item, ++i);

				// Should it be checked?
				boolean checked = false;
				for (DataDescriptor desc : sink.getDataDescriptors()) {
					if (desc.equals(dataDescriptor)) {
						checked = true;
					}
				}
				button.setSelection(checked);
			}
		}
	}

	@Override
	public String getTitleToolTip() {
		return "Agent editor";
	}
}
