package com.blwy.zjh.ui.view.rewardtouchlinearlayout;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.blwy.zjh.R;
import com.blwy.zjh.ui.view.ControlScrollListView;
import com.blwy.zjh.utils.CommonUtils;
import com.blwy.zjh.utils.Constants;
import com.blwy.zjh.utils.LogUtils;

/**
 * Created by wujinlong on 16/3/15.
 */
public class TouchFrameLayout extends FrameLayout {
    public TouchFrameLayout(Context context) {
        super(context);
    }

    public TouchFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public TouchFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    private float originX;
    private float originY;

    private boolean isPullingNow = false;


    private static final int PULL_DOWN_REFRESH = 1;
    private static final int RELREAS_REFRESH = 2;
    private static final int REFRESHING = 3;

    private ImageView mSun;
    private ImageView mCloud;
    private ImageView mHouse;
    private TextView mHint;


//    private int sunLeft;
//    private int sunBottom;
//    private int sunRefreshBottom;
//    int bigSunSize;
//    int smallSunSize;

    int houseHeight;
    int houseWidth;
    int currentState = PULL_DOWN_REFRESH;//默认下拉刷新状态


    Animation rotateAnim;
    Animation sunUpAnima;
    Animation sunDescent;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {


        float absY = Math.abs(mTarget.getScrollY());

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:

                originX = ev.getX();
                originY = ev.getY();


                break;
            case MotionEvent.ACTION_UP:


                if (isPullingNow && currentState == PULL_DOWN_REFRESH) {

                    resetListView();

                } else if (isPullingNow && currentState == RELREAS_REFRESH) {
                    //由放开刷新 进入刷新
                    currentState = REFRESHING;

                    refreshUI();

                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            currentState = PULL_DOWN_REFRESH;
                            refreshUI();

                        }
                    }, 4000);


                }
                break;
            case MotionEvent.ACTION_MOVE:

                float moveX = ev.getX();
                float moveY = ev.getY();

                float diffX = moveX - originX;
                float diffY = moveY - originY;

                if (Math.abs(diffX) > Math.abs(diffY)) {

                    if (isPullingNow) {
                        //左右滑动的时候 事件要屏蔽掉
                        return true;
                    }
                    return super.dispatchTouchEvent(ev);
                }

                if (currentState == REFRESHING) {
                    return super.dispatchTouchEvent(ev);
                }

                //动态距离显示,与普通ui不同

                if (mFirstVisibleItem == 0 && diffY > 0 && !canChildScrollUp()) {

                    isPullingNow = true;
                    mListView.stopScroll(true);

                    if (absY <= maxHeight) {

                        if (diffY >= 15) {
                            diffY = 15;
                        }

                        mTarget.scrollBy(0, -(int) diffY);

                        LogUtils.tagKevin("滑动的距离" + diffY);

                        if (absY < mearsureHeight && currentState == RELREAS_REFRESH) {
                            //如果拉得不够长,切换变成 一般状态
                            currentState = PULL_DOWN_REFRESH;
                            mHint.setText("向下拉");

                           if( mSun.getAnimation()!=rotateAnim){
                               //首先得旋转
                               startSunAnim(SunAnimType.ROTATE);
                           }



                        } else if (absY > mearsureHeight && currentState == PULL_DOWN_REFRESH) {
                            //如果拉得够长,切换变成  放开刷新状态
                            currentState = RELREAS_REFRESH;
                            mHint.setText("放开刷新");
                            if( mSun.getAnimation()!=sunUpAnima){
                                //太阳升起
                                startSunAnim(SunAnimType.UP);

                            }


                        }

                        //云和太阳
                        mCloud.setPadding((int) absY, 0, 0, 0);
//                        changeImageViewMargin(mSun, sunLeft, 0, 0, sunBottom + (int) (absY / 2));

//                        changeImageSun(absY);
                        //房子
                        setHouseScale(absY);

                        originX = moveX;
                        originY = moveY;

                        printXY();


                    }

                    return true;


                } else if (mFirstVisibleItem == 0 && diffY < 0) {

                    if (absY < 50) {
                        resetListView();

                    }

                    if (isPullingNow) {

                        if (diffY < -15) {
                            diffY = -15;
                        }

                        if (absY < mearsureHeight && currentState == RELREAS_REFRESH) {
                            //如果拉得不够长,切换变成 一般状态
                            currentState = PULL_DOWN_REFRESH;
                            mHint.setText("向下拉");

                            if(mSun.getAnimation()!=sunDescent){
                                startSunAnim(SunAnimType.DESCENT);
                            }

                        } else if (absY > mearsureHeight && currentState == PULL_DOWN_REFRESH) {
                            //如果拉得够长,切换变成  放开刷新状态
                            currentState = RELREAS_REFRESH;
                            mHint.setText("放开刷新");

                            if(mSun.getAnimation()!=rotateAnim){
                                startSunAnim(SunAnimType.ROTATE);
                            }

                        }


                        mTarget.scrollBy(0, -(int) diffY);

                        //云和太阳
                        mCloud.setPadding((int) absY, 0, 0, 0);
//                        changeImageViewMargin(mSun, sunLeft, 0, 0, sunBottom + (int) (absY / 2));

//                        changeImageSun(absY);
                        //房子
                        setHouseScale(absY);

                    }

                    originX = moveX;
                    originY = moveY;


                    printXY();

                } else {


                }


                break;
        }

        return super.dispatchTouchEvent(ev);
    }

//    private void changeImageSun(float absY) {
//        FrameLayout.LayoutParams frameLayoutParams = (LayoutParams) mSun.getLayoutParams();
//        int newSize = bigSunSize - (int) ((absY / mearsureHeight) * (float) (bigSunSize - smallSunSize));
//        frameLayoutParams.height = newSize;
//        frameLayoutParams.width = newSize;
//
//        mSun.setLayoutParams(frameLayoutParams);
//
//    }

    private void setHouseScale(float absY) {

        mHouse.scrollTo((int) (absY * 0.3), 0);

        float scale = 1 + (absY / mearsureHeight) * 0.2f;
        float newWidth = (float) houseWidth * scale;
        float newHeight = (float) houseHeight * scale;

        LogUtils.tagKevin("newWidth" + newWidth);
        LogUtils.tagKevin("newHeight" + newHeight);

        FrameLayout.LayoutParams layoutParams = (LayoutParams) mHouse.getLayoutParams();
        layoutParams.height = (int) newHeight;
        layoutParams.width = (int) newWidth;
        mHouse.setLayoutParams(layoutParams);
    }


    private void resetListView() {
        mTarget.scrollTo(0, 0);
        isPullingNow = false;
        mListView.stopScroll(false);
    }


    private boolean canChildScrollUp() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mListView instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mListView;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return mListView.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mListView, -1);
        }
    }

    private TouchFrameLayoutRefreshingListener mListener;


    public interface TouchFrameLayoutRefreshingListener {
        void onRefreshing();
    }

    /**
     * 提供给外部使用的方法 设置监听器
     *
     * @param listener
     */
    public void setTouchLinearLayoutRefreshingListener(TouchFrameLayoutRefreshingListener listener) {
        mListener = listener;
    }


    /**
     * 回调中使用的方法,标识刷新结束
     */
    public void refreshingComplete() {
        currentState = PULL_DOWN_REFRESH;
        refreshUI();
    }


    private ControlScrollListView mListView;
    private LinearLayout mTarget;
    private int mFirstVisibleItem = -1;

    /**
     * 提供给子类绑定,必须调用,不然报错
     *
     * @param listView
     */
    public void attachListView(ListView listView) {

        if (listView == null) {
            throw new RuntimeException("touchlinear must attatch a listview");
        }
        mListView = (ControlScrollListView) listView;


        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {


            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                mFirstVisibleItem = firstVisibleItem;

            }
        });
    }

    /**
     * 绑定刷新件
     *
     * @param target
     */
    public void attachTargetLinearLayout(LinearLayout target) {
        mTarget = target;
        printXY();

        int scaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        LogUtils.tagKevin("scaledTouchSlop" + scaledTouchSlop);

    }


    private void printXY() {
        LogUtils.tagKevin("mTarget.getScrollY()" + mTarget.getScrollY());
    }

    private FrameLayout mRefreshingHeader;
    int mearsureHeight;
    int maxHeight;

    /**
     * 绑定刷新头
     *
     * @param refreshingHeader
     */
    public void attachRefreshHeader(FrameLayout refreshingHeader) {
        mRefreshingHeader = refreshingHeader;


        mearsureHeight = CommonUtils.dip2px(getContext(), 100);
        maxHeight = CommonUtils.dip2px(getContext(), 120);


        mSun = (ImageView) mRefreshingHeader.findViewById(R.id.header_reward_sun);
        mCloud = (ImageView) mRefreshingHeader.findViewById(R.id.header_reward_cloud);
        mHouse = (ImageView) mRefreshingHeader.findViewById(R.id.header_reward_house);
        mHint = (TextView) mRefreshingHeader.findViewById(R.id.header_reward_text);
        mHint.setVisibility(Constants.IS_TEST_ENV ? VISIBLE : INVISIBLE);

        ViewGroup.LayoutParams layoutParams = mHouse.getLayoutParams();

        houseHeight = layoutParams.height;
        houseWidth = getResources().getDisplayMetrics().widthPixels;


//        sunLeft = CommonUtils.dip2px(getContext(), 20);
//
//        sunBottom = CommonUtils.dip2px(getContext(), 15);
//        sunRefreshBottom = CommonUtils.dip2px(getContext(), 85);
//
//        bigSunSize = CommonUtils.dip2px(getContext(), 55);
//
//        smallSunSize = CommonUtils.dip2px(getContext(), 25);

        LogUtils.tagKevin("mearsureHeight" + mearsureHeight);
        LogUtils.tagKevin("maxHeight" + maxHeight);


        initAnim();
    }

    private void initAnim() {
        rotateAnim = AnimationUtils.loadAnimation(getContext(), R.anim.sun_rotate);
        sunUpAnima = AnimationUtils.loadAnimation(getContext(), R.anim.sun_rise_up);
        sunDescent = AnimationUtils.loadAnimation(getContext(), R.anim.sun_descent);


    }

    private enum SunAnimType{
        ROTATE,UP,DESCENT
    }

    private void startSunAnim(SunAnimType type){

        mSun.clearAnimation();
        switch (type){
            case ROTATE:

                mSun.startAnimation(rotateAnim);
                break;
            case UP:
                mSun.startAnimation(sunUpAnima);

                break;
            case DESCENT:
                mSun.startAnimation(sunDescent);

                break;
        }
    }


    private void refreshUI() {

        switch (currentState) {
            case REFRESHING://正在刷新的ui

                //listview不让滑动
                mListView.stopScroll(true);
                mHint.setText("刷新啦");

                //界面更新
                mTarget.scrollTo(0, -maxHeight);


                //太阳转
                //startSunAnim(SunAnimType.ROTATE);

                //太阳和云的距离
                mCloud.setPadding(300, 0, 0, 0);


                //changeImageSun(mearsureHeight);
                //changeImageViewMargin(mSun, sunLeft, 0, 0, sunRefreshBottom);

                break;
            case PULL_DOWN_REFRESH:

                resetListView();
                mHint.setText("向下拉");
                //太阳不转
                mSun.clearAnimation();

                //太阳和云的距离
                mCloud.setPadding(0, 0, 0, 0);
               // changeImageSun(0);
               // changeImageViewMargin(mSun, sunLeft, 0, 0, sunBottom);

                setHouseScale(0);

                break;
            case RELREAS_REFRESH:

                break;
        }


    }

    private void changeImageViewMargin(ImageView iv, int left, int top, int right, int bottom) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) iv.getLayoutParams();
        layoutParams.setMargins(left, top, right, bottom);
        iv.setLayoutParams(layoutParams);


    }
}
