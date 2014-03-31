package data;

import java.util.ArrayList;

public class User {
	private String userId;
	private String latitude;
	private String longitude;
	private ArrayList<Group> groups;
	private ArrayList<String> tags;
	
	public User(String userId, String latitude, String longitude,
			ArrayList<Group> groups, ArrayList<String> tags) {
		super();
		this.userId = userId;
		this.latitude = latitude;
		this.longitude = longitude;
		this.groups = groups;
		this.tags = tags;
	}
	public ArrayList<String> getTags() {
		return tags;
	}
	public void setTags(ArrayList<String> tags) {
		this.tags = tags;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public ArrayList<Group> getGroups() {
		return groups;
	}
	public void setGroups(ArrayList<Group> groups) {
		this.groups = groups;
	}
	
}
