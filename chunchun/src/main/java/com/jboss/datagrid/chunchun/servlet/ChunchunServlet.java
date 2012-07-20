package com.jboss.datagrid.chunchun.servlet;

import com.jboss.datagrid.chunchun.jsf.InitializeCache;
import com.jboss.datagrid.chunchun.model.User;
import com.jboss.datagrid.chunchun.session.Authenticator;
import com.jboss.datagrid.chunchun.session.DisplayPost;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.jboss.datagrid.chunchun.session.PostBean;
import com.jboss.datagrid.chunchun.session.UserBean;

/**
 * A servlet that invokes application logic based on URL parameters. This is to
 * avoid using layers like JSF which slows down performance and complicates
 * things when debugging.
 *
 * This servlet is dedicated mainly for performance testing.
 *
 * @author Martin Gencur
 */
@WebServlet(urlPatterns={"/chunchunservlet"})
public class ChunchunServlet extends HttpServlet {

   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException {
      String command = request.getParameter("command");
      String userParam = request.getParameter("user"); //in case we need to specify a user for an operation
      int displayLimitParam = 10; //in case we want to specify how many posts to display
      try {
         displayLimitParam = Integer.parseInt(request.getParameter("limit"));
      } catch (NumberFormatException e) {
         // do nothing, default value for displayLimitParam used
      }

      StringBuilder answer = new StringBuilder();
      Authenticator auth = getAuthenticator();
      PostBean postBean = getPostBean();
      UserBean userBean = getUserBean();

      if ("login".equals(command)) {

         //http://localhost:8080/chunchun/chunchunservlet?command=login&user=

         if (!auth.isLoggedIn()) {
            Random r = new Random(System.currentTimeMillis());
            int randomUserId = r.nextInt(InitializeCache.USER_COUNT) + 1;
            String username = "user" + randomUserId;
            String password  = "pass" + randomUserId;
            auth.setUsername(username);
            auth.setPassword(password);
            auth.login();
            answer.append("\n").append("User Logged in");
         }

      } else if ("logout".equals(command)) {

         //http://localhost:8080/chunchun/chunchunservlet?command=logout

         auth.logoutFromServlet();
         answer.append("\n").append("User Logged out");

      } else if ("recentposts".equals(command)) {

         //http://localhost:8080/chunchun/chunchunservlet?command=recentposts     //limit defaults to 10
         //http://localhost:8080/chunchun/chunchunservlet?command=recentposts&limit=20

         postBean.setDisplayedPostsLimit(displayLimitParam);
         List<DisplayPost> recentPosts = postBean.getRecentPosts();
         answer.append("\n").append("Displayed: " + postBean.getDisplayedPostsLimit()).append("\n");
         for (DisplayPost post : recentPosts) {
            answer.append("\n").append(post.getMessage());
         }

      } else if ("newpost".equals(command)) {

         //http://localhost:8080/chunchun/chunchunservlet?command=newpost

         postBean.setMessage("New message from mgencur");
         postBean.sendPost();
         answer.append("\n").append("New post sent: " + postBean.getMessage());

      } else if ("myposts".equals(command)) {

         //http://localhost:8080/chunchun/chunchunservlet?command=myposts

         List<DisplayPost> myPosts = postBean.getMyPosts();
         for (DisplayPost post : myPosts) {
            answer.append("\n").append(post.getMessage());
         }

      } else if ("watching".equals(command)) {

         //http://localhost:8080/chunchun/chunchunservlet?command=watching

         List<User> watchedByMe = userBean.getWatching();
         for (User user : watchedByMe) {
            answer.append("\n").append(user.getName() + " (" + user.getWhoami() + ")");
         }

      } else if ("watchers".equals(command)) {

         //http://localhost:8080/chunchun/chunchunservlet?command=watchers

         List<User> watchers = userBean.getWatchers();
         for (User user : watchers) {
            answer.append("\n").append(user.getName() + " (" + user.getWhoami() + ")");
         }

      } else if ("watchuser".equals(command)) { //watch user according to userParam parameter

         //http://localhost:8080/chunchun/chunchunservlet?command=watchuser&user=NameXY

         List<User> watchers = userBean.getWatchers();
         for (User u : watchers) {
            if (u.getName().equals(userParam)) {
               if (!userBean.isWatchedByMe(u)) {
                  userBean.watchUser(u);
                  answer.append("\n").append("Started watching user " + u.getName());
               }  else {
                  answer.append("\n").append("NoOP - I'm already watching that user");
               }
            }
         }

      } else if ("stopwatchinguser".equals(command)) {

         //http://localhost:8080/chunchun/chunchunservlet?command=stopwatchinguser&user=NameXY

         List<User> watchedByMe = userBean.getWatching();
         for (User u : watchedByMe) {
            if (u.getUsername().equals(userParam)) {
               userBean.stopWatchingUser(u);
            }
         }

      } else {
         answer.append("\n").append("Unknown command");
      }

//      if( answer.toString().length() != 0) {
//         response.setHeader("answer", answer.toString());
//      }
      PrintWriter out = response.getWriter();
      out.print(answer.toString());
      out.flush();
   }

   private Authenticator getAuthenticator() {
      Authenticator auth = getContextualInstance(getBeanManagerFromJNDI(), Authenticator.class);
      return auth;
   }

   private PostBean getPostBean() {
      PostBean postBean = getContextualInstance(getBeanManagerFromJNDI(), PostBean.class);
      return postBean;
   }

   private UserBean getUserBean() {
      UserBean userBean = getContextualInstance(getBeanManagerFromJNDI(), UserBean.class);
      return userBean;
   }

   private BeanManager getBeanManagerFromJNDI() {
      InitialContext context;
      Object result;
      try {
         context = new InitialContext();
         result = context.lookup("java:comp/BeanManager");
      } catch (NamingException e) {
         throw new RuntimeException("BeanManager could not be found in JNDI", e);
      }
      return (BeanManager) result;
   }

   @SuppressWarnings("unchecked")
   public <T> T getContextualInstance(final BeanManager manager, final Class<T> type) {
      T result = null;
      Bean<T> bean = (Bean<T>) manager.resolve(manager.getBeans(type));
      if (bean != null) {
         CreationalContext<T> context = manager.createCreationalContext(bean);
         if (context != null) {
            result = (T) manager.getReference(bean, type, context);
         }
      }
      return result;
   }
}
