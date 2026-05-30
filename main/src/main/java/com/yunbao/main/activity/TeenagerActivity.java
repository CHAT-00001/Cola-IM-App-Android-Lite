package com.yunbao.main.activity;

// main - 我的 - 设置中心 - 青少年模式
// 2026-05-30 05:53

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.main.R;
import com.yunbao.main.http.MainHttpConsts;
import com.yunbao.main.http.MainHttpUtil;

/**
 * 设置
 * 青少年模式
 */
public class TeenagerActivity extends AbsActivity implements View.OnClickListener {


    public static void forward(Context context) {
        Intent intent = new Intent(context, TeenagerActivity.class);
        context.startActivity(intent);
    }


    private TextView mBtnSwitch;
    private TextView mTip;
    private View mBtnPwd;
    private boolean mIsOpen;//是否开了青少年模式
    private boolean mHasSetPwd;//是否设置过密码

    @Override
    protected int getLayoutId() {
        return R.layout.activity_teenager;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.a_120));
        mBtnSwitch = findViewById(R.id.btn_switch);
        mBtnSwitch.setOnClickListener(this);
        mTip = findViewById(R.id.tip);
        mBtnPwd = findViewById(R.id.btn_modify_pwd);
        mBtnPwd.setOnClickListener(this);
        MainHttpUtil.checkTeenager(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    mIsOpen = obj.getIntValue("status") == 1;
                    mHasSetPwd = obj.getIntValue("is_setpassword") == 1;
                    if (mHasSetPwd) {
                        if (mBtnPwd != null) {
                            mBtnPwd.setVisibility(View.VISIBLE);
                        }
                    }
                    if (mIsOpen) {
                        if (mTip != null) {
                            mTip.setText(R.string.a_121_2);
                        }
                        if (mBtnSwitch != null) {
                            mBtnSwitch.setText(R.string.a_126);
                        }
                    } else {
                        if (mTip != null) {
                            mTip.setText(R.string.a_121);
                        }
                        if (mBtnSwitch != null) {
                            mBtnSwitch.setText(R.string.a_125);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_switch) {
            TeenagerPwdActivity.forward(mContext, mIsOpen, mHasSetPwd,1);
        } else if (i == R.id.btn_modify_pwd) {
            TeenagerModifyPwdActivity.forward(mContext);
        }
    }


    @Override
    protected void onDestroy() {
        MainHttpUtil.cancel(MainHttpConsts.CHECK_TEENAGER);
        super.onDestroy();
    }




}

//////// END