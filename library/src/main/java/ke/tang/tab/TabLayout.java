package ke.tang.tab;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import java.util.ArrayList;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.core.view.ViewCompat;
import ke.tang.tab.transformer.CrossFadeDrawableTransformer;
import ke.tang.tab.transformer.DrawableTransformer;

/**
 * Created by TangKe on 14/08/2017.
 */

public class TabLayout extends HorizontalScrollView implements OnTabAppearanceChangeListener {
    private ScrollTabContainerLayout mScrollContainer;
    private boolean mIsCentered;

    private TabFactory mTabFactory;


    private ArrayList<Tab> mTabs = new ArrayList<>();
    private DrawableTransformer mIconTransformer = new CrossFadeDrawableTransformer();

    private int mCurrentPosition;
    private float mCurrentPositionOffset;
    private SmartIndicationInterpolator mIndicationInterpolator;
    private ColorGradient mColorGradient;

    private float mTabTextSize;
    private ColorStateList mTabTextColors;
    private int mTabViewTheme;

    private static final Interpolator sInterpolator = new Interpolator() {
        @Override
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };

    private ValueAnimator mCurrentAnimator;

    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int index = ((ViewGroup) v.getParent()).indexOfChild(v);
            performTabClick(index, mTabs.get(index));
        }
    };

    private OnTabClickListener mOnTabClickListener;

    public TabLayout(Context context) {
        this(context, null);
    }

    public TabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.tabLayoutStyle);
    }

    public TabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScrollContainer = new ScrollTabContainerLayout(context);
        setFillViewport(true);
        super.addView(mScrollContainer, -1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabLayout, defStyleAttr, R.style.Widget_TabLayout);
        setIndicator(a.getResourceId(R.styleable.TabLayout_indicator, 0));
        setTabTextColorResource(a.getResourceId(R.styleable.TabLayout_tabTextColor, 0));
        setTabTextSize(a.getDimensionPixelSize(R.styleable.TabLayout_tabTextSize, 15));
        mScrollContainer.setPadding(a.getDimensionPixelSize(R.styleable.TabLayout_innerPaddingLeft, 0), 0, a.getDimensionPixelSize(R.styleable.TabLayout_innerPaddingRight, 0), 0);
        mIsCentered = a.getBoolean(R.styleable.TabLayout_centered, false);
        mTabViewTheme = a.getResourceId(R.styleable.TabLayout_tabViewTheme, 0);
        setCentered(mIsCentered);
        a.recycle();

        mIndicationInterpolator = new SmartIndicationInterpolator();
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        throw new IllegalStateException("禁止添加View");
    }

    /**
     * 添加Tab
     *
     * @param tab
     */
    public void addTab(Tab tab) {
        mTabs.add(tab);
        tab.getView().setOnClickListener(mOnClickListener);
        mScrollContainer.addView(tab.getView());
    }

    /**
     * 移除Tab
     *
     * @param tab
     */
    public void removeTab(Tab tab) {
        mTabs.remove(tab);
        tab.getView().setOnClickListener(null);
        mScrollContainer.removeView(tab.getView());
    }

    /**
     * 设置跟随Pager滑动的指示器
     *
     * @param indicator
     */
    public void setIndicator(Drawable indicator) {
        setIndicatorInternal(indicator);
    }

    /**
     * 设置跟随Pager滑动的指示器
     *
     * @param indicator
     */
    void setIndicatorInternal(Drawable indicator) {
        mScrollContainer.setIndicator(indicator);
    }

    /**
     * 设置跟随Pager滑动的指示器
     *
     * @param drawableRes
     */
    public void setIndicator(@DrawableRes int drawableRes) {
        if (0 < drawableRes) {
            setIndicatorInternal(getResources().getDrawable(drawableRes));
        } else {
            setIndicatorInternal(null);
        }
    }

    public void setTabTextColorResource(@ColorRes int colorRes) {
        if (0 < colorRes) {
            setTabTextColorInternal(getResources().getColorStateList(colorRes));
        } else {
            setTabTextColorInternal(null);
        }
    }

    /**
     * 设置用于图标Drawable的转化器
     *
     * @param transformer
     */
    public void setIconTransformer(DrawableTransformer transformer) {
        mIconTransformer = transformer;
    }

    /**
     * 设置Tab点击监听
     *
     * @param listener
     */
    public void setOnTabClickListener(OnTabClickListener listener) {
        mOnTabClickListener = listener;
    }

    /**
     * 设置用于生成Tab的工厂类
     *
     * @param factory
     */
    public void setTabFactory(TabFactory factory) {
        mTabFactory = factory;
        mTabs.clear();
        invalidateTabState();
        post(new Runnable() {
            @Override
            public void run() {
                //在下次布局触发后执行, 否则无法获取Tab宽度
                mScrollContainer.updateIndicatorPosition();
            }
        });
    }

    /**
     * 获取Tab数量
     *
     * @return
     */
    public int getTabCount() {
        return mTabs.size();
    }

    /**
     * 获取指定位置的Tab
     *
     * @param index
     * @return
     */
    public Tab getTabAt(int index) {
        return mTabs.get(index);
    }

    private Tab findTabByView(View view) {
        for (Tab tab : mTabs) {
            if (tab.getView() == view) {
                return tab;
            }
        }
        return null;
    }

    /**
     * 构建默认Tab
     *
     * @return
     */
    public Tab newTab() {
        final Context context = getContext();
        Tab tab;
        if (null == mTabFactory) {
            Context contextWrapper = new ContextThemeWrapper(context, mTabViewTheme);
            TabView tabView = new TabView(contextWrapper);
            tabView.setOnTabAppearanceChangeListener(this);
            tabView.setTextSize(mTabTextSize);
            tab = tabView;
        } else {
            tab = mTabFactory.newTab(context);
        }
        tab.setOnTabClickListener(mOnClickListener);
        return tab;
    }

    private void prepareTab(Tab tab, CharSequence title, @DrawableRes int iconRes) {
        tab.setText(title);
        tab.setIcon(iconRes);

        Drawable icon = tab.getIcon();
        if (null != icon) {
            icon.mutate();
        }
    }

    protected void invalidateTabState() {
        final int tabCount = mScrollContainer.getChildCount();

        float totalOffset = mCurrentPosition + mCurrentPositionOffset;
        int prevPosition = (int) Math.floor(totalOffset);
        int nextPosition = (int) Math.ceil(totalOffset);

        for (int index = 0; index < tabCount; index++) {
            Tab tab = findTabByView(mScrollContainer.getChildAt(index));
            invalidateTabIconState(tab, index, prevPosition, nextPosition, totalOffset);
            invalidateTabTextState(tab, index, prevPosition, nextPosition);
        }
    }

    private void invalidateTabIconState(Tab tab, int index, int prevPosition, int nextPosition, float totalOffset) {
        if (null == mIconTransformer) {
            return;
        }
        if (prevPosition == index) {
            mIconTransformer.transform(tab.getIcon(), (int) (DrawableTransformer.MAX_LEVEL * (totalOffset - prevPosition)));
        } else if (nextPosition == index) {
            mIconTransformer.transform(tab.getIcon(), (int) (DrawableTransformer.MAX_LEVEL * (nextPosition - totalOffset)));
        } else {
            mIconTransformer.transform(tab.getIcon(), DrawableTransformer.MAX_LEVEL);
        }
    }

    private void invalidateTabTextState(Tab tab, int index, int prevPosition, int nextPosition) {
        if (tab instanceof TabView) {
            TabView coezalTab = (TabView) tab;
            if (null == mColorGradient) {
                return;
            }
            if (prevPosition == index) {
                coezalTab.setTextColor(mColorGradient.getColor(1 - mCurrentPositionOffset));
            } else if (nextPosition == index) {
                coezalTab.setTextColor(mColorGradient.getColor(mCurrentPositionOffset));
            } else {
                coezalTab.setTextColor(mColorGradient.getStartColor());
            }
        }
    }

    private void prepareTextColorTransitionInfo() {
        if (null == mTabTextColors) {
            mColorGradient = null;
        } else {
            ColorStateList colorStateList = mTabTextColors;
            int startColor = colorStateList.getDefaultColor();
            int endColor = colorStateList.getColorForState(View.SELECTED_STATE_SET, colorStateList.getDefaultColor());
            mColorGradient = new ColorGradient(startColor, endColor);
        }
    }

    public void setCentered(boolean isCentered) {
        mIsCentered = isCentered;
        mScrollContainer.setGravity(isCentered ? Gravity.CENTER : Gravity.NO_GRAVITY);
    }

    public void setTabTextColor(ColorStateList colors) {
        setTabTextColorInternal(colors);
    }

    void setTabTextColorInternal(ColorStateList colors) {
        mTabTextColors = colors;
        prepareTextColorTransitionInfo();
        invalidateTabState();
    }

    public void setTabTextSize(float tabTextSize) {
        mTabTextSize = tabTextSize;
        for (Tab tab : mTabs) {
            if (tab.getView() instanceof TabView) {
                ((TabView) tab.getView()).setTextSize(tabTextSize);
            }
        }
    }

    protected void offset(int position, float positionOffset) {
        mCurrentPosition = position;
        mCurrentPositionOffset = positionOffset;
        invalidateTabState();
        mScrollContainer.setScrollPosition(mCurrentPosition, mCurrentPositionOffset);
    }

    public void setSelected(int position) {
        setSelected(position, true);
    }

    public void setSelected(int position, boolean isAnimate) {
        if (position < 0 || position >= mScrollContainer.getChildCount()) {
            return;
        }

        if (isAnimate) {
            if (null != mCurrentAnimator) {
                mCurrentAnimator.cancel();
            }
            ValueAnimator animator = mCurrentAnimator = ValueAnimator.ofFloat(mCurrentPosition + mCurrentPositionOffset, position);
            animator.setInterpolator(sInterpolator);
            animator.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float animatedValue = (float) valueAnimator.getAnimatedValue();
                    int targetPosition = (int) Math.floor(animatedValue);
                    offset(targetPosition, animatedValue - targetPosition);
                }
            });
            animator.start();
        } else {
            offset(position, 0);
        }
    }

    public void performTabClick(int index, Tab tab) {
        setSelected(index, false);
        if (null != mOnTabClickListener) {
            mOnTabClickListener.onTabClick(index, tab);
        }
    }

    protected void invalidateIndicator() {
        mScrollContainer.updateIndicatorPosition();
    }

    @Override
    public void onTabAppearanceChange(Tab tab) {
        invalidateTabState();
    }

    class ScrollTabContainerLayout extends LinearLayout {
        private int mIndicatorLeft;
        private int mIndicatorRight;
        private Drawable mIndicator;

        public ScrollTabContainerLayout(Context context) {
            super(context);
            setOrientation(LinearLayout.HORIZONTAL);
            setWillNotDraw(false);
        }

        @Override
        protected LayoutParams generateDefaultLayoutParams() {
            if (mIsCentered) {
                return new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            } else {
                return new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1);
            }
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            updateIndicatorPosition();
        }

        private void setIndicatorPosition(int left, int right) {
            if (left != mIndicatorLeft || right != mIndicatorRight) {
                mIndicatorLeft = left;
                mIndicatorRight = right;
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }

        private void updateIndicatorPosition() {
            final View selectedTitle = getChildAt(mCurrentPosition);
            int left, right;

            if (selectedTitle != null && selectedTitle.getWidth() > 0) {
                left = selectedTitle.getLeft();
                right = selectedTitle.getRight();

                float startOffset = mIndicationInterpolator.getLeftEdge(mCurrentPositionOffset);
                float endOffset = mIndicationInterpolator.getRightEdge(mCurrentPositionOffset);

                if (mCurrentPositionOffset > 0f && mCurrentPosition < getChildCount() - 1) {
                    View nextTitle = getChildAt(mCurrentPosition + 1);
                    int nextStart = nextTitle.getLeft();
                    int nextEnd = nextTitle.getRight();
                    left = (int) (startOffset * nextStart + (1.0f - startOffset) * left);
                    right = (int) (endOffset * nextEnd + (1.0f - endOffset) * right);
                }
            } else {
                left = right = -1;
            }

            setIndicatorPosition(left, right);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            final Drawable indicator = mIndicator;
            if (null != indicator && 0 != getChildCount()) {
                final int height = indicator.getIntrinsicHeight();
                final int parentHeight = getHeight();
                indicator.setBounds(mIndicatorLeft, parentHeight - height, mIndicatorRight,
                        parentHeight);
                indicator.draw(canvas);
            }
        }

        public void setIndicator(Drawable indicator) {
            mIndicator = indicator;
            ViewCompat.postInvalidateOnAnimation(this);
        }

        private void setSelectedTabView(int position) {
            final int tabCount = getChildCount();
            if (position < tabCount && !getChildAt(position).isSelected()) {
                for (int i = 0; i < tabCount; i++) {
                    final View child = getChildAt(i);
                    child.setSelected(i == position);
                }
            }
        }

        private void setScrollPosition(int position, float positionOffset) {
            final int roundedPosition = Math.round(position + positionOffset);
            if (roundedPosition < 0 || roundedPosition >= getChildCount()) {
                return;
            }

            updateIndicatorPosition();

            int targetPosition = calculateScrollXForTab(position, positionOffset);
            TabLayout.this.scrollTo(targetPosition, 0);

            setSelectedTabView(roundedPosition);
        }

        private int calculateScrollXForTab(int position, float positionOffset) {
            final View selectedChild = getChildAt(position);
            final View nextChild = position + 1 < getChildCount()
                    ? getChildAt(position + 1) : null;
            final int selectedWidth = selectedChild != null ? selectedChild.getWidth() : 0;
            final int nextWidth = nextChild != null ? nextChild.getWidth() : 0;

            return selectedChild.getLeft()
                    + ((int) ((selectedWidth + nextWidth) * positionOffset * 0.5f))
                    + (selectedChild.getWidth() / 2)
                    - (TabLayout.this.getWidth() / 2);
        }
    }

    public static class SmartIndicationInterpolator {

        private static final float DEFAULT_INDICATOR_INTERPOLATION_FACTOR = 0.4f;

        private final Interpolator leftEdgeInterpolator;
        private final Interpolator rightEdgeInterpolator;

        public SmartIndicationInterpolator() {
            this(DEFAULT_INDICATOR_INTERPOLATION_FACTOR);
        }

        public SmartIndicationInterpolator(float factor) {
            leftEdgeInterpolator = new AccelerateInterpolator();
            rightEdgeInterpolator = new DecelerateInterpolator(factor);
        }

        public float getLeftEdge(float offset) {
            return leftEdgeInterpolator.getInterpolation(offset);
        }


        public float getRightEdge(float offset) {
            return rightEdgeInterpolator.getInterpolation(offset);
        }
    }

    public static class ColorGradient {
        private int mStartColor;
        private int mEndColor;
        private ArgbEvaluator mArgbEvaluator;

        public ColorGradient(@ColorInt int startColor, @ColorInt int endColor) {
            this.mStartColor = startColor;
            this.mEndColor = endColor;
            mArgbEvaluator = new ArgbEvaluator();
        }

        public int getColor(float radio) {
            return (int) mArgbEvaluator.evaluate(radio, mStartColor, mEndColor);
        }

        public int getStartColor() {
            return mStartColor;
        }

        public int getEndColor() {
            return mEndColor;
        }
    }
}