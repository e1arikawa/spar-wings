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
package jp.xet.sparwings.spring.data.slice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.Arrays;
import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Sort;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link SliceImpl}
 */
public class SliceImplTest {
	
	@Test
	public void testNextSlice() {
		// setup
		List<String> content = Arrays.asList("abc", "def");
		Sliceable sliceable = new SliceRequest(3, Sort.Direction.DESC, 30);
		Slice<String> sut = new SliceImpl<>(content, sliceable, true);
		
		// exercise
		Sliceable actual = sut.nextSlice();
		
		// verify
		Sliceable expected = new SliceRequest(4, // ページ数インクリメント
				Sort.Direction.DESC, 30);
		assertThat(actual, is(expected));
	}
	
	@Test
	public void testNextSlice_NotNextSlice() {
		// setup
		List<String> content = Arrays.asList("abc", "def");
		Sliceable sliceable = new SliceRequest(3, Sort.Direction.DESC, 30);
		Slice<String> sut = new SliceImpl<>(content, sliceable, false);
		
		// exercise
		Sliceable actual = sut.nextSlice();
		
		// verify
		assertThat(actual, is(nullValue()));
	}
	
	@Test
	public void testMap() {
		// setup
		List<String> content = Arrays.asList("456", "123");
		Sliceable sliceable = new SliceRequest(3, Sort.Direction.DESC, 30);
		Slice<String> sut = new SliceImpl<>(content, sliceable, false);
		Converter<String, Integer> converter = Integer::parseInt;
		
		// exercise
		Slice<Integer> actual = sut.map(converter);
		
		// verify
		List<Integer> expectedContent = Arrays.asList(456, 123);
		Slice<Integer> expected = new SliceImpl<>(expectedContent, sliceable, false);
		assertThat(actual, is(expected));
	}
}
