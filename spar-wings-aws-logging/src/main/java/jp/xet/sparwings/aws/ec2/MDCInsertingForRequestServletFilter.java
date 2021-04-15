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
package jp.xet.sparwings.aws.ec2;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.MDC;

/**
 * MDC の情報を request scope から設定する Filter.
 * 
 * <p>
 * 本 Filter は、LogbookFilter の直前に登録する必要があります。
 * </p>
 */
public class MDCInsertingForRequestServletFilter implements Filter {
	
	private static final String STORED_MDC_KEY =
			MDCInsertingForRequestServletFilter.class.getName() + "_STORED_MDC_KEY";
	
	
	/**
	 * filter 処理.
	 * 
	 * <p>
	 * 初回リクエストの場合、以下を行う
	 * 
	 * - request scope に MDC の値をコピー
	 * - 後続の filter 呼び出し
	 * 
	 * 2回目以降のリクエストの場合、以下を行う
	 * 
	 * - request scope から MDC の値を設定
	 * - 後続の filter 呼び出し
	 * - MDC の値をクリア
	 * 
	 * </p>
	 * @param request request
	 * @param response response
	 * @param chain chain
	 * @throws IOException 例外
	 * @throws ServletException 例外
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		@SuppressWarnings("unchecked")
		Map<String, String> storedMdc = (Map<String, String>) request.getAttribute(STORED_MDC_KEY);
		if (storedMdc == null) {
			// request scope に未設定の場合、初回扱い
			firstTimeDoFilter(request, response, chain);
		} else {
			// request scope に設定済みの場合、2回目以降の呼び出し
			afterSecondTimeDoFilter(request, response, chain, storedMdc);
		}
	}
	
	private void firstTimeDoFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		// request scope に mdc の値をコピー
		request.setAttribute(STORED_MDC_KEY, MDC.getCopyOfContextMap());
		chain.doFilter(request, response);
	}
	
	private void afterSecondTimeDoFilter(ServletRequest request, ServletResponse response, FilterChain chain,
			Map<String, String> storedMdc) throws IOException, ServletException {
		// request scope の mdc を MDC に展開
		MDC.setContextMap(storedMdc);
		try {
			chain.doFilter(request, response);
		} finally {
			MDC.clear();
		}
	}
}
