package com.droidlogic.tvinput.shortcut;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.util.AttributeSet;
import android.util.Log;

import com.droidlogic.tvinput.R;

public class GuideListView extends ListView implements OnItemSelectedListener {
    private static final String TAG = "GuideListView";

    public static final String ITEM_1               = "item1";
    public static final String ITEM_2               = "item2";
    public static final String ITEM_3               = "item3";

    private Context mContext;
    private int selectedPosition = 0;
    private ViewGroup rootView = null;
     private ListItemSelectedListener mListItemSelectedListener;


    public GuideListView(Context context){
        super(context);
    }
    public GuideListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        setRootView();
        setOnItemSelectedListener(this);
    }

    public boolean dispatchKeyEvent (KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_UP:
                    if (selectedPosition == 0)
                        return true;
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    if (selectedPosition == getAdapter().getCount() -1)
                        return true;
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if (!hasNextFocusView(View.FOCUS_LEFT))
                        return true;
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (!hasNextFocusView(View.FOCUS_RIGHT))
                        return true;
                    break;
            }

            View selectedView = getSelectedView();
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    if ( selectedView != null) {
                        clearChoosed(selectedView);
                    }
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if ( selectedView != null) {
                        setItemTextColor(selectedView, false);
                    }
                    break;
            }
        }

        return super.dispatchKeyEvent(event);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedPosition = position;
        if (view != null) {
            if (hasFocus()) {
                setItemTextColor(view, true);
            } else {
                setItemTextColor(view, false);
            }
            setChoosed(view);
        }
        if (mListItemSelectedListener != null)
            mListItemSelectedListener.onListItemSelected((View)parent, position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
	}

    @Override
    protected void onFocusChanged (boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        View item = getChildAt(selectedPosition);

        if (item != null) {
            if (gainFocus) {
                cleanChoosed();
                setItemTextColor(item, true);
                setChoosed(item);
            } else {
                //setChoosed(selectedPosition, true);
                setItemTextColor(item, false);
            }
        }
    }

    private void setItemTextColor (View view, boolean focused) {
        TextView item_name = null;
        TextView item_status = null;

        View child0 = ((ViewGroup)view).getChildAt(0);
        if (child0 instanceof TextView)
            item_name = (TextView)child0;

        View child1 = ((ViewGroup)view).getChildAt(1);
        if (child1 instanceof TextView)
            item_status = (TextView)child1;

        if (focused) {
            int color_text_focused = mContext.getResources().getColor(R.color.color_text_focused);
            if (item_name != null)
                item_name.setTextColor(color_text_focused);
            if (item_status!= null)
                item_status.setTextColor(color_text_focused);
        } else {
            int color_text_item = mContext.getResources().getColor(R.color.color_text_blue_0);
            if (item_name != null)
                item_name.setTextColor(color_text_item);
            if (item_status!= null)
                item_status.setTextColor(color_text_item);
        }
    }

    private void setRootView() {
        rootView = ((ViewGroup)((Activity)mContext).findViewById(android.R.id.content));
    }

    private boolean hasNextFocusView(int dec){
        if (FocusFinder.getInstance().findNextFocus(rootView, this, dec) == null) {
            return false;
        } else {
            return true;
        }
    }

    public void cleanChoosed() {
        for (int i = 0; i < getChildCount(); i ++) {
            View view = getChildAt(i);
            view.setBackgroundResource(0);
        }
    }

    public void clearChoosed(View view) {
        view.setBackgroundResource(0);
    }

    public void setChoosed(View view) {
        view.setBackgroundResource(R.color.save_selected);
    }

    public void setListItemSelectedListener(ListItemSelectedListener l) {
        mListItemSelectedListener = l;
    }

    public interface ListItemSelectedListener {
        void onListItemSelected(View parent, int position);
    }
}

