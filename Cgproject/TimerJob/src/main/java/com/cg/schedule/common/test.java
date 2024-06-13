package com.cg.schedule.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class test {

    public static void main(String[] args) {
        ArrayList<Integer> list = new ArrayList<>();
        list.add(10);
        list.add(11);
        list.add(12);
        List<Integer> list1 = Arrays.asList(1, 2, 3, 4, 5);
        boolean b = list.addAll(0, list1);
        boolean remove = list.remove(Integer.valueOf(10));
        int i = list.indexOf(12);
        list.add(null);
        System.out.println(1);
        list.forEach(System.out::println);

        ConcurrentLinkedQueue<Integer> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();
        int[] aa = new int[2];
        int length = aa.length;
    }
}
