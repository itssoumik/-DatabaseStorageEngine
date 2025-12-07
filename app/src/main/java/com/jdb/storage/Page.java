package com.jdb.storage;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Represents a fixed-size block of data in the database.
 * Standard Page Size: 4KB (4096 bytes)
 */
public class Page {
    public static final int PAGE_SIZE = 4096;
    private int pageId;
    private byte[] data;

    // Constructor for a new, empty page
    public Page(int pageId) {
        this.pageId = pageId;
        this.data = new byte[PAGE_SIZE];
    }

    // Constructor for loading existing data (from disk)
    public Page(int pageId, byte[] data) {
        this.pageId = pageId;
        if (data.length != PAGE_SIZE) {
            throw new IllegalArgumentException("Data must be exactly " + PAGE_SIZE + " bytes");
        }
        this.data = data;
    }

    public int getPageId() {
        return pageId;
    }

    /**
     * Get the raw byte array (for writing to disk)
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Helper to write an integer at a specific offset
     */
    public void setInt(int offset, int value) {
        // Use ByteBuffer to easily convert int to bytes
        ByteBuffer.wrap(data).putInt(offset, value);
    }

    /**
     * Helper to read an integer from a specific offset
     */
    public int getInt(int offset) {
        return ByteBuffer.wrap(data).getInt(offset);
    }

    @Override
    public String toString() {
        return "Page{id=" + pageId + "}";
    }
}