package com.conecta.restserver;

/**
 * Created by 1765 IRON on 26/03/2018.
 */

public enum RequestType  {
    TRAKERPOS_PULL,
    CONFIG_GIRO_PULL,
    CONFIG_GPS_PULL,
    CONFIG_PUBLISH,
    REFRESH_POS,
    REFRESH_ALERT,
    ALERT_PULL;

}