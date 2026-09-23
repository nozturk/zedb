package com.zedb;

import com.zedb.storage.*;

public class ZedbApp {
     static void main(String[] args) throws Exception {

         try (DiskManager disk =
                      new DiskManager("database.db")) {

             BufferPool bufferPool =
                     new BufferPool(disk, 3);

             HeapFile heapFile =
                     new HeapFile(disk, bufferPool);

             RecordId id =
                     heapFile.insert(new Person(1, 44));

             System.out.println("Record ID = " + id);

             Person person = heapFile.get(id);

             System.out.println(person);

             bufferPool.flushAll();
         }
     }


}
