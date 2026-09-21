package com.zedb.storage;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class BufferPool {
    private final DiskManager diskManager;
    private final int capacity;

    private final Map<Integer, Boolean> dirtyPages;

    private final Map<Integer, Page> pages;

    public BufferPool(DiskManager diskManager, int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException(
                    "Capacity must be greater than zero"
            );
        }

        this.diskManager = diskManager;
        this.capacity = capacity;

        this.pages = new LinkedHashMap<>(
                capacity,
                0.75f,
                true
        );

        this.dirtyPages = new LinkedHashMap<>();
    }

    public Page getPage(int pageId) throws IOException {

        Page page = pages.get(pageId);

        // Cache hit
        if (page != null) {
            return page;
        }

        // Cache miss
        if (pages.size() >= capacity) {
            evictPage();
        }

        page = diskManager.readPage(pageId);

        pages.put(pageId, page);
        dirtyPages.put(pageId, false);

        return page;
    }

    public void markDirty(int pageId) {

        if (!pages.containsKey(pageId)) {
            throw new IllegalArgumentException(
                    "Page is not in buffer pool: " + pageId
            );
        }

        dirtyPages.put(pageId, true);
    }

    public void flushPage(int pageId) throws IOException {
        Page page = pages.get(pageId);

        if (page == null) {
            return;
        }

        if (Boolean.TRUE.equals(dirtyPages.get(pageId))) {
            diskManager.writePage(pageId, page);
            dirtyPages.put(pageId, false);
        }
    }

    public void flushAll() throws IOException {

        for (Integer pageId : pages.keySet()) {
            flushPage(pageId);
        }
    }

    private void evictPage() throws IOException {

        Iterator<Map.Entry<Integer, Page>> iterator =
                pages.entrySet().iterator();

        // First entry = least recently used
        Map.Entry<Integer, Page> victim = iterator.next();

        int pageId = victim.getKey();
        Page page = victim.getValue();

        if (Boolean.TRUE.equals(dirtyPages.get(pageId))) {
            diskManager.writePage(pageId, page);
        }

        iterator.remove();
        dirtyPages.remove(pageId);
    }
}