package com.zedb.storage;

import java.io.IOException;
import java.io.RandomAccessFile;

public class DiskManager implements AutoCloseable{
    private final RandomAccessFile file;

    public DiskManager(String filePath) throws Exception {
        this.file = new RandomAccessFile(filePath, "rw");
    }
    public int allocatePage() throws Exception {
        long fileSize = file.length();

        if(fileSize % Page.PAGE_SIZE != 0){
            throw new IllegalStateException("Corrupted database file");
        }

        int pageId = (int) (fileSize / Page.PAGE_SIZE);

        Page page = new Page();

        writePage(pageId, page);

        return pageId;
    }

    public Page readPage(int PageId) throws Exception {
        long fileSize = file.length();
        long offset = (long) PageId * Page.PAGE_SIZE;
        byte[] data = new byte[Page.PAGE_SIZE];

        file.seek(offset);
        file.readFully(data);

        return new Page(data);
    }

    public void writePage(int pageId, Page page) throws IOException{
        long offset = (long) pageId * Page.PAGE_SIZE;
        file.seek(offset);
        file.write(page.getData());
    }
    @Override
    public void close() throws Exception {

    }
}
