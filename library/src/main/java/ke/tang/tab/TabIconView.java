package ke.tang.tab;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.airbnb.lottie.ImageAssetDelegate;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieImageAsset;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.DrawableRes;
import androidx.annotation.XmlRes;

/**
 * Created by TangKe on 2017/5/2.
 */

public class TabIconView extends FrameLayout implements OnInflateListener, ImageAssetDelegate {
    private static final Map<String, WeakReference<LottieComposition>> mRefCache = new HashMap<>();
    private OnTabAppearanceChangeListener mOnTabAppearanceChangeListener;

    private AnimateStates mAnimateStates;
    private int mIconRes;

    private AnimateState mCurrentAnimateState;
    private ImageView mIcon;
    private LottieAnimationView mAnimateIcon;

    public TabIconView(Context context) {
        this(context, null);
    }

    public TabIconView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabIconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.layout_tab_icon, this);
        mIcon = findViewById(R.id.icon);
        mAnimateIcon = findViewById(R.id.animateIcon);
        mAnimateIcon.setImageAssetDelegate(this);
    }

    public void setOnTabAppearanceChangeListener(OnTabAppearanceChangeListener onTabAppearanceChangeListener) {
        mOnTabAppearanceChangeListener = onTabAppearanceChangeListener;
    }

    public void setAnimateIconResource(@XmlRes int resId) {
        if (resId != -1) {
            XmlResourceParser parser = getContext().getResources().getXml(resId);
            try {
                AttributeSet attr = Xml.asAttributeSet(parser);
                int type;
                while ((type = parser.next()) != XmlPullParser.START_TAG &&
                        type != XmlPullParser.END_DOCUMENT) {
                }

                if (type != XmlPullParser.START_TAG) {
                    throw new XmlPullParserException("No start tag found");
                }

                AnimateStates states = AnimateStates.createFromXml(getContext(), parser, Xml.asAttributeSet(parser));
                setAnimateIcon(states);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
        }
    }

    public void setAnimateIcon(AnimateStates animateStates) {
        mAnimateStates = animateStates;
        mIconRes = 0;
        mCurrentAnimateState = null;
        if (null != animateStates && !animateStates.isInflate()) {
            animateStates.inflate(this);
        }
    }

    public void setIcon(@DrawableRes int resId) {
        mIconRes = resId;
        mAnimateStates = null;
        mIcon.setImageResource(resId);

        mIcon.setVisibility(View.VISIBLE);
        mAnimateIcon.setVisibility(View.GONE);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidateAnimateState();
    }

    private void invalidateAnimateState() {
        if (null != mAnimateStates && mAnimateStates.isInflate()) {
            int[] states = getDrawableState();
            AnimateState matchedState = mAnimateStates.match(states);
            if (mCurrentAnimateState == matchedState) {
                return;
            }
            if (null != matchedState && matchedState.isInflate()) {
                LottieComposition animation = matchedState.getAnimation();
                Drawable drawable = matchedState.getDrawable();
                if (null != animation) {
                    //由于Lottie代码导致
                    mIcon.setVisibility(View.GONE);
                    mAnimateIcon.setVisibility(View.VISIBLE);
                    mAnimateIcon.setComposition(animation);
                    mAnimateIcon.loop(matchedState.isRepeatable());
                    mAnimateIcon.setProgress(0);
                    mAnimateIcon.playAnimation();
                } else if (null != drawable) {
                    mIcon.setVisibility(View.VISIBLE);
                    mAnimateIcon.setVisibility(View.GONE);
                    mIcon.setImageDrawable(drawable);
                }
                mCurrentAnimateState = matchedState;
            }

        }
        if (0 != mIconRes) {
            mIcon.setVisibility(View.VISIBLE);
            mAnimateIcon.setVisibility(View.GONE);
        } else {
            mIcon.setVisibility(View.GONE);
            mAnimateIcon.setVisibility(View.GONE);
        }
    }

    public Drawable getIcon() {
        return mIcon.getDrawable();
    }

    @Override
    public void onInflate() {
        invalidateAnimateState();
    }

    @Override
    public Bitmap fetchBitmap(LottieImageAsset asset) {
        //Lottie会自动使用一次后回收掉, 所以没次都需要从新加载
        try {
            return BitmapFactory.decodeStream(getContext().getAssets().open(asset.getFileName()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
