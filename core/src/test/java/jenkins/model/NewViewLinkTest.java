package jenkins.model;

import hudson.model.Action;
import hudson.model.View;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({NewViewLink.class, Jenkins.class})
public class NewViewLinkTest {
	
    @Mock
    private Jenkins jenkins;
	
    @Mock
    private final String rootUrl = "https://127.0.0.1:8080/";

    private NewViewLink newViewLink;

    private final View view = mock(View.class);
    
    @Before
    public void initTests() throws Exception {
    PowerMockito.mockStatic(Jenkins.class);
    PowerMockito.when(Jenkins.get()).thenReturn(jenkins);
    PowerMockito.when(jenkins.getRootUrl()).thenReturn(rootUrl);
    newViewLink = new NewViewLink();
    }

    @Test
    public void getActionsHasPermission() throws Exception {
        when(view.hasPermission(any())).thenReturn(true);

        final List<Action> actions = newViewLink.createFor(view);

        assertEquals(1, actions.size());
        final Action action = actions.get(0);
        assertEquals(Messages.NewViewLink_NewView(), action.getDisplayName());
        assertEquals(NewViewLink.ICON_FILE_NAME, action.getIconFileName());
        assertEquals(rootUrl + NewViewLink.URL_NAME, action.getUrlName());
    }

    @Test
    public void getActionsNoPermission() throws Exception {
        when(view.hasPermission(any())).thenReturn(false);

        final List<Action> actions = newViewLink.createFor(view);

        assertEquals(1, actions.size());
        final Action action = actions.get(0);
        assertNull(action.getDisplayName());
        assertNull(action.getIconFileName());
        assertEquals(rootUrl + NewViewLink.URL_NAME, action.getUrlName());
    }

}