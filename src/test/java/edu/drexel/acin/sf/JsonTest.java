package edu.drexel.acin.sf;

import com.google.gson.reflect.TypeToken;
import edu.drexel.acin.sf.api.*;
import edu.drexel.acin.sf.util.JSON;
import org.junit.Test;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: ids
 * Date: 3/22/12
 * Time: 6:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class JsonTest {
	
	@Test
	public void testJsonPageForm() {
		final PageForm form =
						new PageForm(
										"Display Name", URI.create("http://entityid/"), new ClassDescriptor(
														URI.create("http://www.lol.com/1"),
														"Class 1",
														"The first class"),
										Arrays.asList(
														new DataField(
														new FieldDescriptor(FieldDescriptor.FieldType.DATA, URI.create(""), "field label", 0.0d, "some lengthy comment about the field", "boolean", true), null, null)));
		System.out.println(JSON.stringify(form));
//		JSON.stringify(forms.getAllEntities(classUri), response.getWriter()));
	}

	@Test
	public void testSerializeMap() {
		final Map<String, ClassDescriptor> test = new HashMap<String, ClassDescriptor>();
		test.put("key1", new ClassDescriptor(URI.create("item1"), "name1", "comment1"));
		test.put("key2", new ClassDescriptor(URI.create("item2"), "name2", "comment2"));
		final String json = JSON.stringify(test);
		System.out.println(json);
		Type fooType = new TypeToken<HashMap<String, ClassDescriptor>>() {}.getType();
		final Map<String, ClassDescriptor> copy = JSON.parse(json, fooType);
		assertEquals(test, copy);
	}
}
