
import java.util.Hashtable;
import java.util.Map;

public class HashtableTest {

    public static void main(String[] args) {
	Hashtable<String, String> hash = new Hashtable<>();
	hash.put("hello","world");
	System.out.println(hash.get("hello"));
	hash.put("hello","no");
	System.out.println(hash.get("hello"));
	Hashtable<Object, Object> hash2 = new Hashtable<>();
	hash2.put("world", "hello");
	System.out.println(hash2.get("world"));
	for (int i = 0; i < 10; i++) {
	    hash.put("yes" + i, "" + i);
	}
	for (Map.Entry<String, String> e : hash.entrySet()) {
	    System.out.println(e.getKey() + ":" + e.getValue());
	}    
    }
}