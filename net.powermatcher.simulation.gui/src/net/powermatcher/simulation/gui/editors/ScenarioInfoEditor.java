package net.powermatcher.simulation.gui.editors;

import java.util.ArrayList;
import java.util.Date;

import net.powermatcher.simulation.configuration.AsFastAsPossibleSimulationClockDescriptor;
import net.powermatcher.simulation.configuration.DataSinkDescriptor;
import net.powermatcher.simulation.configuration.MarketBasisDescriptor;
import net.powermatcher.simulation.configuration.PerAgentCsvDataSinkDescriptor;
import net.powermatcher.simulation.configuration.RealTimeSimulationClockDescriptor;
import net.powermatcher.simulation.configuration.ScenarioDescriptor;
import net.powermatcher.simulation.configuration.SimulationClockDescriptor;
import net.powermatcher.simulation.configuration.SingleFileCsvDataSinkDescriptor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;

public class ScenarioInfoEditor extends EditorPart implements ModifyListener, SelectionListener {

	private final class DateTimeSelector {

		private final Composite composite;
		private final DateTime date;
		private final DateTime time;

		public DateTimeSelector(Composite parent) {
			this.composite = new Composite(parent, SWT.NONE);
			RowLayout layout = new RowLayout(SWT.HORIZONTAL);
			layout.marginLeft = 0;
			layout.marginRight = 0;
			this.composite.setLayout(layout);

			this.date = new DateTime(this.composite, SWT.DATE);
			this.time = new DateTime(this.composite, SWT.TIME);

			this.date.addSelectionListener(ScenarioInfoEditor.this);
			this.time.addSelectionListener(ScenarioInfoEditor.this);
		}

		@SuppressWarnings("deprecation")
		public Date getDate() {
			return new Date(this.date.getYear() - 1900, this.date.getMonth(), this.date.getDay(), this.time.getHours(),
					this.time.getMinutes(), this.time.getSeconds());
		}

		@SuppressWarnings("deprecation")
		public void setDate(Date date) {
			this.date.setDate(date.getYear(), date.getMonth(), date.getDay());
			this.time.setTime(date.getHours(), date.getMinutes(), date.getSeconds());
		}

		public void setEnabled(boolean enabled) {
			this.date.setEnabled(enabled);
			this.time.setEnabled(enabled);
			this.composite.setEnabled(enabled);
		}
	}

	public static final String ID = ScenarioInfoEditor.class.getName();

	private Button asFastAsPossibleClockButton;

	private Text commodityWidget;
	private Text currencyWidget;
	private Table dataSinkTable;
	private final ArrayList<TableEditor> editors1 = new ArrayList<TableEditor>();

	private final ArrayList<TableEditor> editors2 = new ArrayList<TableEditor>();

	private final ArrayList<TableEditor> editors3 = new ArrayList<TableEditor>();

	private DateTimeSelector endTime;

	private Button endTimeEnabledButton;

	private Spinner intervalSpinner;

	private final ModifyListener marketBasisListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			ScenarioInfoEditor.this.saveMarketBasisDescriptor(e.widget);
		}
	};

	private Spinner maximumPriceWidget;

	private Spinner minimumPriceWidget;

	private Spinner pricestepsWidget;

	private Button realTimeClockButton;

	private ScenarioDescriptor scenarioDescriptor;

	private DateTimeSelector startTime;

	private void createDataSinksView(Composite parent) {
		Group dataSinkGroup = new Group(parent, SWT.PUSH);
		dataSinkGroup.setText("Output files");
		dataSinkGroup.setLayout(new GridLayout(1, false));

		Composite composite = new Composite(dataSinkGroup, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		Button addCSVButton = new Button(composite, SWT.NONE);
		addCSVButton.setText("Add single CSV output file");
		addCSVButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				SingleFileCsvDataSinkDescriptor desc = new SingleFileCsvDataSinkDescriptor();
				desc.setSinkId(ScenarioInfoEditor.this.nextDataSinkId("csv"));
				desc.setOutputFile("C:\\changeme.csv");
				ScenarioInfoEditor.this.scenarioDescriptor.getDataSinks().add(desc);
				ScenarioInfoEditor.this.refreshDataSinkTable();
			}
		});
		Button addPerAgentCSVButton = new Button(composite, SWT.NONE);
		addPerAgentCSVButton.setText("Add CSV output file for every agent");
		addPerAgentCSVButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				PerAgentCsvDataSinkDescriptor desc = new PerAgentCsvDataSinkDescriptor();
				desc.setSinkId(ScenarioInfoEditor.this.nextDataSinkId("csv"));
				desc.setOutputDirectory("C:\\");
				ScenarioInfoEditor.this.scenarioDescriptor.getDataSinks().add(desc);
				ScenarioInfoEditor.this.refreshDataSinkTable();
			}
		});

		this.dataSinkTable = new Table(dataSinkGroup, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		this.dataSinkTable.setLinesVisible(true);
		this.dataSinkTable.setHeaderVisible(true);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 200;
		this.dataSinkTable.setLayoutData(data);

		String[] titles = { "Type", "Short name", "Location", "Remove" };
		for (int i = 0; i < titles.length; i++) {
			TableColumn column = new TableColumn(this.dataSinkTable, SWT.NONE);
			column.setText(titles[i]);
		}
		this.dataSinkTable.getColumn(3).setWidth(75);

		refreshDataSinkTable();
	}

	private void createMarketBasisView(Composite parent) {
		Group group = new Group(parent, SWT.PUSH);
		group.setText("Market basis");
		group.setLayout(new GridLayout(2, true));
		GridData fill = new GridData();
		fill.horizontalAlignment = GridData.FILL;
		fill.grabExcessHorizontalSpace = true;

		new Label(group, SWT.NONE).setText("Commodity");
		this.commodityWidget = new Text(group, SWT.BORDER);
		this.commodityWidget.setLayoutData(fill);
		this.commodityWidget.addModifyListener(this.marketBasisListener);

		new Label(group, SWT.NONE).setText("Currency");
		this.currencyWidget = new Text(group, SWT.BORDER);
		this.currencyWidget.setLayoutData(fill);
		this.currencyWidget.addModifyListener(this.marketBasisListener);

		new Label(group, SWT.NONE).setText("Minimum price");
		this.minimumPriceWidget = new Spinner(group, SWT.BORDER);
		this.minimumPriceWidget.setLayoutData(fill);
		this.minimumPriceWidget.setValues(0, -10000, 10000, 2, 1, 100);
		this.minimumPriceWidget.addModifyListener(this.marketBasisListener);

		new Label(group, SWT.NONE).setText("Maximum price");
		this.maximumPriceWidget = new Spinner(group, SWT.BORDER);
		this.maximumPriceWidget.setLayoutData(fill);
		this.maximumPriceWidget.setValues(0, -10000, 10000, 2, 1, 100);
		this.maximumPriceWidget.addModifyListener(this.marketBasisListener);

		new Label(group, SWT.NONE).setText("Price steps");
		this.pricestepsWidget = new Spinner(group, SWT.BORDER);
		this.pricestepsWidget.setLayoutData(fill);
		this.pricestepsWidget.setValues(0, 1, 1000, 0, 1, 10);
		this.pricestepsWidget.addModifyListener(this.marketBasisListener);

		updateMarketBasisView();
	}

	@Override
	public void createPartControl(Composite parent) {
		// Create a composite for the left part of the editor
		Composite leftPart = new Composite(parent, SWT.NONE);
		leftPart.setLayout(new FillLayout(SWT.VERTICAL));

		// Simulation clock
		createSimulationClockView(leftPart);

		// Market basis
		createMarketBasisView(leftPart);

		// DataSinks
		createDataSinksView(parent);

	}

	private Group createSimulationClockView(Composite parent) {
		Group clockGroup = new Group(parent, SWT.PUSH);
		clockGroup.setText("Simulation clock");
		clockGroup.setLayout(new GridLayout(2, false));

		this.realTimeClockButton = new Button(clockGroup, SWT.RADIO);
		this.realTimeClockButton.addSelectionListener(this);
		Label realTime = new Label(clockGroup, SWT.NONE);
		realTime.setText("Real-time");

		this.asFastAsPossibleClockButton = new Button(clockGroup, SWT.RADIO);
		this.asFastAsPossibleClockButton.addSelectionListener(this);
		Group asFastAsPossibleGroup = new Group(clockGroup, SWT.PUSH);
		asFastAsPossibleGroup.setText("As fast as possible");

		asFastAsPossibleGroup.setLayout(new GridLayout(2, false));
		Label startTimeLabel = new Label(asFastAsPossibleGroup, SWT.NONE);
		startTimeLabel.setText("Start time");
		this.startTime = new DateTimeSelector(asFastAsPossibleGroup);

		Label intervalLabel = new Label(asFastAsPossibleGroup, SWT.NONE);
		intervalLabel.setText("Timestep interval (ms)");
		this.intervalSpinner = new Spinner(asFastAsPossibleGroup, SWT.BORDER);
		this.intervalSpinner.setValues(30000, 1, Integer.MAX_VALUE, 0, 1, 100);
		this.intervalSpinner.addModifyListener(this);

		Label hasEndTimeLabel = new Label(asFastAsPossibleGroup, SWT.NONE);
		hasEndTimeLabel.setText("Use end time");
		this.endTimeEnabledButton = new Button(asFastAsPossibleGroup, SWT.CHECK);
		this.endTimeEnabledButton.addSelectionListener(this);

		Label endTimeLabel = new Label(asFastAsPossibleGroup, SWT.NONE);
		endTimeLabel.setText("End time");
		this.endTime = new DateTimeSelector(asFastAsPossibleGroup);

		this.updateSimulationClockView(null);

		return clockGroup;
	}

	@Override
	public void doSave(IProgressMonitor arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		setPartName(input.getName());
		if (input instanceof ScenarioEditorInput) {
			ScenarioEditorInput scenarioEditorInput = (ScenarioEditorInput) input;
			this.scenarioDescriptor = scenarioEditorInput.getScenarioDescriptor();
		} else {
			System.out.println("Input not an instance of ScenarioEditorInput!");
		}
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void modifyText(ModifyEvent e) {
		this.saveChange(e);
	}

	private String nextDataSinkId(String base) {
		String id = null;
		boolean inUse = false;
		int i = 0;
		do {
			// Create id
			i++;
			id = base + i;
			// already in use?
			inUse = false;
			for (DataSinkDescriptor sink : this.scenarioDescriptor.getDataSinks()) {
				if (id.equals(sink.getSinkId())) {
					inUse = true;
					break;
				}
			}
		} while (inUse);
		return id;
	}

	private void refreshDataSinkTable() {
		// empty table including editor elements
		for (TableEditor i : ScenarioInfoEditor.this.editors1) {
			i.getEditor().dispose();
			i.dispose();
		}
		ScenarioInfoEditor.this.editors1.clear();
		for (TableEditor i : ScenarioInfoEditor.this.editors2) {
			i.getEditor().dispose();
			i.dispose();
		}
		ScenarioInfoEditor.this.editors2.clear();
		for (TableEditor i : ScenarioInfoEditor.this.editors3) {
			i.getEditor().dispose();
			i.dispose();
		}
		ScenarioInfoEditor.this.editors3.clear();
		this.dataSinkTable.removeAll();

		int i = 0;
		for (DataSinkDescriptor desc : this.scenarioDescriptor.getDataSinks()) {
			// create item
			TableItem item = new TableItem(this.dataSinkTable, SWT.NONE);
			if (desc instanceof SingleFileCsvDataSinkDescriptor) {
				item.setText(0, "Single CSV-file");
				item.setText(1, desc.getSinkId());
				item.setText(2, ((SingleFileCsvDataSinkDescriptor) desc).getOutputFile());
			} else if (desc instanceof PerAgentCsvDataSinkDescriptor) {
				item.setText(0, "CSV-file per agent");
				item.setText(1, desc.getSinkId());
				item.setText(2, ((PerAgentCsvDataSinkDescriptor) desc).getOutputDirectory());
			}
			item.setText(3, "-");

			// create form
			TableEditor editor1 = new TableEditor(this.dataSinkTable);
			this.editors1.add(editor1);
			Text shortNameText = new Text(this.dataSinkTable, SWT.NONE);
			shortNameText.setText(desc.getSinkId());
			shortNameText.setData("item", i);
			shortNameText.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					Text widget = (Text) e.widget;
					int index = (Integer) widget.getData("item");
					ScenarioInfoEditor.this.scenarioDescriptor.getDataSinks().get(index).setSinkId(widget.getText());
				}
			});
			editor1.grabHorizontal = true;
			editor1.setEditor(shortNameText, item, 1);

			TableEditor editor2 = new TableEditor(this.dataSinkTable);
			this.editors2.add(editor2);
			Button browseButton = new Button(this.dataSinkTable, SWT.NONE);
			browseButton.setText("...");
			browseButton.pack();
			browseButton.setData("item", i);
			if (desc instanceof SingleFileCsvDataSinkDescriptor) {
				browseButton.setData("dialogType", "file");
				browseButton.setData("fileName", ((SingleFileCsvDataSinkDescriptor) desc).getOutputFile());
			} else if (desc instanceof PerAgentCsvDataSinkDescriptor) {
				browseButton.setData("dialogType", "directory");
				browseButton.setData("fileName", ((PerAgentCsvDataSinkDescriptor) desc).getOutputDirectory());
			}
			editor2.minimumWidth = browseButton.getSize().x;
			editor2.horizontalAlignment = SWT.RIGHT;
			editor2.setEditor(browseButton, item, 2);
			browseButton.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}

				@Override
				public void widgetSelected(SelectionEvent e) {
					int index = (Integer) e.widget.getData("item");
					String output = null;
					if ("file".equals(e.widget.getData("dialogType"))) {
						FileDialog dialog = new FileDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
								.getShell(), SWT.SAVE);
						dialog.setFileName((String) e.widget.getData("fileName"));
						dialog.setFilterExtensions(new String[] { "csv" });
						dialog.setFilterNames(new String[] { "CSV-file" });
						output = dialog.open();
						if (output != null) {
							((SingleFileCsvDataSinkDescriptor) ScenarioInfoEditor.this.scenarioDescriptor
									.getDataSinks().get(index)).setOutputFile(output);
						}
					} else {
						DirectoryDialog dialog = new DirectoryDialog(PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getShell(), SWT.SAVE);
						output = dialog.open();
						if (output != null) {
							((PerAgentCsvDataSinkDescriptor) ScenarioInfoEditor.this.scenarioDescriptor.getDataSinks()
									.get(index)).setOutputDirectory(output);
						}
					}
					if (output != null) {
						ScenarioInfoEditor.this.refreshDataSinkTable();
					}
				}
			});

			TableEditor editor3 = new TableEditor(this.dataSinkTable);
			this.editors3.add(editor3);
			Button removeButton = new Button(this.dataSinkTable, SWT.NONE);
			removeButton.setData("item", i);
			removeButton.setText("Remove");
			removeButton.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}

				@Override
				public void widgetSelected(SelectionEvent e) {
					int i = (Integer) e.widget.getData("item");
					ScenarioInfoEditor.this.scenarioDescriptor.getDataSinks().remove(i);
					ScenarioInfoEditor.this.refreshDataSinkTable();
				}
			});
			editor3.grabHorizontal = true;
			editor3.setEditor(removeButton, item, 3);

			i++;
		}
		for (int j = 0; j < 3; j++) {
			this.dataSinkTable.getColumn(j).pack();
		}
	}

	private void saveChange(TypedEvent e) {
		Object source = e.getSource();

		saveSimulationClockDescriptor(source);

		updateSimulationClockView(source);
	}

	protected void saveMarketBasisDescriptor(Widget widget) {
		MarketBasisDescriptor marketBasis = this.scenarioDescriptor.getMarketBasisDescriptor();
		if (widget == this.commodityWidget) {
			marketBasis.setCommodity(this.commodityWidget.getText());
		} else if (widget == this.currencyWidget) {
			marketBasis.setCurrency(this.currencyWidget.getText());
		} else if (widget == this.minimumPriceWidget) {
			marketBasis.setMinimumPrice(this.minimumPriceWidget.getSelection() / 100.0);
		} else if (widget == this.maximumPriceWidget) {
			marketBasis.setMaximumPrice(this.maximumPriceWidget.getSelection() / 100.0);
		} else if (widget == this.pricestepsWidget) {
			marketBasis.setPriceSteps(this.pricestepsWidget.getSelection());
		}
	}

	private void saveSimulationClockDescriptor(Object source) {
		if (this.realTimeClockButton.getSelection()) { // realTimeClock
			this.scenarioDescriptor.setSimulationClockDescriptor(new RealTimeSimulationClockDescriptor());
		} else { // as fast as possible clock
			AsFastAsPossibleSimulationClockDescriptor desc = new AsFastAsPossibleSimulationClockDescriptor();
			desc.setStartTime(this.startTime.getDate());
			desc.setTimestepIntervalMillis(this.intervalSpinner.getSelection());
			if (this.endTimeEnabledButton.getSelection()) {
				desc.setEndTime(this.endTime.getDate());
			} else {
				desc.setEndTime(null);
			}
			this.scenarioDescriptor.setSimulationClockDescriptor(desc);
		}
	}

	@Override
	public void setFocus() {
		this.refreshDataSinkTable();
	}

	private void updateMarketBasisView() {
		MarketBasisDescriptor marketBasis = this.scenarioDescriptor.getMarketBasisDescriptor();
		this.commodityWidget.setText(marketBasis.getCommodity());
		this.currencyWidget.setText(marketBasis.getCurrency());
		this.minimumPriceWidget.setSelection((int) (marketBasis.getMinimumPrice() * 100.0));
		this.maximumPriceWidget.setSelection((int) (marketBasis.getMaximumPrice() * 100.0));
		this.pricestepsWidget.setSelection(marketBasis.getPriceSteps());
	}

	/**
	 * Update the simulation clock form elements. If source == null, the whole
	 * form will be update
	 * 
	 * @param source
	 *            the widget with the changed value
	 */
	private void updateSimulationClockView(Object source) {
		if (source == null) {
			SimulationClockDescriptor simulationClockDescriptor = this.scenarioDescriptor
					.getSimulationClockDescriptor();
			this.realTimeClockButton
					.setSelection(simulationClockDescriptor instanceof RealTimeSimulationClockDescriptor);
			this.asFastAsPossibleClockButton
					.setSelection(simulationClockDescriptor instanceof AsFastAsPossibleSimulationClockDescriptor);
			if (this.asFastAsPossibleClockButton.getSelection()) {
				AsFastAsPossibleSimulationClockDescriptor desc = (AsFastAsPossibleSimulationClockDescriptor) simulationClockDescriptor;
				this.startTime.setDate(desc.getStartTime());
				this.intervalSpinner.setSelection((int) desc.getTimestepIntervalMillis());
				if (desc.getEndTime() == null) {
					this.endTimeEnabledButton.setSelection(false);
				} else {
					this.endTimeEnabledButton.setSelection(true);
					this.endTime.setDate(desc.getEndTime());
				}
			}
		}
		if (source == null || source == this.realTimeClockButton || source == this.asFastAsPossibleClockButton) {
			if (this.realTimeClockButton.getSelection()) { // realTimeClock
				this.startTime.setEnabled(false);
				this.endTime.setEnabled(false);
				this.intervalSpinner.setEnabled(false);
				this.endTimeEnabledButton.setEnabled(false);
			} else { // as fast as possible clock
				this.startTime.setEnabled(true);
				this.endTime.setEnabled(this.endTimeEnabledButton.getSelection());
				this.intervalSpinner.setEnabled(true);
				this.endTimeEnabledButton.setEnabled(true);
			}
		}
		if (source == null || source == this.endTimeEnabledButton) {
			this.endTime.setEnabled(this.endTimeEnabledButton.getSelection());
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		this.saveChange(e);
	}
	
	@Override
	public String getTitleToolTip() {
		return "Scenario editor";
	}
}
