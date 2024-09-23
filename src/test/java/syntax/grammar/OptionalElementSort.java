package syntax.grammar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class OptionalElementSort {

    @Test
    public void x() {
        List<Integer> r = sortByBinaryOnes(10);
        for (Integer i : r) {
            System.out.println(Integer.toBinaryString(i));
        }
        System.out.println(r.size());
    }

    public static List<Integer> sortByBinaryOnes(int bits) {

        List<Integer> l = new ArrayList<>();

        System.out.println(Math.pow(2, bits) - 1);
        for (int i = (int) Math.pow(2, bits) - 1; i >= 0; i--) {
            l.add(i);
        }
        System.out.println(l.size());
        List<Integer> r = sortByBinaryOnes(l);

        Collections.reverse(r);

        return r;
    }

    private static List<Integer> sortByBinaryOnes(List<Integer> arr) {

        Collections.sort(arr);

        List<List<Integer>> v = new ArrayList<>(32);

        for (int i = 0; i < 32; i++) {
            v.add(new ArrayList<>());
        }

        for (int i = 0; i < arr.size(); i++) {
            int x = 0;
            int y = arr.get(i);
            while (y > 0) {
                if ((y & 1) == 1) x++;
                y >>= 1;
            }
            v.get(x).add(arr.get(i));
        }

        List<Integer> ans = new ArrayList<>();
         for (int i = 0; i < 32; i++) {
            for (int j = 0; j < v.get(i).size(); j++) {
                   ans.add(v.get(i).get(j));
            }
         }
         return ans;
     }

}
