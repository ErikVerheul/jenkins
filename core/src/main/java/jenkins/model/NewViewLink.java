package jenkins.model;

import com.google.common.annotations.VisibleForTesting;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.TransientViewActionFactory;
import hudson.model.View;
import java.util.Collections;
import java.util.List;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;

@Extension
@Restricted(DoNotUse.class)
public class NewViewLink extends TransientViewActionFactory {

    @VisibleForTesting
    static final String ICON_FILE_NAME = "folder";
    @VisibleForTesting
    public static final String URL_NAME = "newView";

    @Override
    public List<Action> createFor(View v) {
        return Collections.singletonList(new Action() {

            @Override
            public String getIconFileName() {
                if (!hasPermission(v)) {
                    return null;
                }

                return ICON_FILE_NAME;
            }

            @Override
            public String getDisplayName() {
                if (!hasPermission(v)) {
                    return null;
                }

                return Messages.NewViewLink_NewView();
            }

            @Override
            public String getUrlName() {
                String urlName = Jenkins.get().getRootUrl() + URL_NAME;
                return urlName;
            }

            private boolean hasPermission(View view) {
                return view.hasPermission(View.CREATE);
            }

        });
    }
}
