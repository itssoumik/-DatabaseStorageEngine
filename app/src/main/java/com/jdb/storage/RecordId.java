package com.jdb.storage;

public class RecordId {
    public int pageId;
    public int slotNumber;

    public RecordId(int pageId, int slotNumber) {
        this.pageId = pageId;
        this.slotNumber = slotNumber;
    }
}