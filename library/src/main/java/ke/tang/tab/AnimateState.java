package ke.tang.tab;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.StateSet;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.OnCompositionLoadedListener;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by TangKe on 2017/3/9.
 */

public class AnimateState {
    public static String TAG = "item";
    private int[] mStates;

    private String mPath;
    private int mDrawableId;

    private boolean mIsInflate;
    private boolean mIsInflating;
    private Context mContext;

    private LottieComposition mAnimation;
    private Drawable mDrawable;
    private boolean mIsRepeatable;

    public AnimateState(Context context, AttributeSet attrs) {
        mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AnimateState);
        mPath = a.getString(R.styleable.AnimateState_android_path);
        mDrawableId = a.getResourceId(R.styleable.AnimateState_android_drawable, 0);
        mIsRepeatable = a.getBoolean(R.styleable.AnimateState_android_isRepeatable, false);
        if (TextUtils.isEmpty(mPath) && mDrawableId == 0) {
            throw new IllegalArgumentException("你至少需要提供path或者drawable其中一个");
        }
        mStates = extractStateSet(attrs);
        a.recycle();
    }

    static AnimateState createFromXml(Context context, XmlPullParser parser, AttributeSet attrs) throws IOException, XmlPullParserException {
        String name = parser.getName();
        AnimateState state = null;
        if (TAG.equals(name)) {
            state = new AnimateState(context, attrs);
        }
        return state;
    }

    public LottieComposition getAnimation() {
        return mAnimation;
    }

    public Drawable getDrawable() {
        return mDrawable;
    }

    int[] getStates() {
        return mStates;
    }

    int[] extractStateSet(AttributeSet attrs) {
        int j = 0;
        final int numAttrs = attrs.getAttributeCount();
        int[] states = new int[numAttrs];
        for (int i = 0; i < numAttrs; i++) {
            final int stateResId = attrs.getAttributeNameResource(i);
            if (stateResId != 0 && stateResId != android.R.attr.path && stateResId != android.R.attr.drawable && stateResId != android.R.attr.isRepeatable) {
                states[j++] = attrs.getAttributeBooleanValue(i, false)
                        ? stateResId : -stateResId;
            }
        }
        states = StateSet.trimStateSet(states, j);
        return states;
    }

    public boolean isRepeatable() {
        return mIsRepeatable;
    }

    boolean isInflate() {
        return mIsInflate;
    }

    void inflate(final OnInflateListener listener) {
        if (mIsInflating) {
            return;
        }

        if (mIsInflate) {
            notifyInflate(listener);
            return;
        }
        mIsInflating = true;
        if (!TextUtils.isEmpty(mPath)) {
            try {
                LottieComposition.Factory.fromInputStream(mContext.getAssets().open(mPath), new OnCompositionLoadedListener() {
                    @Override
                    public void onCompositionLoaded(LottieComposition composition) {
                        mAnimation = composition;
                        mIsInflate = true;
                        notifyInflate(listener);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (mDrawableId > 0) {
            mDrawable = mContext.getDrawable(mDrawableId);
            mIsInflate = true;
            notifyInflate(listener);
        }
    }

    private void notifyInflate(OnInflateListener listener) {
        if (null != listener) {
            listener.onInflate();
        }
    }
}
