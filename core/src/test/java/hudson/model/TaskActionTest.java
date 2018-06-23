package hudson.model;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import hudson.console.AnnotatedLargeText;
import hudson.security.ACL;
import hudson.security.Permission;
import org.acegisecurity.Authentication;
import org.junit.Test;

/**
 * @author Jerome Lacoste
 */
public class TaskActionTest {

    private static class MyTaskThread extends TaskThread {
        MyTaskThread(TaskAction taskAction) {
            super(taskAction, ListenerAndText.forMemory(taskAction));
        }
        @Override
        protected void perform(TaskListener listener) throws Exception {
            listener.hyperlink("/localpath", "a link");
        }
    }

    private static class MyTaskAction extends TaskAction {
        void start() {
            workerThread = new MyTaskThread(this);
            workerThread.start();
        }

        @Override
        public String getIconFileName() {
            return "Iconfilename";
        }
        @Override
        public String getDisplayName() {
            return "My Task Thread";
        }

        @Override
        public String getUrlName() {
            return "xyz";
        }
        @Override
        protected Permission getPermission() {
            return Permission.READ;
        }

        @Override
        protected ACL getACL() {
            return ACL.lambda((a, p) -> true);
        }
    }

    @Test
    public void annotatedText() throws Exception {
        MyTaskAction action = new MyTaskAction();
        action.start();
        AnnotatedLargeText annotatedText = action.obtainLog();
        while (!annotatedText.isComplete()) {
            Thread.sleep(10);
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        annotatedText.writeLogTo(0, os);
        assertTrue(os.toString("UTF-8").startsWith("a linkCompleted"));
    }
}
