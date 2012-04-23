package com.jboss.datagrid.tweetquick.jsf;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import javax.faces.application.Application;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;
import org.infinispan.api.BasicCache;
import com.jboss.datagrid.tweetquick.model.Tweet;
import com.jboss.datagrid.tweetquick.model.TweetKey;
import com.jboss.datagrid.tweetquick.model.User;
import com.jboss.datagrid.tweetquick.session.CacheContainerProvider;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

/**
 * 
 * @author Martin Gencur
 * 
 */
public class InitializeCache implements SystemEventListener {

	private static final int USER_COUNT = 100;
	
	private static final int SEVEN_DAYS_IN_MILLISECONDS = 7 * 24 * 3600 * 1000;
	Random randomNumber = new Random();
	
	private Logger log = Logger.getLogger(this.getClass().getName());

	private CacheContainerProvider provider;

	private UserTransaction utx;

	@Override
	public void processEvent(SystemEvent event) throws AbortProcessingException {
		provider = getContextualInstance(getBeanManagerFromJNDI(),
				CacheContainerProvider.class);
		startup();
	}

	public void startup() {
        BasicCache<String, Object> users = provider.getCacheContainer().getCache("userCache");
        BasicCache<TweetKey, Object> tweets = provider.getCacheContainer().getCache("tweetCache");
        
        utx = getUserTransactionFromJNDI();
        
        try {
            utx.begin();
            for (int i = 1; i != USER_COUNT; i++) {
            	User u = new User("user" + i, "Name" + i, "Surname" + i, "tmpPasswd", "Description of person " + i );
            	String encryptedPass = hashPassword("pass" + i);
            	u.setPassword(encryptedPass);
            	
            	//GENERATE 5 TWEETS FOR EACH USER
            	for (int j = 1; j != 5; j++) {
            		long randomTime = getRandomTime();
            		Tweet t = new Tweet(u.getUsername(), "Tweet number " + j + " for user " + u.getName() + " at " + new Date(randomTime), randomTime );
            		//store the tweet in a cache
            		tweets.put(t.getKey(), t);
            		u.getTweets().add(t.getKey());
            	}
            	//store the user in a cache
            	users.put(u.getUsername(), u);
            }
            
            //GENERATE 10 RANDOM FOLLOWERS AND FOLLOWINGS FOR EACH USER
            for (int i = 1; i != USER_COUNT; i++) {
            	User u = (User) users.get("user" + i);
            	for (User follower : generateRandomUsers(u, 10, USER_COUNT)) {
            		u.getFollowers().add(follower.getUsername());
            	}
            	for (User following : generateRandomUsers(u, 30, USER_COUNT)) {
            		u.getFollowing().add(following.getUsername());
            	}
            	users.replace("user" + i, u);
            }
            
            utx.commit();
            log.info("Successfully imported data!");
        } catch (Exception e) {
            log.warning("An exception occured while populating the datagrid! Rolling back the transaction");
            e.printStackTrace();
            if (utx != null) {
                try {
                    utx.rollback();
                } catch (Exception e1) {
                }
            }
        }
    }

	private long getRandomTime() {
		//get random time at most 7 days old
		return Calendar.getInstance().getTimeInMillis() - randomNumber.nextInt(SEVEN_DAYS_IN_MILLISECONDS);
	}

	private Set<User> generateRandomUsers(User forWhom, int count, int outOf) {
		BasicCache<String, Object> users = provider.getCacheContainer().getCache("userCache");
		Random r = new Random();
		Set<User> result = new HashSet<User>();
		while (result.size() != count) {
			int id = (r.nextInt(outOf - 1)) + 1;  //do not return 0
			User u = (User) users.get("user" + id);
			if (u != null && u.equals(forWhom)) continue;
			result.add(u);
		}
		return result;
	}
	
	public static String hashPassword(String password) {
		String hashword = null;
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(password.getBytes());
			BigInteger hash = new BigInteger(1, md5.digest());
			hashword = hash.toString(16);
		} catch (NoSuchAlgorithmException nsae) {
			throw new RuntimeException("No MD5 algorithm found for password encryption!");
		}
		return hashword;
	}

	private BeanManager getBeanManagerFromJNDI() {
		InitialContext context;
		Object result;
		try {
			context = new InitialContext();
			result = context.lookup("java:comp/BeanManager");
		} catch (NamingException e) {
			throw new RuntimeException(
					"BeanManager could not be found in JNDI", e);
		}
		return (BeanManager) result;
	}

	private UserTransaction getUserTransactionFromJNDI() {
		InitialContext context;
		Object result;
		try {
			context = new InitialContext();
			result = context.lookup("java:comp/UserTransaction");
		} catch (NamingException ex) {
			throw new RuntimeException(
					"UserTransaction could not be found in JNDI", ex);
		}
		return (UserTransaction) result;
	}

	@SuppressWarnings("unchecked")
	public <T> T getContextualInstance(final BeanManager manager,
			final Class<T> type) {
		T result = null;
		Bean<T> bean = (Bean<T>) manager.resolve(manager.getBeans(type));
		if (bean != null) {
			CreationalContext<T> context = manager
					.createCreationalContext(bean);
			if (context != null) {
				result = (T) manager.getReference(bean, type, context);
			}
		}
		return result;
	}

	@Override
	public boolean isListenerForSource(Object source) {
		return source instanceof Application;
	}
}
