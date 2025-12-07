package com.jdb.storage;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PageTest {

    @Test
    public void testPageData() {
        Page p = new Page(1);
        
        // Test writing an integer to the beginning of the page
        p.setInt(0, 12345);
        
        // Test reading it back
        assertEquals(12345, p.getInt(0));
    }
}