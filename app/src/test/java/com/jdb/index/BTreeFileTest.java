package com.jdb.index;

import com.jdb.buffer.BufferManager;
import com.jdb.storage.HeapFile;
import com.jdb.storage.Page;
import com.jdb.storage.RecordId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

public class BTreeFileTest {
    private File tempFile = new File("btree_index.dat");

    @AfterEach
    public void cleanup() {
        if (tempFile.exists()) tempFile.delete();
    }

    @Test
    public void testFullIndexOperations() {
        // 1. Setup the Infrastructure (Disk + Cache)
        HeapFile disk = new HeapFile(tempFile);
        
        // Create a file with at least 1 page (Root Page 0)
        Page p0 = new Page(0);
        disk.writePage(p0); 
        
        BufferManager bm = new BufferManager(disk, 10); // 10 Pages in RAM

        // 2. Initialize the B+ Tree on Page 0
        BTreeFile btree = new BTreeFile(bm, 0);

        // 3. Insert Data
        // We are saying: "Key 100 is stored at Page 5, Slot 1"
        btree.insert(100, new RecordId(5, 1));
        btree.insert(50, new RecordId(2, 8));
        btree.insert(200, new RecordId(9, 3));

        // 4. Force Flush (Simulate saving to disk and clearing RAM)
        bm.flushAll();

        // 5. Restart / Search
        // We create a NEW instance to ensure we are reading from the 'disk' (via BufferManager)
        BTreeFile btreeReader = new BTreeFile(bm, 0);
        
        RecordId result = btreeReader.find(50);
        
        assertNotNull(result);
        assertEquals(2, result.pageId);
        assertEquals(8, result.slotNumber);
        
        // Verify 200
        assertEquals(9, btreeReader.find(200).pageId);
    }
}