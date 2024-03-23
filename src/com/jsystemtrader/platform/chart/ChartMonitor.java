package com.jsystemtrader.platform.chart;

import java.awt.Cursor;
import java.awt.Graphics;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

@SuppressWarnings("serial")
public class ChartMonitor extends ChartPanel {

    public ChartMonitor(JFreeChart chart, boolean useBuffer) {
        super(chart, useBuffer);
    }

    @Override
    public void paint(Graphics g) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        super.paint(g);
        setCursor(Cursor.getDefaultCursor());
    }

}
