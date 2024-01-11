package com.jsystemtrader.indicator;

import java.util.List;

import com.jsystemtrader.platform.quote.QuoteHistory;

//Please reference StochasticK.java
// formula for %D = ( %K(1) + %K(2) +.. )/n 
public class StochasticD extends Indicator {

	protected int d_length;
	private Indicator parent;

	public StochasticD(QuoteHistory qh, Indicator parent, int d_length, boolean isUsingSecondQH) {
		super(qh, isUsingSecondQH);
		this.d_length = d_length;
		this.parent = parent;
	}

	@Override
	public double calculate() {
		List<IndicatorValue> parentHistory = this.parent.getHistory();

		if (parentHistory.size() > this.d_length) {
			int start = parentHistory.size() - 1;
			double sum = 0.0;
			for (int size = 0; size < this.d_length; size++) {
				sum += parentHistory.get(start - size).getValue();
			}
			value = sum / this.d_length;
		}
		return value;
	}

}