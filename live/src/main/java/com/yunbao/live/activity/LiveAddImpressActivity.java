package com.yunbao.live.activity;

// live - 用户资料 - 添加印象
// 2026-05-30 06:16

import android.text.TextUtils;
import android.view.ViewGroup;

import com.yunbao.common.Constants;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.live.R;
import com.yunbao.live.views.LiveAddImpressViewHolder;

/**
 * LIVE
 * 添加印象
 */

public class LiveAddImpressActivity extends AbsActivity {

    private LiveAddImpressViewHolder mLiveAddImpressViewHolder;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_empty;
    }

    @Override
    protected void main() {
        String toUid = getIntent().getStringExtra(Constants.TO_UID);
        if (TextUtils.isEmpty(toUid)) {
            return;
        }
        mLiveAddImpressViewHolder = new LiveAddImpressViewHolder(mContext, (ViewGroup) findViewById(R.id.container));
        mLiveAddImpressViewHolder.subscribeActivityLifeCycle();
        mLiveAddImpressViewHolder.addToParent();
        mLiveAddImpressViewHolder.setToUid(toUid);
        mLiveAddImpressViewHolder.loadData();
    }


    @Override
    public void onBackPressed() {
        if (mLiveAddImpressViewHolder != null && mLiveAddImpressViewHolder.isUpdatedImpress()) {
            setResult(RESULT_OK);
            finish();
        } else {
            super.onBackPressed();
        }
    }
}


//////// END