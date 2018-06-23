package jenkins.security.s2m;

import hudson.PluginWrapper;
import javax.annotation.CheckForNull;
import jenkins.model.Jenkins;

/**
* @author Kohsuke Kawaguchi
*/
public /*for Jelly*/ class RejectedCallable {
    public final Class clazz;

    /*package*/ RejectedCallable(Class clazz) {
        this.clazz = clazz;
    }

    public @CheckForNull
    PluginWrapper getPlugin() {
        return Jenkins.get().pluginManager.whichPlugin(clazz);
    }
}
