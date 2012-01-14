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
package de.erichseifert.gral.plots.lines;

import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import de.erichseifert.gral.graphics.AbstractDrawable;
import de.erichseifert.gral.graphics.Drawable;
import de.erichseifert.gral.graphics.DrawingContext;
import de.erichseifert.gral.plots.DataPoint;
import de.erichseifert.gral.util.GraphicsUtils;


/**
 * Class that connects two dimensional data points with a straight line.
 */
public class DefaultLineRenderer2D extends AbstractLineRenderer2D {
	/** Version id for serialization. */
	private static final long serialVersionUID = -1728830281555843667L;
	/** Number of line segments which will be reserved to avoid unnecessary
	copying of array data. */
	private static final int INITIAL_LINE_CAPACITY = 10000;

	/**
	 * Initializes a new {@code DefaultLineRenderer2D} instance.
	 */
	public DefaultLineRenderer2D() {
	}

	/**
	 * Returns a graphical representation for the line defined by
	 *{@code e points}.
	 * @param points Points to be used for creating the line.
	 * @return Representation of the line.
	 */
	public Drawable getLine(final Iterable<DataPoint> points) {
		Drawable d = new AbstractDrawable() {
			/** Version id for serialization. */
			private static final long serialVersionUID = 7995515716470892483L;

			public void draw(DrawingContext context) {
				// Construct shape
				Path2D line = new Path2D.Double(
					Path2D.WIND_NON_ZERO, INITIAL_LINE_CAPACITY);
				for (DataPoint point : points) {
					Point2D pos = point.getPosition().getPoint2D();
					if (line.getCurrentPoint() == null) {
						line.moveTo(pos.getX(), pos.getY());
					} else {
						line.lineTo(pos.getX(), pos.getY());
					}
				}

				// Draw line
				Shape lineShape = punch(line, points);
				Paint paint = DefaultLineRenderer2D.this
					.getSetting(LineRenderer.COLOR);
				GraphicsUtils.fillPaintedShape(
					context.getGraphics(), lineShape, paint, null);
			}
		};
		return d;
	}

}
