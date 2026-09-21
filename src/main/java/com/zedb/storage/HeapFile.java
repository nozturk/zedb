package com.zedb.storage;

import java.io.IOException;

public class HeapFile {

    private final DiskManager diskManager;
    private final BufferPool bufferPool;

    public HeapFile(
            DiskManager diskManager,
            BufferPool bufferPool) {

        this.diskManager = diskManager;
        this.bufferPool = bufferPool;
    }

    public RecordId insert(Person person) throws Exception {

        long pageCount = diskManager.getPageCount();

        for (int pageId = 0; pageId < pageCount; pageId++) {

            Page page = bufferPool.getPage(pageId);

            try {
                int slotId = page.insert(person);

                bufferPool.markDirty(pageId);

                return new RecordId(pageId, slotId);

            } catch (IllegalStateException e) {
                // Page is full.
                // Try the next page.
            }
        }

        // No existing page had enough space.
        int pageId = diskManager.allocatePage();

        Page page = bufferPool.getPage(pageId);

        int slotId = page.insert(person);

        bufferPool.markDirty(pageId);

        return new RecordId(pageId, slotId);
    }

    public Person get(RecordId recordId) throws IOException {

        Page page = bufferPool.getPage(recordId.pageId());

        return page.readRecord(recordId.slotId());
    }
}