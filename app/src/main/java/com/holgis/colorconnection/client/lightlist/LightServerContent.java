package com.holgis.colorconnection.client.lightlist;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class LightServerContent {

    public static final List<LightServerContent.LightServerItem> ITEMS = new ArrayList<>();

    public static void AddLight(String name, String endpoint) {
        for (int i = 0; i < ITEMS.size(); ++i) {
            if (ITEMS.get(i).EndpointId.equals(endpoint)) {
                return;
            }
        }
        ITEMS.add(new LightServerContent.LightServerItem(name, endpoint));
    }

    public static void UpdateLight(String endpoint, boolean connected) {
        for(int i=0;i<ITEMS.size();++i) {
            LightServerContent.LightServerItem ls = ITEMS.get(i);
            if(ls.EndpointId.equals(endpoint)) {
                ls.setConnected(connected);
                return;
            }
        }
    }

    public static  void RemoveLight(String endpoint) {
        for(int i=0;i<ITEMS.size();++i) {
            if(ITEMS.get(i).EndpointId.equals(endpoint)) {
                ITEMS.remove(i);
                return;
            }
        }
    }

    public static class LightServerItem {

        public final String Name;
        public final String EndpointId;
        public  boolean Connected;

        public LightServerItem(String name, String endpointId) {
            this.Name = name;
            this.EndpointId =endpointId;
            this.Connected = false;
        }

        public void setConnected(boolean connected) {
            this.Connected = connected;
        }

        public boolean isConnected() {
            return this.Connected;
        }
    }
}
