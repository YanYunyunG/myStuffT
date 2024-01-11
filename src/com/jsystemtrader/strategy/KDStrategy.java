package com.jsystemtrader.strategy;

import com.jsystemtrader.indicator.Indicator;
import com.jsystemtrader.indicator.SMA;
import com.jsystemtrader.indicator.StochasticD;
import com.jsystemtrader.indicator.StochasticK;
import com.jsystemtrader.platform.model.JSystemTraderException;
import com.jsystemtrader.platform.optimizer.StrategyParams;
import com.jsystemtrader.platform.quote.BarSize;
import com.jsystemtrader.platform.schedule.TradingInterval;
import com.jsystemtrader.platform.strategy.Strategy;
import com.jsystemtrader.platform.util.ContractFactory;

public class KDStrategy extends Strategy
{
	private static String K_5MIN_LENGTH="K 5Mins Length";
	private static String D_5MIN_LENGTH="D 5Mins Length";
	private static String K_15MIN_LENGTH="K 15Mins Length";
	private static String D_15MIN_LENGTH="D 15Mins Length";
	private static String RSV_LENGTH ="RSV Length";
	
	private com.ib.client.Contract contract = null;
	private Indicator  k_indicator, d_indicator;
	int k_barSize = 5; 
	int d_len = 3;
	
    public KDStrategy(StrategyParams params)
        throws JSystemTraderException
    {
    	contract = ContractFactory.makeStockContract("RIM", "TSE", "CAD");
        setStrategy(contract, BarSize.Min5, false);
        init( );

        k_indicator = new StochasticK( quoteHistory,k_barSize , false );
        d_indicator = new StochasticD( quoteHistory,k_indicator, d_len, false );
       
        addIndicator( "SMA5", new SMA( quoteHistory, 5), 0);
//        addIndicator( "SMA5", new SMA( quoteHistory, 10), 0);
        
       
        String k5m_title = "K("+ String.valueOf( k_barSize ) + "," + String.valueOf( d_len ) + ")";
        addIndicator(k5m_title, k_indicator, 1);
        addIndicator("", d_indicator, 1);
  
//        String d5m_title = "D 5Mins("+ 
//             String.valueOf((int)d5Min_length) +")";
//        addIndicator(d5m_title, d5Min,1);
//        
 /*       String k15m_title = "K 15Mins("+ 
            String.valueOf( (int)rsv_length)+","+String.valueOf((int)k15Min_length) +")";
        addIndicator(k15m_title, k15Min, 2);
      
        String d15m_title = "D 15Mins("+ 
             String.valueOf((int)d15Min_length) +")";

        addIndicator(d15m_title, d15Min,2);*/
    }

 /*   public BarSize getSecondBarSize()
    {
       return BarSize.Min15;
    }
  */
    public boolean saveChartToFile()
    {
    	return false;
    }
   
    // The parameters could be defined for this strategy. 
    public StrategyParams initParams()
    {
        StrategyParams params = getParams();
       //we suppose to have two entries;
        if ( params.getAll().size()> 4)
        {
        	return params;
        }
        	
        params = 	new StrategyParams();
        params.add(RSV_LENGTH, 8D, 8D, 1D);
        params.add(K_5MIN_LENGTH, 5D, 5D, 1D);
        params.add(D_5MIN_LENGTH, 3D, 3D, 1D);
        params.add(K_15MIN_LENGTH, 5D, 5D, 1D);
        params.add(D_15MIN_LENGTH, 3D, 3D, 1D);
       
        return params;
    }

    public TradingInterval initTradingInterval()
        throws JSystemTraderException
    {
        return new TradingInterval( tradingStartTime, tradingEndTime, "America/New_York", false);
    }
    
    
    public  synchronized void onBar()
    {
     
//    	double k5Value = k_indicator.calculate();
//    	double d5Value = d5Min.calculate();
//    	double k_d_5min = k5Value - d5Value;
    	
    	/*double k15Value = k15Min.calculate();
    	double d15Value = d15Min.calculate();
    	double k_d_15min = k15Value - d15Value;
    	
    	
    	System.out.println("Strategy ObBar");
    	System.out.println(" 5min k-d=" + String.valueOf(k_d_5min ));
    	System.out.println(" 15min k-d=" + String.valueOf(k_d_15min ));
    	
    	
    	if ( k_d_15min > 0 && k_d_5min > 0 && k5Value < 50.00 && k15Value < 50)
    	{
    		position = shareNumber;
    		System.out.println( "==k_d_15min > 0 && k_d_5min > 0 && k5Value < 50.00 && k15Value < 50 position to 100");
    		
    	}
    	if ( k_d_5min < 0 && k5Value < 70  && k_d_15min < 0 && position > 0 )
    	{
    		position = 0;
    		System.out.println( "==  k_d_5min < 0 && k5Value < 70  && k_d_15min < 0 , position to 0");
    	}
    	
    	if ( k_d_15min < 0 && k_d_5min < 0 && k5Value > 50.00 && k15Value > 50.00 )
    	{
    		position = -shareNumber;
    		position = 0;
    		System.out.println( "== k_d_15min < 0 && k_d_5min < 0 && k5Value > 50.00 && k15Value > 50.00, position to -100");
    	}
    	
    	if ( k_d_5min > 0  && k5Value > 45 &&  k_d_15min  >0 && k15Value > 25 && position < 0 )
    	{
    		position = 0;
    		System.out.println( "==  k_d_5min > 0  && k5Value > 45 &&  k_d_15min  >0 && k15Value > 25 && position < 0, position to 0");
    	}*/
    }
    
    private void init()
    {
    	 StrategyParams params = getParams();
//    
//    	 rsv_length = params.getAvarage(RSV_LENGTH,8D);
//    	 kLen = params.getAvarage(K_5MIN_LENGTH,5D);
//    	 d5Min_length = params.getAvarage(D_5MIN_LENGTH,3D);
//    	 k15Min_length = params.getAvarage(K_15MIN_LENGTH,5D);
//    	 d15Min_length = params.getAvarage(D_15MIN_LENGTH,3D);
//    	
//    	if ( shareNumber == 0) 
//    	{
//    		shareNumber = 100;
//    	}
//    
//    	if ( tradingStartTime == null )
//    	{
//    		tradingStartTime = "9:50";
//    	}
//    	
//    	if ( tradingEndTime == null )
//    	{
//    		tradingEndTime ="15:56";
//    	}
    	
    }

}
