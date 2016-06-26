package com.holgis.colorconnection.client;

import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Connections;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.holgis.colorconnection.client.lightlist.LightServerContent;
import com.holgis.colorconnection.client.lightlist.LightServerListFragment;
import com.holgis.colorconnection.client.controller.OnControllerCommander;
import com.holgis.colorconnection.client.lightlist.OnServerListCommander;
import com.holgis.colorconnection.R;
import com.holgis.colorconnection.client.controller.ControllerFragment;
import com.holgis.colorconnection.helper.NetHelper;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ControllerActivity extends AppCompatActivity implements
        ControllerFragment.OnControllerListener,
        LightServerListFragment.OnServerListListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        Connections.EndpointDiscoveryListener,
        Connections.MessageListener{

    private GoogleApiClient mGoogleApiClient;

    ViewPager mViewPager;
    TabAdapter mTabsAdapter;
    OnControllerCommander mColorCommander;
    OnServerListCommander mListCommander;

    List<String> mVisibleEndpointIds = new ArrayList<>();
    List<String> mConnectdEndpointIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_controller);

        final ActionBar bar =  getSupportActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

        mViewPager = (ViewPager) findViewById(R.id.pager);

        mTabsAdapter = new TabAdapter(this, mViewPager);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.title_fragment_light_list),
                LightServerListFragment.class, null);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.title_fragment_controller),
                ControllerFragment.class, null);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Nearby.CONNECTIONS_API)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mGoogleApiClient.connect();

        updateColorCount();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {

            stopDiscovery();

            disconnectAll();

            Nearby.Connections.stopAllEndpoints(mGoogleApiClient);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

        startDiscovery();
    }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) { }

    private void startDiscovery() {

        if (NetHelper.isConnected(this)) {

            // Implement logic when device is not connected to a network
            String serviceId = getString(R.string.service_id);

            // Set an appropriate timeout length in milliseconds
            long DISCOVER_TIMEOUT = 0;

            // Discover nearby apps that are advertising with the required service ID.
            Nearby.Connections.startDiscovery(mGoogleApiClient, serviceId, DISCOVER_TIMEOUT, this)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                            } else {
                                // If the user hits 'Discover' multiple times in the timeout window,
                                // the error will be STATUS_ALREADY_DISCOVERING
                                int statusCode = status.getStatusCode();
                                if (statusCode == ConnectionsStatusCodes.STATUS_ALREADY_DISCOVERING) {
                                } else {
                                }
                            }
                        }
                    });
        }
    }

    private void stopDiscovery() {
        String serviceId = getString(R.string.service_id);
        Nearby.Connections.stopDiscovery(mGoogleApiClient, serviceId);
    }

    @Override
    public void onEndpointFound(final String endpointId, String deviceId,
                                String serviceId, final String endpointName) {
        // This device is discovering endpoints and has located an advertiser.
        // Write your logic to initiate a connection with the device at
        // the endpoint ID

        String msg = "onEndpointFound: EP:" + endpointId + ", DID:" + deviceId +
                ", SID:" + serviceId + ", EPN:" + endpointName;

        if(!has(mVisibleEndpointIds, endpointId)) {
            mVisibleEndpointIds.add(endpointId);
            if(!has(mConnectdEndpointIds, endpointId)) {
                LightServerContent.AddLight(endpointName, endpointId);
                if(mListCommander!=null){
                    mListCommander.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onEndpointLost(String remoteEndpointId) {
        // An endpoint that was previously available for connection is no longer. It may have
        // stopped advertising, gone out of range, or lost connectivity. Dismiss any dialog that
        // was offering a connection.
        mVisibleEndpointIds.remove(remoteEndpointId);
        if(!has(mConnectdEndpointIds,remoteEndpointId)) {
            LightServerContent.RemoveLight(remoteEndpointId);
            if(mListCommander!=null){
                mListCommander.notifyDataSetChanged();
            }
        }
    }

    private void connectTo(String remoteEndpointId) {
        // Send a connection request to a remote endpoint. By passing 'null' for
        // the name, the Nearby Connections API will construct a default name
        // based on device model such as 'LGE Nexus 5'.
        String myName = null;

        byte[] payload = null;
        if(mColorCommander!=null){
            payload = buildPayload(mColorCommander.getControllerColor());
        }

        Nearby.Connections.sendConnectionRequest(mGoogleApiClient, myName,
                remoteEndpointId, payload, new Connections.ConnectionResponseCallback() {
                    @Override
                    public void onConnectionResponse(String remoteEndpointId, Status status,
                                                     byte[] bytes) {
                        if (status.isSuccess()) {
                            // Successful connection
                            if(!has(mConnectdEndpointIds,remoteEndpointId)) {
                                mConnectdEndpointIds.add(remoteEndpointId);
                            }
                            updateColorCount();
                            LightServerContent.UpdateLight(remoteEndpointId, true);
                            if(mListCommander!=null){
                                mListCommander.notifyDataSetChanged();
                            }
                            if(mColorCommander!=null){
                                if(bytes.length>=3*4){
                                    mColorCommander.setControllerColor(parsePayload(bytes));
                                }
                            }
                        } else {
                            // Failed connection
                            Toast.makeText(ControllerActivity.this, "Requests failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, this);
    }

    @Override
    public void onMessageReceived(String s, byte[] bytes, boolean b) {
        //not used
    }

    @Override
    public void onDisconnected(String remoteEndpointId) {
        if(has(mVisibleEndpointIds,remoteEndpointId)) {
            LightServerContent.UpdateLight(remoteEndpointId, false);
        } else {
            LightServerContent.RemoveLight(remoteEndpointId);
        }
        if(mListCommander!=null){
            mListCommander.notifyDataSetChanged();
        }
        mConnectdEndpointIds.remove(remoteEndpointId);
        updateColorCount();
    }

    private void disconnectAll() {
        for(String ep : mConnectdEndpointIds) {
            Nearby.Connections.disconnectFromEndpoint(mGoogleApiClient, ep);
            LightServerContent.UpdateLight(ep, false);
        }
        if(mListCommander!=null){
            mListCommander.notifyDataSetChanged();
        }
        mConnectdEndpointIds.clear();
        updateColorCount();
    }

    private void updateColorCount() {
        if(mColorCommander !=null){
            mColorCommander.onConnectionCountChanged(mConnectdEndpointIds.size());
        }
    }

    @Override
    public void onAttachColorCommander(OnControllerCommander commander) {
        mColorCommander = commander;
        updateColorCount();
    }

    @Override
    public void onDetachColorCommander() {
        mColorCommander = null;
    }

    @Override
    public void onColorChanged(int color){
        sendMessageUdp(color, null);
    }

    @Override
    public void onColorSelected(int color){
        sendMessage(color, null);
    }

    @Override
    public void onAttachServerListCommander(OnServerListCommander commander) {
        mListCommander = commander;
    }

    @Override
    public void onDetachServerListCommander() {
        mListCommander = null;
    }

    @Override
    public void onCheckedChanged(LightServerContent.LightServerItem lightServer, boolean checked) {

        if(lightServer.isConnected() && !checked) {
            Nearby.Connections.disconnectFromEndpoint(mGoogleApiClient, lightServer.EndpointId);

            mConnectdEndpointIds.remove(lightServer.EndpointId);
            LightServerContent.UpdateLight(lightServer.EndpointId, false);
            if(mListCommander!=null){
                mListCommander.notifyDataSetChanged();
            }

            updateColorCount();
        }
        else if(checked) {
            connectTo(lightServer.EndpointId);
        }
    }

    private void sendMessage(int color, String colorEndpoint) {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && !mConnectdEndpointIds.isEmpty()) {

            byte[] payload = buildPayload(color);

            if(colorEndpoint==null) {
                Nearby.Connections.sendReliableMessage(mGoogleApiClient, mConnectdEndpointIds, payload);
            } else {
                Nearby.Connections.sendReliableMessage(mGoogleApiClient, colorEndpoint, payload);
            }
        }
    }

    private void sendMessageUdp(int color, String colorEndpoint) {

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && !mConnectdEndpointIds.isEmpty()) {

            byte[] payload = buildPayload(color);

            if(colorEndpoint==null) {
                Nearby.Connections.sendUnreliableMessage(mGoogleApiClient, mConnectdEndpointIds, payload);
            } else {
                Nearby.Connections.sendUnreliableMessage(mGoogleApiClient, colorEndpoint, payload);
            }
        }
    }

    private byte[] buildPayload(int color){

        ByteBuffer bb = ByteBuffer.allocate(4*3);
        bb.putInt(Color.red(color));
        bb.putInt(Color.green(color));
        bb.putInt(Color.blue(color));

        return bb.array();
    }
    private int parsePayload(byte[] payload){

        ByteBuffer bb = ByteBuffer.wrap(payload);

        int red = bb.getInt(0);
        int green = bb.getInt(4);
        int blue = bb.getInt(8);

        return Color.rgb(red,green,blue);
    }


    private boolean has(List<String> items, String item){
        return items.indexOf(item) != -1;
    }
}
