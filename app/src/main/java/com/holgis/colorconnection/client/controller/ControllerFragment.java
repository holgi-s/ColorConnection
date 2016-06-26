package com.holgis.colorconnection.client.controller;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.holgis.colorconnection.R;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SVBar;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ControllerFragment.OnControllerListener} interface
 * to handle interaction events.
 * Use the {@link ControllerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ControllerFragment extends Fragment implements
        ColorPicker.OnColorChangedListener,
        ColorPicker.OnColorSelectedListener,
        OnControllerCommander {

    private ColorPicker mPicker;
    private SVBar mSVBar;
    private TextView mConnected;
    private OnControllerListener mListener;

    private int mColorCount = 0;

    public ControllerFragment() {
        // Required empty public constructo
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ControllerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ControllerFragment newInstance() {
        ControllerFragment fragment = new ControllerFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_controller, container, false);

        mPicker = (ColorPicker)view.findViewById(R.id.picker);
        mSVBar = (SVBar)view.findViewById(R.id.svbar);

        mPicker.addSVBar(mSVBar);
        mPicker.setShowOldCenterColor(false);
        mPicker.setOnColorChangedListener(this);
        mPicker.setOnColorSelectedListener(this);

        mConnected = (TextView)view.findViewById(R.id.colorCount);
        onConnectionCountChanged(mColorCount);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnControllerListener) {
            mListener = (OnControllerListener) context;
            mListener.onAttachColorCommander(this);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.onDetachColorCommander();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnControllerListener {

        void onAttachColorCommander(OnControllerCommander commander);
        void onDetachColorCommander();

        void onColorChanged(int color);
        void onColorSelected(int color);
    }

    @Override
    public void onColorChanged(int color, boolean fromUser) {
        if (mListener != null && fromUser) {
            mListener.onColorChanged(color);
        }
    }

    @Override
    public void onColorSelected(int color, boolean fromUser) {
        if (mListener != null && fromUser) {
            mListener.onColorSelected(color);
        }
    }

    public void onConnectionCountChanged(int count){
        String connected = getResources().getQuantityString(R.plurals.color_count, count, count);
        mColorCount = count;
        if(mConnected!=null) {
            mConnected.setText(connected);
        }
    }

    public int getControllerColor() {
        if(mPicker!=null) {
            return mPicker.getColor();
        }
        return Color.BLACK;
    }

    public void setControllerColor(int color) {
        if(mPicker!=null) {
            mPicker.setColor(color);
        }
    }

}
