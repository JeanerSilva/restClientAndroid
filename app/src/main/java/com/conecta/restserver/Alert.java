package com.conecta.restserver;

/**
 * Created by 1765 IRON on 26/03/2018.
 */
public class Alert {
    String pos;
    String font;
    String time;

    public Alert() {}

    public Alert(String pos, String font, String time) {
        this.pos = pos;
        this.font = font;
        this.time = time;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Alert [pos=" + pos + ", font=" + font + ", time=" + time + "]";
    }


}
