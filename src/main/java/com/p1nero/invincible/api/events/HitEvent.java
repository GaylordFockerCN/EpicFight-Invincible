package com.p1nero.invincible.api.events;

import com.p1nero.invincible.api.Side;

public class HitEvent extends BaseEvent{

    public final int phaseIndex;

    public HitEvent(int phaseIndex, BaseConsumer consumer, Side side) {
        super(consumer, side);
        this.phaseIndex = phaseIndex;
    }

    public HitEvent(int phaseIndex, BaseConsumer event) {
        super(event);
        this.phaseIndex = phaseIndex;
    }

    public HitEvent(BaseConsumer event, Side side) {
        super(event, side);
        this.phaseIndex = -1;
    }

    public HitEvent(BaseConsumer event) {
        super(event);
        this.phaseIndex = -1;
    }

}
