package com.jdb.storage;

import java.nio.charset.StandardCharsets;

public enum Type {
    INT,
    STRING;

    /**
     * Returns the size in bytes. 
     * Note: STRING is variable length, so this is just a placeholder or base overhead.
     */
    public int getLen() {
        if (this == INT) return 4;
        return 0; // Variable length
    }

    /**
     * Helper to parse bytes into a Java Object based on type.
     */
    public Object parse(byte[] data, int offset) {
        if (this == INT) {
            return (data[offset] << 24) | ((data[offset + 1] & 0xFF) << 16) |
                   ((data[offset + 2] & 0xFF) << 8) | (data[offset + 3] & 0xFF);
        } else {
            // Strings are stored as [Length (4 bytes)] + [Characters...]
            // We assume the caller handles the length reading for Strings separately
            // or provides the raw byte chunk.
            return new String(data, StandardCharsets.UTF_8);
        }
    }
}