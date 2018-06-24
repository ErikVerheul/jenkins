package hudson.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * @author Kohsuke Kawaguchi
 */
public class PackedMapTest {

    static class Holder {
        PackedMap pm;
    }

    private final XStream2 xs = new XStream2();

    @Test
    public void basic() {
        Map<String,String> o = new TreeMap<>();
        o.put("a","b");
        o.put("c","d");

        PackedMap<String,String> p = PackedMap.of(o);
        assertEquals("b",p.get("a"));
        assertEquals("d", p.get("c"));
        assertEquals(p.size(),2);
        for (Entry<String,String> e : p.entrySet()) {
            System.out.println(e.getKey()+'='+e.getValue());
        }

        Holder h = new Holder();
        h.pm = p;
        String xml = xs.toXML(h);
        assertEquals(
                "<hudson.util.PackedMapTest_-Holder>\n" +
                "  <pm>\n" +
                "    <entry>\n" +
                "      <string>a</string>\n" +
                "      <string>b</string>\n" +
                "    </entry>\n" +
                "    <entry>\n" +
                "      <string>c</string>\n" +
                "      <string>d</string>\n" +
                "    </entry>\n" +
                "  </pm>\n" +
                "</hudson.util.PackedMapTest_-Holder>",
                xml);

        xs.fromXML(xml);
    }
}
