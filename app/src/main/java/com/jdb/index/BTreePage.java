package com.jdb.index;

import com.jdb.storage.Page;

/**
 * Base class for both Leaf and Internal B+ Tree nodes.
 * * Header Layout:
 * [0-3]: Page Type (0=Internal, 1=Leaf)
 * [4-7]: Current Key Count
 * [8-11]: Max Key Capacity
 */
public class BTreePage {
    protected Page page;
    
    // Offsets for the header
    private static final int OFFSET_TYPE = 0;
    private static final int OFFSET_COUNT = 4;
    private static final int OFFSET_MAX = 8;
    protected static final int HEADER_SIZE = 12; // Bytes used by header

    public static final int TYPE_INTERNAL = 0;
    public static final int TYPE_LEAF = 1;

    public BTreePage(Page page, int type, int maxCapacity) {
        this.page = page;
        // Initialize header if count is 0 (new page)
        if (getKeyCount() == 0) {
            setPageType(type);
            setMaxCapacity(maxCapacity);
            setKeyCount(0);
        }
    }

    public int getPageType() {
        return page.getInt(OFFSET_TYPE);
    }
    
    public void setPageType(int type) {
        page.setInt(OFFSET_TYPE, type);
    }

    public int getKeyCount() {
        return page.getInt(OFFSET_COUNT);
    }

    public void setKeyCount(int count) {
        page.setInt(OFFSET_COUNT, count);
    }

    public int getMaxCapacity() {
        return page.getInt(OFFSET_MAX);
    }
    
    public void setMaxCapacity(int max) {
        page.setInt(OFFSET_MAX, max);
    }
    
    public boolean isLeaf() {
        return getPageType() == TYPE_LEAF;
    }
}