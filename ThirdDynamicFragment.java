package com.blwy.zjh.ui.activity.reward;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.blwy.zjh.R;
import com.blwy.zjh.ZJHApplication;
import com.blwy.zjh.bridge.DynamicBean;
import com.blwy.zjh.bridge.DynamicBeanList;
import com.blwy.zjh.bridge.DynamicCommentBean;
import com.blwy.zjh.bridge.DynamicToUserBean;
import com.blwy.zjh.bridge.GoodBean;
import com.blwy.zjh.bridge.LoginJsonBean;
import com.blwy.zjh.bridge.PropertyBean;
import com.blwy.zjh.dao.PraiseDao;
import com.blwy.zjh.dao.bean.PraiseBean;
import com.blwy.zjh.imagebrowse.ImagePagerActivity;
import com.blwy.zjh.service.CallbackHandler;
import com.blwy.zjh.service.RewardAndPunishService;
import com.blwy.zjh.service.UserService;
import com.blwy.zjh.ui.activity.BaseFragment;
import com.blwy.zjh.ui.activity.property.EmployeeInfoDetailActivity;
import com.blwy.zjh.ui.view.RoundImageView;
import com.blwy.zjh.utils.CommonUtils;
import com.blwy.zjh.utils.Constants;
import com.blwy.zjh.utils.FileUtils;
import com.blwy.zjh.utils.ImageLoaderUtils;
import com.blwy.zjh.utils.LogUtils;
import com.blwy.zjh.utils.NetworkUtils;
import com.blwy.zjh.utils.ScreenUtils;
import com.blwy.zjh.utils.TimeUtils;
import com.blwy.zjh.utils.ToastUtils;
import com.easemob.util.DensityUtil;
import com.nostra13.universalimageloader.core.download.ImageDownloader.Scheme;

import java.util.ArrayList;
import java.util.List;

public class ThirdDynamicFragment extends BaseFragment {
    public static final int EXTRA_CREATE_DYNAMIC = 0x01;
    public static final int EXTRA_CREATE_COMMENT_FLOWER = 0x02;
    public static final int EXTRA_CREATE_COMMENT_KNIFE = 0x03;
    public static final int TYPE_NEWEST = 0x04;
    public static final int TYPE_HOTEST = 0x05;
    private static final String THUMP_FILE_SUFFIX = "_thumb";
    public static final Integer PAGE_LIMIT = 15;
    private static final String TYPE = "type";

    private ThirdDynamicMainActivity mContext;
    private LayoutInflater mInflater;
    private View mView;
    private ListView mListView;
    private LinearLayout mTouchReload;
    private List<DynamicBean> mDynamicList = new ArrayList<DynamicBean>();
    private DynamicAdapter mAdapter;
    private Long mUserID;
    private Long mVillageID;
    private int mCurrentItem;
    private boolean mIsPullDown = false;
    private boolean mIsHot = false;
    private boolean mIsVisible = false;
    private boolean mHasLoadedOnce = false;
    private boolean mInited = false;
    private boolean mIsListViewReady=false;


    public boolean isListViewReady(){
        return mIsListViewReady;
    }

    public static ThirdDynamicFragment newInstance(boolean isHot) {
        ThirdDynamicFragment instance = new ThirdDynamicFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(TYPE, isHot);
        instance.setArguments(bundle);
        return instance;
    }

    public ThirdDynamicFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.new_fragment_reward_dynamic, null, false);
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        initData();
        initListener();
        loadDataFromServer();
    }

    private void loadDataFromServer() {
        if (!mInited || !mIsVisible) {
            return;
        }
        mHasLoadedOnce = true;
        showLoadingDialog();
        loadData(0L, mVillageID, 0L);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser && !mHasLoadedOnce) {
            mIsVisible = true;
            loadDataFromServer();
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    private void initView() {
        mListView = (ListView) mView.findViewById(R.id.pr_dynamic_listview);
        mTouchReload = (LinearLayout) mView.findViewById(R.id.dynamic_listview_empty);
        //绑定
        mIsListViewReady=true;
    }

    private void initData() {
        mIsHot = getArguments().getBoolean(TYPE);
        LoginJsonBean user = ZJHApplication.getInstance().getLoginInfo();
        if (user != null) {
            mUserID = user.getUserID();
        }
        mContext = (ThirdDynamicMainActivity) getActivity();
        mInflater = LayoutInflater.from(mContext);
        mAdapter = new DynamicAdapter();
        mListView.setAdapter(mAdapter);

        List<PropertyBean> mPropertyList = UserService.getInstance().getUserVillageList();
        if (mPropertyList != null && mPropertyList.size() > 0) {
            PropertyBean bean = mPropertyList.get(0);
            if (bean != null) {
                mVillageID = bean.getVillageID() == null ? -1L : bean.getVillageID();
            }
        }
        mInited = true;
        mIsPullDown=true;
//       loadData(0L, mVillageID);
    }

    private void initListener() {



        //mListView.setMode(Mode.PULL_FROM_END);
//        mListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {
//
//            @Override
//            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
//
//                mIsPullDown = true;
//                loadData(0L, mVillageID, 0L);
//            }
//
//            @Override
//            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
//                mIsPullDown = false;
//                int size = mDynamicList.size();
//                if (size > 0) {
//                    DynamicBean bean = mDynamicList.get(size - 1);
//                    if (bean != null) {
//                        LogUtils.e("initListener", "----------------initListener="+size);
//                        loadData(bean.create_time, mVillageID, (long) size);
//                    }
//                }
//            }});

        // 滑动时暂停加载图片
        //mListView.getRefreshableView().setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));

        // 加载失败时点击重新加载

        mTouchReload.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dismissEmptyView();
//                if (!mListView.isRefreshing()) {
//                    mListView.autoRefresh();
//                }
            }
        });
    }
    /**
     *
     * @param create_time 创建时间
     * @param villageID 小区ID
     * @param pageSize  请求数据开始位置
     */
    public void loadData(Long create_time, Long villageID,Long pageSize) {
        if (!NetworkUtils.isNetworkConnected(mContext)) {
            dismissLoadingDialog();
            mListView.postDelayed(new Runnable() {
                @Override
                public void run() {
//                    mListView.onRefreshComplete();
                }
            }, 2000);
            ToastUtils.show(mContext, R.string.no_available_network);
            return;
        }
        RewardAndPunishService.getDynamicList(mUserID, villageID, pageSize, create_time, PAGE_LIMIT, mIsHot, new CallbackHandler(new Handler()) {
            @Override
            public void callbackOnHandler(CallbackInfo<?> info) {
                if (info == null || getActivity() == null) {
                    showEmptyView();
                    return;
                }
                if (mIsPullDown) {
                    mDynamicList.clear();
                    mAdapter.notifyDataSetChanged();
                }
                if (info.bError) {
                    showEmptyView();
                    ToastUtils.show(mContext, info.errorMsg);
                } else {
                    DynamicBeanList dynamicBeanList = (DynamicBeanList) info.mt;
                    List<DynamicBean> tempList = new ArrayList<DynamicBean>();
                    if (dynamicBeanList != null && dynamicBeanList.getRows() != null) {
                        tempList = dynamicBeanList.getRows();
                        if (tempList != null) {
                            mDynamicList.addAll(tempList);
                        }
                    }

                    mAdapter.notifyDataSetChanged();
//                    if (tempList != null && tempList.size() < PAGE_LIMIT) {
//                        mListView.setMode(Mode.DISABLED);
//                    } else {
//                        mListView.setMode(Mode.PULL_FROM_END);
//                    }
                }

//                mListView.onRefreshComplete();
                dismissLoadingDialog();

                if (mDynamicList.size() <= 0) {
                    showEmptyView();
                } else {
                    dismissEmptyView();
                }

            }
        });
    }

    public static class ViewHolder {
        public ImageView mSenderAvatorIv;
        public TextView mSenderNameTv;;
        public TextView mCreateTimeTv;
        public TextView mContentTv;
        public LinearLayout praiseLayout;
        public TextView praiseTv;
        public TextView mFlowerNumTv;
        public TextView mKnifeNumTv;
        public TextView mPraiseNumTv;

        public RoundImageView mRewardPunishIv;
        public List<ImageView> mImageList = new ArrayList<ImageView>();
        public LinearLayout mToUserAvatorContainer0;
        public LinearLayout mToUserAvatorContainer1;
        public LinearLayout mCommentContainer;
        public LinearLayout praisecommentContainer;
        public DynamicBean mCommentEntity;
    }

    public class DynamicAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mDynamicList == null ? 0 : mDynamicList.size();
        }

        @Override
        public DynamicBean getItem(int position) {
            return mDynamicList == null ? null : mDynamicList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            DynamicBean bean = mDynamicList.get(position);
            return bean.images == null ? 0 : bean.images.size();
        }

        @Override
        public int getViewTypeCount() {
            return 10;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            DynamicBean bean = mDynamicList.get(position);
            if (bean == null) {
                return convertView;
            }
            List<String> imageList = bean.images;
            int imageNum = imageList == null ? 0 : imageList.size();
            int imageSize = (imageNum >= 9 ? 9 : imageNum);
            if (convertView == null) {
                int layoutResId = mContext.getResources().getIdentifier("listitem_dynamic_image" + imageSize,
                        "layout", mContext.getPackageName());
                convertView = mInflater.inflate(layoutResId, null);
                viewHolder = new ViewHolder();
                viewHolder.mSenderAvatorIv = (ImageView) convertView.findViewById(R.id.iv_sender_avator);
                viewHolder.mSenderNameTv = (TextView) convertView.findViewById(R.id.tv_sender);
                viewHolder.mCreateTimeTv = (TextView) convertView.findViewById(R.id.tv_create_time);
                viewHolder.mContentTv = (TextView) convertView.findViewById(R.id.tv_content);
                viewHolder.praiseLayout = (LinearLayout) convertView.findViewById(R.id.ll_praise_layout);
                viewHolder.praiseTv = (TextView) convertView.findViewById(R.id.tv_praise_persons);
                viewHolder.mToUserAvatorContainer0 = (LinearLayout) convertView
                        .findViewById(R.id.ll_to_user_container0);
                viewHolder.mToUserAvatorContainer1 = (LinearLayout) convertView
                        .findViewById(R.id.ll_to_user_container1);
                viewHolder.mFlowerNumTv = (TextView) convertView.findViewById(R.id.tv_flowers);
                viewHolder.mKnifeNumTv = (TextView) convertView.findViewById(R.id.tv_penalty);
                viewHolder.mPraiseNumTv = (TextView) convertView.findViewById(R.id.tv_praise);
                viewHolder.mRewardPunishIv = (RoundImageView) convertView.findViewById(R.id.tv_reward_punish);
                viewHolder.mCommentContainer = (LinearLayout) convertView
                        .findViewById(R.id.comment_container);
                viewHolder.praisecommentContainer = (LinearLayout) convertView
                        .findViewById(R.id.praise_and_comment_container);
                for (int i = 0; i < imageSize; i++) {
                    int imageResId = getResources().getIdentifier("image" + i, "id",
                            mContext.getPackageName());
                    ImageView iv = (ImageView) convertView.findViewById(imageResId);
                    viewHolder.mImageList.add(iv);
                }
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            setSenderInfo(viewHolder, bean);
            setPraise(viewHolder.praiseLayout, viewHolder.praiseTv, bean.goods);
            setComments(viewHolder, bean.comments, bean.goods);
            setToUsers(viewHolder, bean.to_users);
            setImages(viewHolder, imageSize, bean);
            setBottomBar(viewHolder, bean);
            setListeners(viewHolder, bean, position);
            return convertView;
        }
    }

    private String getAnonymousName(String userName) {
        String name = userName == null ? "" : userName;
        int length = name.length();
        StringBuilder sb = new StringBuilder();
        if (length <= 0) {
            sb.append("**** (匿名)");
        } else if (length > 0 && length <= 2) {
            sb.append(name.substring(0, 1)).append("**** (匿名)");
        } else {
            sb.append(name.substring(0, 1)).append("***").append(name.substring(name.length() - 1))
                    .append(" (匿名)");
        }
        return sb.toString();
    }

    private void setSenderInfo(ViewHolder viewHolder, DynamicBean bean) {
        String headUrl = bean.userPhoto;
        if (bean.praise_negative == 0) {
            viewHolder.mRewardPunishIv.setImageResource(R.drawable.reward);
        } else {
            viewHolder.mRewardPunishIv.setImageResource(R.drawable.penalty);
        }
        Integer anonymou = (bean.anonymous == null ? 0 : bean.anonymous);
        boolean isanonymous = (anonymou == 1 ? true : false);
        if (!isanonymous) {
            ImageLoaderUtils.showImage(headUrl, viewHolder.mSenderAvatorIv, R.drawable.default_headicon);
            viewHolder.mSenderNameTv.setText(bean.nickname);
        } else {
            try {
                viewHolder.mSenderAvatorIv.setImageResource(R.drawable.default_headicon);
                viewHolder.mSenderNameTv.setText(getAnonymousName(bean.nickname));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setPraise(LinearLayout layout, TextView tv, List<GoodBean> goods) {
        if (goods != null && goods.size() > 0) {
            layout.setVisibility(View.VISIBLE);
            List<GoodBean> goodsPerson = goods;
            StringBuilder builder = new StringBuilder();
            for (GoodBean person : goodsPerson) {
                String name = person.getNickname() == null ? "匿名用户" : person.getNickname();
                builder.append(name + "，");
            }
            builder.deleteCharAt(builder.length() - 1);
            tv.setText(builder);
        } else {
            layout.setVisibility(View.GONE);
        }
    }

    private void setComments(ViewHolder viewHolder, List<DynamicCommentBean> beans, List<GoodBean> goods) {
        viewHolder.mCommentContainer.removeAllViews();
        if (goods != null && goods.size() > 0) {
            if (beans != null && beans.size() > 0) {
                View divierView = new View(mContext);
                divierView.setBackgroundColor(Color.parseColor("#dedede"));
                LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, DensityUtil.dip2px(mContext,
                                0.5F));
                viewHolder.mCommentContainer.addView(divierView, dividerParams);
            }
        }
        if (beans != null) {
            for (int i = 0; i < beans.size(); i++) {
                DynamicCommentBean bean = beans.get(i);
                if (bean == null) {
                    continue;
                }
                View view = mInflater.inflate(R.layout.listitem_dynamic_list_comment, null);
                TextView textview = (TextView) view.findViewById(R.id.tv_sender);

                Integer anonymou = (bean.anonymous == null ? 0 : bean.anonymous);
                boolean isanonymous = (anonymou == 1 ? true : false);
                if (!isanonymous) {
                    textview.setText(bean.nickname);
                } else {
                    textview.setText(getAnonymousName(bean.nickname));
                }
                // textview = (TextView)view.findViewById(R.id.tv_create_time);
                // textview.setText(TimeUtils.formatLogicTime2(bean.getCreate_time()
                // * 1000));
                textview = (TextView) view.findViewById(R.id.tv_flower_or_knife_num);
                if (bean.flower_num > 0) {
                    String flowerStr = mContext.getResources().getString(R.string.send_number_flowers);
                    textview.setText(String.format(flowerStr, bean.flower_num));
                } else if (bean.knife_num > 0) {
                    String hammerStr = mContext.getResources().getString(R.string.give_number_hammer);
                    textview.setText(String.format(hammerStr, bean.knife_num));
                }
                textview = (TextView) view.findViewById(R.id.tv_comment);
                String content = TextUtils.isEmpty(bean.content) ? "" : bean.content.trim();
                if (!TextUtils.isEmpty(content)) {
                    textview.setText(content);
                } else {
                    textview.setVisibility(View.GONE);
                }
                viewHolder.mCommentContainer.addView(view);
                if (i != (beans.size() - 1)) {
                    View divierView = new View(mContext);
                    divierView.setBackgroundColor(Color.parseColor("#dedede"));
                    LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, DensityUtil.dip2px(
                                    mContext, 0.5F));
                    viewHolder.mCommentContainer.addView(divierView, dividerParams);
                }
            }
        }
        int goodssize = goods == null ? 0 : goods.size();
        int beanssize = beans == null ? 0 : beans.size();
        if (goodssize <= 0 && beanssize <= 0) {
            viewHolder.praisecommentContainer.setVisibility(View.GONE);
        } else {
            viewHolder.praisecommentContainer.setVisibility(View.VISIBLE);
        }
    }

    private void setToUsers(ViewHolder viewHolder, List<DynamicToUserBean> toUserList) {
        viewHolder.mToUserAvatorContainer0.removeAllViews();
        viewHolder.mToUserAvatorContainer1.removeAllViews();
        if (toUserList != null) {
            for (DynamicToUserBean toUser : toUserList) {
                if (toUser == null) {
                    continue;
                }
                final Long userID = toUser.getUserID();
                ImageView imageView = new RoundImageView(mContext);
                imageView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        openWorkerInfoPage(userID);
                    }
                });
                ImageLoaderUtils.showImage(toUser.userPhoto, imageView, R.drawable.default_headicon);
                int size = ScreenUtils.dip2px(mContext, 30);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                params.leftMargin = ScreenUtils.dip2px(mContext, 7);
                if (viewHolder.mToUserAvatorContainer0.getChildCount() < 5) {
                    viewHolder.mToUserAvatorContainer1.setVisibility(View.GONE);
                    viewHolder.mToUserAvatorContainer0.addView(imageView, params);
                } else {
                    viewHolder.mToUserAvatorContainer1.setVisibility(View.VISIBLE);
                    viewHolder.mToUserAvatorContainer1.addView(imageView, params);
                }
            }
        }
    }

    private void setImages(ViewHolder viewHolder, int imageSize, DynamicBean bean) {
        List<String> imageList = bean.getImages();
        final List<String> urlList = new ArrayList<String>();
        for (int i = 0; i < imageSize; i++) {
            final int j = i;
            ImageView iv = viewHolder.mImageList.get(i);
            String imageUrl = imageList.get(i);
            String originalUrl = "";
            if ("''".equals(imageUrl)) {
                Long messageID = bean.getId();
                Long senderID = bean.getSender_id();
                if (mUserID.equals(senderID)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(Constants.FILE.UPLOAD_CACHE).append("/").append(messageID).append("/image")
                            .append(i).append(".jpg");
                    String cacheKey = sb.toString().trim();
                    String cacheFileName = Scheme.FILE.wrap(Constants.FILE.UPLOAD_IMAGE_CACHE + "/"
                            + FileUtils.generateFileName(cacheKey));
                    imageUrl = cacheFileName;
                }
            }
//            if (!TextUtils.isEmpty(imageUrl)) {
//                if (imageUrl.contains(THUMP_FILE_SUFFIX)) {
//                    originalUrl = imageUrl.replace(THUMP_FILE_SUFFIX, "");
//                }
//            } else {
//                originalUrl = imageUrl;
//            }

            if (!TextUtils.isEmpty(imageUrl) && imageUrl.contains(THUMP_FILE_SUFFIX)) {
                originalUrl = imageUrl.replace(THUMP_FILE_SUFFIX, "");
            } else {
                originalUrl = imageUrl;
            }
            ImageLoaderUtils.showImage(imageUrl, iv, R.drawable.bg_grey);
            urlList.add(originalUrl);
            iv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    String[] urls = new String[urlList.size()];
                    urlList.toArray(urls);
                    startImageBrower(j, urls);
                }
            });
        }
    }

    private void setBottomBar(ViewHolder viewHolder, DynamicBean bean) {
        viewHolder.mCreateTimeTv.setText(TimeUtils.formatLogicTime2(bean.create_time * 1000));
        String content = bean.content == null ? "" : bean.content.trim();
        if (!TextUtils.isEmpty(content)) {
            viewHolder.mContentTv.setVisibility(View.VISIBLE);
            viewHolder.mContentTv.setText(content);
        } else {
            viewHolder.mContentTv.setVisibility(View.GONE);
        }
        viewHolder.mFlowerNumTv.setText(CommonUtils.formatNumber(bean.flower_num));
        viewHolder.mKnifeNumTv.setText(CommonUtils.formatNumber(bean.knife_num));
        viewHolder.mPraiseNumTv.setText(CommonUtils.formatNumber(bean.good_num ));
        if (bean.isPraise) {
            viewHolder.mPraiseNumTv.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.btn_good_zambia_selected, 0, 0, 0);
            viewHolder.mPraiseNumTv.setClickable(false);
        } else {
            viewHolder.mPraiseNumTv.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.btn_praise_hand_normal, 0, 0, 0);
            viewHolder.mPraiseNumTv.setClickable(true);
        }
    }

    private void setListeners(final ViewHolder viewHolder, final DynamicBean bean, final int position) {
        viewHolder.mFlowerNumTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentItem = position;
                Intent intent = new Intent(mContext, PraiseFlowerAndMoneyActivity.class);
                intent.putExtra(PraiseFlowerAndMoneyActivity.EXTRA_IS_PRAISE, true);
                intent.putExtra(PraiseFlowerAndMoneyActivity.EXTRA_COMMENT, true);
                intent.putExtra(PraiseFlowerAndMoneyActivity.EXTRA_PERSON_NUM, bean.to_users == null ? 0
                        : bean.to_users.size());
                intent.putExtra(PraiseFlowerAndMoneyActivity.EXTRA_MESSAGE_ID, bean.id);
                startActivityForResult(intent, EXTRA_CREATE_COMMENT_FLOWER);
            }
        });
        viewHolder.mKnifeNumTv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mCurrentItem = position;
                Intent intent = new Intent(mContext, PraiseFlowerAndMoneyActivity.class);
                intent.putExtra(PraiseFlowerAndMoneyActivity.EXTRA_IS_PRAISE, false);
                intent.putExtra(PraiseFlowerAndMoneyActivity.EXTRA_COMMENT, true);
                intent.putExtra(PraiseFlowerAndMoneyActivity.EXTRA_PERSON_NUM, bean.to_users == null ? 0
                        : bean.to_users.size());
                intent.putExtra(PraiseFlowerAndMoneyActivity.EXTRA_MESSAGE_ID, bean.id);
                startActivityForResult(intent, EXTRA_CREATE_COMMENT_KNIFE);
            }
        });

        viewHolder.mPraiseNumTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickPraise(bean, position);
                viewHolder.mPraiseNumTv.setClickable(false);
            }
        });

        viewHolder.mSenderAvatorIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickUserAvator(bean);
            }
        });
    }

    public void openWorkerInfoPage(Long userID) {
        Intent intent = new Intent(mContext, EmployeeInfoDetailActivity.class);
        if (userID != null) {
            intent.putExtra(EmployeeInfoDetailActivity.USERID_EMPLOYEE, String.valueOf(userID));
        }
        mContext.startActivity(intent);
    }

    private void startImageBrower(int position, String[] urls) {
        Intent intent = new Intent(mContext, ImagePagerActivity.class);
        intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_URLS, urls);
        intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_INDEX, position);
        mContext.startActivity(intent);
    }

    private void clickPraise(final DynamicBean dynamicBean, final int position) {
        final LoginJsonBean loginBean = ZJHApplication.getInstance().getLoginInfo();
        if (loginBean == null) {
            return;
        }
        if (dynamicBean == null) {
            return;
        }
        try {
            PraiseBean praise = PraiseDao.getInstance().query(loginBean.userID, dynamicBean.id,
                    PraiseBean.TYPE_DYNAMIC_LIST);
            if (praise == null) {
                dynamicBean.good_num++;
                dynamicBean.isPraise = true;
                mAdapter.notifyDataSetChanged();
                RewardAndPunishService.giveGood(dynamicBean.id, new CallbackHandler(new Handler()) {
                    @Override
                    public void callbackOnHandler(CallbackInfo<?> info) {
                        if (info == null || getActivity() == null) {
                            return;
                        }
                        if (info != null && !info.bError) {
                            PraiseBean p = new PraiseBean(loginBean.userID, dynamicBean.id,
                                    PraiseBean.TYPE_DYNAMIC_LIST);
                            try {
                                PraiseDao.getInstance().insertOrUpdate(p);
                                refreshPage(dynamicBean, position);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshPage(DynamicBean dynamicBean, final int refreshItem) {
        RewardAndPunishService.getSingle(dynamicBean.id, new CallbackHandler(new Handler()) {

            @Override
            public void callbackOnHandler(CallbackInfo<?> info) {
                if (info == null || getActivity() == null) {
                    return;
                }
                if (info != null && !info.bError) {
                    DynamicBean bean = (DynamicBean) info.mt;
                    if (bean != null) {
                        mDynamicList.set(refreshItem, bean);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    private void clickUserAvator(DynamicBean dynamicBean) {
        if (dynamicBean == null) {
            return;
        }
        Long senderID = dynamicBean.getSender_id();
        Long villageID = dynamicBean.getVillage_id();
        String senderName = dynamicBean.getNickname();
        Integer anonymous = dynamicBean.anonymous == null ? 0 : dynamicBean.anonymous;
        boolean isAnonymous = anonymous == 1 ? true : false;
        if (mUserID != null) {
            if (!senderID.equals(mUserID)) {
                if (!isAnonymous) {
                    gotoPersonDynamicHistoryPage(senderID, villageID, senderName);
                } else {
                    ToastUtils.show(mContext, R.string.can_not_review_anonymous_user, Toast.LENGTH_LONG);
                }
            } else {
                gotoPersonDynamicHistoryPage(senderID, villageID, senderName);
            }
        }
    }

    private void gotoPersonDynamicHistoryPage(Long senderID, Long villageID, String senderName) {
        Intent intent = new Intent(mContext, PersonDynamicHistoryActivity.class);
        Bundle b = new Bundle();
        if (senderID != null) {
            b.putLong(PersonDynamicHistoryActivity.SENDER_ID, senderID);
        }
        if (villageID != null) {
            b.putLong(PersonDynamicHistoryActivity.VILLAGE_ID, villageID);
        }
        if (senderName != null) {
            b.putString(PersonDynamicHistoryActivity.SENDER_NAME, senderName);
        }
        intent.putExtra(PersonDynamicHistoryActivity.PERSONBEAN, b);
        mContext.startActivity(intent);
    }

    public void refreshCurrentPage() {
        DynamicBean bean = mDynamicList.get(mCurrentItem);
        if (bean != null) {
            refreshPage(bean, mCurrentItem);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case EXTRA_CREATE_COMMENT_FLOWER:
                if (resultCode != Activity.RESULT_OK) {
                    return;
                }
                if (mDynamicList != null) {
                    DynamicBean bean = mDynamicList.get(mCurrentItem);
                    if (bean != null) {
                        refreshPage(bean, mCurrentItem);
                    }
                }
                break;
            case EXTRA_CREATE_COMMENT_KNIFE:
                if (resultCode != Activity.RESULT_OK) {
                    return;
                }
                if (mDynamicList != null) {
                    DynamicBean bean = mDynamicList.get(mCurrentItem);
                    if (bean != null) {
                        refreshPage(bean, mCurrentItem);
                    }
                }
                break;
            default:
                break;
        }
    }

    public void switchVillage(Long villageID){
        if (villageID == null) {
            return;
        }
        this.mVillageID = villageID;
        refresh();
    }
    
    public void refresh() {
        mListView.setSelection(0);
        LogUtils.tagKevin("refresh中的listView:" + mListView.toString());
        // mListView.autoRefresh();
    }
    
    private void showEmptyView() {
        mTouchReload.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
    }

    private void dismissEmptyView() {
        mTouchReload.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
    }

    public ListView getFragmentListView(){
        return mListView;
    }


}
