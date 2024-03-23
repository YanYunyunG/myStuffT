package com.jsystemtrader.platform.chart;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.RendererUtils;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.xy.XYDataset;

/**
 * Performs fast rendering of large datasets in nearly constant time.
 */
@SuppressWarnings("serial")
public class FastXYPlot extends XYPlot {
    private final HashSet<Integer> renderedPixels = new HashSet<Integer>();

    public FastXYPlot(XYDataset dataset, ValueAxis domainAxis, ValueAxis rangeAxis, XYItemRenderer renderer) {
        super(dataset, domainAxis, rangeAxis, renderer);
    }

    /**
     * Determines if the item is to be rendered in the area of the plot where one of the previous
     * items has already been rendered.
     */
    private boolean hasRendered(XYDataset dataset, ValueAxis xAxis, ValueAxis yAxis, RectangleEdge domainEdge, RectangleEdge rangeEdge, Rectangle2D dataArea, int series, int item, int width) {
        boolean hasRendered = true;

        double xValue = dataset.getXValue(series, item);
        double yValue = dataset.getYValue(series, item);
        int x = (int) xAxis.valueToJava2D(xValue, dataArea, domainEdge);
        int y = (int) yAxis.valueToJava2D(yValue, dataArea, rangeEdge);

        int itemKey = x + width * y;
        if (!renderedPixels.contains(itemKey)) {
            renderedPixels.add(itemKey);
            hasRendered = false;
        }

        return hasRendered;
    }

    @Override
    public boolean render(Graphics2D g2, Rectangle2D dataArea, int index, PlotRenderingInfo info, CrosshairState crosshairState) {
        boolean foundData = false;
        XYDataset dataset = getDataset(index);
        if (!DatasetUtils.isEmptyOrNull(dataset)) {
            foundData = true;
            ValueAxis xAxis = getDomainAxisForDataset(index);
            ValueAxis yAxis = getRangeAxisForDataset(index);
            XYItemRenderer renderer = getRenderer(index);
            if (renderer == null) {
                renderer = getRenderer();
                if (renderer == null) {// no default renderer available
                    return foundData;
                }
            }

            XYItemRendererState state = renderer.initialise(g2, dataArea, this, dataset, info);
            int passCount = renderer.getPassCount();

            renderedPixels.clear();
            RectangleEdge domainEdge = getDomainAxisEdge();
            RectangleEdge rangeEdge = getDomainAxisEdge();
            int width = (int) dataArea.getWidth();

            SeriesRenderingOrder seriesOrder = getSeriesRenderingOrder();
            if (seriesOrder == SeriesRenderingOrder.REVERSE) {
                //render series in reverse order
                for (int pass = 0; pass < passCount; pass++) {
                    int seriesCount = dataset.getSeriesCount();
                    for (int series = seriesCount - 1; series >= 0; series--) {
                        int firstItem = 0;
                        int lastItem = dataset.getItemCount(series) - 1;
                        if (lastItem == -1) {
                            continue;
                        }
                        if (state.getProcessVisibleItemsOnly()) {
                            int[] itemBounds = RendererUtils.findLiveItems(dataset, series, xAxis.getLowerBound(), xAxis.getUpperBound());
                            firstItem = itemBounds[0];
                            lastItem = itemBounds[1];
                        }
                        int items = lastItem - firstItem + 1;
                        boolean renderAll = items < 2 * width;
                        for (int item = firstItem; item <= lastItem; item++) {
                            if (renderAll || !hasRendered(dataset, xAxis, yAxis, domainEdge, rangeEdge, dataArea, series, item, width)) {
                                renderer.drawItem(g2, state, dataArea, info, this, xAxis, yAxis, dataset, series, item, crosshairState, pass);
                            }
                        }
                    }
                }
            } else {
                //render series in forward order
                for (int pass = 0; pass < passCount; pass++) {
                    int seriesCount = dataset.getSeriesCount();
                    for (int series = 0; series < seriesCount; series++) {
                        int firstItem = 0;
                        int lastItem = dataset.getItemCount(series) - 1;
                        if (state.getProcessVisibleItemsOnly()) {
                            int[] itemBounds = RendererUtils.findLiveItems(dataset, series, xAxis.getLowerBound(), xAxis.getUpperBound());
                            firstItem = itemBounds[0];
                            lastItem = itemBounds[1];
                        }
                        int items = lastItem - firstItem + 1;
                        boolean renderAll = items < 2 * width;
                        for (int item = firstItem; item <= lastItem; item++) {
                            if (renderAll || !hasRendered(dataset, xAxis, yAxis, domainEdge, rangeEdge, dataArea, series, item, width)) {
                                renderer.drawItem(g2, state, dataArea, info, this, xAxis, yAxis, dataset, series, item, crosshairState, pass);
                            }
                        }
                    }
                }
            }
        }
        return foundData;
    }

}
