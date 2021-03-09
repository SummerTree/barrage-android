package com.huya.data;

import java.util.ArrayList;

/**
 * @author Acropolis
 */
public class MonitorReqData {

    public String sMetricName = "";

    public java.util.ArrayList<DimensionWrapper> vDimension = new ArrayList<>();

    public java.util.ArrayList<FieldWrapper> vField = new ArrayList<>();

    public java.util.ArrayList<DimensionWrapper> vExLog = new ArrayList<>();

    public long iTS = 0;

    public static class DimensionWrapper {

        public DimensionWrapper(String sName, String sValue) {
            this.sName = sName;
            this.sValue = sValue;
        }

        public String sName = "";

        public String sValue = "";

    }

    public static class FieldWrapper {

        public FieldWrapper(String sName, double fValue) {
            this.sName = sName;
            this.fValue = fValue;
        }

        public String sName = "";

        public double fValue = 0;
    }
}
