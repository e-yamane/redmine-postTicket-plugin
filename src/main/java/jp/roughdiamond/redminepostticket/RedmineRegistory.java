package jp.roughdiamond.redminepostticket;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import hudson.Extension;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.model.AbstractProject;
import hudson.util.CopyOnWriteList;

public class RedmineRegistory extends JobProperty<AbstractProject<?, ?>> {
	@Extension
	public static final DescriptorImpl descriptor = new DescriptorImpl();
    public final String projectName;
	@DataBoundConstructor
	public RedmineRegistory(String projectName) {
//        if (siteName == null) {
//            // defaults to the first one
//            RedmineSite[] sites = DESCRIPTOR.getSites();
//            if (sites.length > 0) {
//                siteName = sites[0].getName();
//            }
//        }
        this.projectName = projectName;
	}
    
	public static final class DescriptorImpl extends JobPropertyDescriptor {
		private final CopyOnWriteList<RedmineProject> sites = new CopyOnWriteList<RedmineProject>();
		
		public DescriptorImpl() {
			super(RedmineRegistory.class);
			load();
		}
		
		@Override
		public String getDisplayName() {
			return "Redmine Project Setting";
		}
		
		public RedmineProject[] getProjects() {
//        	System.out.println(sites.size());
            return sites.toArray(new RedmineProject[sites.size()]);
		}

        void addSite(RedmineProject site) {
            sites.add(site);
        }
		
        @SuppressWarnings("deprecation")
		@Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws hudson.model.Descriptor.FormException {
            sites.replaceBy(req.bindParametersToList(RedmineProject.class, "jp.roughdiamond.redminepostticke."));
//        	sites.replaceBy(req.bindJSONToList(RedmineProject.class, formData));
//            System.out.println(formData.toString());
//        	System.out.println(sites.size());
//        	for(RedmineProject p : sites) {
//        		System.out.println(p.getName());
//        	}
            save();
            return super.configure(req, formData);
        }

		public RedmineProject getProjectByName(final String projectName) {
			return Iterables.getFirst(Iterables.filter(sites, new Predicate<RedmineProject>() {
				@Override
				public boolean apply(RedmineProject project) {
					return project.getName().equals(projectName);
				}
			}), null);
		}
	}
}
