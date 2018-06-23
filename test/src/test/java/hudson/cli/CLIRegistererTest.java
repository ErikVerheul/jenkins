package hudson.cli;

import static hudson.cli.CLICommandInvoker.Matcher.failedWith;
import static hudson.cli.CLICommandInvoker.Matcher.succeededSilently;
import hudson.cli.CLICommandInvoker.Result;
import jenkins.model.Jenkins;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class CLIRegistererTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void testAuthWithSecurityRealmCLIAuthenticator() {
        j.getInstance().setSecurityRealm(j.createDummySecurityRealm());

        CLICommandInvoker command = new CLICommandInvoker(j, "quiet-down");

        Result invocation = command.invokeWithArgs("--username", "foo", "--password", "invalid");
        assertThat(invocation, failedWith(7));
        assertThat(invocation.stderr(), containsString("ERROR: Bad Credentials. Search the server log for "));
        assertThat("Unauthorized command was executed", Jenkins.get().isQuietingDown(), is(false));

        invocation = command.invokeWithArgs("--username", "foo", "--password", "foo");
        assertThat(invocation, succeededSilently());
        assertThat("Authorized command was not executed", Jenkins.get().isQuietingDown(), is(true));
    }
}
