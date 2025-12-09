package com.jdb.buffer;

import com.jdb.storage.HeapFile;
import com.jdb.storage.Page;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

public class BufferManagerTest {
    private File tempFile = new File("buffer_test.dat");

    @AfterEach
    public void cleanup() {
        if (tempFile.exists()) tempFile.delete();
    }

    @Test
    public void testCacheEvictionAndDirtyWrite() {
        HeapFile hf = new HeapFile(tempFile);
        
        // Create a Buffer Manager that can hold ONLY 1 Page
        BufferManager bm = new BufferManager(hf, 1);

        // 1. Create Page 0 and modify it
        Page p0 = new Page(0);
        p0.setInt(0, 100);
        hf.writePage(p0); // Initial write to setup file

        // 2. Load Page 0 into Cache
        Page cachedP0 = bm.getPage(0);
        assertEquals(100, cachedP0.getInt(0));

        // 3. Modify Page 0 in Memory and mark DIRTY
        cachedP0.setInt(0, 200);
        bm.setPageDirty(0, true);

        // 4. Request Page 1 (This forces Page 0 to be EVICTED because cache size is 1)
        // Since Page 0 was dirty, the BufferManager should auto-save it to disk.
        Page p1 = new Page(1);
        p1.setInt(0, 999);
        hf.writePage(p1); // Setup Page 1 on disk
        bm.getPage(1);    // Load Page 1, Evict Page 0

        // 5. Verify Page 0 was actually written to disk
        HeapFile hfCheck = new HeapFile(tempFile);
        Page diskP0 = hfCheck.readPage(0);
        
        assertEquals(200, diskP0.getInt(0), "Page 0 should have been updated on disk upon eviction");
        
        try { hf.close(); } catch(Exception e){}
        try { hfCheck.close(); } catch(Exception e){}
    }
}