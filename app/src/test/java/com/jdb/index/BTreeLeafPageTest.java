package com.jdb.index;

import com.jdb.storage.Page;
import com.jdb.storage.RecordId;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BTreeLeafPageTest {

    @Test
    public void testInsertAndSortedLookup() {
        // 1. Create a raw page and wrap it as a Leaf Node
        Page rawPage = new Page(1);
        BTreeLeafPage leaf = new BTreeLeafPage(rawPage);

        // Verify it initialized as a Leaf (Type 1)
        assertEquals(1, leaf.getPageType());
        assertEquals(0, leaf.getKeyCount());

        // 2. Insert keys out of order: 50, 10, 30
        leaf.insert(50, new RecordId(5, 0));
        leaf.insert(10, new RecordId(1, 0));
        leaf.insert(30, new RecordId(3, 0));

        // 3. Verify Count
        assertEquals(3, leaf.getKeyCount());

        // 4. Verify Sorting: The keys inside should be 10, 30, 50
        // We check this by looking them up
        RecordId r1 = leaf.lookup(10);
        RecordId r2 = leaf.lookup(30);
        RecordId r3 = leaf.lookup(50);

        assertNotNull(r1);
        assertNotNull(r2);
        assertNotNull(r3);

        assertEquals(1, r1.pageId); // Key 10 linked to Page 1
        assertEquals(3, r2.pageId); // Key 30 linked to Page 3
        assertEquals(5, r3.pageId); // Key 50 linked to Page 5
        
        // 5. Verify a non-existent key returns null
        assertNull(leaf.lookup(999));
    }
}