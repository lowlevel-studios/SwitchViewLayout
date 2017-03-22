package st.lowlevel.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.AnimRes;
import android.support.annotation.AttrRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import st.lowlevel.switchviewlayout.R;

public class SwitchViewLayout extends FrameLayout {

    public interface OnViewChangeListener {
        void onViewChange(@NonNull SwitchViewLayout view, int identifier);
    }

    private final SparseArray<View> mViewMap = new SparseArray<>();

    private boolean mAnimationEnabled;
    private Animation mAnimationEnter;
    private Animation mAnimationExit;
    private int mCurrentView = -1;
    private OnViewChangeListener mOnViewChangeListener;

    public SwitchViewLayout(@NonNull Context context) {
        this(context, null);
    }

    public SwitchViewLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwitchViewLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs);
    }

    private void addViewIfNeeded(@NonNull View view) {
        ViewParent parent = view.getParent();

        if (parent == null) {
            addView(view);
            return;
        }

        if (parent != this) {
            throw new IllegalArgumentException(
                    "The given view must be a child of this SwitchViewLayout or have no parent.");
        }
    }

    private View inflate(@LayoutRes int resId) {
        if (resId <= 0) {
            return null;
        }

        return inflate(getContext(), resId, null);
    }

    private void initialize(@NonNull Context context, @Nullable AttributeSet attrs) {
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SwitchViewLayout);

            setAnimationEnabled(
                    ta.getBoolean(R.styleable.SwitchViewLayout_animationEnabled, false));
            setEnterAnimation(
                    ta.getResourceId(R.styleable.SwitchViewLayout_animationEnter, R.anim.svl_fade_in));
            setExitAnimation(
                    ta.getResourceId(R.styleable.SwitchViewLayout_animationExit, R.anim.svl_fade_out));

            ta.recycle();
        }
    }

    private Animation loadAnimation(@AnimRes int resId) {
        if (resId <= 0) {
            return null;
        }

        return AnimationUtils.loadAnimation(getContext(), resId);
    }

    private void setVisibility(@Nullable View view, boolean show, boolean animate) {
        int newVisibility = (show ? VISIBLE : GONE);

        if (view == null || view.getVisibility() == newVisibility) {
            return;
        }

        view.setVisibility(newVisibility);

        if (!mAnimationEnabled || !animate) {
            return;
        }

        switch (newVisibility) {
        case GONE:
            startAnimation(view, mAnimationExit);
            break;

        case VISIBLE:
            startAnimation(view, mAnimationEnter);
            break;
        }
    }

    private void showView(int identifier) {
        int size = mViewMap.size();

        for (int i = 0; i < size; i++) {
            boolean show = (mViewMap.keyAt(i) == identifier);
            setVisibility(mViewMap.valueAt(i), show, true);
        }
    }

    private void startAnimation(@NonNull View view, @Nullable Animation anim) {
        view.clearAnimation();

        if (anim != null) {
            view.startAnimation(anim);
        }
    }

    /**
     * Adds a new view from a resource id
     *
     * @param identifier the view identifier
     * @param resId the view resource id
     * @return SwitchViewLayout
     */
    public SwitchViewLayout addView(int identifier, @IdRes int resId) {
        return addView(identifier, findViewById(resId));
    }

    /**
     * Adds a new view from the given instance
     *
     * @param identifier the view identifier
     * @param view the view instance
     * @return SwitchViewLayout
     */
    public SwitchViewLayout addView(int identifier, @Nullable View view) {
        View oldView = mViewMap.get(identifier);

        if (view == null || view == oldView) {
            return this;
        }

        if (oldView != null) {
            removeView(oldView);
        }

        addViewIfNeeded(view);
        setVisibility(view, isCurrentView(identifier), false);

        mViewMap.put(identifier, view);

        return this;
    }

    /**
     * Adds a new view from a layout resource
     *
     * @param identifier the view identifier
     * @param resId the layout resource
     * @return SwitchViewLayout
     */
    public SwitchViewLayout addViewFromLayout(int identifier, @LayoutRes int resId) {
        return addView(identifier, inflate(resId));
    }

    /**
     * Gets the current view identifier
     *
     * @return the current view identifier or -1 if no view is active
     */
    public int getCurrentView() {
        return mCurrentView;
    }

    /**
     * Checks if a view exists with the given identifier
     *
     * @param identifier the view identifier
     * @return true if a view exists
     */
    public boolean hasView(int identifier) {
        return (mViewMap.get(identifier) != null);
    }

    /**
     * Checks if the identifier belongs to the current view
     *
     * @param identifier the view identifier
     * @return true if the identifier matches
     */
    public boolean isCurrentView(int identifier) {
        return (mCurrentView == identifier);
    }

    /**
     * Removes the view with the given identifier
     *
     * @param identifier the view identifier
     */
    public void removeView(int identifier) {
        View view = mViewMap.get(identifier);

        if (view != null) {
            removeView(view);
        }

        mViewMap.remove(identifier);
    }

    /**
     * Sets the transition animation state
     *
     * @param enabled the desired state
     * @return SwitchViewLayout
     */
    public SwitchViewLayout setAnimationEnabled(boolean enabled) {
        mAnimationEnabled = enabled;
        return this;
    }

    /**
     * Sets the view enter animation
     *
     * @param anim the animation instance or null
     * @return SwitchViewLayout
     */
    public SwitchViewLayout setEnterAnimation(@Nullable Animation anim) {
        mAnimationEnter = anim;
        return this;
    }

    /**
     * Sets the view enter animation
     *
     * @param resId the animation resource
     * @return SwitchViewLayout
     */
    public SwitchViewLayout setEnterAnimation(@AnimRes int resId) {
        return setEnterAnimation(loadAnimation(resId));
    }

    /**
     * Sets the view exit animation
     *
     * @param anim the animation instance or null
     * @return SwitchViewLayout
     */
    public SwitchViewLayout setExitAnimation(@Nullable Animation anim) {
        mAnimationExit = anim;
        return this;
    }

    /**
     * Sets the view exit animation
     *
     * @param resId the animation resource
     * @return SwitchViewLayout
     */
    public SwitchViewLayout setExitAnimation(@AnimRes int resId) {
        return setExitAnimation(loadAnimation(resId));
    }

    /**
     * Sets the listener to notify view changes
     *
     * @param listener the listener instance or null
     * @return SwitchViewLayout
     */
    public SwitchViewLayout setOnViewChangeListener(@Nullable OnViewChangeListener listener) {
        mOnViewChangeListener = listener;
        return this;
    }

    /**
     * Switches the active view in the layout
     *
     * @param identifier the view identifier
     */
    public void switchView(int identifier) {
        if (isCurrentView(identifier)) {
            return;
        }

        showView(identifier);

        mCurrentView = identifier;

        if (mOnViewChangeListener != null) {
            mOnViewChangeListener.onViewChange(this, identifier);
        }
    }
}
