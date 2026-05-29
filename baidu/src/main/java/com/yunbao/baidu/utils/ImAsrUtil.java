package com.yunbao.baidu.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.baidu.aip.asrwakeup3.core.mini.AutoCheck;
import com.baidu.aip.asrwakeup3.core.recog.MyRecognizer;
import com.baidu.aip.asrwakeup3.core.recog.listener.StatusRecogListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cxf on 2018/7/28.
 * 聊天时候语音识别
 */

public class ImAsrUtil {
    private static final String TAG = "ImAsrUtil";
    private AsrCallback mCallback;
    private final Context mContext;
    private MyRecognizer mRecognizer;
    private final String mJsonParams;
    private final Map<String, Object> mParamsMap;
    private Handler mHandler;

    public ImAsrUtil(Context context) {
        mContext = context;
        mParamsMap = new HashMap<>();
        String str;
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = appInfo.metaData;
            int baiduAppId = bundle.getInt("com.baidu.speech.APP_ID");
            String baiduAppKey = bundle.getString("com.baidu.speech.API_KEY");
            String baiduAppSecretKey = bundle.getString("com.baidu.speech.SECRET_KEY");
            mParamsMap.put("appid", baiduAppId);
            mParamsMap.put("key", baiduAppKey);
            mParamsMap.put("secret", baiduAppSecretKey);
            mParamsMap.put("enable.long.speech", true);//长语音
            mParamsMap.put("accept-audio-volume", false);
            mParamsMap.put("disable-punctuation", true);//禁用标点
            JSONObject obj = new JSONObject(mParamsMap);
            str = obj.toJSONString();
        } catch (PackageManager.NameNotFoundException e) {
            str = null;
            e.printStackTrace();
        }
        mJsonParams = str;
        if (TextUtils.isEmpty(mJsonParams)) {
            return;
        }
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                if (msg.obj != null && msg.obj instanceof String) {
                    String str = (String) msg.obj;
                    if (mCallback != null) {
                        mCallback.onResult(str);
                    }
                }
                return false;
            }
        });
        mRecognizer = new MyRecognizer(context, new StatusRecogListener(mHandler));
    }

    /**
     * 开始录音
     */
    public void start() {
        if (mRecognizer != null) {
            new AutoCheck(mContext).checkAsr(mParamsMap);
            mRecognizer.start(mJsonParams);
        }


    }

    /**
     * 停止录音
     */
    public void stop() {
        if (mRecognizer != null) {
            mRecognizer.stop();
        }
    }


    public void release() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mCallback = null;
        if (mRecognizer != null) {
            mRecognizer.release();
        }
    }


    public interface AsrCallback {
        //void onSpeakStart();

        void onResult(String result);

        //void onSpeakEnd();
    }

    public void setAsrCallback(AsrCallback callback) {
        mCallback = callback;
    }
}
