package com.blwy.zjh.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * Created by wujinlong on 16/3/14.
 */
public class ControlScrollListView extends ListView implements AbsListView.OnScrollListener {

    private boolean stopScroll;

    public void stopScroll(boolean flag){
        stopScroll=flag;
    }



    public ControlScrollListView(Context context) {
        super(context);
    }

    public ControlScrollListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ControlScrollListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if(ev.getAction()==MotionEvent.ACTION_MOVE) {

            if(stopScroll){
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }
}
