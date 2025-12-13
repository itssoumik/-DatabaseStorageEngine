package com.jdb.index;

import com.jdb.buffer.BufferManager;
import com.jdb.storage.Page;
import com.jdb.storage.RecordId;

public class BTreeFile {
    private final BufferManager bufferManager;
    private int rootPageId;

    public BTreeFile(BufferManager bufferManager, int rootPageId) {
        this.bufferManager = bufferManager;
        this.rootPageId = rootPageId;
        initRootIfNeeded();
    }

    private void initRootIfNeeded() {
        // (Same as before: Initialize empty root if needed)
        try {
            Page p = bufferManager.getPage(rootPageId);
            if (p.getInt(0) == 0 && p.getInt(4) == 0) {
                BTreeLeafPage leaf = new BTreeLeafPage(p);
                leaf.setPageType(BTreePage.TYPE_LEAF);
                bufferManager.setPageDirty(rootPageId, true);
            }
        } catch (Exception e) {}
    }

    public RecordId find(int key) {
        // (Same as before)
        int currentPageId = rootPageId;
        while (true) {
            Page rawPage = bufferManager.getPage(currentPageId);
            int pageType = rawPage.getInt(0);

            if (pageType == BTreePage.TYPE_LEAF) {
                return new BTreeLeafPage(rawPage).lookup(key);
            } else {
                currentPageId = new BTreeInternalPage(rawPage).lookup(key);
            }
        }
    }

    /**
     * The Main Insert Entry Point
     */
    public void insert(int key, RecordId rid) {
        // recursiveInsert returns a PushUpEntry if the root splits
        PushUpEntry result = insertRecursive(rootPageId, key, rid);

        if (result != null) {
            // ROOT SPLIT! The tree grows in height.
            createNewRoot(result);
        }
    }

    /**
     * Recursive helper. Returns PushUpEntry if the child split, null otherwise.
     */
    private PushUpEntry insertRecursive(int currentPageId, int key, RecordId rid) {
        Page rawPage = bufferManager.getPage(currentPageId);
        int pageType = rawPage.getInt(0);

        if (pageType == BTreePage.TYPE_LEAF) {
            return handleLeafInsert(rawPage, key, rid);
        } else {
            return handleInternalInsert(rawPage, key, rid);
        }
    }

    private PushUpEntry handleLeafInsert(Page rawPage, int key, RecordId rid) {
        BTreeLeafPage leaf = new BTreeLeafPage(rawPage);

        // Case 1: Leaf has space. Just insert.
        if (leaf.getKeyCount() < leaf.getMaxCapacity()) {
            leaf.insert(key, rid);
            bufferManager.setPageDirty(leaf.page.getPageId(), true);
            return null; // No split occurred
        }

        // Case 2: Leaf is full. SPLIT.
        // A. Allocate new page
        int newPageId = bufferManager.allocateNewPage();
        Page newRawPage = bufferManager.getPage(newPageId);
        BTreeLeafPage newLeaf = new BTreeLeafPage(newRawPage);

        // B. Insert the new key into the correct page (Old or New) temp?
        // Simplified: We split first, then insert the new key into the correct half.
        // Note: For simplicity in this tutorial, we assume the new key fits after split.
        
        int splitKey = leaf.split(newLeaf);

        // Decide where to put the new value
        if (key >= splitKey) {
            newLeaf.insert(key, rid);
        } else {
            leaf.insert(key, rid);
        }

        bufferManager.setPageDirty(leaf.page.getPageId(), true);
        bufferManager.setPageDirty(newLeaf.page.getPageId(), true);

        // C. Return the notification to the parent
        return new PushUpEntry(splitKey, newPageId);
    }

    private PushUpEntry handleInternalInsert(Page rawPage, int key, RecordId rid) {
        BTreeInternalPage internal = new BTreeInternalPage(rawPage);
        int childPageId = internal.lookup(key);

        // Recursively go down
        PushUpEntry result = insertRecursive(childPageId, key, rid);

        // If child didn't split, we are done.
        if (result == null) return null;

        // If child DID split, we must insert the pushed-up key into THIS internal node
        if (internal.getKeyCount() < internal.getMaxCapacity()) {
            internal.insert(result.key, result.childPageId);
            bufferManager.setPageDirty(internal.page.getPageId(), true);
            return null;
        } else {
            // THIS internal node is full too! We need to implement Internal Split.
            // For now, let's throw exception to keep this step digestible.
            // (Implementing Internal Split is 80% similar to Leaf Split)
            throw new RuntimeException("Internal Node Full - Depth > 2 not implemented yet!");
        }
    }

    private void createNewRoot(PushUpEntry result) {
        // 1. Allocate a new Page for the new Root
        int newRootId = bufferManager.allocateNewPage();
        Page newRootRaw = bufferManager.getPage(newRootId);
        BTreeInternalPage newRoot = new BTreeInternalPage(newRootRaw);

        // 2. Point the new root to the Old Root (Left) and New Child (Right)
        newRoot.setPointer(0, rootPageId); // Old Root becomes left child
        newRoot.insert(result.key, result.childPageId); // PushUp key points to right child

        // 3. Update the global root pointer
        this.rootPageId = newRootId;
        bufferManager.setPageDirty(newRootId, true);
        
        // NOTE: In a real DB, you must update the "Header Page" on disk to save the new root ID.
        System.out.println("Tree grew! New Root ID: " + rootPageId);
    }
}