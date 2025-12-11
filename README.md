# **JDB-Core: Java Database Storage Engine**

A high-performance, bare-metal database storage engine built **from scratch in pure Java**, designed to mimic the internals of industrial-grade databases like PostgreSQL and MySQL—without implementing SQL.  
JDB-Core focuses entirely on **disk I/O**, **paging**, **memory management**, and **indexing**, providing a foundation for building higher-level database systems.

---

## **Introduction**
**JDB-Core** is a low-level storage engine written in pure Java with **zero external database dependencies**.  
It answers the fundamental question behind database internals:

> **How do we access terabytes of data when we only have gigabytes of RAM?**

JDB-Core provides a minimal yet industrial-grade foundation for:

- Record storage  
- Page-based files  
- B+ Tree indexing  
- Buffer pool memory management  
- Custom binary serialization  
- Efficient disk interaction via Java NIO  

It is ideal for educational purposes, research, or as the engine for a custom database, key–value store, or query layer.

---

## **Features**
- Pure Java implementation (no JDBC, no external DBs)  
- Paged on-disk file architecture (4KB block size)  
- Custom binary serialization for primitive and variable-length data  
- Slotted-page heap file structure  
- B+ Tree index for `O(log N)` record lookups  
- Buffer Pool with LRU eviction policy  
- Dirty-page tracking for consistent writes  
- Disk-aware data structures (page-aligned nodes)  

---

## **Architecture Overview**

### **1. Disk I/O & Paging**
JDB-Core organizes data into **fixed-size 4KB pages**, matching common OS page sizes.  
Key components include:

- **FileChannel + ByteBuffer** for zero-copy I/O  
- **4KB Page Abstraction** for uniform reads/writes  
- **Custom Binary Serialization**  
  - integers  
  - variable strings  
  - pointers/offsets  
- **Heap Files (Slotted Pages)**  
  - Support variable-length records  
  - Maintain record directory inside each page  

This ensures predictable disk access patterns and efficient space management.

---

### **2. Indexing (B+ Tree)**
To avoid full-file linear scans, JDB-Core includes a **disk-aware B+ Tree**:

- **Self-balancing tree** with predictable `O(log N)` operations  
- Nodes sized precisely to fit **one 4KB page**  
- **Internal nodes** store navigation keys  
- **Leaf nodes** store record pointers  
- Minimizes disk seeks and supports large-scale indexing  

---

### **3. Memory Management (Buffer Pool)**
Disk reads are the slowest part of any DBMS.  
JDB-Core includes a custom **Buffer Pool Manager** that caches pages in RAM:

- **LRU eviction** using a Doubly Linked List + HashMap  
- **Pin/Unpin semantics** to manage concurrent usage  
- **Dirty flags** to track modified pages  
- **Flush-on-eviction** for durability  

This enables scaling far beyond available memory.

---

## **Tech Stack**
| Component | Technology |
|----------|------------|
| Language | **Java** |
| Build Tool | **Gradle** |
| Key Libraries | `java.nio.channels.FileChannel`, `java.nio.ByteBuffer` |
| Dependency Policy | **Zero external DB libraries** (no SQLite/H2/JDBC) |