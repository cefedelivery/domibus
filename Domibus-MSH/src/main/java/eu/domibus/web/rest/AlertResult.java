package eu.domibus.web.rest;

import eu.domibus.core.alerts.model.web.AlertRo;

import java.util.ArrayList;
import java.util.List;

public class AlertResult {

    private int count;

    private List<AlertRo> alertsEntries=new ArrayList<>();

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<AlertRo> getAlertsEntries() {
        return alertsEntries;
    }

    public void addAlertEntry(AlertRo alertRo){
        alertsEntries.add(alertRo);
    }


    public void setAlertsEntries(List<AlertRo> alertsEntries) {
        this.alertsEntries = alertsEntries;
    }
}
