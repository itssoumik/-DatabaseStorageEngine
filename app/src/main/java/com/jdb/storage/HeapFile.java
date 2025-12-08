package com.jdb.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class HeapFile {
    private File file;
    private RandomAccessFile raf;

    public HeapFile(File f) {
        this.file = f;
        try {
            // "rw" mode allows both reading and writing
            this.raf = new RandomAccessFile(f, "rw");
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Could not open DB file: " + f.getPath(), e);
        }
    }

    /**
     * Reads a specific page from the disk.
     */
    public Page readPage(int pageId) {
        Page p = new Page(pageId);
        int offset = pageId * Page.PAGE_SIZE;
        
        try {
            if (offset + Page.PAGE_SIZE > raf.length()) {
                throw new IllegalArgumentException("Page " + pageId + " does not exist in file.");
            }
            
            raf.seek(offset);
            raf.readFully(p.getData()); // Fill the page's byte buffer
            return p;
        } catch (IOException e) {
            throw new RuntimeException("Error reading page " + pageId, e);
        }
    }

    /**
     * Writes a page to the disk.
     */
    public void writePage(Page p) {
        int offset = p.getPageId() * Page.PAGE_SIZE;
        try {
            raf.seek(offset);
            raf.write(p.getData());
        } catch (IOException e) {
            throw new RuntimeException("Error writing page " + p.getPageId(), e);
        }
    }

    /**
     * Returns the number of pages currently in the file.
     */
    public int getNumPages() {
        try {
            return (int) (raf.length() / Page.PAGE_SIZE);
        } catch (IOException e) {
            throw new RuntimeException("Error getting file size", e);
        }
    }
    
    public void close() throws IOException {
        raf.close();
    }
}