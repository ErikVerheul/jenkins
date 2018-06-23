package hudson.tasks._maven;

import hudson.MarkupText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class Maven3MojoNoteTest {

	@Test
	public void testAnnotateOtherPlugin() {
		check("[INFO] <b class=maven-mojo>--- gmaven-plugin:1.0-rc-5:generateTestStubs (test-in-groovy) @ jobConfigHistory ---</b>", "[INFO] --- gmaven-plugin:1.0-rc-5:generateTestStubs (test-in-groovy) @ jobConfigHistory ---");
	}

	private void check(final String decorated, final String input) {
		assertTrue(input + " does not match" + Maven3MojoNote.PATTERN, Maven3MojoNote.PATTERN.matcher(input).matches());
		assertEquals(decorated, annotate(input));
	}

    private String annotate(String text) {
        final MarkupText markupText = new MarkupText(text);
        new Maven3MojoNote().annotate(new Object(), markupText, 0);
        return markupText.toString(true);
    }

}
