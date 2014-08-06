package jp.roughdiamond.redminepostticket;

import java.io.Serializable;

import jp.rough_diamond.tools.redmine.IssueRepository;

import org.kohsuke.stapler.DataBoundConstructor;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueStatus;
import com.taskadapter.redmineapi.bean.Tracker;

@SuppressWarnings("serial")
public class RedmineProject implements Serializable {
	private String name;
	private String url;
	private String projectId;
	private String accessKey;
	private String closedStatusName;
	private String trackerName;
	
	@DataBoundConstructor
	public RedmineProject(String name, String url, String projectId,
			String accessKey, String closedStatusName, String trackerName) {
		super();
		this.name = name;
		this.url = url;
		this.projectId = projectId;
		this.accessKey = accessKey;
		this.closedStatusName = closedStatusName;
		this.trackerName = trackerName;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public String getProjectId() {
		return projectId;
	}

	public String getAccessKey() {
		return accessKey;
	}
	
	IssueRepository repository;
	public IssueRepository getRepository() {
		if(repository == null) {
			repository = IssueRepository.getRepository(url, projectId, accessKey); 
		}
		return repository; 
	}
	
	public boolean isClosedIssue(Issue issue) throws RedmineException {
//		System.out.println("issueStatusId:" + issue.getStatusId());
		return issue.getStatusId().equals(getClosedStatus().getId());
	}

	public IssueStatus getClosedStatus() throws RedmineException {
		IssueStatus ret = getRepository().getStatusByName(closedStatusName);
//		System.out.println("closedStatusId:" + ret.getId());
		return ret;
	}
	
	public Tracker getTracker() throws RedmineException {
		Tracker ret = getRepository().getTrackerByName(trackerName);
//		System.out.println("trackerName:" + trackerName);
//		System.out.println("trackerId:" + ret.getId());
		return ret;
	}

	@Override
	public String toString() {
		return String.format("name:[%s],url:[%s],projectId:[%s],ClosedStatusName:[%s],trackerName:[%s]", 
				name, url, projectId, closedStatusName, trackerName);
	}
}
