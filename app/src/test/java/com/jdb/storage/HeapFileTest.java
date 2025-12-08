package com.jdb.storage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

public class HeapFileTest {
    private File tempFile = new File("test_db.dat");

    @AfterEach
    public void cleanup() {
        if (tempFile.exists()) {
            tempFile.delete(); // Clean up the test file after running
        }
    }

    @Test
    public void testWriteAndReadPage() throws IOException {
        HeapFile hf = new HeapFile(tempFile);

        // 1. Create a page (ID 0) and write some data
        Page p1 = new Page(0);
        p1.setInt(0, 999); // Write integer 999 at offset 0
        hf.writePage(p1);

        // 2. Create another page (ID 1)
        Page p2 = new Page(1);
        p2.setInt(0, 888);
        hf.writePage(p2);
        
        hf.close();

        // 3. Re-open the file (simulating a database restart)
        HeapFile hf2 = new HeapFile(tempFile);
        
        Page fetchedPage0 = hf2.readPage(0);
        Page fetchedPage1 = hf2.readPage(1);

        // 4. Verify data persisted
        assertEquals(999, fetchedPage0.getInt(0));
        assertEquals(888, fetchedPage1.getInt(0));
        
        hf2.close();
    }
}