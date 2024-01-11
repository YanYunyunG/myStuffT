package com.jsystemtrader.indicator;

import com.jsystemtrader.platform.quote.QuoteHistory;

// https://en.wikipedia.org/wiki/Stochastic_oscillator 
// https://www.investopedia.com/terms/s/stochasticoscillator.asp
// formula for %K = ( close_price  - lowest_price(n)) /( highest_price(n) - lowest_price(n) ) * 100;

public class StochasticK extends Indicator {

	// The size of the bars we are using for calculate the value
	protected int k_barSize;

	public StochasticK(QuoteHistory qh, int k_barSize, boolean isUsingSecondQH) {
		super(qh, isUsingSecondQH);
		this.k_barSize = k_barSize;
	}

	@Override
	public double calculate() {
		int endBar = qh.size() - 1;
		int startBar = endBar - this.k_barSize;

		double max = 0;
		double min = qh.getPriceBar(startBar).getLow();
		double last;

		for (int bar = startBar; bar <= endBar; bar++) {
			max = Math.max(qh.getPriceBar(bar).getHigh(), max);
			min = Math.min(qh.getPriceBar(bar).getLow(), min);
		}
		last = qh.getPriceBar(endBar).getClose();
		value = ((last - min) / (max - min)) * 100;

		return value;
	}

}