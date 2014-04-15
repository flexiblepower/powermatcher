package net.powermatcher.core.messaging.framework;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author IBM
 * @version 0.9.0
 */
public class Topic {
	/**
	 * Define the level separator (String) constant.
	 */
	public static final String LEVEL_SEPARATOR = "/";
	/**
	 * Define the multi level wildcard (String) constant.
	 */
	public static final String MULTI_LEVEL_WILDCARD = "#";
	/**
	 * Define the single level wildcard (String) constant.
	 */
	public static final String SINGLE_LEVEL_WILDCARD = "+";
	/**
	 * Define the level separator character (char) constant.
	 */
	public static final char LEVEL_SEPARATOR_CHARACTER = '/';
	/**
	 * Define the multi level wildcard character (char) constant.
	 */
	public static final char MULTI_LEVEL_WILDCARD_CHARACTER = '#';
	/**
	 * Define the single level wildcard character (char) constant.
	 */
	public static final char SINGLE_LEVEL_WILDCARD_CHARACTER = '+';

	/**
	 * Create topic and return the Topic result.
	 * 
	 * @return Results of the create topic (<code>Topic</code>) value.
	 * @see #Topic()
	 * @see #Topic(String)
	 * @see #Topic(List)
	 * @see #create(String)
	 * @see #create(List)
	 * @see #isWildcardTopic()
	 */
	public static Topic create() {
		return new Topic();
	}

	/**
	 * Create topic with the specified topic levels parameter and return the
	 * Topic result.
	 * 
	 * @param topicLevels
	 *            The topic levels (<code>List</code>) parameter.
	 * @return Results of the create topic (<code>Topic</code>) value.
	 * @see #Topic()
	 * @see #Topic(String)
	 * @see #Topic(List)
	 * @see #create(String)
	 * @see #create()
	 * @see #isWildcardTopic()
	 */
	public static Topic create(final List<String> topicLevels) {
		return new Topic(topicLevels);
	}

	/**
	 * Create topic with the specified topic pattern parameter and return the
	 * Topic result.
	 * 
	 * @param topicPattern
	 *            The topic pattern (<code>String</code>) parameter.
	 * @return Results of the create topic (<code>Topic</code>) value.
	 * @see #Topic()
	 * @see #Topic(String)
	 * @see #Topic(List)
	 * @see #create(List)
	 * @see #create()
	 * @see #isWildcardTopic()
	 */
	public static Topic create(final String topicPattern) {
		return new Topic(topicPattern);
	}

	/**
	 * Is multi level wildcard with the specified level pattern parameter and
	 * return the boolean result.
	 * 
	 * @param levelPattern
	 *            The level pattern (<code>String</code>) parameter.
	 * @return Results of the is multi level wildcard (<code>boolean</code>)
	 *         value.
	 * @see #isMultiLevelWildcard(int)
	 */
	private static boolean isMultiLevelWildcard(final String levelPattern) {
		return MULTI_LEVEL_WILDCARD.equals(levelPattern);
	}

	/**
	 * Is single level wildcard with the specified level pattern parameter and
	 * return the boolean result.
	 * 
	 * @param levelPattern
	 *            The level pattern (<code>String</code>) parameter.
	 * @return Results of the is single level wildcard (<code>boolean</code>)
	 *         value.
	 * @see #isSingleLevelWildcard(int)
	 */
	private static boolean isSingleLevelWildcard(final String levelPattern) {
		return SINGLE_LEVEL_WILDCARD.equals(levelPattern);
	}

	/**
	 * Is wildcard with the specified topic levels parameter and return the
	 * boolean result.
	 * 
	 * @param topicLevels
	 *            The topic levels (<code>List</code>) parameter.
	 * @return Results of the is wildcard (<code>boolean</code>) value.
	 * @see #isMultiLevelWildcard(int)
	 * @see #isSingleLevelWildcard(int)
	 * @see #isWildcard(int)
	 */
	private static boolean isWildcard(final List<String> topicLevels) {
		for (Iterator<String> iterator = topicLevels.iterator(); iterator.hasNext();) {
			if (isWildcard(iterator.next())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Is wildcard with the specified level pattern parameter and return the
	 * boolean result.
	 * 
	 * @param levelPattern
	 *            The level pattern (<code>String</code>) parameter.
	 * @return Results of the is wildcard (<code>boolean</code>) value.
	 * @see #isMultiLevelWildcard(int)
	 * @see #isSingleLevelWildcard(int)
	 * @see #isWildcard(int)
	 */
	private static boolean isWildcard(final String levelPattern) {
		return isSingleLevelWildcard(levelPattern) || isMultiLevelWildcard(levelPattern);
	}

	/**
	 * Matches with the specified pattern levels and topic levels parameters and
	 * return the boolean result.
	 * 
	 * @param patternLevels
	 *            The pattern levels (<code>List</code>) parameter.
	 * @param topicLevels
	 *            The topic levels (<code>List</code>) parameter.
	 * @return Results of the matches (<code>boolean</code>) value.
	 * @see #matches(Topic)
	 */
	private static boolean matches(final List<String> patternLevels, final List<String> topicLevels) {
		int patternLevelsLength = patternLevels.size();
		int topicLevelsLength = topicLevels.size();
		int i = 0;
		while (i < patternLevelsLength && i < topicLevelsLength
				&& (patternLevels.get(i).equals(SINGLE_LEVEL_WILDCARD) || patternLevels.get(i).equals(topicLevels.get(i)))) {
			i += 1;
		}
		if ((i == patternLevelsLength && i == topicLevelsLength)
				|| (i == patternLevelsLength - 1 && patternLevels.get(i).equals(MULTI_LEVEL_WILDCARD))) {
			return true;
		}
		return false;
	}

	/**
	 * To topic levels with the specified topic pattern parameter and return the
	 * List result.
	 * 
	 * @param topicPattern
	 *            The topic pattern (<code>String</code>) parameter.
	 * @return Results of the to topic levels (<code>List</code>) value.
	 * @see #setTopicLevels(List)
	 */
	private static List<String> toTopicLevels(final String topicPattern) {
		int fromIndex = 0;
		int toIndex = 0;
		List<String> topicLevels = new ArrayList<String>();
		while (toIndex != -1) {
			toIndex = topicPattern.indexOf(LEVEL_SEPARATOR_CHARACTER, fromIndex);
			String topicLevel;
			if (toIndex == -1) {
				topicLevel = topicPattern.substring(fromIndex);
			} else {
				topicLevel = topicPattern.substring(fromIndex, toIndex);
				fromIndex = toIndex + 1;
			}
			topicLevels.add(topicLevel);
		}
		return topicLevels;
	}

	/**
	 * To topic pattern with the specified topic levels parameter and return the
	 * String result.
	 * 
	 * @param topicLevels
	 *            The topic levels (<code>List</code>) parameter.
	 * @return Results of the to topic pattern (<code>String</code>) value.
	 * @see #getTopicPattern()
	 * @see #setTopicPattern(String)
	 */
	private static String toTopicPattern(final List<String> topicLevels) {
		StringBuilder topicPattern = new StringBuilder();
		boolean firstLevel = true;
		for (Iterator<String> iterator = topicLevels.iterator(); iterator.hasNext();) {
			if (!firstLevel) {
				topicPattern.append(LEVEL_SEPARATOR_CHARACTER);
			} else {
				firstLevel = false;
			}
			topicPattern.append(iterator.next());
		}
		return topicPattern.toString();
	}

	/**
	 * Define the topic pattern (String) field.
	 */
	private String topicPattern;

	/**
	 * Define the topic levels (String[]) field.
	 */
	private List<String> topicLevels;

	/**
	 * Define the is wildcard (boolean) field.
	 */
	private boolean isWildcard;

	/**
	 * Constructs an instance of this class.
	 * 
	 * @see #Topic(String)
	 * @see #Topic(List)
	 * @see #create(String)
	 * @see #create(List)
	 * @see #create()
	 * @see #isWildcardTopic()
	 */
	public Topic() {
		List<String> empty = Collections.emptyList();
		setTopicLevels(empty);
	}

	/**
	 * Constructs an instance of this class from the specified topic levels
	 * parameter.
	 * 
	 * @param topicLevels
	 *            The topic levels (<code>List</code>) parameter.
	 * @see #Topic()
	 * @see #Topic(String)
	 * @see #Topic(String[])
	 * @see #create(String)
	 * @see #create(List)
	 * @see #create()
	 * @see #isWildcardTopic()
	 */
	public Topic(final List<String> topicLevels) {
		setTopicLevels(topicLevels);
	}

	/**
	 * Constructs an instance of this class from the specified topic pattern
	 * parameter.
	 * 
	 * @param topicPattern
	 *            The topic pattern (<code>String</code>) parameter.
	 * @see #Topic()
	 * @see #Topic(List)
	 * @see #Topic(String[])
	 * @see #create(String)
	 * @see #create(List)
	 * @see #create()
	 * @see #isWildcardTopic()
	 */
	public Topic(final String topicPattern) {
		setTopicPattern(topicPattern);
	}

	/**
	 * Constructs an instance of this class from the specified topic level
	 * strings parameter.
	 * 
	 * @param topicLevelStrings
	 *            The topic level strings (<code>String[]</code>) parameter.
	 * @see #Topic()
	 * @see #Topic(String)
	 * @see #Topic(List)
	 * @see #create(String)
	 * @see #create(List)
	 * @see #create()
	 * @see #isWildcardTopic()
	 */
	public Topic(final String[] topicLevelStrings) {
		List<String> topicLevels = Arrays.asList(topicLevelStrings);
		setTopicLevels(topicLevels);
	}

	/**
	 * Add level with the specified level pattern parameter and return a new
	 * Topic instance result.
	 * 
	 * @param levelPattern
	 *            The level pattern (<code>String</code>) parameter.
	 * @return Results of the add level (<code>Topic</code>) value on a new
	 *         Topic instance.
	 * @see #getLevel(int)
	 * @see #getTopicLevel(int)
	 */
	public Topic addLevel(final String levelPattern) {
		List<String> newTopicLevels = new ArrayList<String>();
		newTopicLevels.addAll(this.topicLevels);
		newTopicLevels.add(levelPattern);
		return new Topic(newTopicLevels);
	}

	/**
	 * Equals with the specified obj parameter and return the boolean result.
	 * 
	 * @param obj
	 *            The obj (<code>Object</code>) parameter.
	 * @return Results of the equals (<code>boolean</code>) value.
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Topic) {
			return this.topicPattern.equals(((Topic) obj).topicPattern);
		}
		return false;
	}

	/**
	 * Get level with the specified index parameter and return the String
	 * result.
	 * 
	 * @param index
	 *            The index (<code>int</code>) parameter.
	 * @return Results of the get level (<code>String</code>) value.
	 * @see #addLevel(String)
	 * @see #getTopicLevel(int)
	 */
	public String getLevel(final int index) {
		return this.topicLevels.get(index);
	}

	/**
	 * Gets the level count (int) value.
	 * 
	 * @return The level count (<code>int</code>) value.
	 */
	public int getLevelCount() {
		return this.topicLevels.size();
	}

	/**
	 * Get topic level with the specified index parameter and return the String
	 * result.
	 * 
	 * @param index
	 *            The index (<code>int</code>) parameter.
	 * @return Results of the get topic level (<code>String</code>) value.
	 */
	public String getTopicLevel(final int index) {
		return this.topicLevels.get(index);
	}

	/**
	 * Get topic levels and return the List result.
	 * 
	 * @return Results of the get topic levels (<code>List</code>) value.
	 */
	public List<String> getTopicLevels() {
		return Collections.unmodifiableList(this.topicLevels);
	}

	/**
	 * Gets the topic pattern (String) value.
	 * 
	 * @return The topic pattern (<code>String</code>) value.
	 * @see #setTopicPattern(String)
	 */
	public String getTopicPattern() {
		return this.topicPattern;
	}

	/**
	 * Hash code and return the int result.
	 * 
	 * @return Results of the hash code (<code>int</code>) value.
	 */
	@Override
	public int hashCode() {
		return this.topicPattern.hashCode();
	}

	/**
	 * Is multi level wildcard with the specified index parameter and return the
	 * boolean result.
	 * 
	 * @param index
	 *            The index (<code>int</code>) parameter.
	 * @return Results of the is multi level wildcard (<code>boolean</code>)
	 *         value.
	 */
	public boolean isMultiLevelWildcard(final int index) {
		return isMultiLevelWildcard(this.topicLevels.get(index));
	}

	/**
	 * Is single level wildcard with the specified index parameter and return
	 * the boolean result.
	 * 
	 * @param index
	 *            The index (<code>int</code>) parameter.
	 * @return Results of the is single level wildcard (<code>boolean</code>)
	 *         value.
	 */
	public boolean isSingleLevelWildcard(final int index) {
		return isSingleLevelWildcard(this.topicLevels.get(index));
	}

	/**
	 * Is wildcard level with the specified index parameter and return the
	 * boolean result.
	 * 
	 * @param index
	 *            The index (<code>int</code>) parameter.
	 * @return Results of the is wildcard level (<code>boolean</code>) value.
	 * @see #isMultiLevelWildcard(int)
	 * @see #isSingleLevelWildcard(int)
	 */
	public boolean isWildcard(final int index) {
		return isSingleLevelWildcard(index) || isMultiLevelWildcard(index);
	}

	/**
	 * Gets the wildcard topic (boolean) value.
	 * 
	 * @return The wildcard topic (<code>boolean</code>) value.
	 */
	public boolean isWildcardTopic() {
		return this.isWildcard;
	}

	/**
	 * Matches with the specified other parameter and return the boolean result.
	 * 
	 * @param other
	 *            The other (<code>Topic</code>) parameter.
	 * @return Results of the matches (<code>boolean</code>) value.
	 */
	public boolean matches(final Topic other) {
		if (this.topicPattern.equals(other.topicPattern)) {
			return true;
		} else if (this.isWildcardTopic() != other.isWildcardTopic()) {
			return this.isWildcardTopic() ? matches(this.topicLevels, other.topicLevels) : matches(other.topicLevels,
					this.topicLevels);
		}
		return false;
	}

	/**
	 * Sets the topic levels value.
	 * 
	 * @param topicLevels
	 *            The topic levels (<code>List</code>) parameter.
	 */
	private void setTopicLevels(final List<String> topicLevels) {
		this.topicLevels = new ArrayList<String>();
		this.topicLevels.addAll(topicLevels);
		this.isWildcard = isWildcard(topicLevels);
		this.topicPattern = toTopicPattern(topicLevels);
	}

	/**
	 * Sets the topic pattern value.
	 * 
	 * @param topicPattern
	 *            The topic pattern (<code>String</code>) parameter.
	 * @see #getTopicPattern()
	 */
	private void setTopicPattern(final String topicPattern) {
		this.topicPattern = topicPattern;
		this.topicLevels = toTopicLevels(topicPattern);
		this.isWildcard = isWildcard(this.topicLevels);
	}

	/**
	 * Returns the string value.
	 * 
	 * @return The string (<code>String</code>) value.
	 */
	@Override
	public String toString() {
		return this.topicPattern;
	}

}
