package ke.tang.tab;

import android.content.Context;
import android.util.AttributeSet;
import android.util.StateSet;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by TangKe on 2017/3/9.
 */

public class AnimateStates {
    final static String TAG = "animate-states";
    private List<AnimateState> mAnimateStateList = new ArrayList<>();

    private boolean mIsInflate;
    private boolean mIsInflating;
    private int mInflatingCount;

    static AnimateStates createFromXml(Context context, XmlPullParser parser, AttributeSet attr) throws IOException, XmlPullParserException {
        String name = parser.getName();
        AnimateStates states = null;
        if (TAG.equals(name)) {
            states = new AnimateStates();

            final int innerDepth = parser.getDepth() + 1;
            int depth, type;
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT && ((depth = parser.getDepth()) >= innerDepth || type != XmlPullParser.END_TAG)) {
                if (type != XmlPullParser.START_TAG) {
                    continue;
                }

                if (depth > innerDepth) {
                    continue;
                }

                if (parser.getName().equals(AnimateState.TAG)) {
                    states.mAnimateStateList.add(AnimateState.createFromXml(context, parser, attr));
                } else {
                    continue;
                }

            }
        }
        return states;
    }

    public void addState(AnimateState state) {
        mAnimateStateList.add(state);
    }

    public void clearStates() {
        mAnimateStateList.clear();
    }

    public AnimateState match(int[] states) {
        for (AnimateState animateState : mAnimateStateList) {
            if (StateSet.stateSetMatches(animateState.getStates(), states)) {
                return animateState;
            }
        }
        return null;
    }

    boolean isInflate() {
        return mIsInflate;
    }

    void inflate(final OnInflateListener listener) {
        if (mIsInflating) {
            return;
        }
        if (mIsInflate || mAnimateStateList.isEmpty()) {
            if (null != listener) {
                listener.onInflate();
            }
            return;
        }
        mIsInflating = true;
        mInflatingCount = mAnimateStateList.size();
        for (AnimateState state : mAnimateStateList) {
            state.inflate(new OnInflateListener() {
                @Override
                public void onInflate() {
                    mInflatingCount--;
                    if (0 >= mInflatingCount && null != listener) {
                        mIsInflate = true;
                        listener.onInflate();
                    }
                }
            });
        }
    }
}
