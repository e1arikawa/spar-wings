/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.xet.sparwings.common.filters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Collection;
import java.util.Enumeration;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * TODO for daisuke
 * 
 * <pre><code>
 * &lt;filter class="ch.qos.logback.core.filter.EvaluatorFilter">
 *   &lt;evaluator class="ch.qos.logback.classic.boolex.OnMarkerEvaluator">
 *     &lt;marker>HTTP_DUMP&lt;/marker>
 *   &lt;/evaluator>
 *   &lt;onMismatch>NEUTRAL&lt;/onMismatch>
 *   &lt;onMatch>DENY&lt;/onMatch>
 * &lt;/filter>
 * </code></pre>
 */
@Slf4j
public class HttpDumpFilter implements Filter {
	
	/** SLF4J {@link Marker} for HTTP dump */
	public static final Marker MARKER = MarkerFactory.getMarker("HTTP_DUMP");
	
	private static final Encoder ENCODER = Base64.getEncoder();
	
	private static final String NL = System.getProperty("line.separator");
	
	
	static boolean isPrintable(byte[] value) {
		for (byte element : value) {
			char c = (char) element;
			if ((Character.isISOControl(c)
					|| ((element & 0xFF) >= 0x80)) && (Character.isWhitespace(c) == false)) {
				return false;
			}
		}
		return true;
	}
	
	static String buildRequestUrl(HttpServletRequest r) {
		String servletPath = r.getServletPath();
		String pathInfo = r.getPathInfo();
		String queryString = r.getQueryString();
		StringBuilder url = new StringBuilder();
		
		if (servletPath != null) {
			url.append(servletPath);
			if (pathInfo != null) {
				url.append(pathInfo);
			}
		} else {
			url.append(r.getRequestURI().substring(r.getContextPath().length()));
		}
		
		if (queryString != null) {
			url.append('?').append(queryString);
		}
		
		return url.toString();
	}
	
	
	@Setter
	@Getter
	private boolean dumpRequest = true;
	
	@Setter
	@Getter
	private boolean dumpResponse = true;
	
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// do nothing
	}
	
	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		BufferedRequestWrapper bufferedRequest = new BufferedRequestWrapper((HttpServletRequest) servletRequest);
		
		StringBuilder sb = new StringBuilder();
		if (dumpRequest) {
			dumpRequest(bufferedRequest, sb);
		}
		
		final HttpServletResponse response = (HttpServletResponse) servletResponse;
		final ByteArrayPrintWriter pw = new ByteArrayPrintWriter();
		HttpServletResponse wrappedResp = new HttpServletResponseWrapper(response) {
			
			@Override
			public PrintWriter getWriter() {
				return pw.getWriter();
			}
			
			@Override
			public ServletOutputStream getOutputStream() {
				return pw.getStream();
			}
		};
		
		filterChain.doFilter(bufferedRequest, wrappedResp);
		
		byte[] bytes = pw.toByteArray();
		response.getOutputStream().write(bytes);
		
		if (dumpRequest && dumpResponse) {
			sb.append("---").append(NL);
		}
		if (dumpResponse) {
			dumpResponse(wrappedResp, bytes, sb);
		}
		if (dumpRequest || dumpResponse) {
			log.trace(MARKER, sb.toString());
		}
	}
	
	private void dumpRequest(BufferedRequestWrapper bufferedRequest, StringBuilder sb) {
		sb.append(bufferedRequest.getMethod()).append(' ')
			.append(buildRequestUrl(bufferedRequest))
			.append(NL);
		
		Enumeration<String> requestHeaderNames = bufferedRequest.getHeaderNames();
		while (requestHeaderNames.hasMoreElements()) {
			String headerName = requestHeaderNames.nextElement();
			Enumeration<String> headers = bufferedRequest.getHeaders(headerName);
			while (headers.hasMoreElements()) {
				String value = headers.nextElement();
				sb.append(headerName).append(": ").append(value).append(NL);
			}
		}
		byte[] buffer = bufferedRequest.getBuffer();
		if (buffer.length > 0) {
			if (buffer.length <= 256 && isPrintable(buffer)) {
				sb.append(NL);
				sb.append(new String(buffer, StandardCharsets.UTF_8)).append(NL);
			} else {
				sb.append("HttpDumpFilter-Body-Encoding: b64").append(NL);
				sb.append(NL);
				sb.append(ENCODER.encodeToString(buffer)).append(NL);
			}
		}
	}
	
	private void dumpResponse(HttpServletResponse response, byte[] buffer, StringBuilder sb) {
		int status = response.getStatus();
		sb.append(status).append(' ').append(HttpStatus.valueOf(status).getReasonPhrase()).append(NL);
		
		Collection<String> responseHeaderNames = response.getHeaderNames();
		for (String headerName : responseHeaderNames) {
			Collection<String> headers = response.getHeaders(headerName);
			for (String value : headers) {
				sb.append(headerName).append(": ").append(value).append(NL);
			}
		}
		
		if (buffer.length > 0) {
			if (buffer.length <= 256 && isPrintable(buffer)) {
				sb.append(NL);
				sb.append(new String(buffer, StandardCharsets.UTF_8)).append(NL);
			} else {
				sb.append("HttpDumpFilter-Body-Encoding: base64").append(NL);
				sb.append(NL);
				sb.append(ENCODER.encodeToString(buffer)).append(NL);
			}
		}
	}
	
	@Override
	public void destroy() {
		// do nothing
	}
	
	
	@RequiredArgsConstructor
	private static class ByteArrayServletStream extends ServletOutputStream {
		
		final ByteArrayOutputStream baos;
		
		
		@Override
		public void write(int param) throws IOException {
			baos.write(param);
		}
		
		@Override
		public boolean isReady() {
			return false;
		}
		
		@Override
		public void setWriteListener(WriteListener writeListener) {
			// do nothing
		}
	}
	
	private static class ByteArrayPrintWriter {
		
		private ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		@Getter
		private PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8), false);
		
		@Getter
		private ServletOutputStream stream = new ByteArrayServletStream(baos);
		
		
		byte[] toByteArray() {
			return baos.toByteArray();
		}
	}
	
	@RequiredArgsConstructor
	private static class BufferedServletInputStream extends ServletInputStream {
		
		@Delegate
		final ByteArrayInputStream bais;
		
		
		@Override
		public boolean isFinished() {
			return false;
		}
		
		@Override
		public boolean isReady() {
			return false;
		}
		
		@Override
		public void setReadListener(ReadListener readListener) {
			// do nothing
		}
	}
	
	private static class BufferedRequestWrapper extends HttpServletRequestWrapper {
		
		ByteArrayInputStream bais;
		
		ByteArrayOutputStream baos;
		
		BufferedServletInputStream bsis;
		
		@Getter
		byte[] buffer;
		
		
		BufferedRequestWrapper(HttpServletRequest req) throws IOException {
			super(req);
			try (InputStream is = req.getInputStream()) {
				baos = new ByteArrayOutputStream();
				byte[] buf = new byte[1024];
				int letti;
				while ((letti = is.read(buf)) > 0) {
					baos.write(buf, 0, letti);
				}
				buffer = baos.toByteArray();
			}
		}
		
		@Override
		public ServletInputStream getInputStream() {
			try {
				bais = new ByteArrayInputStream(buffer);
				bsis = new BufferedServletInputStream(bais);
			} catch (Exception e) { // NOPMD
				log.error("unexpected", e);
			}
			
			return bsis;
		}
	}
}
