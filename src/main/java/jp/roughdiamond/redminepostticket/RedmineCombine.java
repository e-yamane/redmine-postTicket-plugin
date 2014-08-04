package jp.roughdiamond.redminepostticket;

import java.io.IOException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Descriptor.FormException;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

public class RedmineCombine extends Recorder {
	public final String projectName;
	@DataBoundConstructor
	public RedmineCombine(String projectName) {
		System.out.println("RedmineCombine!!:" + projectName);
		this.projectName = projectName;
	}
	
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {
		Result result = build.getResult();
		System.out.println("RedmineCombine:perform" + result);
		return true;
	}

	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
		public DescriptorImpl() {
			super(RedmineCombine.class);
			System.out.println("DescriptorImpl!!");
			load();
		}
		
		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "Redmineに通知する";
		}
		
		
		private volatile RedmineProject[] projects = new RedmineProject[0];
        public void setProjects(RedmineProject... projects) {
			System.out.println("DescriptorImpl#setProjects!!");
            this.projects = projects;
            save();
    }

		
		public RedmineProject[] getProjects() {
			System.out.println("DescriptorImpl#getProjects!!");
			return RedmineRegistory.descriptor.getProjects();
		}
	}
}
