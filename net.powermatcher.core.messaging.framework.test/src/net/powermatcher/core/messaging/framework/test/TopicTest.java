package net.powermatcher.core.messaging.framework.test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import net.powermatcher.core.messaging.framework.Topic;

import org.junit.Before;
import org.junit.Test;


/**
 * @author IBM
 * @version 0.9.0
 */
public class TopicTest {

	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * 
	 */
	@Test
	public void testEqualsObject() {
		assertTrue(Topic.create("abc").equals(Topic.create("abc")));
		assertFalse(Topic.create("abc").equals(Topic.create("def")));
		assertFalse(Topic.create("abc").equals(Topic.create("+")));
		assertFalse(Topic.create("abc").equals(Topic.create("#")));
	}

	/**
	 * 
	 */
	@Test
	public void testGetLevel() {
		Topic topic1 = Topic.create("abc/def/");
		assertEquals("abc", topic1.getLevel(0));
		assertEquals("def", topic1.getLevel(1));
		assertEquals("", topic1.getLevel(2));
	}

	/**
	 * 
	 */
	@Test
	public void testGetLevelCount() {
		assertEquals(1, Topic.create("").getLevelCount());
		assertEquals(1, Topic.create("abc").getLevelCount());
		assertEquals(2, Topic.create("/").getLevelCount());
		assertEquals(2, Topic.create("/abc").getLevelCount());
		assertEquals(2, Topic.create("abc/def").getLevelCount());
		assertEquals(3, Topic.create("abc/def/").getLevelCount());

		Topic topic = Topic.create();
		assertEquals(0, topic.getLevelCount());
		topic = topic.addLevel("abc");
		assertEquals(1, topic.getLevelCount());
		topic = topic.addLevel("def").addLevel("");
		assertEquals(3, topic.getLevelCount());
	}

	/**
	 * 
	 */
	@Test
	public void testIsWildcard() {
		assertFalse(Topic.create("abc/def").isWildcardTopic());
		assertTrue(Topic.create("abc/+/def").isWildcardTopic());
		assertTrue(Topic.create("abc/#").isWildcardTopic());
		assertFalse(Topic.create("abc/+/def").isWildcard(0));
		assertTrue(Topic.create("abc/+/def").isWildcard(1));
		assertFalse(Topic.create("abc/+/def").isMultiLevelWildcard(1));
		assertTrue(Topic.create("abc/+/def").isSingleLevelWildcard(1));
		assertTrue(Topic.create("abc/#").isWildcardTopic());
		assertTrue(Topic.create("abc/#").isWildcard(1));
		assertTrue(Topic.create("abc/#").isMultiLevelWildcard(1));
		assertFalse(Topic.create("abc/#").isSingleLevelWildcard(1));
	}

	/**
	 * 
	 */
	@Test
	public void testMatches() {
		assertTrue(Topic.create("").matches(Topic.create("")));
		assertTrue(Topic.create("/").matches(Topic.create("/")));
		assertTrue(Topic.create("abc").matches(Topic.create("abc")));
		assertFalse(Topic.create("abc").matches(Topic.create("def")));
		assertFalse(Topic.create("abc").matches(Topic.create("abc/def")));
		assertFalse(Topic.create("abc").matches(Topic.create("abc/")));
		assertTrue(Topic.create("abc").matches(Topic.create("+")));
		assertTrue(Topic.create("abc").matches(Topic.create("#")));
		assertTrue(Topic.create("+").matches(Topic.create("abc")));
		assertTrue(Topic.create("#").matches(Topic.create("abc")));
		assertTrue(Topic.create("/abc").matches(Topic.create("+/+")));
		assertTrue(Topic.create("/abc").matches(Topic.create("/+")));
		assertFalse(Topic.create("/abc").matches(Topic.create("+")));
		assertFalse(Topic.create("abc").matches(Topic.create("+/+")));
		assertFalse(Topic.create("abc").matches(Topic.create("/+")));
		assertFalse(Topic.create("abc/def").matches(Topic.create("+")));
		assertTrue(Topic.create("abc/def").matches(Topic.create("#")));
		assertTrue(Topic.create("abc/def/ghi").matches(Topic.create("#")));
		assertTrue(Topic.create("abc").matches(Topic.create("abc/#")));
		assertTrue(Topic.create("abc/def").matches(Topic.create("abc/#")));
		assertTrue(Topic.create("abc/def/ghi").matches(Topic.create("abc/#")));
		assertTrue(Topic.create("abc/def/ghi").matches(Topic.create("+/def/ghi")));
		assertFalse(Topic.create("abc/def/ghi").matches(Topic.create("+/def/ghij")));
		assertTrue(Topic.create("abc/def/ghi").matches(Topic.create("abc/+/ghi")));
		assertTrue(Topic.create("abc/def/ghi").matches(Topic.create("abc/def/+")));
		assertTrue(Topic.create("abc/def/ghi").matches(Topic.create("+/+/+")));
		assertFalse(Topic.create("abc/def/ghi").matches(Topic.create("+/+")));
		assertTrue(Topic.create("abc/def/ghi").matches(Topic.create("+/#")));
	}

	/**
	 * 
	 */
	@Test
	public void testToString() {
		String t1 = "abc/def";
		Topic topic1 = Topic.create(t1);
		assertEquals(t1, topic1.toString());
		Topic topic2 = Topic.create().addLevel("abc").addLevel("def");
		assertEquals(t1, topic2.toString());
	}

}
