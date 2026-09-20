package com.zedb;

import com.zedb.storage.DiskManager;
import com.zedb.storage.Page;
import com.zedb.storage.Person;

public class ZedbApp {
    public static void main(String[] args) throws Exception {

        // First process
        try (DiskManager disk = new DiskManager("database.db")) {

            int pageId = disk.allocatePage();

            Page page = disk.readPage(pageId);

            int slotId = page.insert(new Person(1, 44));

            disk.writePage(pageId, page);

            System.out.println("pageId = " + pageId);
            System.out.println("slotId = " + slotId);
        }

        // Simulate a completely new process
        try (DiskManager disk = new DiskManager("database.db")) {

            Page page = disk.readPage(0);

            Person person = page.readRecord(0);

            System.out.println(person);
        }
    }

}
