
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
	for (Map.Entry<String, String> e : hash.entrySet()) {
	    if (e.getKey().equals("hello")) {
		System.out.println(e.getKey() + ":" + e.getValue());
	    }
	}
	chmtest();
    }

    public static void chmtest() {
	ConcurrentHashMap<Integer,String> hm = new ConcurrentHashMap<>();
	hm.put(3, "yes");
	System.out.println(hm.get(3));
    }
}