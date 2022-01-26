package com.example.myflowlayouttest;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

class MyFlowLayout extends ViewGroup {

    // 横向间距
    private int mHorizontalSpacing = dp2px(16);
    // 两行之间的纵向间距
    private int mVerticalSpacing = dp2px(8);

    // 记录所有行 中 每一行的view
    private List<List<View>> mAllLineViewsList = new ArrayList<>();

    // 记录每一行的行高
    private List<Integer> mAllLineHeights = new ArrayList<>();

    public MyFlowLayout(Context context) {
        this(context, null);
    }

    public MyFlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // 清空 List
    private void clearMeasureParams() {
        mAllLineViewsList.clear();
        mAllLineHeights.clear();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 多次执行 onMeasure 时，需要先进行清空操作
        clearMeasureParams();

        // 用于保存每一行的 子view List
        List<View> mLineViewsList = new ArrayList<>();
        // 每一行已经使用的宽度
        int mLineWidthUsed = 0;
        // 每一行的高度
        int mLineHeight = 0 ;

        // 流式布局的宽度
        int mFlowLayoutWidth = 0;
        // 流式布局的高度
        int mFlowLayoutHeight = 0;

        // 父容器的padding
        int mParentPaddingLeft = getPaddingLeft();
        int mParentPaddingTop = getPaddingTop();
        int mParentPaddingRight = getPaddingRight();
        int mParentPaddingBottom = getPaddingBottom();

        // ViewGroup解析的父容器给的 宽度
        int selfWidth = MeasureSpec.getSize(widthMeasureSpec);
        // ViewGroup解析的父容器给的 高度
        int selfHeight = MeasureSpec.getSize(heightMeasureSpec);

        // 获取子view的个数
        int childViewCount = getChildCount();

        // 遍历每一个子View，进行度量
        for (int i = 0; i < childViewCount; i++) {
            // 获取第i个子view
            View mChildView = getChildAt(i);

            // 获取子view的 LayoutParams
            LayoutParams mChildViewLayoutParams = mChildView.getLayoutParams();

            // 判断每一个子View是否为Gone
            if (mChildView.getVisibility() != View.GONE) {
                // 通过子view的 LayoutParams 获取子View的 MeasureSpec
                int mChildViewWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                        mParentPaddingLeft + mParentPaddingRight,
                        mChildViewLayoutParams.width
                );
                int mChildViewHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                        mParentPaddingTop + mParentPaddingBottom,
                        mChildViewLayoutParams.height
                );

                // 进行度量子View
                mChildView.measure(mChildViewWidthMeasureSpec, mChildViewHeightMeasureSpec);

                // 获取子view 的度量宽和高
                int mChildViewMeasuredWidth = mChildView.getMeasuredWidth();
                int mChildViewMeasuredHeight = mChildView.getMeasuredHeight();

                // 判断当前view在当前行 是否放得下，如果不行的话，就换行
                if (mChildViewMeasuredWidth + mLineWidthUsed + mHorizontalSpacing > selfWidth) {
                    // 换行
                    // 保存上一行 的 view
                    mAllLineViewsList.add(mLineViewsList);
                    // 保存上一行 的 高度
                    mAllLineHeights.add(mLineHeight);

                    // 流式布局的宽、高
                    mFlowLayoutWidth = Math.max(mFlowLayoutWidth,
                            mLineWidthUsed + mHorizontalSpacing);
                    mFlowLayoutHeight = mFlowLayoutHeight + mLineHeight + mVerticalSpacing;

                    mLineHeight = 0;
                    mLineWidthUsed = 0;
                    mLineViewsList = new ArrayList<>();
                }

                mLineViewsList.add(mChildView);
                // 每行的宽、高
                mLineWidthUsed = mLineWidthUsed + mChildViewMeasuredWidth + mHorizontalSpacing;
                mLineHeight = Math.max(mLineHeight, mChildViewMeasuredHeight);

                // 最后一行的子view
                if (i == childViewCount - 1) {
                    // 把最后一行的子View添加进去
                    mAllLineViewsList.add(mLineViewsList);

                    // 把最后一行的高添加进去
                    mAllLineHeights.add(mLineHeight);

                    // 流式布局的宽、高
                    mFlowLayoutWidth = Math.max(mFlowLayoutWidth,
                            mLineWidthUsed + mHorizontalSpacing);
                    mFlowLayoutHeight = mFlowLayoutHeight + mLineHeight + mVerticalSpacing;
                }
            }
        }

        // 度量父容器
        int mWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        int mHeightMode = MeasureSpec.getMode(heightMeasureSpec);

        int realWidth = (mWidthMode == MeasureSpec.EXACTLY) ? selfWidth : mFlowLayoutWidth;
        int realHeight = (mHeightMode == MeasureSpec.EXACTLY) ? selfHeight : mFlowLayoutHeight;

        // 保存
        setMeasuredDimension(realWidth, realHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // 所有行数
        int mLineCount = mAllLineViewsList.size();

        // padding
        int mCurrentLeft = getPaddingLeft();
        int mCurrentTop = getPaddingTop();

        for (int i = 0; i < mLineCount; i++) {
            // 获取每一行的view
            List<View> mLineViewsList = mAllLineViewsList.get(i);

            // 对每一个子view进行布局
            for (int j = 0; j < mLineViewsList.size(); j++) {
                View view = mLineViewsList.get(j);

                view.layout(
                        mCurrentLeft,
                        mCurrentTop,
                        mCurrentLeft + view.getMeasuredWidth(),
                        mCurrentTop + view.getMeasuredHeight()
                );

                mCurrentLeft = mCurrentLeft + view.getMeasuredWidth() + mHorizontalSpacing;
            }

            mCurrentTop = mCurrentTop + mAllLineHeights.get(i) + mVerticalSpacing;
            mCurrentLeft = getPaddingLeft();
        }
    }

    /**
     * dp 转成 px
     */
    public static int dp2px(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                Resources.getSystem().getDisplayMetrics()
        );
    }

}
