package jp.roughdiamond.redminepostticket;

import java.io.Serializable;

import org.kohsuke.stapler.DataBoundConstructor;

@SuppressWarnings("serial")
public class RedmineProject implements Serializable {
	private String name;
	private String url;
	private String projectId;
	private String accessKey;
	
	@DataBoundConstructor
	public RedmineProject(String name, String url, String projectId,
			String accessKey) {
		super();
		this.name = name;
		this.url = url;
		this.projectId = projectId;
		this.accessKey = accessKey;
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
}
