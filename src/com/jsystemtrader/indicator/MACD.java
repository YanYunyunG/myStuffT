package com.jsystemtrader.indicator;

import com.jsystemtrader.platform.quote.*;

/**
 * MACD
 */
public class MACD extends Indicator {
    private final int fastLength, slowLength;

    public MACD(QuoteHistory qh, int fastLength, int slowLength) {
        super(qh);
        this.fastLength = fastLength;
        this.slowLength = slowLength;
    }

    @Override
    public double calculate() {
        double fastEMA = new ModifiedEMA(qh, fastLength).calculate();
        double slowEMA = new ModifiedEMA(qh, slowLength).calculate();
        value = fastEMA - slowEMA;

        return value;
    }
}
