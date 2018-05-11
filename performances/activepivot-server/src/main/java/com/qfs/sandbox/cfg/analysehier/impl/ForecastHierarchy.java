package com.qfs.sandbox.cfg.analysehier.impl;

import com.qfs.store.query.impl.DatastoreQueryHelper;
import com.quartetfs.biz.pivot.cube.hierarchy.IAnalysisHierarchyInfo;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.cube.hierarchy.IMultiVersionHierarchy;
import com.quartetfs.biz.pivot.cube.hierarchy.axis.impl.AAnalysisHierarchy;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

import java.util.*;

import static com.qfs.sandbox.cfg.datastore.impl.DatastoreDescriptionConfig.*;

@QuartetExtendedPluginValue(intf=IMultiVersionHierarchy.class, key=ForecastHierarchy.PLUGIN_KEY)
public class ForecastHierarchy extends AAnalysisHierarchy {

    public static final String PLUGIN_KEY = "FORECAST_HIERARCHY";

    public static String[] TIME_BUCKET_LEVELS = new String[]{"Date"};
    private static int numberOfLevel = TIME_BUCKET_LEVELS.length;


    public static final int EXTRA_DATES_CARDINAL = 10;

    public static HashSet<Date> extraDates = new HashSet<>();

    /**
     * Constructor
     *
     * @param info the info about the hierarchy
     */
    public ForecastHierarchy(IAnalysisHierarchyInfo info) {
        super(info);
    }

    @Override
    public String getType() {
        return this.PLUGIN_KEY;
    }

    @Override
    public String getLevelName(int levelOrdinal) {
        return TIME_BUCKET_LEVELS[levelOrdinal-1];
    }

    @Override
    public boolean getNeedRebuild() {
        return true;
    }

    @Override
    public int getLevelsCount() {
        return numberOfLevel + 1;
    }

    @Override
    public Collection<Object[]> buildDiscriminatorPaths() {
        final List<Object[]> list = new ArrayList<Object[]>();
        Set<Object> queries = DatastoreQueryHelper.selectDistinct(datastore.getDatastore().getMostRecentVersion(),
                STOCK_PRICE_HISTORY_STORE_NAME, HISTORY_DATE);
        Date date;

        // Adding the existing data
        Date maxDate = null;
        for (Object obj : queries) {
            date = (Date) obj;
            list.add(new Object[]{ILevel.ALLMEMBER, date});
            if (maxDate == null)
                maxDate = date;
            if (date.after(maxDate))
                maxDate = date;
        }
        date = maxDate;

        // Adding extra-dates.
        if (date != null) {
            for (int i = 0; i < EXTRA_DATES_CARDINAL; i++) {
                list.add(new Object[]{ILevel.ALLMEMBER, date});
                extraDates.add(date);
                Calendar c = Calendar.getInstance();
                c.setTime(date);
                c.add(Calendar.DATE, 1);
                date = c.getTime();
            }
        }
        return list;
    }
}
