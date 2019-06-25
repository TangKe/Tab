package ke.tang.tab;

import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.XmlRes;

/**
 * 适用于{@link TabLayout}的tab接口
 * Created by TangKe on 2017/1/5.
 */
public interface Tab {
    int BADGE_STYLE_TEXT = 1;
    int BADGE_STYLE_DOT = 2;

    /**
     * 获取该Tab的图标
     *
     * @return
     */
    Drawable getIcon();

    /**
     * 设置该Tab的图标
     *
     * @param iconRes
     */
    void setIcon(@DrawableRes @XmlRes int iconRes);

    /**
     * 设置该Tab的标题
     *
     * @param title
     */
    void setText(CharSequence title);

    /**
     * 设置Tab的颜色, 用于支持渐变
     *
     * @param color
     */
    void setTextColor(@ColorInt int color);

    /**
     * 设置Tab点击回调
     *
     * @param listener
     */
    void setOnTabClickListener(View.OnClickListener listener);

    /**
     * 设置
     *
     * @param text
     */
    void setBadge(CharSequence text);

    /**
     * 设置气泡是否可见
     *
     * @param isVisible
     */
    void setBadgeVisible(boolean isVisible);

    /**
     * 设置气泡是否可见
     *
     * @param isVisible
     */
    void setBadgeVisible(boolean isVisible, int style);

    /**
     * 获取该Tab对应的View
     *
     * @return
     */
    View getView();
}
