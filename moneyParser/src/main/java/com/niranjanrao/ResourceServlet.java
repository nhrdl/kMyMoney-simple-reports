package com.niranjanrao;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ResourceServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(ResourceServlet.class);
	private final int IO_BUFFER_SIZE = 4024;

	@Override
	protected void doGet(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {

		String contentType = "text/html";
		final String pathInfo = request.getPathInfo().toLowerCase();
		if (pathInfo != null) {
			if (pathInfo.endsWith(".css")) {
				contentType = "text/css";
			} else if (pathInfo.matches("(jpe?g|gif)$")) {
				contentType = "image/gif";
			} else if (pathInfo.endsWith(".js")) {
				contentType = "text/javascript";
			}
		}
		response.setContentType(contentType);

		response.setStatus(HttpServletResponse.SC_OK);

		final String path = request.getPathInfo();

		final InputStream input = getClass().getResourceAsStream(path);
		if (null == input) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		} else {
			LOG.warn("File {} not found, but still returning OK", path);
		}
		try {
			final OutputStream output = response.getOutputStream();
			final byte[] b = new byte[IO_BUFFER_SIZE];
			int read;
			while ((read = input.read(b)) != -1) {
				output.write(b, 0, read);
			}
			output.close();
		} finally {
			input.close();
		}

	}

}
