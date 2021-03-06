package com.example.myflowlayouttest;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

class MyFlowLayout extends ViewGroup {
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

    // inflate时，会用到AttributeSet
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // ViewGroup解析的父容器给的 宽/高 size mode
        int mWidthSize = MeasureSpec.getSize(widthMeasureSpec);
        int mWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        int mHeightSize = MeasureSpec.getSize(heightMeasureSpec);
        int mHeightMode = MeasureSpec.getMode(heightMeasureSpec);

        Log.d("AAAAAA", "mHeightMode: " + (mHeightMode >> 30)); // 0

        // 根据提供的size和mode，创建度量规范
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(mHeightSize, MeasureSpec.EXACTLY);

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

        // 父容器的padding - MyFlowLayout
        int mParentPaddingLeftRight = getPaddingLeft() + getPaddingRight();
        int mParentPaddingTopBottom = getPaddingTop() + getPaddingBottom();

        // 遍历每一个子View，进行度量
        for (int i = 0; i < getChildCount(); i++) {
            // 获取第i个子view
            View mChildView = getChildAt(i);

            // 判断每一个子View是否为Gone
            if (mChildView.getVisibility() != View.GONE) {
                // 度量子view
//                measureChild(mChildView, widthMeasureSpec, heightMeasureSpec);
                measureChildWithMargins(mChildView, widthMeasureSpec, 0, heightMeasureSpec, 0);

                MarginLayoutParams lp = (MarginLayoutParams) mChildView.getLayoutParams();
                int mChildMarginLeftRight = lp.leftMargin + lp.rightMargin;
                int mChildMarginTopBottom = lp.topMargin + lp.bottomMargin;

                // 获取子view 的度量宽和高
                int mChildViewMeasuredWidth = mChildView.getMeasuredWidth();
                int mChildViewMeasuredHeight = mChildView.getMeasuredHeight();

                // 判断当前view在当前行 是否放得下，如果不行的话，就换行
                if (mChildViewMeasuredWidth + mLineWidthUsed + mChildMarginLeftRight + mParentPaddingLeftRight > mWidthSize) {
                    // 换行
                    // 保存上一行 的 view
                    mAllLineViewsList.add(mLineViewsList);
                    // 保存上一行 的 高度
                    mAllLineHeights.add(mLineHeight);

                    // 流式布局的宽、高
                    mFlowLayoutWidth = Math.max(mFlowLayoutWidth, mLineWidthUsed);
                    mFlowLayoutHeight += mLineHeight;

                    mLineHeight = 0;
                    mLineWidthUsed = 0;
                    mLineViewsList = new ArrayList<>();
                }

                mLineViewsList.add(mChildView);

                // 每行的宽、高
                mLineWidthUsed += mChildViewMeasuredWidth + mChildMarginLeftRight;
                mLineHeight = Math.max(mLineHeight, mChildViewMeasuredHeight + mChildMarginTopBottom);

                // 最后一行的子view
                if (i == getChildCount() - 1) {
                    // 把最后一行的子View添加进去
                    mAllLineViewsList.add(mLineViewsList);

                    // 把最后一行的高添加进去
                    mAllLineHeights.add(mLineHeight);

                    // 流式布局的宽、高
                    mFlowLayoutWidth = Math.max(mFlowLayoutWidth, mLineWidthUsed);
                    mFlowLayoutHeight += mLineHeight;
                }
            }
        }

        // 流式布局的宽高，需要加上padding
        mFlowLayoutWidth += mParentPaddingLeftRight;
        mFlowLayoutHeight += mParentPaddingTopBottom;

        int realWidth = (mWidthMode == MeasureSpec.EXACTLY) ? mWidthSize : mFlowLayoutWidth;
        int realHeight = (mHeightMode == MeasureSpec.EXACTLY) ? mHeightSize : mFlowLayoutHeight;

        // 保存
        setMeasuredDimension(realWidth, realHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // X、Y 的起始坐标
        int mCurrentX = getPaddingLeft();
        int mCurrentY = getPaddingTop();

        // 遍历每一行的viewList
        for (int i = 0; i < mAllLineViewsList.size(); i++) {
            // 遍历每一行 的 每一个 子View
            for (int j = 0; j < mAllLineViewsList.get(i).size(); j++) {
                // 获取到子View
                View view = mAllLineViewsList.get(i).get(j);

                MarginLayoutParams lp = (MarginLayoutParams) view.getLayoutParams();

                // 布局坐标：左上右下位置
                int mLeft = mCurrentX + lp.leftMargin;
                int mTop = mCurrentY + lp.topMargin;
                int mRight = mLeft + view.getMeasuredWidth();
                int mBottom = mTop + view.getMeasuredHeight();

                // 布局
                view.layout(mLeft, mTop, mRight, mBottom);

                // 起始 X 坐标移动一个view的 宽度+间隔
                mCurrentX = mRight + lp.rightMargin;
            }

            // 换新的一行后，X坐标移动上一行的高度 + 间隔
            mCurrentY += mAllLineHeights.get(i);
            // X坐标起始为padding
            mCurrentX = getPaddingLeft();
        }
    }
}
