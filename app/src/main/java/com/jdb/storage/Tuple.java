package com.jdb.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Tuple {
    private TupleDesc tupleDesc;
    private List<Object> fields;
    private RecordId recordId; // Location on disk (PageID, SlotNum)

    public Tuple(TupleDesc td) {
        this.tupleDesc = td;
        this.fields = new ArrayList<>();
        // Initialize with nulls
        for (int i = 0; i < td.numFields(); i++) {
            fields.add(null);
        }
    }

    public void setField(int i, Object value) {
        fields.set(i, value);
    }

    public Object getField(int i) {
        return fields.get(i);
    }

    public TupleDesc getTupleDesc() {
        return tupleDesc;
    }

    /**
     * Serializes the Tuple into a Byte Array.
     * Format: 
     * INT -> [4 bytes]
     * STRING -> [4 bytes Length] + [Bytes...]
     */
    public byte[] serialize() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            for (int i = 0; i < tupleDesc.numFields(); i++) {
                Type type = tupleDesc.getType(i);
                Object val = fields.get(i);

                if (type == Type.INT) {
                    dos.writeInt((Integer) val);
                } else if (type == Type.STRING) {
                    byte[] strBytes = ((String) val).getBytes(StandardCharsets.UTF_8);
                    dos.writeInt(strBytes.length); // Write length prefix
                    dos.write(strBytes);           // Write string data
                }
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Serialization failed", e);
        }
    }

    /**
     * Deserializes bytes back into this Tuple.
     */
    public void deserialize(byte[] data) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             DataInputStream dis = new DataInputStream(bais)) {

            for (int i = 0; i < tupleDesc.numFields(); i++) {
                Type type = tupleDesc.getType(i);
                
                if (type == Type.INT) {
                    fields.set(i, dis.readInt());
                } else if (type == Type.STRING) {
                    int len = dis.readInt();
                    byte[] strBytes = new byte[len];
                    dis.readFully(strBytes);
                    fields.set(i, new String(strBytes, StandardCharsets.UTF_8));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Deserialization failed", e);
        }
    }
}