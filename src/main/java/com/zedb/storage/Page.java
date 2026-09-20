package com.zedb.storage;

import java.nio.ByteBuffer;

public class Page {
    public static final int PAGE_SIZE = 4096; // 4KB
    private static final int HEADER_SIZE = 12;
    private static final int SLOT_SIZE = 8; // 4 bytes for offset, 4 bytes for length
    private static final int RECORD_SIZE = 8; // 8 bytes for each record

    private static final int ACTIVE = 0;
    private static final int DELETED = 1;

    private final byte[] data;

    public Page() {
        this.data = new byte[PAGE_SIZE];
        // Initialize header
        setSlotCount(0);
        setFreeSpaceStart(HEADER_SIZE);
        setFreeSpaceEnd(PAGE_SIZE);
    }

    public Page(byte[] data) {
        if (data.length != PAGE_SIZE) {
            throw new IllegalArgumentException("Invalid page size");
        }
        this.data = data;
    }

    public int insert(Person person){
        int slotCount = getSlotCount();
        int freeSpaceStart = getFreeSpaceStart();
        int freeSpaceEnd = getFreeSpaceEnd();

        int newRecordOffset = freeSpaceEnd - RECORD_SIZE;
        int newFreeSpaceStart  = freeSpaceStart + SLOT_SIZE;

        if(newFreeSpaceStart > newRecordOffset){
            throw new IllegalStateException("Not enough space to insert record");
        }

        //Write record
        ByteBuffer recordBuffer = ByteBuffer.wrap(data, newRecordOffset, RECORD_SIZE);

        recordBuffer.putInt(person.id());
        recordBuffer.putInt(person.age());

        //Write slot
        int slotOffset = HEADER_SIZE + slotCount * SLOT_SIZE;
        ByteBuffer slotBuffer = ByteBuffer.wrap(data, slotOffset, SLOT_SIZE);

        slotBuffer.putInt(newRecordOffset);
        slotBuffer.putInt(ACTIVE);

        //Update header
        setSlotCount(slotCount + 1);
        setFreeSpaceStart(newFreeSpaceStart);
        setFreeSpaceEnd(newRecordOffset);

        return slotCount;
    }

    public Person readRecord(int slotIndex){
        if(slotIndex < 0 || slotIndex >= getSlotCount()){
            throw new IndexOutOfBoundsException("Invalid slot index");
        }

        int slotOffset = HEADER_SIZE + slotIndex * SLOT_SIZE;

        ByteBuffer slotBuffer = ByteBuffer.wrap(data, slotOffset, SLOT_SIZE);

        int recordOffset = slotBuffer.getInt();
        int recordStatus = slotBuffer.getInt();

        if(recordStatus == DELETED){
            throw new IllegalStateException("Record is deleted");
        }

        ByteBuffer recordBuffer = ByteBuffer.wrap(data, recordOffset, RECORD_SIZE);

        int id = recordBuffer.getInt();
        int age = recordBuffer.getInt();

        return new Person(id, age);
    }

    private int getSlotCount() {
        return ByteBuffer.wrap(data).getInt(0);
    }

    private void setSlotCount(int count) {
        ByteBuffer.wrap(data).putInt(0, count);
    }

    private int getFreeSpaceStart() {
        return ByteBuffer.wrap(data).getInt(4);
    }

    private void setFreeSpaceStart(int offset) {
        ByteBuffer.wrap(data).putInt(4, offset);
    }

    private int getFreeSpaceEnd() {
        return ByteBuffer.wrap(data).getInt(8);
    }

    private void setFreeSpaceEnd(int offset) {
        ByteBuffer.wrap(data).putInt(8, offset);
    }

    public byte[] getData() {
        return data;
    }

}
