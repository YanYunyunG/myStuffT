package com.jsystemtrader.platform.strategy;

import java.sql.Time;
import java.util.Hashtable;

import com.jsystemtrader.platform.model.JSystemTraderException;
import com.jsystemtrader.platform.schedule.TradingInterval;

public class StrategyPreference {
	
	public static String TICKER_SYMBOL = "tickerSymbol";
	public static String TRADE_SHARE_VOLUMN = "longVolumn";
	public static String TRADE_START_TIME= "tradeStartTime";
	public static String TRADE_END_TIME= "tradeEndTime";
	
	private Hashtable<String , Object> props = new Hashtable<String, Object>();
	
	
	public StrategyPreference( String tickerSymbol, 
			Integer shareNumber,
			String tradingStartTime,
			String tradingEndTime)
	{
		props.put(TICKER_SYMBOL, tickerSymbol);
		props.put(TRADE_SHARE_VOLUMN, shareNumber);
		props.put(TRADE_START_TIME, tradingStartTime);
		props.put(TRADE_END_TIME, tradingEndTime);
	
	}
	

	public static StrategyPreference create( String value ) throws JSystemTraderException 
	{	
		String[] values = value.split(",");
		StrategyPreference ref = new StrategyPreference( values[0],Integer.valueOf( values[1]),values[2],values[3]);
	
		return ref;
	}
	
	public String getTickerSymbol( )
	{
		return (String)props.get(TICKER_SYMBOL );
	}
	
	public Integer getTradeShareVolumn( )
	{
		return (Integer)props.get(TRADE_SHARE_VOLUMN );
	}
	
	public String getTradeStartTime()
	{
		return (String)props.get( TRADE_START_TIME);
	}
	
	public String getTradeEndTime()
	{
		return (String)props.get( TRADE_END_TIME);
	}
	
	public String toString()
	{
		String ret = this.getTickerSymbol()+","
		+ this.getTradeShareVolumn().toString() +","
		+ this.getTradeStartTime() +","
		+ this.getTradeEndTime();
		
		return  ret;
	}
}