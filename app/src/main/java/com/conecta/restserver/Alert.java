package com.conecta.restserver;

/**
 * Created by 1765 IRON on 26/03/2018.
 */

public class Alert {
    String pos;
    String giro;
    String mov;
    String time;

    public Alert() {}

    public Alert(String pos, String giro, String mov, String time) {
        this.pos = pos;
        this.giro = giro;
        this.mov = mov;
        this.time = time;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public String getGiro() {
        return giro;
    }

    public void setGiro(String giro) {
        this.giro = giro;
    }

    public String getMov() {
        return mov;
    }

    public void setMov(String mov) {
        this.mov = mov;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }


    @Override
    public String toString() {
        return "Alert [pos=" + pos + ", giro=" + giro + ", mov=" + mov + ", time=" + time + "]";
    }






}
