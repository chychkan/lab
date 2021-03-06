package com.github.jnthnclt.os.lab.core.sort;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.MinMaxPriorityQueue;
import org.roaringbitmap.RoaringBitmap;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class BitSortPOC {
    /*
     * With large indexes it is prohibitively expensive to do forward looks to sort. This is a POC to see if
     * a bitset data structure can be used to efficiently reduce the problem space of forward lookups.
     */
    public static void main(String[] args) {

        Index index = new Index();

        for (int i = 0; i < 1_000_000; i++) {
            Map<Field, Integer> document = Maps.newHashMap();
            for (Field field : Field.values()) {
                document.put(field, field.generate());
            }
            index.add(i, document);
            if (i % 1000000 == 0) {
                System.out.println(i);
            }
        }


        Field sortField = Field.values()[6];
        List<int[]> input = Lists.newArrayList();
        for (Map.Entry<Integer, Map<Field, Integer>> entry : index.idToDocument.entrySet()) {
            int dex = index.idToIndex.get(entry.getKey());
            input.add(new int[]{dex, entry.getValue().get(sortField)});
        }

        long start = System.currentTimeMillis();
        BitSortTreePOC bitSortTree1 = new BitSortTreePOC();
        bitSortTree1.populate(input, 512);
        System.out.println("BitSortTree elapse:" + (System.currentTimeMillis() - start));


        for (int i = 0; i < 20; i++) {

            Map<Field, Integer> query = Maps.newHashMap();
            for (int j = 0; j < 2; j++) {
                query.put(Field.values()[j], Field.values()[j].generate());
            }
            start = System.currentTimeMillis();
            List<Result> results = index.query(query, sortField, bitSortTree1, 0, 10);
            if (results.size() == 0) {
                continue;
            }

            System.out.println("\nquery:" + i);
            System.out.println("Fancy:"+results.size() + " latency:" + (System.currentTimeMillis() - start) + " " + results);


            start = System.currentTimeMillis();
            results = index.query(query, sortField, null, 0, 10);
            System.out.println("Brute:"+results.size() + " latency:" + (System.currentTimeMillis() - start) + " " + results);
        }
    }

    enum Field {
        fieldA(0, 2),
        fieldB(0, 10),
        fieldC(0, 100),
        fieldD(0, 1000),
        fieldE(0, 10000),
        fieldF(0, 100000),
        fieldG(0, 1000000);

        int min;
        int max;
        Random random = new Random();

        Field(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public int generate() {
            return min + random.nextInt(max);
        }
    }

    static class Index {
        Map<Field, Map<Integer, RoaringBitmap>> bitSets = Maps.newHashMap();
        Map<Integer, Integer> idToIndex = Maps.newHashMap();
        Map<Integer, Integer> indexToId = Maps.newHashMap();
        Map<Integer, Map<Field, Integer>> idToDocument = Maps.newHashMap();
        AtomicInteger idx = new AtomicInteger();

        public void add(int id, Map<Field, Integer> document) {
            Integer index = idToIndex.computeIfAbsent(id, integer -> idx.getAndIncrement());
            indexToId.put(index, id);

            idToDocument.put(index, document);

            for (Map.Entry<Field, Integer> entry : document.entrySet()) {
                Map<Integer, RoaringBitmap> fieldBitmap = bitSets.computeIfAbsent(entry.getKey(),
                        field -> Maps.newHashMap());
                RoaringBitmap bitmap = fieldBitmap.computeIfAbsent(entry.getValue(), integer -> new RoaringBitmap());
                bitmap.add(index);
            }
        }

        public List<Result> query(Map<Field, Integer> query,
                                  Field sort,
                                  BitSortTreePOC bitSortTree,
                                  int offset,
                                  int limit) {


            RoaringBitmap answer = null;
            for (Map.Entry<Field, Integer> entry : query.entrySet()) {
                RoaringBitmap bitmap = bitSets.get(entry.getKey()).get(entry.getValue());
                if (bitmap == null) {
                    return Lists.newArrayList();
                }
                if (answer == null) {
                    answer = bitmap;
                } else {
                    answer.and(bitmap);
                }
            }

            int cardinality = answer.getCardinality();
            //System.out.println("Total:" + cardinality);

            if (bitSortTree != null && cardinality > offset + limit) {
                answer = bitSortTree.topN(answer, offset + limit);
                System.out.println("BitSort Total:" + answer.getCardinality());
            }

            Comparator<Result> comparator = (o1, o2) -> {
                int c = Integer.compare(o1.sort, o2.sort);
                if (c != 0) {
                    return c;
                }
                return Integer.compare(o1.id, o2.id);
            };

            MinMaxPriorityQueue<Result> minMaxPriorityQueue = MinMaxPriorityQueue
                    .orderedBy(comparator)
                    .maximumSize(offset + limit)
                    .create();

            for (Integer index : answer) {
                Integer id = indexToId.get(index);
                Map<Field, Integer> document = idToDocument.get(id);
                minMaxPriorityQueue.add(new Result(id, document.get(sort), document));
            }


            List<Result> result = Lists.newArrayList(minMaxPriorityQueue);
            Collections.sort(result, comparator);
            if (result.size() < offset) {
                return Lists.newArrayList();
            }
            return result.subList(offset, Math.min(offset + limit, result.size()));

        }
    }

    static class Result {
        int id;
        int sort;
        Map<Field, Integer> document;

        public Result(Integer id, Integer sort, Map<Field, Integer> document) {

            this.id = id;
            this.sort = sort;
            this.document = document;
        }

        @Override
        public String toString() {
            return "id=" + id + ", sort=" + sort;
        }
    }

    static class BitSortTreePOC {

        BitSort bitSort;

        public void populate(List<int[]> indexFieldValues, int leafSize) {

            long start = System.currentTimeMillis();
            Collections.sort(indexFieldValues, (o1, o2) -> {
                int c = Integer.compare(o1[1], o2[1]);
                if (c != 0) {
                    return c;
                }
                return Integer.compare(o1[0], o2[0]);
            });
            System.out.println("sort:" + (System.currentTimeMillis() - start));

            int count = indexFieldValues.size();
            bitSort = new BitSort(count, leafSize);

            start = System.currentTimeMillis();
            int i = 0;
            for (int[] indexFieldValue : indexFieldValues) {
                bitSort.add(i, indexFieldValue[0], indexFieldValue[1]);
                i++;
            }
            bitSort.done();
            System.out.println("index:" + (System.currentTimeMillis() - start));
        }


        RoaringBitmap topN(RoaringBitmap answer, int limit) {
            RoaringBitmap keep = new RoaringBitmap();
            bitSort.topN(answer, keep, limit);
            return keep;
        }
    }


}
