/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package jp.xet.sparwings.aws.ec2;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for {@link MDCInsertingForRequestServletFilter}.
 */
@ExtendWith(MockitoExtension.class)
public class MDCInsertingForRequestServletFilterTest {

    @Mock
    ServletRequest request;

    @Mock
    ServletResponse response;

    @Mock
    FilterChain chain;


    /**
     * request scope に MDC が存在しない時のテスト.
     */
    @Test
    public void testDoFilter_FirstTimeDoFilter() throws IOException, ServletException {
        // setup
        when(request.getAttribute(anyString())).thenReturn(null);
        doNothing().when(request).setAttribute(
            anyString(),
            any()
        );
        doNothing().when(chain).doFilter(
            any(),
            any()
        );

        // exercise
        MDCInsertingForRequestServletFilter sut = new MDCInsertingForRequestServletFilter();
        sut.doFilter(
            request,
            response,
            chain
        );

        // verify
        verify(request).getAttribute(MDCInsertingForRequestServletFilter.STORED_MDC_KEY);
        verify(request).setAttribute(
            eq(MDCInsertingForRequestServletFilter.STORED_MDC_KEY),
            any()
        ); // 呼べていれば良い
        verify(chain).doFilter(
            request,
            response
        );
    }

    /**
     * request scope に MDC が存在する時のテスト.
     */
    @Test
    public void testDoFilter_AfterSecondTimeDoFilter() throws IOException, ServletException {
        // setup
        Map<String, String> storedMdc = new HashMap<>();
        storedMdc.put(
            "foo",
            "foo1"
        );
        storedMdc.put(
            "bar",
            "bar1"
        );
        storedMdc.put(
            "baz",
            "baz1"
        );
        when(request.getAttribute(anyString())).thenReturn(storedMdc);
        doNothing().when(chain).doFilter(
            any(),
            any()
        );

        // exercise
        MDCInsertingForRequestServletFilter sut = new MDCInsertingForRequestServletFilter();
        sut.doFilter(
            request,
            response,
            chain
        );

        // verify
        verify(request).getAttribute(MDCInsertingForRequestServletFilter.STORED_MDC_KEY);
        verify(chain).doFilter(
            request,
            response
        );

        verify(
            request,
            never()
        ).setAttribute(
            any(),
            any()
        );
    }

}
