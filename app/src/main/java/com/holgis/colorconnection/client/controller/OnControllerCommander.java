package com.holgis.colorconnection.client.controller;

/**
 * Created by Holger on 20.02.2016.
 */
public interface OnControllerCommander {
    void onConnectionCountChanged(int count);
    int getControllerColor();
    void setControllerColor(int color);
}

