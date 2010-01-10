/* OpenJChart : a free plotting library for the Java(tm) platform
 *
 * (C) Copyright 2009, by Erich Seifert and Michael Seifert.
 *
 * This file is part of OpenJChart.
 *
 * OpenJChart is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenJChart is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenJChart.  If not, see <http://www.gnu.org/licenses/>.
 */

package openjchart.plots;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import openjchart.Drawable;
import openjchart.Legend;
import openjchart.PlotArea2D;
import openjchart.data.DataListener;
import openjchart.data.DataSource;
import openjchart.data.DummyData;
import openjchart.data.Row;
import openjchart.data.statistics.Statistics;
import openjchart.plots.axes.Axis;
import openjchart.plots.axes.AxisRenderer2D;
import openjchart.plots.axes.LinearRenderer2D;
import openjchart.plots.lines.LineRenderer2D;
import openjchart.plots.shapes.DefaultShapeRenderer;
import openjchart.plots.shapes.ShapeRenderer;
import openjchart.util.GraphicsUtils;
import openjchart.util.SettingChangeEvent;

/**
 * Class that displays data in an XY-Plot.
 */
public class XYPlot extends Plot implements DataListener  {
	/** Key for specifying the {@link openjchart.plots.axes.AxisRenderer2D} instance for displaying the x-axis. */
	public static final String KEY_AXIS_X_RENDERER = "xyplot.axis.x.renderer";
	/** Key for specifying the {@link openjchart.plots.axes.AxisRenderer2D} instance for displaying the y-axis. */
	public static final String KEY_AXIS_Y_RENDERER = "xyplot.axis.y.renderer";

	private double minX;
	private double maxX;
	private double minY;
	private double maxY;
	private Axis axisX;
	private Axis axisY;
	private Drawable axisXComp;
	private Drawable axisYComp;

	private final Map<DataSource, ShapeRenderer> shapeRenderers;
	private final Map<DataSource, LineRenderer2D> lineRenderers;

	/**
	 * Class that represents the drawing area of an <code>XYPlot</code>.
	 */
	public class XYPlotArea2D extends PlotArea2D {
		/** Key for specifying whether the horizontal grid lines along the x-axis are drawn. */
		public static final String KEY_GRID_X = "xyplot.grid.x";
		/** Key for specifying whether the vertical grid lines along the y-axis are drawn. */
		public static final String KEY_GRID_Y = "xyplot.grid.y";
		/** Key for specifying the {@link java.awt.Paint} instance to be used to paint the background of the grid. */
		public static final String KEY_GRID_COLOR = "xyplot.grid.color";

		/**
		 * Creates a new XYPlotArea2D object with default settings.
		 */
		public XYPlotArea2D() {
			setSettingDefault(KEY_GRID_X, true);
			setSettingDefault(KEY_GRID_Y, true);
			setSettingDefault(KEY_GRID_COLOR, new Color(0.0f, 0.0f, 0.0f, 0.2f));
		}

		@Override
		public void draw(Graphics2D g2d) {
			drawBackground(g2d);
			drawGrid(g2d);
			drawBorder(g2d);
			drawPlot(g2d);
			drawAxes(g2d);
			drawLegend(g2d);
		}

		/**
		 * Draws the grid into the specified <code>Graphics2D</code> object.
		 * @param g2d Graphics to be used for drawing.
		 */
		protected void drawGrid(Graphics2D g2d) {
			boolean isGridX = getSetting(KEY_GRID_X);
			boolean isGridY = getSetting(KEY_GRID_Y);
			if (!isGridX && !isGridY) {
				return;
			}

			AffineTransform txOrig = g2d.getTransform();
			g2d.translate(getX(), getY());
			AffineTransform txOffset = g2d.getTransform();
			Paint paint = getSetting(KEY_GRID_COLOR);
			Rectangle2D bounds = getBounds();

			// Draw gridX
			if (isGridX) {
				AxisRenderer2D axisXRenderer = XYPlot.this.getSetting(KEY_AXIS_X_RENDERER);
				Shape shapeX = axisXRenderer.getSetting(AxisRenderer2D.KEY_SHAPE);
				Rectangle2D shapeBoundsX = shapeX.getBounds2D();
				List<DataPoint2D> ticksX = axisXRenderer.getTicks(axisX);
				Line2D gridLineVert = new Line2D.Double(
					-shapeBoundsX.getMinX(), -shapeBoundsX.getMinY(),
					-shapeBoundsX.getMinX(), bounds.getHeight() - shapeBoundsX.getMinY()
				);
				for (DataPoint2D tick : ticksX) {
					Point2D tickPoint = tick.getPosition();
					if (tickPoint == null) {
						continue;
					}
					g2d.translate(tickPoint.getX(), tickPoint.getY());
					GraphicsUtils.drawPaintedShape(g2d, gridLineVert, paint, null, null);
					g2d.setTransform(txOffset);
				}
			}

			// Draw gridY
			if (isGridY) {
				AxisRenderer2D axisYRenderer = XYPlot.this.getSetting(KEY_AXIS_Y_RENDERER);
				Shape shapeY = axisYRenderer.getSetting(AxisRenderer2D.KEY_SHAPE);
				Rectangle2D shapeBoundsY = shapeY.getBounds2D();
				List<DataPoint2D> ticksY = axisYRenderer.getTicks(axisY);
				Line2D gridLineHoriz = new Line2D.Double(
					-shapeBoundsY.getMinX(), -shapeBoundsY.getMinY(),
					bounds.getWidth() - shapeBoundsY.getMinX(), -shapeBoundsY.getMinY()
				);
				for (DataPoint2D tick : ticksY) {
					Point2D tickPoint = tick.getPosition();
					g2d.translate(tickPoint.getX(), tickPoint.getY());
					GraphicsUtils.drawPaintedShape(g2d, gridLineHoriz, paint, null, null);
					g2d.setTransform(txOffset);
				}
			}

			g2d.setTransform(txOrig);
		}

		@Override
		protected void drawPlot(Graphics2D g2d) {
			AffineTransform txOrig = g2d.getTransform();
			g2d.translate(getX(), getY());
			AffineTransform txOffset = g2d.getTransform();

			// Paint shapes and lines
			for (DataSource s : data) {
				ShapeRenderer shapeRenderer = getShapeRenderer(s);
				LineRenderer2D lineRenderer = getLineRenderer(s);

				List<DataPoint2D> points = new LinkedList<DataPoint2D>();
				for (int i = 0; i < s.getRowCount(); i++) {
					Row row = new Row(s, i);
					Number valueX = row.get(0);
					Number valueY = row.get(1);
					AxisRenderer2D axisXRenderer = XYPlot.this.getSetting(KEY_AXIS_X_RENDERER);
					AxisRenderer2D axisYRenderer = XYPlot.this.getSetting(KEY_AXIS_Y_RENDERER);
					Point2D axisPosX = axisXRenderer.getPosition(axisX, valueX, true, false);
					Point2D axisPosY = axisYRenderer.getPosition(axisY, valueY, true, false);
					if (axisPosX==null || axisPosY==null) {
						continue;
					}
					Point2D pos = new Point2D.Double(axisPosX.getX(), axisPosY.getY());


					Drawable drawable = null;
					Shape shape = null;
					if (shapeRenderer != null) {
						drawable = shapeRenderer.getShape(row);
						shape = shapeRenderer.getShapePath(row);
					}

					DataPoint2D point = new DataPoint2D(pos, null, drawable, shape, null);
					points.add(point);
				}

				if (lineRenderer != null) {
					DataPoint2D[] pointArray = new DataPoint2D[points.size()];
					points.toArray(pointArray);
					Drawable drawable = lineRenderer.getLine(pointArray);
					drawable.draw(g2d);
				}

				if (shapeRenderer != null) {
					for (DataPoint2D point : points) {
						g2d.translate(point.getPosition().getX(), point.getPosition().getY());
						Drawable drawable = point.getDrawable();
						drawable.draw(g2d);
						g2d.setTransform(txOffset);
					}
				}
			}
			g2d.setTransform(txOrig);
		}
	}

	/**
	 * Class that displays a legend in an <code>XYPlot</code>.
	 */
	public class XYLegend extends Legend {
		protected final DataSource DUMMY_DATA = new DummyData(1, 1, 1.0);

		@Override
		protected void drawSymbol(Graphics2D g2d, Drawable symbol, DataSource data) {
			ShapeRenderer shapeRenderer = shapeRenderers.get(data);
			LineRenderer2D lineRenderer = lineRenderers.get(data);

			Row row = new Row(DUMMY_DATA, 0);
			Rectangle2D bounds = symbol.getBounds();

			DataPoint2D p1 = new DataPoint2D(
				new Point2D.Double(bounds.getMinX(), bounds.getCenterY()), null,
				null, null, null
			);
			DataPoint2D p2 = new DataPoint2D(
				new Point2D.Double(bounds.getCenterX(), bounds.getCenterY()), null,
				null, (shapeRenderer != null) ? shapeRenderer.getShapePath(row) : null, null
			);
			DataPoint2D p3 = new DataPoint2D(
				new Point2D.Double(bounds.getMaxX(), bounds.getCenterY()), null,
				null, null, null
			);

			if (lineRenderer != null) {
				lineRenderer.getLine(p1, p2, p3).draw(g2d);
			}
			if (shapeRenderer != null) {
				Point2D pos = p2.getPosition();
				AffineTransform txOrig = g2d.getTransform();
				g2d.translate(pos.getX(), pos.getY());
				shapeRenderer.getShape(row).draw(g2d);
				g2d.setTransform(txOrig);
			}
		}

	}

	/**
	 * Creates a new <code>XYPlot</code> object with the specified <code>DataSource</code>s and
	 * default settings.
	 * @param data Data to be displayed.
	 */
	public XYPlot(DataSource... data) {
		super(data);

		shapeRenderers = new HashMap<DataSource, ShapeRenderer>();
		lineRenderers = new LinkedHashMap<DataSource, LineRenderer2D>(data.length);

		setPlotArea(new XYPlotArea2D());
		setLegend(new XYLegend());

		ShapeRenderer shapeRendererDefault = new DefaultShapeRenderer();
		for (DataSource source : data) {
			getLegend().add(source);
			setShapeRenderer(source, shapeRendererDefault);
			source.addDataListener(this);
			dataChanged(source);
		}

		// Create axes
		axisX = new Axis(minX, maxX);
		axisY = new Axis(minY, maxY);

		setAxis(Axis.X, axisX, axisXComp);
		setAxis(Axis.Y, axisY, axisYComp);

		setSettingDefault(KEY_AXIS_X_RENDERER, new LinearRenderer2D());
		setSettingDefault(KEY_AXIS_Y_RENDERER, new LinearRenderer2D());
	}

	@Override
	protected void layout() {
		super.layout();
		layoutAxes();
		layoutLegend();
	}

	/**
	 * Calculates the bounds of the axes.
	 */
	protected void layoutAxes() {
		if (getPlotArea() == null) {
			return;
		}

		Rectangle2D plotBounds = getPlotArea().getBounds();
		AxisRenderer2D axisXRenderer = getSetting(KEY_AXIS_X_RENDERER);
		AxisRenderer2D axisYRenderer = getSetting(KEY_AXIS_Y_RENDERER);
		Dimension2D axisXSize = null;
		Dimension2D axisYSize = null;

		// Set the new shapes first to allow for correct positioning
		if (axisXComp != null && axisXRenderer != null) {
			axisXSize = axisXComp.getPreferredSize();
			axisXRenderer.setSetting(AxisRenderer2D.KEY_SHAPE, new Line2D.Double(
				0.0, 0.0,
				plotBounds.getWidth(), 0.0
			));
		}
		if (axisYComp != null && axisYRenderer != null) {
			axisYSize = axisYComp.getPreferredSize();
			axisYRenderer.setSetting(AxisRenderer2D.KEY_SHAPE, new Line2D.Double(
				axisYSize.getWidth(), plotBounds.getHeight(),
				axisYSize.getWidth(), 0.0
			));
		}

		// Set bounds with new axis shapes
		if (axisXComp != null && axisXRenderer != null) {
			Double axisXIntersection = axisXRenderer.getSetting(AxisRenderer2D.KEY_INTERSECTION);
			Point2D axisXPos = axisYRenderer.getPosition(axisY, axisXIntersection, false, false);
			axisXComp.setBounds(
				plotBounds.getMinX(), axisXPos.getY() + plotBounds.getMinY(),
				plotBounds.getWidth(), axisXSize.getHeight()
			);
		}

		if (axisYComp != null && axisYRenderer != null) {
			Double axisYIntersection = axisYRenderer.getSetting(AxisRenderer2D.KEY_INTERSECTION);
			Point2D axisYPos = axisXRenderer.getPosition(axisX, axisYIntersection, false, false);
			axisYComp.setBounds(
				plotBounds.getMinX() - axisYSize.getWidth() + axisYPos.getX(), plotBounds.getMinY(),
				axisYSize.getWidth(), plotBounds.getHeight()
			);
		}
	}

	/**
	 * Calculates the bounds of the legend component.
	 */
	protected void layoutLegend() {
		if (getPlotArea() == null) {
			return;
		}
		Rectangle2D plotBounds = getPlotArea().getBounds();
		getLegendContainer().setBounds(plotBounds);
	}

	@Override
	public void dataChanged(DataSource data) {
		minX =  Double.MAX_VALUE;
		maxX = -Double.MAX_VALUE;
		minY =  Double.MAX_VALUE;
		maxY = -Double.MAX_VALUE;
		final int colX = 0;
		final int colY = 1;
		for (DataSource s : this.data) {
			Statistics stats = s.getStatistics();
			// Set the minimal and maximal value of the axes
			minX = Math.min(minX, stats.get(Statistics.MIN, colX));
			maxX = Math.max(maxX, stats.get(Statistics.MAX, colX));
			minY = Math.min(minY, stats.get(Statistics.MIN, colY));
			maxY = Math.max(maxY, stats.get(Statistics.MAX, colY));
		}
	}

	@Override
	public void settingChanged(SettingChangeEvent event) {
		super.settingChanged(event);

		String key = event.getKey();
		if (KEY_AXIS_X_RENDERER.equals(key)) {
			AxisRenderer2D axisXRenderer = (AxisRenderer2D) event.getValNew();
			axisXComp = axisXRenderer.getRendererComponent(axisX);
			setAxis(Axis.X, axisX, axisXComp);
		}
		else if (KEY_AXIS_Y_RENDERER.equals(key)) {
			AxisRenderer2D axisYRenderer = (AxisRenderer2D) event.getValNew();
			axisYRenderer.setSetting(AxisRenderer2D.KEY_SHAPE_NORMAL_ORIENTATION_CLOCKWISE, true);
			axisYRenderer.setSetting(AxisRenderer2D.KEY_LABEL_ROTATION, 90.0);
			axisYComp = axisYRenderer.getRendererComponent(axisY);
			setAxis(Axis.Y, axisY, axisYComp);
		}
	}

	/**
	 * Returns the <code>ShapeRenderer</code> for the specified <code>DataSource</code>.
	 * @param s DataSource.
	 * @return ShapeRenderer.
	 */
	public ShapeRenderer getShapeRenderer(DataSource s) {
		return shapeRenderers.get(s);
	}

	/**
	 * Sets the <code>ShapeRenderer</code> for a certain <code>DataSource</code> to the
	 * specified instance.
	 * @param s DataSource.
	 * @param shapeRenderer ShapeRenderer to be set.
	 */
	public void setShapeRenderer(DataSource s, ShapeRenderer shapeRenderer) {
		this.shapeRenderers.put(s, shapeRenderer);
	}

	/**
	 * Returns the <code>LineRenderer2D</code> for the specified <code>DataSource</code>.
	 * @param s <code>DataSource</code>.
	 * @return <code>LineRenderer2D</code>.
	 */
	public LineRenderer2D getLineRenderer(DataSource s) {
		return lineRenderers.get(s);
	}

	/**
	 * Sets the <code>LineRenderer2D</code> for a certain <code>DataSource</code> to the
	 * specified value.
	 * @param s <code>DataSource</code>.
	 * @param lineRenderer <code>LineRenderer</code> to be set.
	 */
	public void setLineRenderer(DataSource s, LineRenderer2D lineRenderer) {
		lineRenderers.put(s, lineRenderer);
	}

}
