package com.jsystemtrader.platform.util;

import javax.swing.table.*;
import java.math.*;
import java.text.*;


public class DoubleRenderer extends DefaultTableCellRenderer {
    private final DecimalFormat df;
    private final int SCALE = 4;

    public DoubleRenderer() {
        df = (DecimalFormat) NumberFormat.getNumberInstance();
        df.setMaximumFractionDigits(SCALE);
        df.setGroupingUsed(false);
    }

    @Override
    public void setValue(Object value) {
        String text = "";
        if (value != null) {
            if (value.getClass() == Double.class) {
                if (!Double.isInfinite((Double) value) && !Double.isNaN((Double) value)) {
                    BigDecimal bd = new BigDecimal((Double) value);
                    bd = bd.setScale(SCALE, BigDecimal.ROUND_FLOOR);
                    text = df.format(bd.doubleValue());
                }
                else {
                    text = "N/A";
                }
            } else if (value.getClass() == Integer.class) {
                text = value.toString();
            } else if (value.getClass() == String.class) {
                text = value.toString();
            } else {
                throw new RuntimeException("Could not convert " + value.getClass() + " to a number");
            }
        }

        setHorizontalAlignment(RIGHT);
        setText(text+" ");
    }
}
