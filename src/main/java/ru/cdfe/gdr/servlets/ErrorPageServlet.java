package ru.cdfe.gdr.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ru.cdfe.gdr.representations.ErrorResource;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebServlet("/error")
@Slf4j
public class ErrorPageServlet extends HttpServlet {
	private static final String logTemplate = "Request: [%s %s], Uncaught exception: [%s]";
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			processError(req, resp);
		} catch (RuntimeException e) {
			throw new ServletException(e);
		}
	}

	private void processError(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setStatus(computeStatus(request));
		
		ErrorResource errorResource = processError(request);
		
		if (errorResource != null) {
			response.setContentType("application/json");
			response.setCharacterEncoding(StandardCharsets.UTF_8.name());
			
			new ObjectMapper().writeValue(response.getWriter(), errorResource);
		}
	}
	
	private ErrorResource processError(HttpServletRequest request) {
		Throwable uncaughtException = (Throwable) request.getAttribute("javax.servlet.error.exception");
		ErrorResource errorResource = null;
		
		if (uncaughtException != null) {
			String uri = (String) request.getAttribute("javax.servlet.error.request_uri");
			String method = request.getMethod();
			
			errorResource = new ErrorResource("Internal server error");
			
			log.error(String.format(logTemplate, method, uri, uncaughtException));
		}
		
		return errorResource;
	}
	
	private Integer computeStatus(HttpServletRequest request) {
		Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
		Throwable uncaughtException = (Throwable) request.getAttribute("javax.servlet.error.exception");
	
		if (statusCode == null) {
			if (uncaughtException == null) {
				statusCode = HttpServletResponse.SC_NOT_FOUND;
			} else {
				statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
			}
		}
	
		return statusCode;
	}
}
