package com.softwaremagico.kt.core.tests;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2026 Softwaremagico
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.softwaremagico.kt.core.converters.ConverterUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = "converterUtils")
public class ConverterUtilsTests {

	private static class TestObject {
		private String name;
		private Integer age;
		private String address;

		public TestObject(String name, Integer age, String address) {
			this.name = name;
			this.age = age;
			this.address = address;
		}

		// Getters and setters
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Integer getAge() {
			return age;
		}

		public void setAge(Integer age) {
			this.age = age;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}
	}

	@Test
	public void testGetNullPropertyNamesWithAllPropertiesSet() {
		final TestObject obj = new TestObject("John", 30, "123 Main St");
		final String[] nullPropertyNames = ConverterUtils.getNullPropertyNames(obj);

		Assert.assertNotNull(nullPropertyNames);
		// Only 'class' should be null (from Object)
		Assert.assertTrue(nullPropertyNames.length == 0 ||
			(nullPropertyNames.length == 1 && "class".equals(nullPropertyNames[0])));
		Assert.assertFalse(nullPropertyNames.length > 2);
	}

	@Test
	public void testGetNullPropertyNamesWithOneNullProperty() {
		final TestObject obj = new TestObject("John", 30, null);
		final String[] nullPropertyNames = ConverterUtils.getNullPropertyNames(obj);

		Assert.assertNotNull(nullPropertyNames);
		Assert.assertTrue(nullPropertyNames.length > 0);
		Assert.assertTrue(contains(nullPropertyNames, "address"));
		Assert.assertFalse(contains(nullPropertyNames, "name"));
		Assert.assertFalse(contains(nullPropertyNames, "age"));
	}

	@Test
	public void testGetNullPropertyNamesWithMultipleNullProperties() {
		final TestObject obj = new TestObject(null, null, "123 Main St");
		final String[] nullPropertyNames = ConverterUtils.getNullPropertyNames(obj);

		Assert.assertNotNull(nullPropertyNames);
		Assert.assertTrue(nullPropertyNames.length > 0);
		Assert.assertTrue(contains(nullPropertyNames, "name"));
		Assert.assertTrue(contains(nullPropertyNames, "age"));
		Assert.assertFalse(contains(nullPropertyNames, "address"));
	}

	@Test
	public void testGetNullPropertyNamesWithAllNullProperties() {
		final TestObject obj = new TestObject(null, null, null);
		final String[] nullPropertyNames = ConverterUtils.getNullPropertyNames(obj);

		Assert.assertNotNull(nullPropertyNames);
		Assert.assertTrue(nullPropertyNames.length > 0);
		Assert.assertTrue(contains(nullPropertyNames, "name"));
		Assert.assertTrue(contains(nullPropertyNames, "age"));
		Assert.assertTrue(contains(nullPropertyNames, "address"));
		Assert.assertFalse(nullPropertyNames.length == 0);
	}

	@Test
	public void testGetNullPropertyNamesReturnsArray() {
		final TestObject obj = new TestObject("Test", 25, "Address");
		final String[] nullPropertyNames = ConverterUtils.getNullPropertyNames(obj);

		Assert.assertNotNull(nullPropertyNames);
		Assert.assertTrue(nullPropertyNames instanceof String[]);
		Assert.assertTrue(nullPropertyNames.getClass().isArray());
	}

	@Test
	public void testGetNullPropertyNamesWithIntegerValue() {
		final TestObject obj = new TestObject("John", 0, "123 Main St");
		final String[] nullPropertyNames = ConverterUtils.getNullPropertyNames(obj);

		Assert.assertNotNull(nullPropertyNames);
		// Zero value should NOT be considered as null property
		Assert.assertFalse(contains(nullPropertyNames, "age"));
	}

	@Test
	public void testGetNullPropertyNamesWithEmptyString() {
		final TestObject obj = new TestObject("", 30, "123 Main St");
		final String[] nullPropertyNames = ConverterUtils.getNullPropertyNames(obj);

		Assert.assertNotNull(nullPropertyNames);
		// Empty string should NOT be considered as null property
		Assert.assertFalse(contains(nullPropertyNames, "name"));
	}

	@Test
	public void testGetNullPropertyNamesDoesNotModifyObject() {
		final TestObject obj = new TestObject("John", 30, "123 Main St");
		final String originalName = obj.getName();
		final Integer originalAge = obj.getAge();
		final String originalAddress = obj.getAddress();

		ConverterUtils.getNullPropertyNames(obj);

		// Object should remain unchanged
		Assert.assertEquals(obj.getName(), originalName);
		Assert.assertEquals(obj.getAge(), originalAge);
		Assert.assertEquals(obj.getAddress(), originalAddress);
	}

	@Test
	public void testGetNullPropertyNamesConsistency() {
		final TestObject obj1 = new TestObject(null, 30, "123 Main St");
		final TestObject obj2 = new TestObject(null, 30, "123 Main St");

		final String[] nullProps1 = ConverterUtils.getNullPropertyNames(obj1);
		final String[] nullProps2 = ConverterUtils.getNullPropertyNames(obj2);

		Assert.assertNotNull(nullProps1);
		Assert.assertNotNull(nullProps2);
		Assert.assertEquals(nullProps1.length, nullProps2.length);
		Assert.assertTrue(contains(nullProps1, "name"));
		Assert.assertTrue(contains(nullProps2, "name"));
	}

	@Test
	public void testGetNullPropertyNamesWithMixedNullNonNull() {
		final TestObject obj = new TestObject("Alice", null, null);
		final String[] nullPropertyNames = ConverterUtils.getNullPropertyNames(obj);

		Assert.assertNotNull(nullPropertyNames);
		Assert.assertTrue(nullPropertyNames.length >= 2);
		Assert.assertTrue(contains(nullPropertyNames, "age"));
		Assert.assertTrue(contains(nullPropertyNames, "address"));
		Assert.assertFalse(contains(nullPropertyNames, "name"));
		Assert.assertEquals(nullPropertyNames.length, countNullProperties("Alice", null, null));
	}

	private boolean contains(String[] array, String value) {
		for (final String item : array) {
			if (value.equals(item)) {
				return true;
			}
		}
		return false;
	}

	private int countNullProperties(String name, Integer age, String address) {
		int count = 0;
		if (name == null) count++;
		if (age == null) count++;
		if (address == null) count++;
		return count;
	}
}


