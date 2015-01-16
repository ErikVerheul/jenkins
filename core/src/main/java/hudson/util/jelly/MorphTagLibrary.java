package hudson.util.jelly;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.TagLibrary;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.jelly.impl.ExpressionAttribute;
import org.apache.commons.jelly.impl.TagScript;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Jelly tag library for literal-like tags, with an ability to add arbitrary attributes taken from a map.
 *
 * <p>
 * Tags from this namespace ("jelly:hudson.util.jelly.MorphTagLibrary") behaves mostly like literal static tags,
 * except it interprets two attributes "ATTRIBUTES" and "EXCEPT" in a special way.
 *
 * The "ATTRIBUTES" attribute should have a Jelly expression that points to a {@link Map} object,
 * and the contents of the map are added as attributes of this tag, with the exceptions of entries whose key
 * values are listed in the "EXCEPT" attribute.
 *
 * The "EXCEPT" attribute takes a white-space separated list of attribute names that should be ignored even
 * if it's in the map.
 *
 * <p>
 * The explicit literal attributes, if specified, always take precedence over the dynamic attributes added by the map.
 *
 * <p>
 * See textbox.jelly as an example of using this tag library. 
 *
 * @author Kohsuke Kawaguchi
 * @since 1.342
 */
public class MorphTagLibrary extends TagLibrary {
    /**
     * This code is really only used for dealing with dynamic tag libraries, so no point in implementing
     * this for statically used tag libraries.
     */
    @Override
    public Tag createTag(final String name, Attributes attributes) throws JellyException {
        return null;
    }

    @Override
    public TagScript createTagScript(final String tagName, Attributes attributes) throws JellyException {
        return new TagScript() {
            private Object evalAttribute(String name, JellyContext context) {
                ExpressionAttribute e = attributes.get(name);
                if (e==null) {
                    return null;
                }
                return e.exp.evaluate(context);
            }

            private Collection<?> getExclusions(JellyContext context) {
                Object exclusion = evalAttribute(EXCEPT_ATTRIBUTES,context);
                if (exclusion==null) {
                    return Collections.emptySet();
                }
                if (exclusion instanceof String) {
                    return Arrays.asList(exclusion.toString().split("\\s+")); // split by whitespace
                }
                if (exclusion instanceof Collection) {
                    return (Collection)exclusion;
                }
                throw new IllegalArgumentException("Expected collection for exclusion but found :"+exclusion);
            }

            @Override
            public void run(JellyContext context, XMLOutput output) throws JellyTagException {
                AttributesImpl actual = new AttributesImpl();

                Collection<?> exclusions = getExclusions(context);

                Map<String,?> meta = (Map)evalAttribute(META_ATTRIBUTES,context);
                if (meta!=null) {
                    for (Map.Entry<String,?> e : meta.entrySet()) {
                        String key = e.getKey();
                        // @see jelly.impl.DynamicTag.setAttribute() -- ${attrs} has duplicates with "Attr" suffix
                        if (key.endsWith("Attr") && meta.containsKey(key.substring(0, key.length()-4))) {
                            continue;
                        }
                        // @see http://github.com/jenkinsci/jelly/commit/4ae67d15957b5b4d32751619997a3cb2a6ad56ed
                        if (key.equals("ownerTag")) {
                            continue;
                        }
                        if (!exclusions.contains(key)) {
                            Object v = e.getValue();
                            if (v!=null) {
                                actual.addAttribute("", key, key,"CDATA", v.toString());
                            }
                        }
                    }
                } else {
                    meta = Collections.emptyMap();
                }

                for (Map.Entry<String,ExpressionAttribute> e : attributes.entrySet()) {
                    String name = e.getKey();
                    if (name.equals(META_ATTRIBUTES) || name.equals(EXCEPT_ATTRIBUTES)) {
                        continue;   // already handled
                    }
                    if (meta.containsKey(name)) {
                        // if the explicit value is also generated by a map, delete it first.
                        // this is O(N) operation, but we don't expect there to be a lot of collisions.
                        int idx = actual.getIndex(name);
                        if(idx>=0) {
                            actual.removeAttribute(idx);
                        }
                    }

                    Expression expression = e.getValue().exp;
                    actual.addAttribute("",name,name,"CDATA",expression.evaluateAsString(context));
                }

                try {
                    output.startElement(tagName,actual);
                    getTagBody().run(context,output);
                    output.endElement(tagName);
                } catch (SAXException x) {
                    throw new JellyTagException(x);
                }
            }
        };
    }

    private static final String META_ATTRIBUTES = "ATTRIBUTES";
    private static final String EXCEPT_ATTRIBUTES = "EXCEPT";
}
