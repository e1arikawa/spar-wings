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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.springframework.data.domain.Sort;

import org.junit.jupiter.api.Test;

import jp.xet.sparwings.spring.data.exceptions.InvalidSliceableException;

/**
 * Test for {@link SliceRequest}
 */
public class SliceRequestTest {
	
	@Test
	public void testGetOffset() {
		// setup
		SliceRequest sut = new SliceRequest(3, Sort.Direction.ASC, 14);
		
		// exercise
		Integer actual = sut.getOffset();
		
		// verify
		assertThat(actual).isEqualTo(3 * 14);
	}
	
	@Test
	public void testGetOffset_ZeroPageNumber() {
		// setup
		SliceRequest sut = new SliceRequest(0, Sort.Direction.ASC, 14);
		
		// exercise
		Integer actual = sut.getOffset();
		
		// verify
		assertThat(actual).isEqualTo(0); // 0 * 14
	}
	
	@Test
	public void testGetOffset_NullValue() {
		// setup
		SliceRequest sut = new SliceRequest(null, Sort.Direction.ASC, null);
		
		// exercise
		Integer actual = sut.getOffset();
		
		// verify
		assertThat(actual).isNull();
	}
	
	@Test
	public void testValidate_NullPageNumber_ISE() {
		// setup
		SliceRequest sut = new SliceRequest(null, Sort.Direction.ASC, null);
		
		// exercise
		Throwable actual = catchThrowable(sut::validate);
		
		// verify
		assertThat(actual).isInstanceOfSatisfying(InvalidSliceableException.class,
				e -> assertThat(e.getMessage()).isEqualTo("pageNumber must be not null."));
	}
	
	@Test
	public void testValidate_NullMaxContentSize_ISE() {
		// setup
		SliceRequest sut = new SliceRequest(0, Sort.Direction.ASC, null);
		
		// exercise
		Throwable actual = catchThrowable(sut::validate);
		
		// verify
		assertThat(actual).isInstanceOfSatisfying(InvalidSliceableException.class,
				e -> assertThat(e.getMessage()).isEqualTo("maxContentSize must be not null."));
	}
	
	@Test
	public void testValidate_OverMaxTotalContentLimit_ISE() {
		// setup
		SliceRequest sut = new SliceRequest(0, Sort.Direction.ASC, 2001); // 0 * 2001 + 2001
		
		// exercise
		Throwable actual = catchThrowable(sut::validate);
		
		// verify
		assertThat(actual).isInstanceOfSatisfying(InvalidSliceableException.class,
				e -> assertThat(e.getMessage()).isEqualTo("Cannot get elements beyond 2000."));
	}
	
	@Test
	public void testValidate_SameMaxTotalContentLimit() {
		// setup
		SliceRequest sut = new SliceRequest(1, Sort.Direction.ASC, 1000); // 1 * 1000 + 1000
		
		// exercise
		sut.validate(); // 例外が起きないこと
	}
	
	@Test
	public void testValidate_LessThanMaxTotalContentLimit() {
		// setup
		SliceRequest sut = new SliceRequest(5, Sort.Direction.ASC, 200); // 5 * 200 + 200
		
		// exercise
		sut.validate(); // 例外が起きないこと
	}
}
