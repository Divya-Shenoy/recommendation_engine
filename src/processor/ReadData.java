package processor;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

import data.Event;
import data.Group;
import data.User;
import data.UserGroup;

public class ReadData {
	
	private Map<UserGroup, Integer> userGroupParticipationScore = new HashMap<UserGroup, Integer>();
	private Map<UserGroup, Integer> userGroupDistanceScore = new HashMap<UserGroup, Integer>();
	private ArrayList<User> users = new ArrayList<User>();
	private ArrayList<Group> groups = new ArrayList<Group>();
	private MongoClient mongoClient;
	
	public void generateUserParticipationScore(){
		try {
			mongoClient = new MongoClient();
			DB db = mongoClient.getDB( "meetup_database" );
			DBCollection coll =  db.getCollection("users");
			DBCursor cursor = coll.find();
			
			while(cursor.hasNext()) {				
				String next = String.format("%s", cursor.next());
			//	System.out.println(next);
				JSONObject jObject = new JSONObject(next);
				String userId = jObject.getString("userId");
				JSONObject location = jObject.getJSONObject("location");
				String latitude = location.getString("lat");
				String longitude = location.getString("long");
				
				JSONArray tags = jObject.getJSONArray("tag");
				ArrayList<String> userTags = new ArrayList<String>();
				for(int j=0; j<tags.length(); j++) {
					userTags.add(tags.getString(j));
				}
				JSONArray groups = jObject.getJSONArray("groups");
				ArrayList<Group> userGroups = new ArrayList<Group>();
				
				for(int j=0;j<groups.length(); j++) {
					Group group = new Group();
					group.setGroupId(groups.getJSONObject(j).getString("id"));										
					JSONArray events = groups.getJSONObject(j).getJSONArray("events");
					int score = events.length();
					userGroupParticipationScore.put(new UserGroup(userId, group.getGroupId()), score);
					userGroups.add(group);
				}
				users.add(new User(userId, latitude, longitude, userGroups, userTags));
			}
		} catch (UnknownHostException e) {
			System.out.println("Unable to open mongodb client");
		} catch (JSONException e) {
			System.out.println("Unable to parse JSON object");
		} 
		
		/*for(Map.Entry<UserGroup, Integer> userGroup : userGroupParticipationScore.entrySet()) {
			System.out.println("key: "+ userGroup.getKey() + "value: " + userGroup.getValue());
		}*/
	}
	
	public void generateTagSimilarity(){
		Integer[][] userTagMatrix = new Integer[users.size()][groups.size()]; 
		Integer[][] userTagMatrixTranspose = new Integer[groups.size()][users.size()];
		Integer[][] tagSimilarityMatrix = new Integer[groups.size()][groups.size()];
		
		for(int grpIndex=0; grpIndex<userTagMatrix[0].length; grpIndex++) {
			Set<String> groupTags = new HashSet<String>();
			groupTags.addAll(groups.get(grpIndex).getTags());
			for(int userIndex=0; userIndex<userTagMatrix.length; userIndex++) {
				int count=0;
				for(String tag: users.get(userIndex).getTags()) {
					if(groupTags.contains(tag)) {
						count++;
					}
				}
				userTagMatrix[userIndex][grpIndex] = count;
				userTagMatrixTranspose[grpIndex][userIndex] = count;
			}
		}
		printMatrix(userTagMatrix);
		printMatrix(userTagMatrixTranspose);
		tagSimilarityMatrix = multiply(userTagMatrixTranspose, userTagMatrix);
		printMatrix(tagSimilarityMatrix);
	}
	
	public  Integer[][] multiply(Integer[][] a, Integer[][] b) {
	       int rowsInA = a.length;
	       int columnsInA = a[0].length; // same as rows in B
	       int columnsInB = b[0].length;
	       
	       Integer[][] c = new Integer[rowsInA][columnsInB];
	       for (int i = 0; i < rowsInA; i++) {
	           for (int j = 0; j < columnsInB; j++) {
	        	   c[i][j] = 0;
	               for (int k = 0; k < columnsInA; k++) {
	                   c[i][j] = c[i][j] + a[i][k] * b[k][j];
	               }
	           }
	       }
	       return c;
	   }
	
	private void printMatrix(Integer[][] matrix) {
		for(int i=0;i<matrix.length;i++){
			System.out.println("\n");
			for(int j=0; j<matrix[0].length; j++) {
				System.out.print(matrix[i][j] + " ");
			}
		}
	}
	
	public void generateUserGroupDistanceScore() {
		try {
			mongoClient = new MongoClient();
			DB db = mongoClient.getDB( "meetup_database" );
			DBCollection coll =  db.getCollection("groups");
			DBCursor cursor = coll.find();
			
			while(cursor.hasNext()) {	
				String next = String.format("%s", cursor.next());
			//	System.out.println(next);
				JSONObject jObject = new JSONObject(next);
				String groupId = jObject.getString("id");
				JSONArray eventsObject = jObject.getJSONArray("events");
				ArrayList<Event> eventList  = new ArrayList<Event>();
				for(int i=0; i<eventsObject.length() ; i++) {
					JSONObject eventJObject = eventsObject.getJSONObject(i);
					Event event = new Event();
					event.setEventId(eventJObject.getString("id"));
					event.setGroupId(groupId);
					event.setLatitude(eventJObject.getJSONObject("location").getString("lat"));
					event.setLongitude(eventJObject.getJSONObject("location").getString("long"));
					eventList.add(event);
				}
				Group group = new Group();
				group.setEvents(eventList);
				group.setGroupId(groupId);
				group.setLatitude(eventList.get(0).getLatitude());
				group.setLongitude(eventList.get(0).getLongitude());
				ArrayList<String> tagList = new ArrayList<String>();
				tagList.add(jObject.getString("tag"));
				group.setTags(tagList);
				groups.add(group);
			}
		
		} catch (UnknownHostException e) {
			System.out.println("Unable to open mongodb client");
		} catch (JSONException e) {
			System.out.println("Unable to parse JSON object");
		} 
		
		/*for(Group group : groups) {
			
			System.out.println("groupId: " + group.getGroupId() + "tags: " + group.getTags().get(0) 
					+ "latitude: " + group.getLatitude() + "long: " + group.getLongitude());
			
			for(Event event: group.getEvents()){
				System.out.println("eventID: " + event.getEventId()
						+ "event lat: " + event.getLatitude() + "event long: " + event.getLongitude()
						+ "event grpid: " + event.getGroupId());
			}
		}*/
		int score = 1;
		
		for(User user : users) {
			Double lat1 = Double.parseDouble(user.getLatitude());
			Double long1 = Double.parseDouble(user.getLongitude());
			
			for(Group group : groups){
				Double lat2 = Double.parseDouble(group.getLatitude());
				Double long2 = Double.parseDouble(group.getLongitude());
				Double distance = distance(lat1, long1, lat2, long2);
				if(distance >= 100)
					score = 1;
				else if(distance<100 && distance >=75)
					score = 2;
				else if(distance<75 && distance>=50)
					score = 3;
				else if(distance<50 && distance>=25)
					score=4;
				else if(distance<25)
					score=5;
				userGroupDistanceScore.put(new UserGroup(user.getUserId(), group.getGroupId()), score);
				/*System.out.println("user: " + user.getUserId() + "group: " + group.getGroupId() 
						+ "distance: " + distance);*/
			}
		}
		
		/*for(Map.Entry<UserGroup, Integer> userGroup : userGroupDistanceScore.entrySet()) {
			System.out.println("key: "+ userGroup.getKey() + "value: " + userGroup.getValue());
		}*/
	}
 	
	public double distance(double lat1, double lon1, double lat2, double lon2) {
	      double theta = lon1 - lon2;
	      double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
	      dist = Math.acos(dist);
	      dist = rad2deg(dist);
	      dist = dist * 60 * 1.1515;
	      return (dist);
	    }

	    //  This function converts decimal degrees to radians             
	    private double deg2rad(double deg) {
	      return (deg * Math.PI / 180.0);
	    }

	    // This function converts radians to decimal degrees            
	    private double rad2deg(double rad) {
	      return (rad * 180.0 / Math.PI);
	    }
	    
	public static void main(String[] args) {
		ReadData rd = new ReadData();
		rd.generateUserParticipationScore();
		rd.generateUserGroupDistanceScore();
		rd.generateTagSimilarity();
		//System.out.println(rd.distance(37.350219, -122.014834, 37.398946, -121.920407) + " Miles\n");
	}

}
