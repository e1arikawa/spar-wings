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
package jp.xet.sparwings.spring.data.web;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import org.junit.jupiter.api.Test;

import jp.xet.sparwings.spring.data.slice.SliceRequest;
import jp.xet.sparwings.spring.data.slice.Sliceable;
import jp.xet.sparwings.spring.web.httpexceptions.HttpBadRequestException;

/**
 * Test for {@link SliceableHandlerMethodArgumentResolver}.
 */
@SuppressWarnings("javadoc")
public class SliceableHandlerMethodArgumentResolverTest {
	
	SliceableHandlerMethodArgumentResolver sut = new SliceableHandlerMethodArgumentResolver();
	
	
	public void simpleHandler(Sliceable sliceable) {
		// nothing to do
	}
	
	@Test
	public void testSimpleHandler() throws Exception {
		// setup
		Method method = getClass().getMethod("simpleHandler", Sliceable.class);
		MethodParameter methodParametere = new MethodParameter(method, 0);
		ModelAndViewContainer mavContainer = mock(ModelAndViewContainer.class);
		NativeWebRequest webRequest = mock(NativeWebRequest.class);
		WebDataBinderFactory binderFactory = mock(WebDataBinderFactory.class);
		
		// exercise
		Object actual = sut.resolveArgument(methodParametere, mavContainer, webRequest, binderFactory);
		
		// verify
		assertThat(actual, is(notNullValue()));
		assertThat(actual, is(instanceOf(Sliceable.class)));
		assertThat(actual, is(sut.getFallbackSliceable()));
	}
	
	public void defaultHandler(@SliceableDefault Sliceable sliceable) {
		// nothing to do
	}
	
	@Test
	public void testDefaultHandler() throws Exception {
		// setup
		Method method = getClass().getMethod("defaultHandler", Sliceable.class);
		MethodParameter methodParametere = new MethodParameter(method, 0);
		ModelAndViewContainer mavContainer = mock(ModelAndViewContainer.class);
		NativeWebRequest webRequest = mock(NativeWebRequest.class);
		WebDataBinderFactory binderFactory = mock(WebDataBinderFactory.class);
		
		// exercise
		Object actual = sut.resolveArgument(methodParametere, mavContainer, webRequest, binderFactory);
		
		// verify
		assertThat(actual, is(notNullValue()));
		assertThat(actual, is(instanceOf(Sliceable.class)));
		Sliceable actualSliceable = (Sliceable) actual;
		
		Sliceable expected = new SliceRequest(0, Direction.ASC, 10);
		assertThat(actualSliceable, is(expected));
	}
	
	public void defaultHandlerWithValue(@SliceableDefault(size = 123) Sliceable sliceable) {
		// nothing to do
	}
	
	@Test
	public void testDefaultHandlerWithValue() throws Exception {
		// setup
		Method method = getClass().getMethod("defaultHandlerWithValue", Sliceable.class);
		MethodParameter methodParametere = new MethodParameter(method, 0);
		ModelAndViewContainer mavContainer = mock(ModelAndViewContainer.class);
		NativeWebRequest webRequest = mock(NativeWebRequest.class);
		WebDataBinderFactory binderFactory = mock(WebDataBinderFactory.class);
		
		// exercise
		Object actual = sut.resolveArgument(methodParametere, mavContainer, webRequest, binderFactory);
		
		// verify
		assertThat(actual, is(notNullValue()));
		assertThat(actual, is(instanceOf(Sliceable.class)));
		Sliceable actualSliceable = (Sliceable) actual;
		
		Sliceable expected = new SliceRequest(0, Direction.ASC, 123);
		assertThat(actualSliceable, is(expected));
	}
	
	public void multipleSliceable(Sliceable c1, Sliceable c2) {
		// nothing to do
	}
	
	@Test()
	public void testMultipleSliceable() throws Exception {
        assertThrows(IllegalStateException.class, () -> {
    		// setup
    		Method method = getClass().getMethod("multipleSliceable", Sliceable.class, Sliceable.class);
    		MethodParameter methodParametere = new MethodParameter(method, 0);
    		ModelAndViewContainer mavContainer = mock(ModelAndViewContainer.class);
    		NativeWebRequest webRequest = mock(NativeWebRequest.class);
    		WebDataBinderFactory binderFactory = mock(WebDataBinderFactory.class);
    		
    		// exercise
    		sut.resolveArgument(methodParametere, mavContainer, webRequest, binderFactory);
    		
    		// assert exception
    		fail();
        });
	}
	
	@Test
	public void testSimpleHandlerWithSizeParameter() throws Exception {
		// setup
		Method method = getClass().getMethod("simpleHandler", Sliceable.class);
		MethodParameter methodParametere = new MethodParameter(method, 0);
		ModelAndViewContainer mavContainer = mock(ModelAndViewContainer.class);
		NativeWebRequest webRequest = mock(NativeWebRequest.class);
		when(webRequest.getParameter(eq("size"))).thenReturn("123");
		WebDataBinderFactory binderFactory = mock(WebDataBinderFactory.class);
		
		// exercise
		Object actual = sut.resolveArgument(methodParametere, mavContainer, webRequest, binderFactory);
		
		// verify
		assertThat(actual, is(notNullValue()));
		assertThat(actual, is(instanceOf(Sliceable.class)));
		Sliceable actualSliceable = (Sliceable) actual;
		
		Sliceable expected = new SliceRequest(0, null, 123);
		assertThat(actualSliceable, is(expected));
	}
	
	@Test
	public void testDefaultHandlerWithSizeParameter() throws Exception {
		// setup
		Method method = getClass().getMethod("defaultHandler", Sliceable.class);
		MethodParameter methodParametere = new MethodParameter(method, 0);
		ModelAndViewContainer mavContainer = mock(ModelAndViewContainer.class);
		NativeWebRequest webRequest = mock(NativeWebRequest.class);
		when(webRequest.getParameter(eq("size"))).thenReturn("123");
		WebDataBinderFactory binderFactory = mock(WebDataBinderFactory.class);
		
		// exercise
		Object actual = sut.resolveArgument(methodParametere, mavContainer, webRequest, binderFactory);
		
		// verify
		assertThat(actual, is(notNullValue()));
		assertThat(actual, is(instanceOf(Sliceable.class)));
		Sliceable actualSliceable = (Sliceable) actual;
		
		Sliceable expected = new SliceRequest(0, Direction.ASC, 123);
		assertThat(actualSliceable, is(expected));
	}
	
	@Test
	public void testDefaultHandlerWithValueWithSizeParameter() throws Exception {
		// setup
		Method method = getClass().getMethod("defaultHandlerWithValue", Sliceable.class);
		MethodParameter methodParametere = new MethodParameter(method, 0);
		ModelAndViewContainer mavContainer = mock(ModelAndViewContainer.class);
		NativeWebRequest webRequest = mock(NativeWebRequest.class);
		when(webRequest.getParameter(eq("size"))).thenReturn("123");
		WebDataBinderFactory binderFactory = mock(WebDataBinderFactory.class);
		
		// exercise
		Object actual = sut.resolveArgument(methodParametere, mavContainer, webRequest, binderFactory);
		
		// verify
		assertThat(actual, is(notNullValue()));
		assertThat(actual, is(instanceOf(Sliceable.class)));
		Sliceable actualSliceable = (Sliceable) actual;
		
		Sliceable expected = new SliceRequest(0, Direction.ASC, 123);
		assertThat(actualSliceable, is(expected));
	}
	
	@Test
	public void testSimpleHandlerWithExceededSizeParameter() throws Exception {
		// setup
		Method method = getClass().getMethod("simpleHandler", Sliceable.class);
		MethodParameter methodParametere = new MethodParameter(method, 0);
		ModelAndViewContainer mavContainer = mock(ModelAndViewContainer.class);
		NativeWebRequest webRequest = mock(NativeWebRequest.class);
		when(webRequest.getParameter(eq("size"))).thenReturn("12345");
		WebDataBinderFactory binderFactory = mock(WebDataBinderFactory.class);
		
		// exercise
		Object actual = sut.resolveArgument(methodParametere, mavContainer, webRequest, binderFactory);
		
		// verify
		assertThat(actual, is(notNullValue()));
		assertThat(actual, is(instanceOf(Sliceable.class)));
		Sliceable actualSliceable = (Sliceable) actual;
		
		Sliceable expected = new SliceRequest(0, null, 2000); // 2000 を超えないこと
		assertThat(actualSliceable, is(expected));
	}
	
	@Test
	public void testDefaultHandlerWithExceededSizeParameter() throws Exception {
		// setup
		Method method = getClass().getMethod("defaultHandler", Sliceable.class);
		MethodParameter methodParametere = new MethodParameter(method, 0);
		ModelAndViewContainer mavContainer = mock(ModelAndViewContainer.class);
		NativeWebRequest webRequest = mock(NativeWebRequest.class);
		when(webRequest.getParameter(eq("size"))).thenReturn("12345");
		WebDataBinderFactory binderFactory = mock(WebDataBinderFactory.class);
		
		// exercise
		Object actual = sut.resolveArgument(methodParametere, mavContainer, webRequest, binderFactory);
		
		// verify
		assertThat(actual, is(notNullValue()));
		assertThat(actual, is(instanceOf(Sliceable.class)));
		Sliceable actualSliceable = (Sliceable) actual;
		
		Sliceable expected = new SliceRequest(0, Direction.ASC, 2000); // 2000 を超えないこと
		assertThat(actualSliceable, is(expected));
	}
	
	@Test
	public void testNegativeSize() throws Exception {
		// setup
		Method method = getClass().getMethod("simpleHandler", Sliceable.class);
		MethodParameter methodParametere = new MethodParameter(method, 0);
		ModelAndViewContainer mavContainer = mock(ModelAndViewContainer.class);
		NativeWebRequest webRequest = mock(NativeWebRequest.class);
		when(webRequest.getParameter(eq("size"))).thenReturn("-1");
		WebDataBinderFactory binderFactory = mock(WebDataBinderFactory.class);
		
		// exercise
		Object actual = sut.resolveArgument(methodParametere, mavContainer, webRequest, binderFactory);
		
		// verify
		assertThat(actual, is(notNullValue()));
		assertThat(actual, is(instanceOf(Sliceable.class)));
		Sliceable actualSliceable = (Sliceable) actual;
		
		Sliceable expected = new SliceRequest(0, null, 1); // 強制的に 1
		assertThat(actualSliceable, is(expected));
	}
	
	@Test
	public void testSimpleHandlerWithIllegalSizeParameter() throws Exception {
		// setup
		Method method = getClass().getMethod("simpleHandler", Sliceable.class);
		MethodParameter methodParametere = new MethodParameter(method, 0);
		ModelAndViewContainer mavContainer = mock(ModelAndViewContainer.class);
		NativeWebRequest webRequest = mock(NativeWebRequest.class);
		when(webRequest.getParameter(eq("size"))).thenReturn("foo");
		WebDataBinderFactory binderFactory = mock(WebDataBinderFactory.class);
		
		// exercise
		Object actual = sut.resolveArgument(methodParametere, mavContainer, webRequest, binderFactory);
		
		// verify
		assertThat(actual, is(notNullValue()));
		assertThat(actual, is(instanceOf(Sliceable.class)));
		Sliceable actualSliceable = (Sliceable) actual;
		
		Sliceable expected = new SliceRequest(0, null, 2000); // 強制的に 2000
		assertThat(actualSliceable, is(expected));
	}
	
	@Test
	public void testDefaultHandlerWithIllegalSizeParameter() throws Exception {
		// setup
		Method method = getClass().getMethod("defaultHandler", Sliceable.class);
		MethodParameter methodParametere = new MethodParameter(method, 0);
		ModelAndViewContainer mavContainer = mock(ModelAndViewContainer.class);
		NativeWebRequest webRequest = mock(NativeWebRequest.class);
		when(webRequest.getParameter(eq("size"))).thenReturn("foo");
		WebDataBinderFactory binderFactory = mock(WebDataBinderFactory.class);
		
		// exercise
		Object actual = sut.resolveArgument(methodParametere, mavContainer, webRequest, binderFactory);
		
		// verify
		assertThat(actual, is(notNullValue()));
		assertThat(actual, is(instanceOf(Sliceable.class)));
		Sliceable actualSliceable = (Sliceable) actual;
		
		Sliceable expected = new SliceRequest(0, Direction.ASC, 10); // 強制的に 10
		assertThat(actualSliceable, is(expected));
	}
	
	@Test
	public void testDefaultHandlerWithValueWithIllegalSizeParameter() throws Exception {
		// setup
		Method method = getClass().getMethod("defaultHandlerWithValue", Sliceable.class);
		MethodParameter methodParametere = new MethodParameter(method, 0);
		ModelAndViewContainer mavContainer = mock(ModelAndViewContainer.class);
		NativeWebRequest webRequest = mock(NativeWebRequest.class);
		when(webRequest.getParameter(eq("size"))).thenReturn("foo");
		WebDataBinderFactory binderFactory = mock(WebDataBinderFactory.class);
		
		// exercise
		Object actual = sut.resolveArgument(methodParametere, mavContainer, webRequest, binderFactory);
		
		// verify
		assertThat(actual, is(notNullValue()));
		assertThat(actual, is(instanceOf(Sliceable.class)));
		Sliceable actualSliceable = (Sliceable) actual;
		
		Sliceable expected = new SliceRequest(0, Direction.ASC, 123); // 強制的に default 値
		assertThat(actualSliceable, is(expected));
	}
	
	@Test
	public void testDefaultHandlerWithValueWithPageNumberParameter() throws Exception {
		// setup
		Method method = getClass().getMethod("simpleHandler", Sliceable.class);
		MethodParameter methodParametere = new MethodParameter(method, 0);
		ModelAndViewContainer mavContainer = mock(ModelAndViewContainer.class);
		NativeWebRequest webRequest = mock(NativeWebRequest.class);
		when(webRequest.getParameter(eq("page_number"))).thenReturn("0");
		WebDataBinderFactory binderFactory = mock(WebDataBinderFactory.class);
		
		// exercise
		Object actual = sut.resolveArgument(methodParametere, mavContainer, webRequest, binderFactory);
		
		// verify
		assertThat(actual, is(notNullValue()));
		assertThat(actual, is(instanceOf(Sliceable.class)));
		Sliceable actualSliceable = (Sliceable) actual;
		
		Sliceable expected = new SliceRequest(0, null, 2000);
		assertThat(actualSliceable, is(expected));
	}
	
	@Test
	public void testDefaultHandlerWithValueWithPageNumberParameter_Over() throws Exception {
		// setup
		Method method = getClass().getMethod("simpleHandler", Sliceable.class);
		MethodParameter methodParametere = new MethodParameter(method, 0);
		ModelAndViewContainer mavContainer = mock(ModelAndViewContainer.class);
		NativeWebRequest webRequest = mock(NativeWebRequest.class);
		when(webRequest.getParameter(eq("page_number"))).thenReturn("1"); // over
		WebDataBinderFactory binderFactory = mock(WebDataBinderFactory.class);
		
		// exercise
		Throwable actual =
				catchThrowable(() -> sut.resolveArgument(methodParametere, mavContainer, webRequest, binderFactory));
		
		// verify
		assertThat(actual, is(notNullValue()));
		assertThat(actual, is(instanceOf(HttpBadRequestException.class)));
		assertThat(actual.getMessage(), is("Cannot get elements beyond 2000."));
	}
	
	@Test
	public void testDefaultHandlerWithValueWithIllegalPageNumberParameter() throws Exception {
		// setup
		Method method = getClass().getMethod("simpleHandler", Sliceable.class);
		MethodParameter methodParametere = new MethodParameter(method, 0);
		ModelAndViewContainer mavContainer = mock(ModelAndViewContainer.class);
		NativeWebRequest webRequest = mock(NativeWebRequest.class);
		when(webRequest.getParameter(eq("page_number"))).thenReturn("foo");
		WebDataBinderFactory binderFactory = mock(WebDataBinderFactory.class);
		
		// exercise
		Object actual = sut.resolveArgument(methodParametere, mavContainer, webRequest, binderFactory);
		
		// verify
		assertThat(actual, is(notNullValue()));
		assertThat(actual, is(instanceOf(Sliceable.class)));
		Sliceable actualSliceable = (Sliceable) actual;
		
		Sliceable expected = new SliceRequest(0, null, 2000);
		assertThat(actualSliceable, is(expected));
	}
	
	@Test
	public void testPageNumberAndSize() throws Exception {
		// setup
		Method method = getClass().getMethod("simpleHandler", Sliceable.class);
		MethodParameter methodParametere = new MethodParameter(method, 0);
		ModelAndViewContainer mavContainer = mock(ModelAndViewContainer.class);
		NativeWebRequest webRequest = mock(NativeWebRequest.class);
		when(webRequest.getParameter(eq("size"))).thenReturn("10");
		when(webRequest.getParameter(eq("page_number"))).thenReturn("101");
		WebDataBinderFactory binderFactory = mock(WebDataBinderFactory.class);
		
		// exercise
		Object actual = sut.resolveArgument(methodParametere, mavContainer, webRequest, binderFactory);
		
		// verify
		assertThat(actual, is(notNullValue()));
		assertThat(actual, is(instanceOf(Sliceable.class)));
		Sliceable actualSliceable = (Sliceable) actual;
		
		Sliceable expected = new SliceRequest(101, null, 10);
		assertThat(actualSliceable, is(expected));
	}
	
	@Test
	public void testDirection() throws Exception {
		Method method = getClass().getMethod("simpleHandler", Sliceable.class);
		MethodParameter methodParametere = new MethodParameter(method, 0);
		ModelAndViewContainer mavContainer = mock(ModelAndViewContainer.class);
		NativeWebRequest webRequest = mock(NativeWebRequest.class);
		when(webRequest.getParameter(eq("direction"))).thenReturn("DESC");
		WebDataBinderFactory binderFactory = mock(WebDataBinderFactory.class);
		
		// exercise
		Object actual = sut.resolveArgument(methodParametere, mavContainer, webRequest, binderFactory);
		
		// verify
		assertThat(actual, is(notNullValue()));
		assertThat(actual, is(instanceOf(Sliceable.class)));
		Sliceable actualSliceable = (Sliceable) actual;
		
		Sliceable expected = new SliceRequest(0, Direction.DESC, 2000);
		assertThat(actualSliceable, is(expected));
	}
	
	@Test
	public void testIllegalDirection() throws Exception {
		// setup
		Method method = getClass().getMethod("simpleHandler", Sliceable.class);
		MethodParameter methodParametere = new MethodParameter(method, 0);
		ModelAndViewContainer mavContainer = mock(ModelAndViewContainer.class);
		NativeWebRequest webRequest = mock(NativeWebRequest.class);
		when(webRequest.getParameter(eq("direction"))).thenReturn("FOO");
		WebDataBinderFactory binderFactory = mock(WebDataBinderFactory.class);
		
		// exercise
		Object actual = sut.resolveArgument(methodParametere, mavContainer, webRequest, binderFactory);
		
		// verify
		assertThat(actual, is(notNullValue()));
		assertThat(actual, is(instanceOf(Sliceable.class)));
		Sliceable actualSliceable = (Sliceable) actual;
		
		Sliceable expected = new SliceRequest(0, null, 2000);
		assertThat(actualSliceable, is(expected));
	}
	
	@Test
	public void testFull() throws Exception {
		// setup
		Method method = getClass().getMethod("simpleHandler", Sliceable.class);
		MethodParameter methodParametere = new MethodParameter(method, 0);
		ModelAndViewContainer mavContainer = mock(ModelAndViewContainer.class);
		NativeWebRequest webRequest = mock(NativeWebRequest.class);
		when(webRequest.getParameter(eq("size"))).thenReturn("12");
		when(webRequest.getParameter(eq("page_number"))).thenReturn("13");
		when(webRequest.getParameter(eq("direction"))).thenReturn("DESC");
		WebDataBinderFactory binderFactory = mock(WebDataBinderFactory.class);
		
		// exercise
		Object actual = sut.resolveArgument(methodParametere, mavContainer, webRequest, binderFactory);
		
		// verify
		assertThat(actual, is(notNullValue()));
		assertThat(actual, is(instanceOf(Sliceable.class)));
		Sliceable actualSliceable = (Sliceable) actual;
		
		Sliceable expected = new SliceRequest(13, Direction.DESC, 12);
		assertThat(actualSliceable, is(expected));
	}
	
	public void defaultHandlerWithFullValue(
			@SliceableDefault(size = 10, pageNumber = 2, direction = Direction.DESC) Sliceable sliceable) {
		// nothing to do
	}
	
	@Test
	public void testDefaultHandlerWithFullValue() throws Exception {
		// setup
		Method method = getClass().getMethod("defaultHandlerWithFullValue", Sliceable.class);
		MethodParameter methodParametere = new MethodParameter(method, 0);
		ModelAndViewContainer mavContainer = mock(ModelAndViewContainer.class);
		NativeWebRequest webRequest = mock(NativeWebRequest.class);
		WebDataBinderFactory binderFactory = mock(WebDataBinderFactory.class);
		
		// exercise
		Object actual = sut.resolveArgument(methodParametere, mavContainer, webRequest, binderFactory);
		
		// verify
		assertThat(actual, is(notNullValue()));
		assertThat(actual, is(instanceOf(Sliceable.class)));
		Sliceable actualSliceable = (Sliceable) actual;
		
		Sliceable expected = new SliceRequest(2, Direction.DESC, 10);
		assertThat(actualSliceable, is(expected));
	}
	
	@Test
	public void testDefaultHandlerWithFullValue_WithParameter() throws Exception {
		// setup
		Method method = getClass().getMethod("defaultHandlerWithFullValue", Sliceable.class);
		MethodParameter methodParametere = new MethodParameter(method, 0);
		ModelAndViewContainer mavContainer = mock(ModelAndViewContainer.class);
		NativeWebRequest webRequest = mock(NativeWebRequest.class);
		when(webRequest.getParameter(eq("size"))).thenReturn("3");
		when(webRequest.getParameter(eq("page_number"))).thenReturn("345");
		when(webRequest.getParameter(eq("direction"))).thenReturn("ASC");
		WebDataBinderFactory binderFactory = mock(WebDataBinderFactory.class);
		
		// exercise
		Object actual = sut.resolveArgument(methodParametere, mavContainer, webRequest, binderFactory);
		
		// verify
		assertThat(actual, is(notNullValue()));
		assertThat(actual, is(instanceOf(Sliceable.class)));
		Sliceable actualSliceable = (Sliceable) actual;
		
		Sliceable expected = new SliceRequest(345, Direction.ASC, 3);
		assertThat(actualSliceable, is(expected));
	}
	
	@Test
	public void testDefaultHandlerWithFullValue_WithOneParameter() throws Exception {
		// setup
		Method method = getClass().getMethod("defaultHandlerWithFullValue", Sliceable.class);
		MethodParameter methodParametere = new MethodParameter(method, 0);
		ModelAndViewContainer mavContainer = mock(ModelAndViewContainer.class);
		NativeWebRequest webRequest = mock(NativeWebRequest.class);
		when(webRequest.getParameter(eq("page_number"))).thenReturn("1");
		WebDataBinderFactory binderFactory = mock(WebDataBinderFactory.class);
		
		// exercise
		Object actual = sut.resolveArgument(methodParametere, mavContainer, webRequest, binderFactory);
		
		// verify
		assertThat(actual, is(notNullValue()));
		assertThat(actual, is(instanceOf(Sliceable.class)));
		Sliceable actualSliceable = (Sliceable) actual;
		
		Sliceable expected = new SliceRequest(1, Direction.DESC, 10);
		assertThat(actualSliceable, is(expected));
	}
	
	@Test
	public void testDefaultHandlerWithFullValue_WithInvalidParameter() throws Exception {
		// setup
		Method method = getClass().getMethod("defaultHandlerWithFullValue", Sliceable.class);
		MethodParameter methodParametere = new MethodParameter(method, 0);
		ModelAndViewContainer mavContainer = mock(ModelAndViewContainer.class);
		NativeWebRequest webRequest = mock(NativeWebRequest.class);
		when(webRequest.getParameter(eq("size"))).thenReturn("a");
		when(webRequest.getParameter(eq("page_number"))).thenReturn("b");
		when(webRequest.getParameter(eq("direction"))).thenReturn("4649");
		WebDataBinderFactory binderFactory = mock(WebDataBinderFactory.class);
		
		// exercise
		Object actual = sut.resolveArgument(methodParametere, mavContainer, webRequest, binderFactory);
		
		// verify
		assertThat(actual, is(notNullValue()));
		assertThat(actual, is(instanceOf(Sliceable.class)));
		Sliceable actualSliceable = (Sliceable) actual;
		
		Sliceable expected = new SliceRequest(2, Direction.DESC, 10);
		assertThat(actualSliceable, is(expected));
	}
}
