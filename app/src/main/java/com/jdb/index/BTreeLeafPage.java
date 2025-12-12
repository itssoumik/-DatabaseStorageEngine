package com.jdb.index;

import com.jdb.storage.Page;
import com.jdb.storage.RecordId;

/**
 * Leaf Node Layout:
 * [Header]
 * [Key 1][PageID 1][Slot 1]
 * [Key 2][PageID 2][Slot 2]
 * ...
 */
public class BTreeLeafPage extends BTreePage {
    // Each entry is: 4 bytes (Key) + 4 bytes (PageId) + 4 bytes (SlotNum) = 12 bytes
    private static final int ENTRY_SIZE = 12;

    public BTreeLeafPage(Page page) {
        // Calculate max capacity based on page size (4096 - 12) / 12 ~= 340 keys
        super(page, TYPE_LEAF, (Page.PAGE_SIZE - HEADER_SIZE) / ENTRY_SIZE);
    }

    /**
     * Inserts a key and recordId in SORTED order.
     */
    public void insert(int key, RecordId rid) {
        int count = getKeyCount();
        if (count >= getMaxCapacity()) {
            throw new RuntimeException("Leaf Page is full! (Split logic needed)");
        }

        // 1. Find the position to insert (Simple Linear Search for now)
        int i = count - 1;
        while (i >= 0 && getKeyAt(i) > key) {
            // Shift entry to the right
            copyEntry(i, i + 1);
            i--;
        }

        // 2. Insert new entry
        int targetIndex = i + 1;
        setKeyAt(targetIndex, key);
        setValueAt(targetIndex, rid);

        // 3. Update count
        setKeyCount(count + 1);
    }

    /**
     * Returns the RecordId for a given key, or null if not found.
     */
    public RecordId lookup(int key) {
        for (int i = 0; i < getKeyCount(); i++) {
            if (getKeyAt(i) == key) {
                return getValueAt(i);
            }
        }
        return null;
    }

    // --- Helpers to read/write specific slots ---

    private int getKeyAt(int index) {
        int offset = HEADER_SIZE + (index * ENTRY_SIZE);
        return page.getInt(offset);
    }

    private void setKeyAt(int index, int key) {
        int offset = HEADER_SIZE + (index * ENTRY_SIZE);
        page.setInt(offset, key);
    }

    private RecordId getValueAt(int index) {
        int offset = HEADER_SIZE + (index * ENTRY_SIZE) + 4; // Skip Key (4 bytes)
        int pageId = page.getInt(offset);
        int slotNum = page.getInt(offset + 4);
        return new RecordId(pageId, slotNum);
    }

    private void setValueAt(int index, RecordId rid) {
        int offset = HEADER_SIZE + (index * ENTRY_SIZE) + 4;
        page.setInt(offset, rid.pageId);
        page.setInt(offset + 4, rid.slotNumber);
    }

    private void copyEntry(int fromIndex, int toIndex) {
        setKeyAt(toIndex, getKeyAt(fromIndex));
        setValueAt(toIndex, getValueAt(fromIndex));
    }
}