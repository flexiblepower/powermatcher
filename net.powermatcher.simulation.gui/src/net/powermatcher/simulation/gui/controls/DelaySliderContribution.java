package net.powermatcher.simulation.gui.controls;

import java.util.concurrent.TimeUnit;

import net.powermatcher.simulation.gui.Application;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

public class DelaySliderContribution extends WorkbenchWindowControlContribution {
	public DelaySliderContribution() {
	}

	public DelaySliderContribution(String id) {
		super(id);
	}

	@Override
	protected Control createControl(Composite parent) {
		final Slider slider = new Slider(parent, SWT.HORIZONTAL);

		slider.setMinimum(0);
		slider.setMaximum(100);
		
		slider.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				long delay = (long) Math.pow(slider.getSelection(), 2);
				Application.getInstance().getSimulationControl().setDelay(delay, TimeUnit.MILLISECONDS);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		return slider;
	}
}
