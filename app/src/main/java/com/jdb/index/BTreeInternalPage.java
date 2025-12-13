package com.jdb.index;

import com.jdb.storage.Page;

/**
 * Internal Node Layout:
 * [Header]
 * [Key 0 (Invalid)][Child PageID 0]  <-- The "Leftmost" pointer
 * [Key 1]          [Child PageID 1]
 * [Key 2]          [Child PageID 2]
 * ...
 */
public class BTreeInternalPage extends BTreePage {
    // Entry = 4 bytes (Key) + 4 bytes (PageId) = 8 bytes
    private static final int ENTRY_SIZE = 8;

    public BTreeInternalPage(Page page) {
        // Calculate max capacity: (4096 - 12) / 8 ~= 510 entries
        super(page, TYPE_INTERNAL, (Page.PAGE_SIZE - HEADER_SIZE) / ENTRY_SIZE);
    }

    /**
     * Finds the Child PageID that *might* contain the key.
     * Logic: Find the largest key in this node that is <= searchKey.
     */
    public int lookup(int key) {
        int count = getKeyCount();
        
        // Start checking from the last key and move backwards (simple logic)
        // We skip index 0 because it's the leftmost dummy key usually
        for (int i = count - 1; i > 0; i--) {
            if (key >= getKeyAt(i)) {
                return getValueAt(i);
            }
        }
        
        // If smaller than all keys, return the leftmost pointer (index 0)
        return getValueAt(0);
    }

    /**
     * Inserts a new key and child pointer in SORTED order.
     * Note: This is usually called when a child node splits and pushes a key up.
     */
    public void insert(int key, int childPageId) {
        int count = getKeyCount();
        if (count >= getMaxCapacity()) {
            throw new RuntimeException("Internal Page is full!");
        }

        // 1. Find position
        int i = count - 1;
        while (i > 0 && getKeyAt(i) > key) {
            copyEntry(i, i + 1);
            i--;
        }

        // 2. Insert
        int targetIndex = i + 1;
        setKeyAt(targetIndex, key);
        setValueAt(targetIndex, childPageId);

        // 3. Update count
        setKeyCount(count + 1);
    }

    /**
     * Sets the pointer for the very first entry (Leftmost child).
     * The key at index 0 is technically ignored/dummy.
     */
    public void setPointer(int index, int childPageId) {
        setValueAt(index, childPageId);
    }

    // --- Helpers ---

    private int getKeyAt(int index) {
        int offset = HEADER_SIZE + (index * ENTRY_SIZE);
        return page.getInt(offset);
    }

    private void setKeyAt(int index, int key) {
        int offset = HEADER_SIZE + (index * ENTRY_SIZE);
        page.setInt(offset, key);
    }

    private int getValueAt(int index) {
        int offset = HEADER_SIZE + (index * ENTRY_SIZE) + 4; // Skip Key (4 bytes)
        return page.getInt(offset);
    }

    private void setValueAt(int index, int childPageId) {
        int offset = HEADER_SIZE + (index * ENTRY_SIZE) + 4;
        page.setInt(offset, childPageId);
    }

    private void copyEntry(int fromIndex, int toIndex) {
        setKeyAt(toIndex, getKeyAt(fromIndex));
        setValueAt(toIndex, getValueAt(fromIndex));
    }
}