/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
import org.csstudio.swt.widgets.datadefinition.IManualValueChangeListener;
import org.csstudio.swt.widgets.figures.GaugeFigure;
import org.csstudio.swt.widgets.figures.KnobFigure;
import org.csstudio.swt.widgets.figures.TankFigure;
import org.csstudio.swt.widgets.figures.ThermometerFigure;
import org.csstudio.swt.xygraph.util.XYGraphMediaFactory;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;


/**
 * A live updated Gauge Example.
 * @author Xihui Chen
 *
 */
public class MultipleWidgetsExample {
	public static void main(String[] args) {
		final Shell shell = new Shell();
		shell.setText("Multiple Widgets Example");
		shell.setSize(400, 400);
	    GridLayout layout = new GridLayout();
	    layout.numColumns=2;
	    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
	    shell.setLayout(layout);
	    //create canvases to hold the widgets.
	    Canvas knobCanvas = new Canvas(shell, SWT.NONE);
	    knobCanvas.setLayoutData(gd);
	    Canvas gaugeCanvas = new Canvas(shell, SWT.NONE);
	    gaugeCanvas.setLayoutData(gd);
	    Canvas thermoCanvas = new Canvas(shell, SWT.NONE);
	    thermoCanvas.setLayoutData(gd);
	    Canvas tankCanvas = new Canvas(shell, SWT.NONE);
	    tankCanvas.setLayoutData(gd);
	    
	    //use LightweightSystem to create the bridge between SWT and draw2D
		LightweightSystem lws = new LightweightSystem(knobCanvas);		
		//Create widgets
		final KnobFigure knobFigure = new KnobFigure();			
		lws.setContents(knobFigure);	
		
		lws = new LightweightSystem(gaugeCanvas);		
		final GaugeFigure gauge = new GaugeFigure();		
		gauge.setBackgroundColor(
				XYGraphMediaFactory.getInstance().getColor(0, 0, 0));
		gauge.setForegroundColor(
				XYGraphMediaFactory.getInstance().getColor(255, 255, 255));
		lws.setContents(gauge);	
		
		lws = new LightweightSystem(thermoCanvas);		
		final ThermometerFigure thermo = new ThermometerFigure();			
		lws.setContents(thermo);	
		
		lws = new LightweightSystem(tankCanvas);		
		final TankFigure tank = new TankFigure();			
		lws.setContents(tank);
		
		
		
		
		//Add listener
		knobFigure.addManualValueChangeListener(new IManualValueChangeListener() {			
			@Override
			public void manualValueChanged(double newValue) {
				gauge.setValue(newValue);
				thermo.setValue(newValue);
				tank.setValue(newValue);
			}
		});	
		shell.open();
	    Display display = Display.getDefault();
	    while (!shell.isDisposed()) {
	      if (!display.readAndDispatch())
	        display.sleep();
	    }

	   
	}
}
