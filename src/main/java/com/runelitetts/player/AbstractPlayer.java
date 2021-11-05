package com.runelitetts.player;

import java.io.IOException;

public abstract class AbstractPlayer {

    public abstract void play() throws IOException;

    public abstract void stop();

    public abstract void await();
}
