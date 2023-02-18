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
        this.fragmentStack.push(fragment);
        return true;
    }

    @Nullable
    public BaseFragment pop() {
        if (this.fragmentStack.size() <= 1) {
            return null;
        }
        return this.fragmentStack.pop();
    }

    @Nullable
    public BaseFragment getTopFragment() {
        if (this.fragmentStack.isEmpty()) {
            return null;
        }
        return this.fragmentStack.peek();
    }

    public boolean canGoBack() {
        return this.fragmentStack.size() > 1;
    }

    public boolean isEmpty() {
        return this.fragmentStack.isEmpty();
    }

    public int size() {
        return this.fragmentStack.size();
    }
}
