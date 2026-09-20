package com.zedb.storage;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class BufferPool {
    private final DiskManager diskManager;
    private final int capacity;

    private final Map<Integer, Boolean> dirtyPages;

    private final Map<Integer, Page> pages;

    public BufferPool(DiskManager diskManager, int capacity) {
        this.diskManager = diskManager;
        this.capacity = capacity;
        this.dirtyPages = new LinkedHashMap<>();
        this.pages = new LinkedHashMap<>();
    }

    public Page getPage(int pageId) throws IOException {
        Page page = pages.get(pageId);

        if(page != null){
            return page;
        }
        page = diskManager.readPage(pageId);
        pages.put(pageId, page);

        return page;
    }

    public void markDirty(int pageId) {
        if(!pages.containsKey(pageId)){
            throw new IllegalArgumentException("Page not found in buffer pool");
        }
        dirtyPages.put(pageId, true);
    }

    public void flushPage(int pageId) throws IOException{
        Page page = pages.get(pageId);

        if(page == null){
            return;
        }

        if(Boolean.TRUE.equals(dirtyPages.get(pageId))){
            diskManager.writePage(pageId, page);
            dirtyPages.put(pageId, false);
        }
    }

    public void flushAll() throws IOException {

        for (Integer pageId : pages.keySet()) {
            flushPage(pageId);
        }
    }
}
