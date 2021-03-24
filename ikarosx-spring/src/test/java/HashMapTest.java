import org.junit.Test;

import java.util.HashMap;

/**
 * @author Ikarosx
 * @date 2021/03/24
 */
public class HashMapTest {
    @Test
    public void test () {
        HashMap<String, String> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("11", "11");
        stringStringHashMap.put("22", "22");
        for (String s : stringStringHashMap.keySet()) {
            System.out.println(s);
            stringStringHashMap.put("33", "33");
        }
    }
}
