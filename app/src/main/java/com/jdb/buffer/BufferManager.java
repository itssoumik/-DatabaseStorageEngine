package com.jdb.buffer;

import com.jdb.storage.HeapFile;
import com.jdb.storage.Page;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class BufferManager {
    private final HeapFile diskManager;
    private final Map<Integer, Page> pageCache;
    private final int maxPages;

    // Track which pages have been modified and need saving
    private final Map<Integer, Boolean> dirtyPages; 

    public BufferManager(HeapFile diskManager, int maxPages) {
        this.diskManager = diskManager;
        this.maxPages = maxPages;
        this.dirtyPages = new HashMap<>();

        // LinkedHashMap with accessOrder = true acts as an LRU Cache
        this.pageCache = new LinkedHashMap<>(maxPages, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, Page> eldest) {
                if (size() > BufferManager.this.maxPages) {
                    evictPage(eldest.getKey()); // Write to disk before kicking out
                    return true;
                }
                return false;
            }
        };
    }

    /**
     * Retrieves a page. If not in cache, loads it from disk.
     */
    public Page getPage(int pageId) {
        if (pageCache.containsKey(pageId)) {
            return pageCache.get(pageId);
        }

        // Not in cache? Load from disk
        Page p = diskManager.readPage(pageId);
        pageCache.put(pageId, p);
        return p;
    }

    /**
     * Marks a page as "Dirty" (modified). 
     * It will be written to disk when evicted or flushed.
     */
    public void setPageDirty(int pageId, boolean dirty) {
        if (pageCache.containsKey(pageId)) {
            dirtyPages.put(pageId, dirty);
        }
    }

    /**
     * Helper to write a specific page back to disk
     */
    private void evictPage(int pageId) {
        if (dirtyPages.getOrDefault(pageId, false)) {
            Page p = pageCache.get(pageId);
            if (p != null) {
                diskManager.writePage(p);
                dirtyPages.remove(pageId);
            }
        }
    }

    /**
     * Forces all dirty pages to disk (Call this on shutdown)
     */
    public void flushAll() {
        for (Integer pageId : pageCache.keySet()) {
            evictPage(pageId);
        }
        pageCache.clear();
    }

    public int allocateNewPage() {
        // Calculate new ID based on current file size
        int newPageId = diskManager.getNumPages();
        // Just reading it will create it in our specific HeapFile implementation logic
        // But explicitly, we should create a blank page
        Page p = new Page(newPageId);
        diskManager.writePage(p); 
        return newPageId;
    }
}