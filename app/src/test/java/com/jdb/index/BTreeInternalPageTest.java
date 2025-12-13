package com.jdb.index;

import com.jdb.storage.Page;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BTreeInternalPageTest {

    @Test
    public void testLookupNavigation() {
        // 1. Create Internal Node
        Page rawPage = new Page(2);
        BTreeInternalPage internal = new BTreeInternalPage(rawPage);

        // 2. Setup initial Leftmost Pointer (Index 0)
        // "Everything smaller than the first key goes to Page 100"
        internal.setKeyCount(1);
        internal.setPointer(0, 100); 

        // 3. Insert Separator Keys
        // Key 10 points to Page 200 (So range 10-20 goes to 200)
        internal.insert(10, 200);
        // Key 20 points to Page 300 (So range 20+ goes to 300)
        internal.insert(20, 300);

        // 4. Test Navigation
        
        // Case A: Look for 5 (Smaller than 10) -> Should go to Leftmost (100)
        assertEquals(100, internal.lookup(5));

        // Case B: Look for 15 (Between 10 and 20) -> Should go to 200
        assertEquals(200, internal.lookup(15));

        // Case C: Look for 25 (Larger than 20) -> Should go to 300
        assertEquals(300, internal.lookup(25));
        
        // Case D: Look for exact key 10 -> Should go to 200
        assertEquals(200, internal.lookup(10));
    }
}