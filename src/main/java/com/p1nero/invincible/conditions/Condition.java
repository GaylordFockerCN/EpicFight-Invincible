package com.p1nero.invincible.conditions;

public interface Condition<T> {
    boolean predicate(T target);
}