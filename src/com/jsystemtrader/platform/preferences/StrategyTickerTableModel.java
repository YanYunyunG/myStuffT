package com.jsystemtrader.platform.preferences;

import java.util.ArrayList;

import com.jsystemtrader.platform.model.TableDataModel;
import com.jsystemtrader.platform.strategy.StrategyPreference;
import com.jsystemtrader.platform.strategy.StrategyPreferences;

/**
 * Strategy parameters table model.
 */
public class StrategyTickerTableModel extends TableDataModel {
	
	 enum Column {
	        Ticker("Ticker", String.class),
	        TradeShareVolumn("Long Volumn", Integer.class),
	        TradeStartTime("Trade Start", String.class),
	        TradeEndTime("Trade End", String.class);
	        
	        private final String columnName;
	        private final Class<?> columnClass;

	        Column(String columnName, Class<?> columnClass) {
	            this.columnName = columnName;
	            this.columnClass = columnClass;
	        }
	    }

    public StrategyTickerTableModel() {
    	
    	 Column[] columns = Column.values();
         ArrayList<String> allColumns = new ArrayList<String>();
      
         for (Column column : columns) {
             allColumns.add(column.columnName);
         }

         setSchema(allColumns.toArray(new String[columns.length]));
    }

      @Override
    public Class<?> getColumnClass(int col)
    {
        Column column = Column.values()[col];
        return column.columnClass;
    }

    @Override
    public boolean isCellEditable(int row, int col)
    {
    	return true;
    }
    
    public void removeRowAt( int row)
    {
    	removeRow( row);
    }
   
    public void addTickerPreference( StrategyPreference tpref )
    {	
    	 Object[] row = new Object[getColumnCount() + 1];
         row[0] = tpref.getTickerSymbol();
         row[1] = tpref.getTradeShareVolumn();
         row[2] = tpref.getTradeStartTime();
         row[3] = tpref.getTradeEndTime();
         addRow(row);
    }
    
    public void setTickersParams(StrategyPreferences strategyParams) {
        removeAllData();

        for (StrategyPreference tpref : strategyParams.getAll()) {
        	addTickerPreference( tpref);
        }
    }
    
    
    public StrategyPreferences getPreferences() 
    {
    	StrategyPreferences prefs = new StrategyPreferences();

    	for( int idx =0; idx < getRowCount(); idx++ )
    	{
    		Object[] values = getRow( idx);

    		prefs.addPreference( new StrategyPreference((String)values[0],
    				 (Integer)values[1],(String)values[2], (String)values[3]));
    	}
    	return prefs;

 
    }
    
   

}
