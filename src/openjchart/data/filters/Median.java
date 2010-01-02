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

package openjchart.data.filters;

import java.util.ArrayList;
import java.util.List;

import openjchart.data.DataSource;
import openjchart.util.MathUtils;

public class Median extends Filter {
	private int windowSize;
	private int offset;

	public Median(DataSource original, int windowSize, int offset, Mode mode, int... cols) {
		super(original, mode, cols);
		this.windowSize = windowSize;
		this.offset = offset;
		filter();
	}

	@Override
	protected void filter() {
		clear();
		if (getWindowSize() <= 0) {
			return;
		}
		List<List<Double>> colWindows = new ArrayList<List<Double>>(getColumnCount());
		for (int colIndex = 0; colIndex < getColumnCount(); colIndex++) {
			List<Double> window = new ArrayList<Double>(getWindowSize());
			colWindows.add(window);
			if (!isFiltered(colIndex)) {
				continue;
			}
			// Prefill window
			for (int rowIndex = getOffset() - getWindowSize(); rowIndex < 0; rowIndex++) {
				double v = getOriginal(colIndex, rowIndex).doubleValue();
				window.add(v);
			}
		}
		for (int rowIndex = 0; rowIndex < getRowCount(); rowIndex++) {
			double[] filteredRow = new double[getColumnCount()];
			for (int colIndex = 0; colIndex < filteredRow.length; colIndex++) {
				if (isFiltered(colIndex)) {
					List<Double> window = colWindows.get(colIndex);
					if (window.size() >= getWindowSize()) {
						window.remove(0);
					}
					double v = getOriginal(colIndex, rowIndex - getOffset() + getWindowSize()).doubleValue();
					window.add(v);
					filteredRow[colIndex] = median(window);
				} else {
					filteredRow[colIndex] = getOriginal(colIndex, rowIndex).doubleValue();
				}
			}
			add(filteredRow);
		}
	}

	private double median(List<Double> w) {
		if (w.size() == 1) {
			return w.get(0);
		}
		List<Double> window = new ArrayList<Double>(w.size());
		for (Double v : w) {
			if (Double.isNaN(v)) {
				return Double.NaN;
			}
			window.add(v);
		}
		int medianIndex = MathUtils.randomizedSelect(window, 0, window.size() - 1, window.size()/2);
		double median = window.get(medianIndex);
		if ((window.size() & 1) == 0) {
			int medianUpperIndex = MathUtils.randomizedSelect(window, 0, window.size() - 1, window.size()/2 + 1);
			double medianUpper = window.get(medianUpperIndex);
			median = (median + medianUpper)/2.0;
		}
		return median;
	}

	public int getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
		dataChanged(this);
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
		dataChanged(this);
	}

}