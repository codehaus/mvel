package org.mvel2.util;

/**
 * This class implements a version of the
 * quicksort algorithm using a partition
 * algorithm that does not rely on the
 * first element of the array being vacant,
 * nor does it guarantee that the chosen
 * pivot value is at the split point of
 * the partition.
 *
 * @author Cay Horstmann
 */
public class QuickSort {
    public static void main(String[] args) {
        QuickSort qs = new QuickSort(new int[]{50, 20, 21, 209, 10, 77, 8, 9, 55, 73, 41, 99});
        qs.sort();
    }

    public static int[] quickSort(int[] num) {
        new QuickSort(num).sort();
        return num;
    }

    public QuickSort(int[] anArray) {
        a = anArray;
    }

    /**
     * Sorts the array managed by this sorter
     */
    public void sort() {
        sort(0, a.length - 1);
    }

    public void sort(int low, int high) {
        if (low >= high) return;
        int p = partition(low, high);
        sort(low, p);

        sort(p + 1, high);
    }

    private int partition(int low, int high) {
        // First element
        int pivot = a[low];

        // Middle element
        //int middle = (low + high) / 2;
        //int pivot = a[middle];

        int i = low - 1;
        int j = high + 1;

        while (i < j) {
            i++;
            while (a[i] < pivot) i++;
            j--;
            while (a[j] > pivot) j--;
            if (i < j) swap(i, j);
        }

        return j;
    }

    /**
     * Swaps two entries of the array.
     *
     * @param i the first position to swap
     * @param j the second position to swap
     */
    private void swap(int i, int j) {
        int temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    private int[] a;
}
