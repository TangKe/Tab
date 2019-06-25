package ke.tang.tab.transformer;

import android.graphics.drawable.Drawable;

import androidx.annotation.IntRange;

/**
 * 用于对{@link Drawable}进行变换
 * Created by TangKe on 2017/1/4.
 */
public interface DrawableTransformer {
    int MAX_LEVEL = 10000;

    /**
     * 对{@link Drawable}进行操作
     *
     * @param drawable 需要被变换的{@link Drawable}
     * @param level    用于控制变化进度, 从0-10000取值, 正常状态会是10000
     */
    void transform(Drawable drawable, @IntRange(from = 0, to = MAX_LEVEL) int level);
}
