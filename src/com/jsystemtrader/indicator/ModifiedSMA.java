package com.jsystemtrader.indicator;

import com.jsystemtrader.platform.quote.PriceBar;
import com.jsystemtrader.platform.quote.QuoteHistory;
import com.jsystemtrader.platform.quote.Snapshot;

public class ModifiedSMA extends Indicator
{
	private int length;

	public ModifiedSMA(QuoteHistory qh, int length)
	{
		super(qh);
		this.length = length;
	}

	public double testCalculate( Snapshot currentPrice )
	{
		int endBar = qh.size() - 1;
		int startBar = endBar - length+1;
		
		if ( startBar < 0 )
		{
			startBar = endBar - 2;// At least we have to have two bars to calculate, try this one
		}

		double sma = currentPrice.getClose();

		for (int bar = startBar; bar <= endBar; bar++) 
		{

			PriceBar pbar = qh.getPriceBar( bar );

			double barClose = pbar.getClose();

			if (pbar.getDate() < qh.getOpenPriceBar().getDate())
			{
				barClose  = qh.getOpenPriceBar().getClose();
			}
			sma += barClose;
		}

		value = sma / (endBar - startBar + 1);
		return value;
	}

	@Override
	public double calculate() 
	{
		int endBar = qh.size() - 1;
		int startBar = endBar - length;
		
		double sma = 0;

		//At least we have to have two bars to calculate, try this one
		if ( startBar <0 )
		{
			startBar = endBar -2;
		}
		
		for (int bar = startBar; bar <= endBar; bar++) 
		{
			PriceBar pbar = qh.getPriceBar( bar );
			double barClose = pbar.getClose();
			
			//we dont' want yesterday's price,before open, we always use open prices.
			if (pbar.getDate() < qh.getOpenPriceBar().getDate())
        	{
        		barClose  = qh.getOpenPriceBar().getClose();
        	}
			
			sma += barClose;
		}

		value = sma / (endBar - startBar + 1);
	
		return value;
	}
    
 
}
