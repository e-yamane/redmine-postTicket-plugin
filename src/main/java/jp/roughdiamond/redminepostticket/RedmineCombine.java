package jp.roughdiamond.redminepostticket;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.rough_diamond.tools.redmine.IssueRepository;

import org.kohsuke.stapler.DataBoundConstructor;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.bean.Issue;

public class RedmineCombine extends Recorder {
	public final String projectName;
	private final String queryId;

	@DataBoundConstructor
	public RedmineCombine(String projectName, String queryId) {
		this.projectName = projectName;
		this.queryId = queryId;
	}
	
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {
		try {
			Result result = build.getResult();
			listener.getLogger().println(result);
//			EnvVars vars = build.getEnvironment(listener);
//			for(Map.Entry<String, String> entry : vars.entrySet()) {
//				System.out.println(entry.getKey() + ":" + entry.getValue());
//			}
			RedmineProject project = RedmineRegistory.descriptor.getProjectByName(projectName);
			if(project == null) {
				listener.getLogger().println("プロジェクト名に対応する情報が存在しません");
				Logger.getLogger(RedmineCombine.class.getName()).log(Level.SEVERE, "プロジェクト名に対応する情報が存在しません");
				return false;
			}
			listener.getLogger().println(project);
			final String subjectPrefix = String.format("[jenkins][%s]", build.getProject().getName());
			Issue issue = getTargetIssue(project, subjectPrefix);
			if(result.isBetterOrEqualTo(Result.SUCCESS)) {
				recoveryPost(project, subjectPrefix, issue);
			} else {
				failurePost(project, subjectPrefix, issue, build, listener);
			}
			return true;
		} catch (RedmineException e) {
			Logger.getLogger(RedmineCombine.class.getName()).log(Level.SEVERE, e.getMessage(), e);
			return false;
		}
	}

	Issue getTargetIssue(final RedmineProject project, final String subjectPrefix) throws RedmineException {
		IssueRepository repository = project.getRepository();
		Predicate<Issue> filter = new Predicate<Issue>(){
			@Override
			public boolean apply(Issue issue) {
				try {
//					System.out.println(issue.getSubject() + ":" + issue.getStatusName());
					return (issue.getSubject().startsWith(subjectPrefix) && !project.isClosedIssue(issue));
				} catch (RedmineException e) {
					throw new RuntimeException(e);
				}
			}
		};
		Iterable<Issue> issues = (getQueryId() == null) 
				? repository.byParam(new HashMap<String, String>(), filter)
				: repository.byQueryId(getQueryId(), filter);
		return Iterables.getFirst(Iterables.filter(issues, filter), null);
	}

	private void failurePost(RedmineProject project, String subjectPrefix, Issue issue, AbstractBuild<?, ?> build, BuildListener listener) throws IOException, InterruptedException, RedmineException {
		StringBuilder sb = new StringBuilder();
		if(issue == null) {
			issue = new Issue();
			issue.setSubject(subjectPrefix + "ビルドエラーが発生しました");
			issue.setTracker(project.getTracker());
		} else {
			sb.append("ビルドはまだ不安定です。\n\n");
		}
		EnvVars vars = build.getEnvironment(listener);
		sb.append(vars.get("BUILD_URL") + "\n\n");
//ログを書くとActivitiesが汚くなるので出力を止める
//        List<String> logs = build.getLog(50);
//        for(String log : logs) {
//        	sb.append(log + "\n");
//        }
        String description = sb.toString();
        if(issue.getId() == null) {
        	issue.setDescription(description);
        	project.getRepository().createIssue(issue);
        } else {
            issue.setNotes(description);
            project.getRepository().updateIssue(issue);
        }
	}

	private void recoveryPost(RedmineProject project, String subjectPrefix, Issue issue) throws RedmineException {
		if(issue != null) {
			issue.setNotes("ビルドが正常に戻りました。");
			issue.setStatusId(project.getClosedStatus().getId());
			issue.setStatusName(project.getClosedStatus().getName());
            project.getRepository().updateIssue(issue);
		}
	}

	public Integer getQueryId() {
        try {
        	return Integer.parseInt(queryId);
        } catch(Exception e) {
        	return null; 
        }
	}

	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
		public DescriptorImpl() {
			super(RedmineCombine.class);
//			System.out.println("DescriptorImpl!!");
			load();
		}
		
		@SuppressWarnings("rawtypes")
		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "Redmineに通知する";
		}
		
		public RedmineProject[] getProjects() {
//			System.out.println("DescriptorImpl#getProjects!!");
			return RedmineRegistory.descriptor.getProjects();
		}
	}
}
