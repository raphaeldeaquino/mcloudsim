/*
 * Title:        Mobile CloudSim Toolkit
 * Description:  Extension of CloudSim Toolkit for Modeling and Simulation of Publish/Subscribe 
 * 				 Communication Paradigm with Subscriber Connectivity Change
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2014-2016, Universidade Federal de Goiás, Brazil
 */

package br.ufg.inf.mcloudsim.utils;

import java.util.Arrays;
import java.util.List;

/**
 * @author Raphael Gomes
 *
 */
public final class Utils {

	public static String formatData(Comparable<?> data) {
		String s = "";

		if (data instanceof Double) {
			s = String.format("%.1f", data);
		} else {
			s += data;
		}

		return s;
	}

	public static String toString(double[] a) {
		if (a == null)
			return "null";
		int iMax = a.length - 1;
		if (iMax == -1)
			return "[]";

		StringBuilder b = new StringBuilder();
		b.append('[');
		for (int i = 0;; i++) {
			b.append(formatData(a[i]));
			if (i == iMax)
				return b.append(']').toString();
			b.append(", ");
		}
	}

	public static double upTrimmedAvg(double[] values, double trimP) {
		double[] array = Arrays.copyOf(values, values.length);

		Arrays.sort(array);

		int size = (int) Math.floor(array.length * (1.0 - trimP));
		double sum = 0.0;

		for (int i = 0; i < size; i++) {
			sum += array[i];
		}

		return sum / (double) size;
	}

	public static double avg(List<Double> values) {
		if (values == null || values.size() == 0) {
			return Double.NaN;
		} else {
			double sum = 0.0;

			for (int i = 0; i < values.size(); i++) {
				sum += values.get(i);
			}

			return sum / values.size();
		}

	}
}
