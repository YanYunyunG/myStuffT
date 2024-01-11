package com.jsystemtrader.strategy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import com.jsystemtrader.indicator.Indicator;
import com.jsystemtrader.indicator.IndicatorValue;
import com.jsystemtrader.platform.quote.PriceBar;
import com.jsystemtrader.platform.quote.QuoteHistory;

public class Utils {
	
	private final static  int HIGHEST  = 1;
	private final static  int LOWEST  = 2;

	private static boolean isInSameDay( long startDate, long date)
	{
		Date start = new Date(startDate );
		Date thisDate = new Date( date);
		return thisDate.getDate() == start.getDate();
	}
 
	/*
	 * number is how many number of k d lines cross.
	 */
	private static double findPreviewPeekValue( Indicator kLine, Indicator dLine,int type, int number )
	{
		//backward search for n-th peek point
		double peekValue = -1;

		long startDate = kLine.getDate();
		int kIdx =  kLine.getHistory().size() -1;
		int dIdx = dLine.getHistory().size() -1;
		double k_d = 0;
		int count = 0; 
		
		long peekDate = 0;
		
	//	System.out.println(" IN found peek point k = "+ String.valueOf( kLine.getHistory().get(kIdx).getValue()));
	//	System.out.println(" k-d  = "+ String.valueOf( kLine.getHistory().get(kIdx).getValue()- 
		//		 dLine.getHistory().get(dIdx).getValue()));
		
		//System.out.println(" start time =" + new Date( kLine.getHistory().get( kIdx).getDate()).toString());
		
		for( ; kIdx> 0 && dIdx > 0; kIdx--,dIdx--)
		{
			double k_value =  kLine.getHistory().get( kIdx).getValue();
			double d_value =  dLine.getHistory().get( dIdx).getValue();
			
			if ( ! isInSameDay( startDate,kLine.getHistory().get( kIdx).getDate() ))
			{
				break; // we only search for one day. we don't care about previous day.
			}
			double k_d_new = k_value - d_value;
			
				
			if ( k_d * k_d_new <0 )
			{
				count += 1;
				peekValue = k_value;
				peekDate =  kLine.getHistory().get( dIdx).getDate();
			}
			
			k_d = k_d_new;

		   if ( type == HIGHEST )
			{
				if ( peekValue < k_value )
				{
					peekValue = k_value;
					peekDate = kLine.getHistory().get( dIdx).getDate();
				}
			}
			if ( count ==  number )
			{
				break;
			}
		}
		
	//	System.out.println("found peek value = "+ peekValue + " date = "+ new Date( peekDate).toString());
	
		return peekValue;
	}
	
	/*
	 *  kd line went down from high point  and kValue great than 50
	 *  this high should great than last high see case on in rimDay1-1.png time 13:15 short
	 */
	public static boolean isKDDownTrendCaseOne(QuoteHistory qh ,
			Indicator kLine,
			Indicator dLine)
			
	{
	    double lastHighest5kValue = findPreviewPeekValue( kLine, dLine, HIGHEST ,3 );
		double thisPeek5kValue = findPreviewPeekValue( kLine, dLine, HIGHEST ,1);
		
		int size = kLine.getHistory().size();
		
		if ( (thisPeek5kValue -  lastHighest5kValue ) > 0 ) //last down peek is lower than this down peek. 
		{
			if ( kLine.getHistory().get( size -1 ).getValue() > 50 )
			{
				return true;
			}
		}else if ( thisPeek5kValue > 75  &&  kLine.getHistory().get( size -1 ).getValue()< 70)  //this peek is very higer
		{
			return true;
		}
		return false;
	}
	
	/*
	 *  kd line went down from high point  and kValue great than 50
	 *  this high should great than last high see case on in rimDay1-1.png time 13:15 short
	 */
	public static boolean isKDUpTrendCaseOne(QuoteHistory qh ,
			Indicator kLine,
			Indicator dLine)
	{
		boolean ret = false;
		double lastHighest5kValue = findPreviewPeekValue( kLine, dLine, HIGHEST ,3 );
		double thisLowest5kValue = findPreviewPeekValue( kLine, dLine, LOWEST ,1 );
		
		if ( lastHighest5kValue > 75 &&  kLine.getValue() > 22 && thisLowest5kValue <  kLine.getValue() )
		{
			ret = true;
		}
		return ret;
	}
	
	public static boolean isKDDownTrendCaseTwo(QuoteHistory qh ,
			Indicator kLine,
			Indicator dLine,
			Indicator sma5,
			Indicator sma15)
	{
	    double lastHighest5kValue = findPreviewPeekValue( kLine, dLine, HIGHEST ,3 );
		double thisPeek5kValue = findPreviewPeekValue( kLine, dLine, HIGHEST ,1);
		
		//sma5 peek point.
		double lastPeekPrice = findPreviewPeekValue( sma5,sma15,HIGHEST, 1 );
		
		double test = ( lastPeekPrice - qh.getLastPriceBar().getMidpoint())/qh.getLastPriceBar().getMidpoint() ;
		boolean alreadyDownLess = (( lastPeekPrice - qh.getLastPriceBar().getMidpoint())/qh.getLastPriceBar().getMidpoint()) < 0.015;
		
		if (( (lastHighest5kValue < 50)  || ( thisPeek5kValue < 50 )) &&
				 isBothDownTrend( sma5, sma15, 2) &&
				 alreadyDownLess)
		{
			return true;
		}
		
		return false;
	}	
	
	public static boolean isKDUpTrendCaseTwo(QuoteHistory qh ,
			Indicator kLine,
			Indicator dLine,
			Indicator sma5,
			Indicator sma15)
	{	
		double lastHighest5kValue = findPreviewPeekValue( kLine, dLine, HIGHEST ,2 );
		if ( lastHighest5kValue < 50)
		{
			System.out.println(" in buyback case two , last 5k peek value = "+
					String.valueOf( lastHighest5kValue) + " now k value = "+ String.valueOf( kLine.getValue()));
			if ( kLine.getValue() > 50)
			{
				System.out.println(" upTread Case two = "+ new Date( kLine.getDate()).toString());
				return true;
			}
		}
		return false;
	}
	public static boolean isOneUpTrend(Indicator sma5, int deepth)
	{
		boolean sma5UpTrend = false;
    	
    	int sma5HistorySize = sma5.getHistory().size()-1;
    	
    	for( int idx =0; idx < deepth; idx++ )
    	{
    		sma5UpTrend =( sma5.getHistory().get(sma5HistorySize - idx ).getValue() 
        			- sma5.getHistory().get(sma5HistorySize-1 - idx ).getValue() ) >0;
        	if ( ! sma5UpTrend )
        	{
        		break;
        	}
    	}
    	return sma5UpTrend;
    }
	
	public static boolean isBothUpTrend(Indicator sma5, Indicator sma15)
	{
		return isBothUpTrend( sma5, sma15, 1);
	}
	
	public static boolean isBothUpTrend(Indicator sma5, Indicator sma15, int deepth)
	{
		
		boolean sma5UpTrend = false;
    	boolean sma15Uprend  = false;
    	
    	int sma5HistorySize = sma5.getHistory().size()-1;
    	int sma15HistorySize = sma15.getHistory().size() -1 ;
    	
    	for( int idx = 0; idx < deepth; idx++ )
    	{
    		sma5UpTrend =( sma5.getHistory().get(sma5HistorySize - idx ).getValue() 
    			- sma5.getHistory().get(sma5HistorySize-1 - idx ).getValue() ) >0;
        	sma15Uprend =( sma15.getHistory().get(sma15HistorySize- idx ).getValue() 
    					- sma15.getHistory().get(sma15HistorySize-1 - idx ).getValue() )>0;
        	
        	if ( ! (sma5UpTrend && sma15Uprend))
        	{
        		break;
        	}
    	}

    	return sma5UpTrend  && sma15Uprend;
	}
	
	  public static boolean isBothDownTrend( Indicator sma5, Indicator sma15)
	  {
		  return isBothDownTrend( sma5, sma15, 1);
	  }
	  
    public static boolean isBothDownTrend( Indicator sma5, Indicator sma15, int deepth)
    {
    	boolean sma5DownTrend = false;
    	boolean sma15Downrend  = false;
    	
    	int sma5HistorySize = sma5.getHistory().size()-1;
    	int sma15HistorySize = sma15.getHistory().size() -1 ;
    	
    	for( int idx = 0; idx < deepth; idx++ )
    	{
    		sma5DownTrend =( sma5.getHistory().get(sma5HistorySize - idx ).getValue() 
    			- sma5.getHistory().get(sma5HistorySize-1 - idx ).getValue() ) < 0;
        	sma15Downrend =( sma15.getHistory().get(sma15HistorySize- idx ).getValue() 
    					- sma15.getHistory().get(sma15HistorySize-1 - idx ).getValue() )<0;
        	
        	if ( ! (sma5DownTrend && sma15Downrend))
        	{
        		break;
        	}
    	}

    	return sma5DownTrend  && sma15Downrend;
    }
	
    public static boolean isHugeChangeforPriceBars( QuoteHistory qh , double percentage )
	{
    	return isHugeChangeforPriceBars( qh, percentage,1);
	}
    
	public static boolean isHugeChangeforPriceBars( QuoteHistory qh , double percentage, int deepth )
	{
		int index = qh.getSize() -1;
		boolean isHugeChage = false;
		int count = 0;
		
		for( ; index > 0 && count < deepth ; index--, count++ )
		{
			double high = qh.getPriceBar( index).getHigh();
			double low = qh.getPriceBar( index ).getLow();
			double changeTest = ( high - low )/qh.getPriceBar( index).getMidpoint();
    	
			isHugeChage = changeTest >percentage;
			if ( isHugeChage)
			{
				break;
			}
		}
		
		return isHugeChage;
	}
	
	/*
	 * time should look like "10:10" 
	 */
	public static boolean isTimeEarlierThan( String time, long date )
	{
		Date dd = new Date( date );
		boolean ret = false;
		int hour1 = new Integer( time.substring( 0, time.indexOf(":")) );
		int min1 = new Integer(time.substring( time.indexOf(":") +1 ));
		int hour2 = dd.getHours();
		int min2 = dd.getMinutes();
		ret =  hour2 < hour1;
		if ( ! ret  && ( hour2 == hour1 ))
		{ 
			ret = ( min2 < min1 );
		}
		
		return ret;
	}
}