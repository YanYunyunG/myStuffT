package com.jsystemtrader.platform.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class StrategyPreferences
{
	private List<StrategyPreference> list =null;
	
	public StrategyPreferences(){
		list = new ArrayList<StrategyPreference>();
	}

	public void addPreference( StrategyPreference tickerPrefer) {
		list.add( tickerPrefer);
	}
	
	public List<StrategyPreference>  getAll() {
		return list;
	}
	
	public static StrategyPreferences create( String value ) {
		StrategyPreferences preferences = new StrategyPreferences();
		try {
			StringTokenizer  token = new StringTokenizer( value, separator);
			while( token.hasMoreTokens() ) {
				preferences.addPreference( StrategyPreference.create(  token.nextToken()));
			}
		}catch( Exception _e) {
			_e.printStackTrace();
		}
		return preferences;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(StrategyPreference pref : getAll()) {
			sb.append( pref.toString());
			sb.append( separator);
		}
		return sb.toString();
	}

	private static String separator = "||";
}
