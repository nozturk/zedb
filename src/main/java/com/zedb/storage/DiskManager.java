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

    public Page readPage(int pageId) throws IOException {
        long fileSize = file.length();
        long offset = (long) pageId * Page.PAGE_SIZE;
        byte[] data = new byte[Page.PAGE_SIZE];
        if (pageId < 0 || offset + Page.PAGE_SIZE > fileSize) {
            throw new IllegalArgumentException("Page does not exist: " + pageId);
        }

        file.seek(offset);
        file.readFully(data);

        return new Page(data);
    }

    public void writePage(int pageId, Page page) throws IOException{
        long offset = (long) pageId * Page.PAGE_SIZE;
        file.seek(offset);
        file.write(page.getData());
    }

    public long getPageCount() throws IOException {
        long fileSize = file.length();
        if (fileSize % Page.PAGE_SIZE != 0) {
            throw new IllegalStateException(
                    "Corrupted database file"
            );
        }

        return fileSize / Page.PAGE_SIZE;
    }
    @Override
    public void close() throws Exception {

    }
}
