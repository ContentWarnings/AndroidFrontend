package com.example.moviementor.other;

import androidx.annotation.Nullable;

import com.example.moviementor.fragments.BaseFragment;

import java.util.Stack;

public class FragmentStack {
    private Stack<BaseFragment> fragmentStack;

    public FragmentStack() {
        this.fragmentStack = new Stack<>();
    }

    public boolean push(final BaseFragment fragment) {
        if (fragment == null) {
            return false;
        }
        fragmentStack.push(fragment);
        return true;
    }

    @Nullable
    public BaseFragment pop() {
        if (fragmentStack.size() <= 1) {
            return null;
        }
        return fragmentStack.pop();
    }

    @Nullable
    public BaseFragment getTopFragment() {
        if (fragmentStack.isEmpty()) {
            return null;
        }
        return fragmentStack.peek();
    }

    public boolean canGoBack() {
        return fragmentStack.size() > 1;
    }
}
