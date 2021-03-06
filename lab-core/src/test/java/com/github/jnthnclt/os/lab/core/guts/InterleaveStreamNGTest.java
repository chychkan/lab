package com.github.jnthnclt.os.lab.core.guts;

import com.github.jnthnclt.os.lab.base.BolBuffer;
import com.github.jnthnclt.os.lab.base.UIO;
import com.github.jnthnclt.os.lab.core.LABEnvironment;
import com.github.jnthnclt.os.lab.core.LABHeapPressure;
import com.github.jnthnclt.os.lab.core.LABStats;
import com.github.jnthnclt.os.lab.core.TestUtils;
import com.github.jnthnclt.os.lab.core.api.rawhide.LABRawhide;
import com.github.jnthnclt.os.lab.core.api.rawhide.Rawhide;
import com.github.jnthnclt.os.lab.core.guts.allocators.LABAppendOnlyAllocator;
import com.github.jnthnclt.os.lab.core.guts.allocators.LABConcurrentSkipListMap;
import com.github.jnthnclt.os.lab.core.guts.allocators.LABConcurrentSkipListMemory;
import com.github.jnthnclt.os.lab.core.guts.allocators.LABIndexableMemory;
import com.github.jnthnclt.os.lab.core.guts.api.ReadIndex;
import com.github.jnthnclt.os.lab.core.guts.api.Scanner;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author jonathan.colt
 */
public class InterleaveStreamNGTest {

    @Test
    public void testNext() throws Exception {

        InterleaveStream ips = new InterleaveStream(LABRawhide.SINGLETON,
            ActiveScan.indexToFeeds(new ReadIndex[] {
                sequenceIndex(new long[] { 1, 2, 3, 4, 5 }, new long[] { 3, 3, 3, 3, 3 })
            }, false,false, null, null, LABRawhide.SINGLETON, null));

        List<Expected> expected = new ArrayList<>();
        expected.add(new Expected(1, 3));
        expected.add(new Expected(2, 3));
        expected.add(new Expected(3, 3));
        expected.add(new Expected(4, 3));
        expected.add(new Expected(5, 3));

        assertExpected(ips, expected);
        ips.close();
    }

    @Test
    public void testNext0() throws Exception {

        InterleaveStream ips = new InterleaveStream(LABRawhide.SINGLETON,
            ActiveScan.indexToFeeds(new ReadIndex[] {
                sequenceIndex(new long[] { 1, 2, 3, 4, 5 }, new long[] { 3, 3, 3, 3, 3 }),
                sequenceIndex(new long[] { 1, 2, 3, 4, 5 }, new long[] { 1, 1, 1, 1, 1 })
            }, false,false, null, null, LABRawhide.SINGLETON, null));

        List<Expected> expected = new ArrayList<>();
        expected.add(new Expected(1, 3));
        expected.add(new Expected(2, 3));
        expected.add(new Expected(3, 3));
        expected.add(new Expected(4, 3));
        expected.add(new Expected(5, 3));

        assertExpected(ips, expected);
        ips.close();
    }

    @Test
    public void testNext1() throws Exception {

        InterleaveStream ips = new InterleaveStream(LABRawhide.SINGLETON,
            ActiveScan.indexToFeeds(new ReadIndex[] {
                sequenceIndex(new long[] { 1, 2, 3, 4, 5 }, new long[] { 3, 3, 3, 3, 3 }),
                sequenceIndex(new long[] { 1, 2, 3, 4, 5 }, new long[] { 2, 2, 2, 2, 2 }),
                sequenceIndex(new long[] { 1, 2, 3, 4, 5 }, new long[] { 1, 1, 1, 1, 1 })
            }, false,false, null, null, LABRawhide.SINGLETON, null));

        List<Expected> expected = new ArrayList<>();
        expected.add(new Expected(1, 3));
        expected.add(new Expected(2, 3));
        expected.add(new Expected(3, 3));
        expected.add(new Expected(4, 3));
        expected.add(new Expected(5, 3));

        assertExpected(ips, expected);
        ips.close();
    }

    @Test
    public void testNext2() throws Exception {

        InterleaveStream ips = new InterleaveStream(LABRawhide.SINGLETON,
            ActiveScan.indexToFeeds(new ReadIndex[] {
                sequenceIndex(new long[] { 10, 21, 29, 41, 50 }, new long[] { 1, 0, 0, 0, 1 }),
                sequenceIndex(new long[] { 10, 21, 29, 40, 50 }, new long[] { 0, 0, 0, 1, 0 }),
                sequenceIndex(new long[] { 10, 20, 30, 39, 50 }, new long[] { 0, 1, 1, 0, 0 })
            }, false,false, null, null, LABRawhide.SINGLETON, null));

        List<Expected> expected = new ArrayList<>();
        expected.add(new Expected(10, 1));
        expected.add(new Expected(20, 1));
        expected.add(new Expected(21, 0));
        expected.add(new Expected(29, 0));
        expected.add(new Expected(30, 1));
        expected.add(new Expected(39, 0));
        expected.add(new Expected(40, 1));
        expected.add(new Expected(41, 0));
        expected.add(new Expected(50, 1));

        assertExpected(ips, expected);
        ips.close();

    }

    @Test
    public void testNext3() throws Exception {

        InterleaveStream ips = new InterleaveStream(LABRawhide.SINGLETON,
            ActiveScan.indexToFeeds(new ReadIndex[] {
                sequenceIndex(new long[] { 10, 21, 29, 41, 50 }, new long[] { 1, 0, 0, 0, 1 }),
                sequenceIndex(new long[] { 10, 20, 30, 39, 50 }, new long[] { 0, 1, 1, 0, 0 }),
                sequenceIndex(new long[] { 10, 21, 29, 40, 50 }, new long[] { 0, 0, 0, 1, 0 })
            }, false,false, null, null, LABRawhide.SINGLETON, null));

        List<Expected> expected = new ArrayList<>();
        expected.add(new Expected(10, 1));
        expected.add(new Expected(20, 1));
        expected.add(new Expected(21, 0));
        expected.add(new Expected(29, 0));
        expected.add(new Expected(30, 1));
        expected.add(new Expected(39, 0));
        expected.add(new Expected(40, 1));
        expected.add(new Expected(41, 0));
        expected.add(new Expected(50, 1));

        assertExpected(ips, expected);
        ips.close();

    }

    @Test
    public void testNext4() throws Exception {

        InterleaveStream ips = new InterleaveStream(LABRawhide.SINGLETON,
            ActiveScan.indexToFeeds(new ReadIndex[] {
                sequenceIndex(new long[] { 9, 20, 30, 39, 50 }, new long[] { 0, 1, 1, 0, 0 }),
                sequenceIndex(new long[] { 10, 21, 29, 41, 50 }, new long[] { 1, 0, 0, 0, 1 }),
                sequenceIndex(new long[] { 10, 21, 31, 40, 50 }, new long[] { 0, 0, 0, 1, 0 })
            }, false,false, null, null, LABRawhide.SINGLETON, null));

        List<Expected> expected = new ArrayList<>();
        expected.add(new Expected(9, 0));
        expected.add(new Expected(10, 1));
        expected.add(new Expected(20, 1));
        expected.add(new Expected(21, 0));
        expected.add(new Expected(29, 0));
        expected.add(new Expected(30, 1));
        expected.add(new Expected(31, 0));
        expected.add(new Expected(39, 0));
        expected.add(new Expected(40, 1));
        expected.add(new Expected(41, 0));
        expected.add(new Expected(50, 1));

        assertExpected(ips, expected);
        ips.close();

    }

    @Test
    public void testNext5() throws Exception {

        InterleaveStream ips = new InterleaveStream(LABRawhide.SINGLETON,
            ActiveScan.indexToFeeds(new ReadIndex[] {
                sequenceIndex(new long[] { 1,2,3 }, new long[] { 1,2,3 }),
                sequenceIndex(new long[] { 4,5,6 }, new long[] { 4,5,6 }),
                sequenceIndex(new long[] { 7,8,9 }, new long[] { 7,8,9 })
            }, false,false, null, null, LABRawhide.SINGLETON, null));

        List<Expected> expected = new ArrayList<>();
        expected.add(new Expected(1, 1));
        expected.add(new Expected(2, 2));
        expected.add(new Expected(3, 3));
        expected.add(new Expected(4, 4));
        expected.add(new Expected(5, 5));
        expected.add(new Expected(6, 6));
        expected.add(new Expected(7, 7));
        expected.add(new Expected(8, 8));
        expected.add(new Expected(9, 9));

        assertExpected(ips, expected);
        ips.close();

    }

    @Test
    public void testNext6() throws Exception {

        InterleaveStream ips = new InterleaveStream(LABRawhide.SINGLETON,
            ActiveScan.indexToFeeds(new ReadIndex[] {
                sequenceIndex(new long[] { 1 }, new long[] { 1 }),
                sequenceIndex(new long[] { 4 }, new long[] { 4 }),
                sequenceIndex(new long[] { 7 }, new long[] { 7 })
            }, false,false, null, null, LABRawhide.SINGLETON, null));

        List<Expected> expected = new ArrayList<>();
        expected.add(new Expected(1, 1));
        expected.add(new Expected(4, 4));
        expected.add(new Expected(7, 7));

        assertExpected(ips, expected);
        ips.close();

    }

    @Test
    public void testNext7() throws Exception {

        InterleaveStream ips = new InterleaveStream(LABRawhide.SINGLETON,
            ActiveScan.indexToFeeds(new ReadIndex[] {
                sequenceIndex(new long[] { 1 }, new long[] { 3 }),
                sequenceIndex(new long[] { 1 }, new long[] { 1 }),
                sequenceIndex(new long[] { 1 }, new long[] { 2 })
            }, false,false, null, null, LABRawhide.SINGLETON, null));

        List<Expected> expected = new ArrayList<>();
        expected.add(new Expected(1, 3));

        assertExpected(ips, expected);
        ips.close();

    }


    private ReadIndex sequenceIndex(long[] keys, long[] values) {
        return new ReadIndex() {
            @Override
            public void release() {
                throw new UnsupportedOperationException("Not supported.");
            }

            @Override
            public Scanner rangeScan(boolean hashIndexEnabled, boolean pointFrom, byte[] from, byte[] to, BolBuffer entryBuffer, BolBuffer entryKeyBuffer) throws Exception {
                throw new UnsupportedOperationException("Not supported.");
            }

            @Override
            public Scanner rowScan(BolBuffer entryBuffer, BolBuffer entryKeyBuffer) throws Exception {
                return nextEntrySequence(keys, values);
            }

            @Override
            public BolBuffer pointScan(boolean hashIndexEnabled, byte[] key) throws Exception {
                throw new UnsupportedOperationException("Not supported.");
            }

            @Override
            public long count() throws Exception {
                throw new UnsupportedOperationException("Not supported.");
            }
        };
    }

    @Test
    public void testNext10() throws Exception {

        int count = 10;
        int step = 100;
        int indexes = 4;

        Random rand = new Random(123);
        ExecutorService destroy = Executors.newSingleThreadExecutor();
        Rawhide rawhide = LABRawhide.SINGLETON;

        ConcurrentSkipListMap<byte[], byte[]> desired = new ConcurrentSkipListMap<>(rawhide.getKeyComparator());
        AtomicLong globalHeapCostInBytes = new AtomicLong();
        LABStats labStats = new LABStats(globalHeapCostInBytes);

        LABHeapPressure labHeapPressure = new LABHeapPressure(labStats,
            LABEnvironment.buildLABHeapSchedulerThreadPool(1),
            "default",
            512,
            1024,
            globalHeapCostInBytes,
            LABHeapPressure.FreeHeapStrategy.mostBytesFirst
        );
        LABMemoryIndex[] memoryIndexes = new LABMemoryIndex[indexes];
        ReadIndex[] reorderIndexReaders = new ReadIndex[indexes];
        ReadIndex[] readerIndexs = new ReadIndex[indexes];
        BolBuffer keyBuffer = new BolBuffer();
        try {
            for (int wi = 0; wi < indexes; wi++) {

                int i = (indexes - 1) - wi;

                memoryIndexes[i] = new LABMemoryIndex(destroy, labHeapPressure, labStats,
                    rawhide,
                    new LABConcurrentSkipListMap(labStats,
                        new LABConcurrentSkipListMemory(rawhide,
                            new LABIndexableMemory(new LABAppendOnlyAllocator("test", 2))
                        ),
                        new StripingBolBufferLocks(1024)
                    ));
                TestUtils.append(rand, memoryIndexes[i], 0, step, count, desired, keyBuffer);
                readerIndexs[wi] = memoryIndexes[i].acquireReader();
                reorderIndexReaders[i] = readerIndexs[wi];
            }



            List<Expected> expected = new ArrayList<>();
            Random r = new Random();
            List<Expected> skipScanExpected = Lists.newArrayList();
            //System.out.println("Expected:");
            for (Map.Entry<byte[], byte[]> entry : desired.entrySet()) {
                long key = UIO.bytesLong(entry.getKey());
                long value = TestUtils.value(entry.getValue());
                expected.add(new Expected(key, value));

                if (r.nextBoolean()) {
                    skipScanExpected.add(new Expected(key, value));
                }

                //System.out.println(key + " timestamp:" + value);
            }
            //System.out.println("\n");

            InterleaveStream ips = new InterleaveStream(rawhide,
                ActiveScan.indexToFeeds(reorderIndexReaders, false,false, null, null, rawhide,
                    new BolBuffer(UIO.longBytes(skipScanExpected.get(0).key))));
            assertExpectedSkipScan(ips, skipScanExpected);

            ips = new InterleaveStream(rawhide,
                ActiveScan.indexToFeeds(reorderIndexReaders, false,false,null, null, rawhide, null));
            assertExpected(ips, expected);
        } finally {
            for (ReadIndex readerIndex : readerIndexs) {
                if (readerIndex != null) {
                    readerIndex.release();
                }
            }
        }
    }



    private void assertExpected(InterleaveStream ips, List<Expected> expected) throws Exception {
        boolean[] passed = { true };

        BolBuffer rawEntry = new BolBuffer();
        while ((rawEntry = ips.next(rawEntry,null)) != null) {

            Expected expect = expected.remove(0);
            long key = TestUtils.key(rawEntry);
            long value = TestUtils.value(rawEntry);

            //System.out.println("key:" + key + " vs " + expect.key + " value:" + value + " vs " + expect.value);
            if (key != expect.key) {
                passed[0] = false;
            }
            if (value != expect.value) {
                passed[0] = false;
            }
        }
        Assert.assertTrue(passed[0], "key or value miss match");
        Assert.assertTrue(expected.isEmpty(), "failed to remove all");
    }

    private void assertExpectedSkipScan(InterleaveStream ips, List<Expected> expected) throws Exception {
        boolean[] passed = { true };

        for (Expected e : expected) {
            BolBuffer rawEntry = ips.next(new BolBuffer(), new BolBuffer(UIO.longBytes(e.key)));
            long key = TestUtils.key(rawEntry);
            long value = TestUtils.value(rawEntry);

            System.out.println( key + " vs " + e.key);
        }


//        while ((rawEntry = ips.next(rawEntry,null)) != null) {
//
//            Expected expect = expected.remove(0);
//            long key = TestUtils.key(rawEntry);
//            long value = TestUtils.value(rawEntry);
//
//            //System.out.println("key:" + key + " vs " + expect.key + " value:" + value + " vs " + expect.value);
//            if (key != expect.key) {
//                passed[0] = false;
//            }
//            if (value != expect.value) {
//                passed[0] = false;
//            }
//        }
//        Assert.assertTrue(passed[0], "key or value miss match");
//        Assert.assertTrue(expected.isEmpty(), "failed to remove all");
    }


    /*

     */
    static private class Expected {

        long key;
        long value;

        private Expected(long key, long value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return "Expected{" + "key=" + key + ", value=" + value + '}';
        }

    }

    public Scanner nextEntrySequence(long[] keys, long[] values) {
        int[] index = { 0 };

        return new Scanner() {
            @Override
            public BolBuffer next(BolBuffer rawEntry, BolBuffer nextHint) throws Exception {
                if (index[0] < keys.length) {
                    byte[] rawEntryBytes = TestUtils.rawEntry(keys[index[0]], values[index[0]]);
                    index[0]++;
                    return new BolBuffer(rawEntryBytes);
                }
                return null;
            }

            @Override
            public void close() {
            }
        };
    }

}
