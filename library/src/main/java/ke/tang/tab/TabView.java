package ke.tang.tab;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.XmlRes;

/**
 * 默认{@link Tab}实现, 在{@link TabLayout}没有设置{@link TabFactory}的时候, 默认使用该类
 */
public class TabView extends FrameLayout implements Tab, OnTabAppearanceChangeListener {
    private TextView mText;
    private TabIconView mIcon;
    private TextView mBadgeText;
    private ImageView mBadge;

    private TypedValue mValue = new TypedValue();

    private OnTabAppearanceChangeListener mOnTabAppearanceChangeListener;

    public TabView(Context context) {
        this(context, null);
    }

    public TabView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.tabViewStyle);
    }

    public TabView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.layout_tab_view, this);
        mText = findViewById(android.R.id.text1);
        mIcon = findViewById(android.R.id.icon);
        mIcon.setOnTabAppearanceChangeListener(this);
        mBadgeText = findViewById(R.id.badgeText);
        mBadge = findViewById(R.id.badge);
    }

    @Override
    public Drawable getIcon() {
        return mIcon.getIcon();
    }

    @Override
    public void setIcon(@DrawableRes @XmlRes int iconRes) {
        if (0 < iconRes) {
            String type = getResources().getResourceTypeName(iconRes);
            switch (type) {
                case "xml":
                    mIcon.setAnimateIconResource(iconRes);
                    break;
                case "drawable":
                    mIcon.setIcon(iconRes);
                    break;
            }
        }
    }

    @Override
    public void setText(CharSequence title) {
        mText.setText(title);
        mText.setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);
    }

    @Override
    public void setTextColor(@ColorInt int color) {
        mText.setTextColor(color);
    }

    public void setTextSize(float size) {
        mText.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
    }

    public ColorStateList getTextColors() {
        return mText.getTextColors();
    }

    @Override
    public void setOnTabClickListener(OnClickListener listener) {
        setOnClickListener(listener);
    }

    @Override
    public void setBadge(CharSequence text) {
        mBadgeText.setText(text);
    }

    @Override
    public void setBadgeVisible(boolean isVisible) {
        setBadgeVisible(isVisible, BADGE_STYLE_TEXT);
    }

    @Override
    public void setBadgeVisible(boolean isVisible, int style) {
        switch (style) {
            case BADGE_STYLE_DOT:
                mBadgeText.setVisibility(View.INVISIBLE);
                mBadge.setVisibility(isVisible ? View.VISIBLE : INVISIBLE);
                break;
            case BADGE_STYLE_TEXT:
                mBadge.setVisibility(View.INVISIBLE);
                mBadgeText.setVisibility(isVisible ? View.VISIBLE : INVISIBLE);
                break;
        }
    }

    @Override
    public View getView() {
        return this;
    }

    public void setOnTabAppearanceChangeListener(OnTabAppearanceChangeListener onTabAppearanceChangeListener) {
        mOnTabAppearanceChangeListener = onTabAppearanceChangeListener;
    }

    @Override
    public void onTabAppearanceChange(Tab tab) {
        if (null != mOnTabAppearanceChangeListener) {
            mOnTabAppearanceChangeListener.onTabAppearanceChange(this);
        }
    }
}