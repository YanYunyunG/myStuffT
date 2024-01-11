package com.jsystemtrader.indicator;

import java.util.List;

import com.jsystemtrader.platform.quote.PriceBar;
import com.jsystemtrader.platform.quote.QuoteHistory;

public class ModifiedEMA extends Indicator {
    private final int length;
    private final double multiplier;

    public ModifiedEMA(QuoteHistory qh, int length) {
        super(qh);
        this.length = length;
        multiplier = 2. / (length + 1.);
    }

    @Override
    public double calculate() 
    {
    	 List<PriceBar> priceBars = qh.getAll();
         int lastBar = priceBars.size() - 1;
         int startBar = lastBar - 2* length + 1;
         
         if ( startBar < 0)
         {
        	//At least we have to have two bars to calculate, try this one
     		 startBar = lastBar -2;
         }
         
         double ema = priceBars.get(startBar).getClose();
     
         for (int bar = startBar; bar <= lastBar; bar++)
         {
         	PriceBar pbar = priceBars.get(bar);
         	double barClose = pbar.getClose();
         
         	//we dont' want yesterday's price,before open, we always use open prices.
		/*	if (pbar.getDate() < qh.getOpenPriceBar().getDate())
        	{
        		barClose  = qh.getOpenPriceBar().getClose();
        	}
       */
         	ema = ema + (barClose - ema) * multiplier;
         }

         value = ema;
         return value;
     }
 }

