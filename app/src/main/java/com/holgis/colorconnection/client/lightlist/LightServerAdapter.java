package com.holgis.colorconnection.client.lightlist;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.holgis.colorconnection.R;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link LightServerContent} and makes a call to the
 * specified {@link LightServerListFragment.OnServerListListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class LightServerAdapter extends RecyclerView.Adapter<LightServerAdapter.ViewHolder> {

    private final List<LightServerContent.LightServerItem> mLightServer;
    private final LightServerListFragment.OnServerListListener mListener;

    public LightServerAdapter(List<LightServerContent.LightServerItem> items, LightServerListFragment.OnServerListListener listener) {

        mLightServer = items;
        mListener = listener;
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_server, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        LightServerContent.LightServerItem lightServer = mLightServer.get(position);

        holder.mItem = lightServer;
        holder.mName.setText(lightServer.Name);
        holder.mEndpoint.setText(lightServer.EndpointId);
        holder.mConnected.setChecked(lightServer.Connected);

        holder.mConnected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                mListener.onCheckedChanged(holder.mItem, checked);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mLightServer.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mName;
        public final TextView mEndpoint;
        public final SwitchCompat mConnected;

        public LightServerContent.LightServerItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mName = (TextView) view.findViewById(R.id.name);
            mEndpoint = (TextView) view.findViewById(R.id.endpoint);
            mConnected = (SwitchCompat) view.findViewById(R.id.connected);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mEndpoint.getText() + "'";
        }
    }
}
