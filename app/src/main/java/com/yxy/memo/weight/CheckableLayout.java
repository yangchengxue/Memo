package com.yxy.memo.weight;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.RelativeLayout;

/**
  *
  * @Package:        com.yxy.memo.weight
  * @ClassName:      CheckableLayout
  * @CreateDate:     2019/12/18 20:49
  * @Description:    自定义的可被选择的布局，继承自RelativeLayout，在SpinnerPopuwindow中的ListView的item布局中使用到
 */
public class CheckableLayout extends RelativeLayout implements Checkable {
    boolean mChecked = false;

    private static final int[] CHECKED_STATE_SET = { android.R.attr.state_checked };

    public CheckableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckableLayout(Context context) {
        super(context);
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            refreshDrawableState();
        }
    }

    @Override
    public void toggle() {
        mChecked = !mChecked;
        refreshDrawableState();
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    @Override
    public boolean performClick() {
        toggle();
        return super.performClick();
    }
}
