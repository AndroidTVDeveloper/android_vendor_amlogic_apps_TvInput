package com.droidlogic.tvsource.ui;

import java.util.List;

import com.droidlogic.app.tv.DroidLogicTvUtils;
import com.droidlogic.tvsource.R;
import com.droidlogic.tvsource.Utils;
import com.droidlogic.tvsource.ui.SourceButton.OnSourceClickListener;

import android.content.Context;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class SourceInputListLayout extends LinearLayout implements OnSourceClickListener {
    private static final String TAG = "SourceInputListLayout";
    private Context mContext;
    private LinearLayout mRoot;
    private TvInputManager mTvInputManager;
    //max index of all hardware devices in mSourceMenuLayout
    private int maxHardwareIndex = 0;
    private int mSourceCount;

    private SourceButton defSourceInput;
    private SourceButton preSourceInput;
    private SourceButton curSourceInput;

    private onSourceInputClickListener mClickListener;

    private int defaultDeviceId;
    private int defaultAtvChannel;
    private int defaultDtvChannel;
    private boolean defaultDtvType;

    public SourceInputListLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        inflate(mContext, R.layout.source_input_list, this);
        mRoot = (LinearLayout) findViewById(R.id.source_root);
        mTvInputManager = (TvInputManager) mContext.getSystemService(Context.TV_INPUT_SERVICE);
    }

    public void remove(String inputId) {
        if (isListEmpty() || TextUtils.isEmpty(inputId))
            return;
        int i = 0;
        for (; i < mSourceCount; i++) {
            SourceButton tmp = (SourceButton) mRoot.getChildAt(i);
            if (TextUtils.equals(tmp.getInputId(), inputId)) {
                if (tmp.isHardware())
                    maxHardwareIndex--;
                mRoot.removeViewAt(i);
                mSourceCount--;
                if (TextUtils.equals(inputId, curSourceInput.getInputId())) {
                    preSourceInput = defSourceInput;
                    curSourceInput = defSourceInput;
                }
                break;
            }
        }
    }

    public int add(String inputId) {
        int input_list = mTvInputManager.getTvInputList().size();
        Utils.logd(TAG, "==== add, size=" + input_list + "count=" + mSourceCount);
        if (TextUtils.isEmpty(inputId) || mSourceCount == input_list)
            return 0;
        TvInputInfo info = mTvInputManager.getTvInputInfo(inputId);
        SourceButton sb = new SourceButton(mContext, info);
        if (sb.isHardware()) {
            initSourceInput(sb);
            if (maxHardwareIndex == 0) {
                mRoot.addView(sb, 1);
                maxHardwareIndex++;
                mSourceCount++;
            } else {
                int lo = 1;
                int hi = maxHardwareIndex;

                while (lo <= hi) {
                    final int mid = (lo + hi) >>> 1;
                    final SourceButton temp = (SourceButton) mRoot.getChildAt(mid);
                    final int temp_id = temp.getDeviceId();
                    if (temp_id < sb.getDeviceId()) {
                        lo = mid + 1;
                    } else if (temp_id > sb.getDeviceId()) {
                        hi = mid - 1;
                    }
                }
                mRoot.addView(sb, lo);
                maxHardwareIndex++;
                mSourceCount++;
            }
        } else {
            mRoot.addView(sb);
            mSourceCount++;
        }
        sb.setOnSourceClickListener(this);
        if (curSourceInput == null && mTvInputManager.getTvInputList().size() == mSourceCount) {
            preSourceInput = defSourceInput;
            curSourceInput = defSourceInput;
        }
        return 1;
    }

    public int refresh() {
        Utils.logd(TAG, "==== refresh");
        maxHardwareIndex = 0;
        mSourceCount = 0;
        List<TvInputInfo> input_list = mTvInputManager.getTvInputList();
        if (input_list.size() < 1) {
            return mSourceCount;
        }

        if (mRoot.getChildCount() > 1) {
            mRoot.removeViews(1, mRoot.getChildCount() - 1);
        }
        for (TvInputInfo info : input_list) {
            SourceButton sb = new SourceButton(mContext, info);
            if (sb.isHardware()) {
                initSourceInput(sb);
                if (maxHardwareIndex == 0) {
                    mRoot.addView(sb, 1);
                    maxHardwareIndex++;
                    mSourceCount++;
                } else {
                    int lo = 1;
                    int hi = maxHardwareIndex;

                    while (lo <= hi) {
                        final int mid = (lo + hi) >>> 1;
                        final SourceButton temp = (SourceButton) mRoot.getChildAt(mid);
                        final int temp_id = temp.getDeviceId();
                        if (temp_id < sb.getDeviceId()) {
                            lo = mid + 1;
                        } else if (temp_id > sb.getDeviceId()) {
                            hi = mid - 1;
                        }
                    }
                    mRoot.addView(sb, lo);
                    maxHardwareIndex++;
                    mSourceCount++;
                }
            } else {
                mRoot.addView(sb);
                mSourceCount++;
            }
            sb.setOnSourceClickListener(this);
        }
        if (curSourceInput == null) {
            preSourceInput = defSourceInput;
            curSourceInput = defSourceInput;
        }
        return mSourceCount;
    }

    private void initSourceInput(SourceButton sb) {
        if (sb.getSourceType() == DroidLogicTvUtils.SOURCE_TYPE_ATV && defaultAtvChannel >= 0) {
            sb.moveToChannel(defaultAtvChannel, false);
        } else if (sb.getSourceType() == DroidLogicTvUtils.SOURCE_TYPE_DTV && defaultDtvChannel >= 0) {
            sb.moveToChannel(defaultDtvChannel, defaultDtvType);
        }

        if (defaultDeviceId == sb.getDeviceId()) {
            curSourceInput = sb;
        }

        if (sb.getDeviceId() == DroidLogicTvUtils.DEVICE_ID_ATV)
            defSourceInput = sb;
    }

    public void setDefaultSourceInfo(int device_id, int atv_channel, int dtv_channel,
            boolean is_radio) {
        Utils.logd(TAG, "device_id=" + device_id + ", atv_channel=" + atv_channel
                + ", dtv_channel=" + dtv_channel + ", is_radio=" + is_radio);
        defaultDeviceId = device_id;
        defaultAtvChannel = atv_channel;
        defaultDtvChannel = dtv_channel;
        defaultDtvType = is_radio;
    }

    public void stateChange(String inputId, int state) {
        return;
    }

    public void update(String inputId) {
        return;
    }

    public SourceButton getCurSourceInput() {
        return curSourceInput;
    }

    public SourceButton getPreSourceInput() {
        return preSourceInput;
    }

    public int getSourceCount() {
        return mSourceCount;
    }

    @Override
    public void onButtonClick(SourceButton sb) {
        Utils.logd(TAG, "==== onButtonClick ====" + sb);
        preSourceInput = curSourceInput;
        curSourceInput = sb;
        mClickListener.onSourceInputClick();
    }

    private boolean isListEmpty() {
        mSourceCount = mRoot.getChildCount();
        return mSourceCount > 1 ? false : true;
    }

    public void setOnSourceInputClickListener(onSourceInputClickListener l) {
        mClickListener = l;
    }

    public interface onSourceInputClickListener {
        void onSourceInputClick();
    }
}