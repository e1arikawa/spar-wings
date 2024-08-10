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
package jp.xet.sparwings.spring.data.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.domain.Sort;

import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.xet.sparwings.spring.data.slice.Slice;
import jp.xet.sparwings.spring.data.slice.SliceImpl;
import jp.xet.sparwings.spring.data.slice.SliceRequest;
import jp.xet.sparwings.spring.data.slice.Sliceable;

/**
 * Test for {@link SlicedResources}
 */
public class SlicedResourcesTest {
	
	private static final ObjectMapper OM = new ObjectMapper();
	
	
	@Test
	public void testToJsonString_ByStringList() throws Exception {
		// setup
		List<String> content = Arrays.asList("aaa", "bbb", "ccc");
		Slice<String> slice = new SliceImpl<>(content, new SliceRequest(2, Sort.Direction.ASC, 10), false);
		SlicedResources<String> stringsSliceResource = new SlicedResources<>("strings", slice);
		
		// exercise
		String actual = OM.writeValueAsString(stringsSliceResource);
		
		// verify
		String expectedJsonString = "{"
				+ "  \"_embedded\": {"
				+ "    \"strings\": ["
				+ "      \"aaa\", \"bbb\", \"ccc\""
				+ "    ]"
				+ "  },"
				+ "  \"page\": {"
				+ "    \"size\": 3,"
				+ "    \"number\": 2,"
				+ "    \"has_next_page\": false"
				+ "  }"
				+ "}";
		JSONAssert.assertEquals(actual, expectedJsonString, JSONCompareMode.STRICT);
	}
	
	@Test
	public void testToJsonString_ByBeanList() throws Exception {
		// setup
		List<SampleBean> content = Arrays.asList(new SampleBean("aaa", "bbb"), new SampleBean("ccc", "ddd"));
		Slice<SampleBean> slice = new SliceImpl<>(content, new SliceRequest(7, Sort.Direction.ASC, 10), true);
		SlicedResources<SampleBean> beansSliceResource = new SlicedResources<>("beans", slice);
		
		// exercise
		String actual = OM.writeValueAsString(beansSliceResource);
		
		// verify
		String expectedJsonString = "{"
				+ "  \"_embedded\": {"
				+ "    \"beans\": ["
				+ "      {"
				+ "        \"foo\": \"aaa\","
				+ "        \"bar\": \"bbb\""
				+ "      },"
				+ "      {"
				+ "        \"foo\": \"ccc\","
				+ "        \"bar\": \"ddd\""
				+ "      }"
				+ "    ]"
				+ "  },"
				+ "  \"page\": {"
				+ "    \"size\": 2,"
				+ "    \"number\": 7,"
				+ "    \"has_next_page\": true"
				+ "  }"
				+ "}";
		JSONAssert.assertEquals(actual, expectedJsonString, JSONCompareMode.STRICT);
	}
	
	@Test
	public void testConstructorWithWrapperFunction() {
		// setup
		List<String> content = Arrays.asList("100", "102", "404");
		Sliceable sliceable = new SliceRequest(2, Sort.Direction.ASC, 10);
		Slice<String> slice = new SliceImpl<>(content, sliceable, true);
		Function<String, Integer> wrapperFunction = Integer::parseInt;
		
		// exercise
		SlicedResources<Integer> actual = new SlicedResources<>("elements", slice, wrapperFunction);
		
		// verify
		List<Integer> expectedContent = Arrays.asList(100, 102, 404);
		Slice<Integer> expectedSlice = new SliceImpl<>(expectedContent, sliceable, true);
		SlicedResources<Integer> expected = new SlicedResources<>("elements", expectedSlice);
		assertThat(actual, is(expected));
	}
	
	@Test
	public void testConstructor() {
		// setup
		List<String> content = Arrays.asList("100", "102", "404");
		
		// exercise
		SlicedResources<String> actual = new SlicedResources<>("elements", content);
		
		// verify
		SlicedResources<String> expected = new SlicedResources<>("elements", content,
				new SlicedResources.SliceMetadata(content.size(), null, false));
		assertThat(actual, is(expected));
	}
	
	
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@SuppressWarnings("javadoc")
	public static class SampleBean {
		
		private String foo;
		
		private String bar;
	}
}
