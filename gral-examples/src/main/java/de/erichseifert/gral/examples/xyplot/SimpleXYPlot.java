/*
 * GRAL: GRAphing Library for Java(R)
 *
 * (C) Copyright 2009-2012 Erich Seifert <dev[at]erichseifert.de>,
 * Michael Seifert <michael[at]erichseifert.de>
 *
 * This file is part of GRAL.
 *
 * GRAL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GRAL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GRAL.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.erichseifert.gral.examples.xyplot;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.erichseifert.gral.data.DataSeries;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.examples.ExamplePanel;
import de.erichseifert.gral.plots.PlotArea;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.axes.AxisRenderer;
import de.erichseifert.gral.plots.axes.LogarithmicRenderer2D;
import de.erichseifert.gral.plots.lines.DiscreteLineRenderer2D;
import de.erichseifert.gral.plots.lines.LineRenderer;
import de.erichseifert.gral.plots.points.DefaultPointRenderer2D;
import de.erichseifert.gral.plots.points.PointRenderer;
import de.erichseifert.gral.plots.points.SizeablePointRenderer;
import de.erichseifert.gral.ui.InteractivePanel;
import de.erichseifert.gral.util.GraphicsUtils;
import de.erichseifert.gral.util.Insets2D;
import de.erichseifert.gral.util.Orientation;


public class SimpleXYPlot extends ExamplePanel {
	/** Version id for serialization. */
	private static final long serialVersionUID = -5263057758564264676L;

	/** Instance to generate random data values. */
	private static final Random random = new Random();

	@SuppressWarnings("unchecked")
	public SimpleXYPlot() {
		// Generate data
		DataTable data = new DataTable(Double.class, Double.class, Double.class,
				Double.class, Double.class, Double.class);
		for (double x = 1.0; x <= 400.0; x *= 1.5) {
			double x2 = x/5.0;
			data.add(x2, -Math.sqrt(x2) + 5.0,  5.0*Math.log10(x2),
				random.nextDouble() + 1.0, random.nextDouble() + 0.5, 1.0 + 2.0*random.nextDouble());
		}

		// Create data series
		DataSeries seriesLog = new DataSeries(data, 0, 2, 3, 4);
		DataSeries seriesLin = new DataSeries(data, 0, 1, 5);

		// Create new xy-plot
		XYPlot plot = new XYPlot(seriesLog, seriesLin);

		// Format plot
		plot.setInsets(new Insets2D.Double(20.0, 40.0, 40.0, 40.0));
		plot.setSetting(XYPlot.BACKGROUND, Color.WHITE);
		plot.setSetting(XYPlot.TITLE, getDescription());

		// Format plot area
		plot.getPlotArea().setSetting(PlotArea.BACKGROUND, new RadialGradientPaint(
			new Point2D.Double(0.5, 0.5),
			0.75f,
			new float[] { 0.6f, 0.8f, 1.0f },
			new Color[] { new Color(0, 0, 0, 0), new Color(0, 0, 0, 32), new Color(0, 0, 0, 128) }
		));
		plot.getPlotArea().setSetting(PlotArea.BORDER, null);

		// Format axes
		AxisRenderer axisRendererX = new LogarithmicRenderer2D();
		AxisRenderer axisRendererY = plot.getAxisRenderer(XYPlot.AXIS_Y);
		axisRendererX.setSetting(AxisRenderer.LABEL, "Logarithmic axis");
		plot.setAxisRenderer(XYPlot.AXIS_X, axisRendererX);
		// Custom tick labels
		Map<Double, String> labels = new HashMap<Double, String>();
		labels.put(2.0, "Two");
		labels.put(1.5, "OnePointFive");
		axisRendererX.setSetting(AxisRenderer.TICKS_CUSTOM, labels);
		// Custom stroke for the x-axis
		BasicStroke stroke = new BasicStroke(2f);
		axisRendererX.setSetting(AxisRenderer.SHAPE_STROKE, stroke);
		axisRendererY.setSetting(AxisRenderer.LABEL, "Linear axis");
		// Change intersection point of Y axis
		axisRendererY.setSetting(AxisRenderer.INTERSECTION, 1.0);
		// Change tick spacing
		axisRendererX.setSetting(AxisRenderer.TICKS_SPACING, 2.0);

		// Format rendering of data points
		PointRenderer sizeablePointRenderer = new SizeablePointRenderer();
		sizeablePointRenderer.setSetting(PointRenderer.COLOR, GraphicsUtils.deriveDarker(COLOR1));
		plot.setPointRenderer(seriesLin, sizeablePointRenderer);
		PointRenderer defaultPointRenderer = new DefaultPointRenderer2D();
		defaultPointRenderer.setSetting(PointRenderer.COLOR, GraphicsUtils.deriveDarker(COLOR2));
		defaultPointRenderer.setSetting(PointRenderer.ERROR_DISPLAYED, true);
		defaultPointRenderer.setSetting(PointRenderer.ERROR_COLOR, COLOR2);
		plot.setPointRenderer(seriesLog, defaultPointRenderer);

		// Format data lines
		LineRenderer discreteRenderer = new DiscreteLineRenderer2D();
		discreteRenderer.setSetting(LineRenderer.COLOR, COLOR1);
		discreteRenderer.setSetting(LineRenderer.STROKE, new BasicStroke(
			3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
			10.0f, new float[] {3f, 6f}, 0.0f));
		plot.setLineRenderer(seriesLin, discreteRenderer);
		// Custom gaps for points
		discreteRenderer.setSetting(LineRenderer.GAP, 2.0);
		discreteRenderer.setSetting(LineRenderer.GAP_ROUNDED, true);
		// Custom ascending
		discreteRenderer.setSetting(DiscreteLineRenderer2D.ASCENT_DIRECTION,
			Orientation.VERTICAL);
		discreteRenderer.setSetting(DiscreteLineRenderer2D.ASCENDING_POINT,
			0.5);

		// Add plot to Swing component
		add(new InteractivePanel(plot), BorderLayout.CENTER);
	}

	@Override
	public String getTitle() {
		return "x-y plot";
	}

	@Override
	public String getDescription() {
		return "Styled x-y plot with example data";
	}

	public static void main(String[] args) {
		new SimpleXYPlot().showInFrame();
	}
}
