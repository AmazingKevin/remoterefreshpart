package com.blwy.zjh.ui.activity.reward;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.blwy.zjh.R;
import com.blwy.zjh.bridge.PropertyBean;
import com.blwy.zjh.http.ThreadPoolUtils;
import com.blwy.zjh.imageselect.MultiSelectImageActivity;
import com.blwy.zjh.service.UploadImageService;
import com.blwy.zjh.service.UserService;
import com.blwy.zjh.ui.activity.BaseActivity;
import com.blwy.zjh.ui.view.PhotoSelectView;
import com.blwy.zjh.ui.view.dialog.CustomListDialogFragment;
import com.blwy.zjh.ui.view.dialog.ICustomDialogListener;
import com.blwy.zjh.ui.view.dialog.SelectDialog;
import com.blwy.zjh.ui.view.rewardtouchlinearlayout.TouchFrameLayout;
import com.blwy.zjh.utils.Constants;
import com.blwy.zjh.utils.FileUtils;
import com.blwy.zjh.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 新版赏罚令首页
 * 
 * @author
 */
public class ThirdDynamicMainActivity extends BaseActivity {

    public static final int TAKE_PICTURE = 0x8549;
    public static final int SELECT_GALLERY = 0x8762;
    public static final int IMAGE_LIMIT = 9;
    public static final int EXTRA_CREATE_DYNAMIC = 4589;
    public static final Integer PAGE_LIMIT = 15;

    private List<PropertyBean> mPropertyList;
    private RadioGroup mGroup;
    private RadioButton mLeftBtn;
    private RadioButton mRightBtn;
    private ViewPager mPager;
    private SparseArray<Fragment> mFragments = new SparseArray<Fragment>();
    private ArrayList<String> mImagePaths;
    private String mCapturePath;
    private MyAdapter mAdapter;
    private Long mVillageID;
    private TouchFrameLayout mContainer;

    @Override
    protected int getContentRes() {
        return R.layout.activity_third_dynamic_new_switch_pager;
    }

    @Override
    protected void initTitle() {
        super.initTitle();
        mTitleBuilder.buildLeftBackSubTitle(R.string.rewards_and_punishments, null, R.drawable.btn_picture_camera_normal, new OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.title_center_layout:
                        showSelectVillage();
                        break;

                    case R.id.ib_right:
                        showSelectPhoto(ThirdDynamicMainActivity.this);
                        break;
                    default:
                        break;
                }
            }
        },false);
        
        mTitleBuilder.findViewById(R.id.ib_right).setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {

                gotoTextCreate();
                return false;
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
        setListeners();
        checkFailedImagesAndUpload();
    }

    private void initView() {
        mGroup = (RadioGroup) findViewById(R.id.rg_third_table_group);
        mLeftBtn = (RadioButton) findViewById(R.id.rb_frame_table_left);
        mRightBtn = (RadioButton) findViewById(R.id.rb_frame_table_right);
        mPager = (ViewPager) findViewById(R.id.vp_frame_content_pager);
        mContainer = (TouchFrameLayout) findViewById(R.id.frame_contain_dynamic_main);

        FrameLayout refreshingHeader = (FrameLayout) findViewById(R.id.framelayout_refreshing_header);
        LinearLayout mTarget = (LinearLayout) findViewById(R.id.ll_target_view);
        mContainer.attachTargetLinearLayout(mTarget);
        mContainer.attachRefreshHeader(refreshingHeader);


        mLeftBtn.setText(R.string.newest);
        mRightBtn.setText(R.string.hotest);
        mLeftBtn.setChecked(true);
    }

    private void initData() {
        mPropertyList = UserService.getInstance().getUserVillageList();
        if (mPropertyList != null && mPropertyList.size() > 0) {
            PropertyBean bean = mPropertyList.get(0);
            if (bean != null) {
                mVillageID = bean.getVillageID() == null ? -1L : bean.getVillageID();
                String mVillageName = bean.getName() == null ? "" : bean.getName();
                mTitleBuilder.setSubtitleText(mVillageName);
            }
        }

        ThirdDynamicFragment newestFragment = ThirdDynamicFragment.newInstance(false);
        ThirdDynamicFragment hotestFragment = ThirdDynamicFragment.newInstance(true);
        mFragments.put(0, newestFragment);
        mFragments.put(1, hotestFragment);

        mAdapter = new MyAdapter(getSupportFragmentManager());
        mPager.setAdapter(mAdapter);

        initFistFragmentAttachListView();

    }

    /**
     * 初始化第1个fragment的绑定
     */
    private void initFistFragmentAttachListView() {
        mPager.postDelayed(new Runnable() {
            @Override
            public void run() {
                ThirdDynamicFragment newestFragment = (ThirdDynamicFragment) mFragments.get(0);
                if (newestFragment.isListViewReady()) {
                    attachContainAndFragmentList(newestFragment.getFragmentListView());
                } else {
                    initFistFragmentAttachListView();
                }

            }
        }, 1200);
    }



    private void setListeners() {
        mGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                ThirdDynamicFragment fragment = null;

                switch (checkedId) {
                    case R.id.rb_frame_table_left:
                        mPager.setCurrentItem(0, true);
                        fragment = (ThirdDynamicFragment) mFragments.get(0);

                        break;

                    case R.id.rb_frame_table_right:
                        mPager.setCurrentItem(1, true);
                        fragment = (ThirdDynamicFragment) mFragments.get(1);

                        break;
                    default:
                        break;
                }

                 attachContainAndFragmentList(fragment.getFragmentListView());

            }
        });
        
        mPager.addOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {

                ThirdDynamicFragment fragment = null;

                switch (arg0) {
                    case 0:
                        mLeftBtn.setChecked(true);
                        fragment = (ThirdDynamicFragment) mFragments.get(0);
                        break;

                    case 1:
                        mRightBtn.setChecked(true);
                        fragment = (ThirdDynamicFragment) mFragments.get(1);
                        break;

                    default:
                        break;
                }
//                mContainer.refreshingComplete();
                 attachContainAndFragmentList(fragment.getFragmentListView());
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });




    }

    private void showSelectVillage() {

        if (mPropertyList != null && mPropertyList.size() > 0) {
            String[] villages = new String[mPropertyList.size()];
            for (int i = 0; i < mPropertyList.size(); i++) {
                villages[i] = mPropertyList.get(i).getName();
            }
            CustomListDialogFragment.show(ThirdDynamicMainActivity.this,
                    getString(R.string.select_village_to_view_reward), villages, new ICustomDialogListener() {
                        @Override
                        public void onListItemSelected(String value, int number) {
                            Long currentVillageID = mPropertyList.get(number).villageID;
                            mTitleBuilder.setSubtitleText(mPropertyList.get(number).name);
                            if (currentVillageID != null) {
                                mVillageID = currentVillageID;
                                switchVillage(mVillageID);
                            }
                        }
                    });
        } else {
            ToastUtils.show(ThirdDynamicMainActivity.this, "未获取到您的小区信息");
        }
    }
    
    public void showSelectPhoto(Activity activity) {
        final SelectDialog psd = new SelectDialog(activity);
        List<String> dataList = new ArrayList<String>();
        dataList.add(getString(R.string.take_photo));
        dataList.add(getString(R.string.select_from_gallery));
        psd.setItems(dataList);
        psd.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = null;
                switch (position) {
                    case 0:
                        psd.dismiss();
                        String state = Environment.getExternalStorageState();
                        if (Environment.MEDIA_MOUNTED.equals(state)) {
                            intent = new Intent("android.media.action.IMAGE_CAPTURE");
                            File dir = new File(Constants.FILE.CAPTURE_IMAGE_DIR);
                            if (!dir.exists()) {
                                dir.mkdirs();
                            }
                            mCapturePath = dir + "/IMG_" + System.currentTimeMillis() + ".jpg";

                            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mCapturePath)));
                            startActivityForResult(intent, TAKE_PICTURE);
                        } else {
                            ToastUtils.show(ThirdDynamicMainActivity.this, R.string.please_confirm_sdcard);
                        }
                        break;
                    case 1:
                        psd.dismiss();
                        intent = new Intent(ThirdDynamicMainActivity.this, MultiSelectImageActivity.class);
                        intent.putExtra(MultiSelectImageActivity.sExtraMaxNum, IMAGE_LIMIT);
                        startActivityForResult(intent, SELECT_GALLERY);
                        break;
                    default:
                        break;
                }
            }
        });
        psd.show();
    }

    private void gotoCreate() {
        Intent intent = new Intent(this, CreateDynamicActivity.class);
        intent.putExtra(CreateDynamicActivity.EXTRA_SELECT_IMAGE, mImagePaths);
        intent.putExtra(CreateDynamicActivity.CURRENT_VILLIAGE_ID, mVillageID);
        startActivityForResult(intent, EXTRA_CREATE_DYNAMIC);
    }

    private void gotoTextCreate() {
        Intent intent = new Intent(this, CreateDynamicActivity.class);
        intent.putExtra(CreateDynamicActivity.CURRENT_VILLIAGE_ID, mVillageID);
        startActivityForResult(intent, EXTRA_CREATE_DYNAMIC);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PhotoSelectView.SELECT_GALLERY:
                if (resultCode != RESULT_OK) {
                    return;
                }
                if (data == null) {
                    return;
                }
                mImagePaths = (ArrayList<String>) data.getSerializableExtra(MultiSelectImageActivity.sExtraResultGallery);
                gotoCreate();
                break;
            case PhotoSelectView.TAKE_PICTURE:
                if (resultCode != RESULT_OK) {
                    return;
                }
                mImagePaths = new ArrayList<String>();
                mImagePaths.add(mCapturePath);
                gotoCreate();
                break;
            case EXTRA_CREATE_DYNAMIC:
                if (resultCode != RESULT_OK) {
                    return;
                }
                refreshPage();
                break;
            default:
                break;
        }
    }

    private void switchVillage(Long villageID) {
        for (int i = 0; i < mFragments.size(); i++) {
            ((ThirdDynamicFragment) mFragments.get(i)).switchVillage(mVillageID);
        }
    }
    
    private void refreshPage() {

        ((ThirdDynamicFragment) mFragments.get(0)).refresh();

    }
    
    private void checkFailedImagesAndUpload() {
        ThreadPoolUtils.execute(new Runnable() {

            @Override
            public void run() {
                Map<String, List<File>> imageMaps = FileUtils.scanFiles(Constants.FILE.UPLOAD_CACHE);
                if (imageMaps != null) {
                    for (Map.Entry<String, List<File>> entry : imageMaps.entrySet()) {
                        try {
                            Long messageID = Long.valueOf(entry.getKey());
                            List<File> images = entry.getValue();
                            if (messageID != null && images != null && images.size() > 0) {
                                UploadImageService.uploadImage("sfl", messageID, images, null);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }
    
    private class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public Fragment getItem(int arg0) {
            return mFragments.get(arg0);
        }
    }

    @Override
      protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("filePath", mCapturePath);

        getSupportFragmentManager().putFragment(outState, "myfragment0", mFragments.get(0));
        getSupportFragmentManager().putFragment(outState,"myfragment1",mFragments.get(1));


    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (TextUtils.isEmpty(mCapturePath)) {
            mCapturePath = savedInstanceState.getString("filePath");
        }

        mFragments.setValueAt(0, getSupportFragmentManager().getFragment(savedInstanceState, "myfragment0"));
        mFragments.setValueAt(1, getSupportFragmentManager().getFragment(savedInstanceState, "myfragment1"));


    }


    void attachContainAndFragmentList(ListView listView){
        mContainer.attachListView(listView);
    }

}
