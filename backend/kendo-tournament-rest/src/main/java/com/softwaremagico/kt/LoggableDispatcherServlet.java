package com.softwaremagico.kt;

import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoggableDispatcherServlet extends DispatcherServlet {
	private static final long serialVersionUID = -8650983209144626130L;

	@Override
	protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (!(request instanceof ContentCachingRequestWrapper)) {
			request = new ContentCachingRequestWrapper(request);
		}
		super.doDispatch(request, response);
	}
}
