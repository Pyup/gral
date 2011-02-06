/*
 * GRAL: GRAphing Library for Java(R)
 *
 * (C) Copyright 2009-2011 Erich Seifert <dev[at]erichseifert.de>,
 * Michael Seifert <michael.seifert[at]gmx.net>
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
package de.erichseifert.gral.examples.pieplot;

import java.awt.BorderLayout;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.plots.PiePlot;
import de.erichseifert.gral.ui.InteractivePanel;
import de.erichseifert.gral.util.Insets2D;


public class SimplePiePlot extends JPanel {
	/** Version id for serialization. */
	private static final long serialVersionUID = 1L;

	public SimplePiePlot() {
		super(new BorderLayout());

		// Create data
		DataTable data = new DataTable(Integer.class);
		Random r = new Random();
		for (int i = 0; i < 15; i++) {
			int val = r.nextInt(10) + 1;
			data.add((r.nextDouble() <= 0.15) ? -val : val);
		}

		// Create new pie plot
		PiePlot plot = new PiePlot(data);

		// Format plot
		plot.setSetting(PiePlot.TITLE, "A Sample Pie Plot");
		// Change relative size of pie
		plot.setSetting(PiePlot.RADIUS, 0.9);
		// Change relative size of inner region
		plot.setSetting(PiePlot.RADIUS_INNER, 0.4);
		// Change the width of gaps between segments
		plot.setSetting(PiePlot.GAP, 0.2);
		// Change rotation
		//plot.setSetting(PiePlot.CLOCKWISE, false);
		// Custom start angle
		//plot.setSetting(PiePlot.START, 70.0);
		// Custom colors
		//plot.setSetting(PiePlot.COLORS, new RainbowColors());
		// Random blue colors
		//QuasiRandomColors colors = new QuasiRandomColors();
		//colors.setColorVariance(new float[] {0.60f, 0.00f, 0.75f, 0.25f, 0.25f, 0.75f});
		//plot.setSetting(PiePlot.COLORS, colors);
		plot.setInsets(new Insets2D.Double(20.0, 40.0, 40.0, 40.0));

		// Add plot to Swing component
		add(new InteractivePanel(plot), BorderLayout.CENTER);
	}

	public static void main(String[] args) {
		SimplePiePlot example = new SimplePiePlot();
		JFrame frame = new JFrame("GRALTest");
		frame.getContentPane().add(example, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setVisible(true);
	}
}