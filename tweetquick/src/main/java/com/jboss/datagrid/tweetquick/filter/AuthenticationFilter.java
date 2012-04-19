package com.jboss.datagrid.tweetquick.filter;

import java.io.IOException;


import javax.servlet.*;
import javax.servlet.http.*;

import com.jboss.datagrid.tweetquick.session.Authenticator;

public final class AuthenticationFilter implements Filter 
{
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException 
    {
        HttpSession session = ((HttpServletRequest) request).getSession();
        Authenticator auth = (Authenticator) session.getAttribute("auth");
        if (auth != null && auth.isLoggedIn())
        {
            chain.doFilter(request, response);
        }
        else
        {
        	((HttpServletResponse) response).sendRedirect("login.jsf");
        }
    }

    public void destroy() 
    {
    }

    public void init(FilterConfig filterConfig) 
    {
    }

}