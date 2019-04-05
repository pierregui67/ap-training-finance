package com.qfs.sandbox.cfg.impl;
import com.qfs.msg.IColumnCalculator;
import com.qfs.msg.csv.ILineReader;

public class ColumnCalculator  implements IColumnCalculator<ILineReader> {

    private String columnName;

    public ColumnCalculator(String columnName) {
        this.columnName = columnName;
    }

    @Override
    public String getColumnName() {
        return this.columnName;
    }

    @Override
    public Object compute(IColumnCalculationContext<ILineReader> context) {
        String fileName = context.getContext().getCurrentFile().getName();
        String strBegin = "PriceHistory_";
        String strEnd = ".csv";
        fileName.replace(strBegin, "");
        fileName.replace(strEnd,"");
        fileName.replace("-",".");
        return fileName;
    }

}
