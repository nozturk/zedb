package com.zedb;

import com.zedb.storage.BufferPool;
import com.zedb.storage.DiskManager;
import com.zedb.storage.Page;
import com.zedb.storage.Person;

public class ZedbApp {
     static void main(String[] args) throws Exception {

        try (DiskManager disk = new DiskManager("database.db")) {
            disk.allocatePage();
            BufferPool bufferPool = new BufferPool(disk, 3);

            Page page = bufferPool.getPage(0);

            page.insert(new Person(1, 44));

            bufferPool.markDirty(0);

            bufferPool.flushAll();
        }

        try (DiskManager disk = new DiskManager("database.db")) {

            Page page = disk.readPage(0);

            Person person = page.readRecord(0);

            System.out.println(person);
        }
    }


}
