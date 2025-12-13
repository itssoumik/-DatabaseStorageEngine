package com.jdb.index;

public class PushUpEntry {
    public int key;
    public int childPageId;

    public PushUpEntry(int key, int childPageId) {
        this.key = key;
        this.childPageId = childPageId;
    }
}