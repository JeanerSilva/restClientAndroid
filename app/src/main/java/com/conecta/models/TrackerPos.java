package com.conecta.models;

/**
 * Created by 1765 IRON on 25/03/2018.
 */

public class TrackerPos {
    String pos;
    String time;

    public TrackerPos() {

    }

    public TrackerPos(String pos, String time) {
        this.pos = pos;
        this.time = time;
    }
    public String getPos() {
        return pos;
    }
    public void setPos(String pos) {
        this.pos = pos;
    }
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    @Override
    public String toString() {
        return "TrackerPos [pos=" + pos + ", time=" + time + "]";
    }
}
