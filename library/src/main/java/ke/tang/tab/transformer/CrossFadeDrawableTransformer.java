package ke.tang.tab.transformer;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import androidx.annotation.IntRange;

/**
 * 对{@link LayerDrawable}进行淡出淡入操作, 默认显示最顶层的{@link Drawable}, 随着level增大我逐渐显示最底层的{@link Drawable}
 * Created by TangKe on 2017/1/4.
 */
public class CrossFadeDrawableTransformer implements DrawableTransformer {
    @Override
    public void transform(Drawable drawable, @IntRange(from = 0, to = MAX_LEVEL) int level) {
        if (null == drawable || !(drawable instanceof LayerDrawable)) {
            return;
        }

        LayerDrawable layerDrawable = (LayerDrawable) drawable;
        final int layerCount = layerDrawable.getNumberOfLayers();
        if (2 <= layerCount) {
            //层数大于或等于2的时候
            for (int index = 0; index < layerCount; index++) {
                Drawable layer = layerDrawable.getDrawable(index).mutate();
                int targetAlpha = (int) (255 * (0 == index ? MAX_LEVEL - level : level) * 1.0f / MAX_LEVEL);
                layer.setAlpha(targetAlpha);
                layer.invalidateSelf();
            }
        }
        layerDrawable.invalidateSelf();
    }
}
