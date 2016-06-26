package com.holgis.colorconnection.server;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AppIdentifier;
import com.google.android.gms.nearby.connection.AppMetadata;
import com.google.android.gms.nearby.connection.Connections;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.holgis.colorconnection.R;
import com.holgis.colorconnection.helper.NetHelper;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ColorActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        Connections.ConnectionRequestListener, Connections.MessageListener {

    private GoogleApiClient mGoogleApiClient;
    private int mColor = Color.BLACK;
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 1000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private TextView mContentView;
    private View mRootView;

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            if (Build.VERSION.SDK_INT >= 11) {
                try {
                    mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

                }catch(Exception e){

                }
            }
        }

    };

    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_color);

        mRootView = findViewById(R.id.root_view);
        mContentView = (TextView) findViewById(R.id.fullscreen_content);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Nearby.CONNECTIONS_API)
                .build();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    @Override
    protected void onResume() {
        super.onResume();

        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        startAdvertising();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            stopAdvertising();
            Nearby.Connections.stopAllEndpoints(mGoogleApiClient);
            mGoogleApiClient.disconnect();
        }
    }

    private void startAdvertising() {

        if (NetHelper.isConnected(this)) {

            // Identify that this device is the host

            // Advertising with an AppIdentifer lets other devices on the
            // network discover this application and prompt the user to
            // install the application.
            List<AppIdentifier> appIdentifierList = new ArrayList<>();
            appIdentifierList.add(new AppIdentifier(getPackageName()));
            AppMetadata appMetadata = new AppMetadata(appIdentifierList);

            // The advertising timeout is set to run indefinitely
            // Positive values represent timeout in milliseconds
            long NO_TIMEOUT = 0L;

            String name = null;
            Nearby.Connections.startAdvertising(mGoogleApiClient, name, appMetadata, NO_TIMEOUT, this)
                    .setResultCallback(new ResultCallback<Connections.StartAdvertisingResult>() {
                        @Override
                        public void onResult(Connections.StartAdvertisingResult result) {
                            if (result.getStatus().isSuccess()) {
                                // Device is advertising
                            } else {
                                int statusCode = result.getStatus().getStatusCode();
                                // Advertising failed - see statusCode for more details
                            }
                        }
                    });
        }
    }

    private void stopAdvertising() {
        Nearby.Connections.stopAdvertising(mGoogleApiClient);
    }

    @Override
    public void onConnectionRequest(final String remoteEndpointId, String remoteDeviceId,
                                    String remoteEndpointName, final byte[] payload) {

        byte[] myPayload = null;

        if(mColor != Color.BLACK) {
            myPayload = buildPayload(mColor);
        }
        // Automatically accept all requests

        Nearby.Connections.acceptConnectionRequest(mGoogleApiClient, remoteEndpointId,
                myPayload, this).setResultCallback(new ResultCallback<Status>() {

            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    if (checkColor(payload) && mColor == Color.BLACK) {
                        mColor = parseColor(payload);
                        mRootView.setBackgroundColor(mColor);
                    }
                    mContentView.setText("");
                    stopAdvertising();
                } else {
                    Toast.makeText(ColorActivity.this, "Accept failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDisconnected(String remoteEndpointId) {
        mContentView.setText(R.string.dummy_content);
        startAdvertising();
    }

    @Override
    public void onMessageReceived(String endpointId, byte[] payload, boolean isReliable) {
        if(checkColor(payload)){
            mColor = parseColor(payload);
            mRootView.setBackgroundColor(mColor);
        }
    }

    private boolean checkColor(byte[] payload) {
        return (payload!=null && payload.length >= 4 * 3);
    }

    private int parseColor(byte[] payload) {
        if (payload.length >= 4 * 3) {
            ByteBuffer bb = ByteBuffer.wrap(payload);
            int red = bb.getInt();
            int green = bb.getInt();
            int blue = bb.getInt();
            return Color.rgb(red, green, blue);
        }
        return 0;
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private byte[] buildPayload(int color){

        ByteBuffer bb = ByteBuffer.allocate(4*3);
        bb.putInt(Color.red(color));
        bb.putInt(Color.green(color));
        bb.putInt(Color.blue(color));

        return bb.array();
    }
}
