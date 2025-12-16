package com.jdb;

import com.jdb.storage.*;
import java.io.File;
import java.io.RandomAccessFile;

public class DBReader {
    public static void main(String[] args) {
        File dbFile = new File("data.db");

        if (!dbFile.exists()) {
            System.out.println("Error: data.db not found. Run Main.java first!");
            return;
        }

        // 1. Re-define the Schema (Must match Main.java)
        TupleDesc schema = new TupleDesc();
        schema.addField(Type.INT, "id");
        schema.addField(Type.STRING, "name");
        schema.addField(Type.INT, "age");

        System.out.println("Reading " + dbFile.length() + " bytes from data.db...\n");

        // 2. Print Header
        System.out.printf("%-10s %-20s %-10s%n", "ID", "NAME", "AGE");
        System.out.println("------------------------------------------");

        try {
            RandomAccessFile raf = new RandomAccessFile(dbFile, "r");
            
            // Matches Page.PAGE_SIZE (assuming 4096 or similar constant in your Page class)
            // If you haven't defined it, check Page.java (usually 4096)
            int pageSize = 4096; 
            
            long fileLength = raf.length();
            int numPages = (int) (fileLength / pageSize);

            for (int i = 0; i < numPages; i++) {
                // Read raw page bytes
                byte[] data = new byte[pageSize];
                raf.read(data);

                // Load into HeapPage
                Page rawPage = new Page(i, data);
                HeapPage heapPage = new HeapPage(rawPage);

                // --- KEY CHANGE HERE ---
                // Your HeapPage tracks the exact count of tuples.
                // We loop exactly from 0 to getNumTuples() - 1.
                int count = heapPage.getNumTuples();
                
                for (int slot = 0; slot < count; slot++) {
                    Tuple t = heapPage.getTuple(slot, schema);
                    
                    System.out.printf("%-10s %-20s %-10s%n", 
                        t.getField(0), 
                        t.getField(1), 
                        t.getField(2)
                    );
                }
            }
            raf.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("------------------------------------------");
        System.out.println("End of File.");
    }
}