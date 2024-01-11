package com.jsystemtrader.platform.optimizer;

import java.util.*;

/**
 */
public class StrategyParams {
    private final List<StrategyParam> params;
    private final Map<String, StrategyParam> paramsLookUp;

    public StrategyParams() {
        params = new ArrayList<StrategyParam>();
        paramsLookUp = new HashMap<String, StrategyParam>();
    }

    // copy constructor
    public StrategyParams(StrategyParams params) {
        this.params = new ArrayList<StrategyParam>();
        this.paramsLookUp = new HashMap<String, StrategyParam>();
        for (StrategyParam param : params.getAll()) {
            StrategyParam paramCopy = new StrategyParam(param);
            this.params.add(paramCopy);
            this.paramsLookUp.put(paramCopy.getName(), paramCopy);
        }
    }

    public List<StrategyParam> getAll() {
        return params;
    }

    public void add(String name, double min, double max, double step) {
        StrategyParam param = new StrategyParam(name, min, max, step);
        params.add(param);
        paramsLookUp.put(name, param);
    }

    public void add( StrategyParam par )
    {
    	if ( par != null )
    	{
    		if ( paramsLookUp.get(par.getName()) != null){
    		
    			return;
    		}
    		params.add( par );
    		paramsLookUp.put(par.getName(), par);
    	}
    }
 
    public int size() {
        return params.size();
    }

    public StrategyParam get(int index) {
        return params.get(index);
    }


    public double get(String name, double defaultValue) {
        double value = defaultValue;
        StrategyParam param = paramsLookUp.get(name);
        if (param != null) {
            value = param.getValue();
        }

        return value;
    }
    
    public double getAvarage( String name , double defaultValue )
    {
    	  double value = defaultValue;
          StrategyParam param = paramsLookUp.get(name);
          if (param != null) {
              return param.getAverage();
          }
          return defaultValue;
    }

    public int iterations() {
        int iterations = 1;
        for (StrategyParam param : params) {
            iterations *= param.iterations();
        }
        return iterations;
    }

    public static StrategyParams create( String value)
    {
    	try {
    		StrategyParams retParams = new StrategyParams();

    		StringTokenizer token = new StringTokenizer( value , "|");
    
    		while( token.hasMoreTokens() )
    		{
    			retParams.add( StrategyParam.create( token.nextToken()));
    		}

    		return retParams;
    	}catch( Exception _e )
    	{
    		;
    	}
    	return null;
    }

    @Override
    public String toString() {
      
    	StringBuilder sb = new StringBuilder();
    	
    	for( int idx =0; idx < params.size(); idx++)
    	{
    		StrategyParam pa = params.get( idx);
    		sb.append( pa.getName());
    		sb.append( ",");
    		sb.append( pa.getMin());
    		sb.append( ",");
    		sb.append( pa.getMax());
    		sb.append( ",");
    		sb.append( pa.getStep());
    		sb.append( ",");
    		sb.append( pa.getValue());
    		sb.append("|");
    		
    	}
    	
    	String ret = sb.toString();
    	if ( ret.endsWith("|"))
    	{
    		ret = ret.substring(0, ret.length()-1);
    	}
    	return ret;
    }
    

}
