package com.jboss.datagrid.tweetquick.session;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.infinispan.api.BasicCache;

import com.jboss.datagrid.tweetquick.model.User;

@Named
@SessionScoped
public class UserBean implements Serializable {

	private static final long serialVersionUID = -5419061180849357611L;

	@Inject
	private Authenticator auth;
	
	private User watchedUser;
		
	@Inject
	private CacheContainerProvider provider;
	
	BasicCache<String, Object> userCache;
	
	public List<User> getFollowing() {
		if (watchedUser == null) {
			watchedUser = auth.getUser();
		}
		List<User> returnFollowing = new LinkedList<User>();
		List<String> following = watchedUser.getFollowing();
		for (String username : following) {
			User u = (User) getUserCache().get(username);
			returnFollowing.add(u);
	    }
		return returnFollowing;
	}
	
	public List<User> getFollowers() {
		if (watchedUser == null) {
			watchedUser = auth.getUser();
		}
		List<User> returnFollowers = new LinkedList<User>();
		List<String> followers = watchedUser.getFollowers();
		for (String username : followers) {
			User u = (User) getUserCache().get(username);
			returnFollowers.add(u);
		}
		return returnFollowers;
	}
	
	public String showUser(User user) {
		this.watchedUser = user;
		return "userdetails";
	}
	
	public String showUser(DisplayTweet tweet) {
		this.watchedUser = (User) getUserCache().get(tweet.getUsername());
		return "userdetails";
	}
	
	private BasicCache<String, Object> getUserCache() {
		if (userCache != null) {
			return userCache;
		} else {
			return provider.getCacheContainer().getCache("userCache");
		}
	}
	
	public User getWatchedUser() {
		return watchedUser;
	}
	
	public String goHome() {
		this.watchedUser = auth.getUser();
		return "home";
	}
	
}
