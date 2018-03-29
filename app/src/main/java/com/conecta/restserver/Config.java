package com.conecta.restserver;

public class Config {
    private String gpsStatus;
    private String giroStatus;
    private String time;
    private String gpsTime;
    private String gpsDist;
    private String giroSense;
    private String timerTransmit;
    private String whatsApp;

    public String getWhatsApp() {
        return whatsApp;
    }

    public void setWhatsApp(String whatsApp) {
        this.whatsApp = whatsApp;
    }

    public Config() {}

    public String getGpsStatus() {
        return gpsStatus;
    }

    public void setGpsStatus(String gpsStatus) {
        this.gpsStatus = gpsStatus;
    }

    public String getGiroStatus() {
        return giroStatus;
    }

    public void setGiroStatus(String giroStatus) {
        this.giroStatus = giroStatus;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getGpsTime() {
        return gpsTime;
    }

    public void setGpsTime(String gpsTime) {
        this.gpsTime = gpsTime;
    }

    public String getGpsDist() {
        return gpsDist;
    }

    public void setGpsDist(String gpsDist) {
        this.gpsDist = gpsDist;
    }

    public String getGiroSense() {
        return giroSense;
    }

    public void setGiroSense(String giroSense) {
        this.giroSense = giroSense;
    }

    public String getTimerTransmit() {
        return timerTransmit;
    }

    public void setTimerTransmit(String timerTransmit) {
        this.timerTransmit = timerTransmit;
    }

    @Override
    public String toString() {
        return "Config [gpsStatus=" + gpsStatus + ", giroStatus=" + giroStatus + ", time=" + time + ", gpsTime="
                + gpsTime + ", gpsDist=" + gpsDist + ", giroSense=" + giroSense + ", timerTransmit=" + timerTransmit
                + ", whatsApp=" + whatsApp + "]";
    }
}
