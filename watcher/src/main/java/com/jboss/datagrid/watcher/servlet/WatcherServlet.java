package com.jboss.datagrid.watcher.servlet;

import com.jboss.datagrid.watcher.model.User;
import com.jboss.datagrid.watcher.session.Authenticator;
import com.jboss.datagrid.watcher.session.DisplayPost;
import com.jboss.datagrid.watcher.session.PostBean;
import com.jboss.datagrid.watcher.session.UserBean;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.enterprise.inject.Instance;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * A servlet that invokes application logic based on URL parameters. This is to
 * avoid using layers like JSF which slows down performance and complicates
 * things when debugging.
 *
 * This servlet is dedicated mainly for performance testing.
 *
 * @author Martin Gencur
 */
@WebServlet(urlPatterns={"/watcherservlet"})
public class WatcherServlet extends HttpServlet {

   @Inject
   private Instance<Authenticator> auth;

   @Inject
   private Instance<PostBean> postBean;

   @Inject
   private Instance<UserBean> userBean;

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
      System.out.println("command: " + command);
      System.out.println("userParam: " + userParam);

      if ("login".equals(command)) {

         //http://localhost:8080/watcher/watcherservlet?command=login

         if (!auth.get().isLoggedIn()) {
            auth.get().login();
            answer.append("\n").append("User Logged in");
         }

      } else if ("logout".equals(command)) {

         //http://localhost:8080/watcher/watcherservlet?command=logout

         auth.get().logoutFromServlet();
         answer.append("\n").append("User Logged out");

      } else if ("recentposts".equals(command)) {

         //http://localhost:8080/watcher/watcherservlet?command=recentposts     //limit defaults to 10
         //http://localhost:8080/watcher/watcherservlet?command=recentposts&limit=20

         postBean.get().setDisplayedPostsLimit(displayLimitParam);
         List<DisplayPost> recentPosts = postBean.get().getRecentPosts();
         answer.append("\n").append("Displayed: " + postBean.get().getDisplayedPostsLimit()).append("\n");
         for (DisplayPost post : recentPosts) {
            answer.append("\n").append(post.getMessage());
         }

      } else if ("newpost".equals(command)) {

         //http://localhost:8080/watcher/watcherservlet?command=newpost

         postBean.get().setMessage("New message from mgencur");
         postBean.get().sendPost();
         answer.append("\n").append("New post sent: " + postBean.get().getMessage());

      } else if ("myposts".equals(command)) {

         //http://localhost:8080/watcher/watcherservlet?command=myposts

         List<DisplayPost> myPosts = postBean.get().getMyPosts();
         for (DisplayPost post : myPosts) {
            answer.append("\n").append(post.getMessage());
         }

      } else if ("watching".equals(command)) {

         //http://localhost:8080/watcher/watcherservlet?command=watching

         List<User> watchedByMe = userBean.get().getWatching();
         for (User user : watchedByMe) {
            answer.append("\n").append(user.getName() + " (" + user.getWhoami() + ")");
         }

      } else if ("watchers".equals(command)) {

         //http://localhost:8080/watcher/watcherservlet?command=watchers

         List<User> watchers = userBean.get().getWatchers();
         for (User user : watchers) {
            answer.append("\n").append(user.getName() + " (" + user.getWhoami() + ")");
         }

      } else if ("watchuser".equals(command)) { //watch user according to userParam parameter

         //http://localhost:8080/watcher/watcherservlet?command=watchuser&user=NameXY

         List<User> watchers = userBean.get().getWatchers();
         for (User u : watchers) {
            if (u.getName().equals(userParam)) {
               if (!userBean.get().isWatchedByMe(u)) {
                  userBean.get().watchUser(u);
                  answer.append("\n").append("Started watching user " + u.getName());
               }  else {
                  answer.append("\n").append("NoOP - I'm already watching that user");
               }
            }
         }

      } else if ("stopwatchinguser".equals(command)) {

         //http://localhost:8080/watcher/watcherservlet?command=stopwatchinguser&user=NameXY

         List<User> watchedByMe = userBean.get().getWatching();
         for (User u : watchedByMe) {
            if (u.getUsername().equals(userParam)) {
               userBean.get().stopWatchingUser(u);
            }
         }

      }

      if( answer.toString().length() != 0) {
         response.setHeader("answer", answer.toString());
      }
      PrintWriter out = response.getWriter();
      out.print(answer.toString());
      out.flush();
   }
}
