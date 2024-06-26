package to.lodestone.bookshelfapi.api.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArrayUtil {

    /**
     * Chunks the given list of players into sublists of the specified size.
     *
     * @param players the list of players to chunk
     * @param chunkSize the desired size of each chunk
     * @return a list of chunks, where each chunk is a list of players
     */
    public static <T> List<List<T>> chunk(List<T> players, int chunkSize) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("Chunk size must be greater than 0");
        }

        List<List<T>> chunks = new ArrayList<>();
        int size = players.size();
        for (int i = 0; i < size; i += chunkSize) {
            int end = Math.min(size, i + chunkSize);
            chunks.add(new ArrayList<>(players.subList(i, end)));
        }
        return chunks;
    }

    /**
     * Combines two lists of the same type into one.
     *
     * @param first the first list
     * @param second the second list
     * @param <T> the type of elements in the lists
     * @return a combined list containing elements from both lists
     */
    public static <T> List<T> combine(List<T> first, List<T> second) {
        List<T> combined = new ArrayList<>(first);
        combined.addAll(second);
        return combined;
    }

    /**
     * Combines a list and an array of the same type into one list.
     *
     * @param list the list
     * @param array the array
     * @param <T> the type of elements in the list and array
     * @return a combined list containing elements from the list and the array
     */
    public static <T> List<T> combine(List<T> list, T... array) {
        List<T> combined = new ArrayList<>(list);
        combined.addAll(Arrays.asList(array));
        return combined;
    }

    /**
     * Combines an array and a list of the same type into one list.
     *
     * @param array the array
     * @param list the list
     * @param <T> the type of elements in the array and list
     * @return a combined list containing elements from the array and the list
     */
    public static <T> List<T> combine(T[] array, List<T> list) {
        List<T> combined = new ArrayList<>(Arrays.asList(array));
        combined.addAll(list);
        return combined;
    }

    /**
     * Combines two arrays of the same type into one list.
     *
     * @param first the first array
     * @param second the second array
     * @param <T> the type of elements in the arrays
     * @return a combined list containing elements from both arrays
     */
    public static <T> List<T> combine(T[] first, T... second) {
        List<T> combined = new ArrayList<>(Arrays.asList(first));
        combined.addAll(Arrays.asList(second));
        return combined;
    }

}
