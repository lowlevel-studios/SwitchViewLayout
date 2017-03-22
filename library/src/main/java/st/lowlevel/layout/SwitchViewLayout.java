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
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import st.lowlevel.switchviewlayout.R;

public class SwitchViewLayout extends FrameLayout {

    public interface OnViewChangeListener {
        void onViewChange(@NonNull SwitchViewLayout view, int id);
    }

    private Animation mAnimationEnter;
    private Animation mAnimationExit;
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

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        child.setVisibility(GONE);
    }

    private View findChild(@IdRes int id) {
        for (View child : getChildren()) {
            if (child.getId() == id) {
                return child;
            }
        }

        return null;
    }

    @NonNull
    private List<View> getChildren() {
        List<View> list = new ArrayList<>();

        for (int i = 0; i < getChildCount(); i++) {
            list.add(getChildAt(i));
        }

        return list;
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
        int newVisibility = show ? VISIBLE : GONE;

        if (view == null || view.getVisibility() == newVisibility) {
            return;
        }

        view.setVisibility(newVisibility);

        if (!animate) {
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

    private void showView(int id, boolean animate) {
        for (View child : getChildren()) {
            setVisibility(child, (child.getId() == id), animate);
        }
    }

    private void startAnimation(@NonNull View view, @Nullable Animation anim) {
        view.clearAnimation();

        if (anim != null) {
            view.startAnimation(anim);
        }
    }

    /**
     * Adds a new view from the given instance
     *
     * @param id the view id
     * @param view the view instance
     * @return SwitchViewLayout
     */
    public SwitchViewLayout addView(int id, @Nullable View view) {
        View oldView = findChild(id);

        if (view == null || view == oldView) {
            return this;
        }

        if (oldView != null) {
            removeView(oldView);
        }

        view.setId(id);
        addView(view);

        return this;
    }

    /**
     * Adds a new view from the given layout resource
     *
     * @param id the view id
     * @param resId the layout resource
     * @return SwitchViewLayout
     */
    public SwitchViewLayout addView(int id, @LayoutRes int resId) {
        return addView(id, inflate(resId));
    }

    /**
     * Gets the current view id
     *
     * @return the current view id or -1 if no view is active
     */
    public int getCurrentView() {
        for (View child : getChildren()) {
            if (child.getVisibility() == VISIBLE) {
                return child.getId();
            }
        }

        return -1;
    }

    /**
     * Checks if a view exists with the given id
     *
     * @param id the view id
     * @return true if a view exists
     */
    public boolean hasView(int id) {
        return (findChild(id) != null);
    }

    /**
     * Checks if the id belongs to the current view
     *
     * @param id the view id
     * @return true if the id matches
     */
    public boolean isCurrentView(int id) {
        return (getCurrentView() == id);
    }

    /**
     * Checks if the given view is the current view
     *
     * @param view the view instance
     * @return true if the view matches
     */
    public boolean isCurrentView(@NonNull View view) {
        return isCurrentView(view.getId());
    }

    /**
     * Removes the view with the given id
     *
     * @param id the view id
     */
    public void removeView(int id) {
        View view = findChild(id);

        if (view != null) {
            removeView(view);
        }
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
     * @param id the view id
     */
    public void switchView(int id) {
        switchView(id, false);
    }

    /**
     * Switches the active view in the layout
     *
     * @param id the view id
     * @param animate true if the transition should be animated
     */
    public void switchView(int id, boolean animate) {
        if (isCurrentView(id)) {
            return;
        }

        showView(id, animate);

        if (mOnViewChangeListener != null) {
            mOnViewChangeListener.onViewChange(this, id);
        }
    }
}
