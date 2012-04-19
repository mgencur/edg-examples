package com.jboss.datagrid.tweetquick.session;

import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.UserTransaction;

import org.infinispan.api.BasicCache;

import com.jboss.datagrid.tweetquick.model.Tweet;
import com.jboss.datagrid.tweetquick.model.TweetKey;
import com.jboss.datagrid.tweetquick.model.User;

@Named
@RequestScoped
public class TweetBeen {

	private static final int RECENT_POSTS_LIMIT = 100;
	
	private String message;

	@Inject
	private Authenticator auth;

	@Inject
	private CacheContainerProvider provider;

	@Inject
	private UserTransaction utx;

	public String sendTweet() {
		Tweet t = new Tweet(auth.getUsername(), message);
		BasicCache<String, Object> users = provider.getCacheContainer()
				.getCache("userCache");
		BasicCache<TweetKey, Object> tweets = provider.getCacheContainer()
				.getCache("tweetCache");
		try {
			utx.begin();
			//todo - is it necessary to retrieve it again and not use the one in authenticator?
			User u = (User) users.get(auth.getUsername());
			tweets.put(t.getKey(), t);
			u.getTweets().add(t.getKey());
			users.replace(auth.getUsername(), u);
			utx.commit();
		} catch (Exception e) {
			if (utx != null) {
				try {
					utx.rollback();
				} catch (Exception e1) {
				}
			}
		}
		return null;
	}
	
	public List<Tweet> getRecentTweets() {
		LinkedList<Tweet> recentTweets = new LinkedList<Tweet>();
		BasicCache<TweetKey, Object> tweetCache = provider.getCacheContainer()
				.getCache("tweetCache");
		BasicCache<String, Object> userCache = provider.getCacheContainer()
				.getCache("userCache");
		
		List<String> following = auth.getUser().getFollowing();
		
		//get all people that I'm following
		for (String username : following) {
			User u = (User) userCache.get(username);
			//get their tweets
			List<TweetKey> tweetKeys = u.getTweets();
			/* 
			 * for each key from all people that I follow, find out if the tweet is newer 
			 * than what we have, if yes, update our collection
			 */
			for (TweetKey key : tweetKeys) {
				Tweet t = (Tweet) tweetCache.get(key);
				if (recentTweets.size() == 0) {
					recentTweets.add(t);
				} else {
					int counter = 0;
					for (Tweet recentTweet : recentTweets) {
						if (t.getTimeOfPost() > recentTweet.getTimeOfPost()) {
							recentTweets.add(counter, t); //insert one position before -> get ordered collection
							if (recentTweets.size() >= RECENT_POSTS_LIMIT) {
								recentTweets.removeLast();
							}
							break;
						}
						counter++;
					}
				}
			}
		}
		return recentTweets;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
