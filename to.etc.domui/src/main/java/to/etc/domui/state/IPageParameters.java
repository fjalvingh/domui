package to.etc.domui.state;

import javax.annotation.*;


public interface IPageParameters {
	/**
	 * Creates copy of current PageParameters.
	 * Since modification of live page params is not allowed, in order to navigate to other page with similar set of params, use this method to get params template for new page navigation.
	 * @return
	 */
	PageParameters getUnlockedCopy();

	/**
	 * Indicates whether a given parameter name exists in this PageParameters object.
	 *
	 * @param name, the name of the parameter to be checked for.
	 * @return true when the parameter exists, false otherwise.
	 */
	boolean hasParameter(String name);

	/**
	 * Gets the value for the specified parametername as an int (primitive).
	 * If the parameter does not exists or the value cannot be converted to an int, a MissingParameterException is thrown.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @return the value as an int
	 */
	int getInt(String name);

	/**
	 * Gets the value for the specified parametername as an int (primitive).
	 * If the parameter does cannot be converted to an int, a MissingParameterException is thrown.
	 * When the parameter does not exist, the specified default value is returned.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @param df, the default value to be returned, when the specified parameter does not exist.
	 * @return the value as an int
	 */
	int getInt(String name, int df);

	/**
	 * Gets the value for the specified parametername as a long (primitive).
	 * When multiple value exists for the specified parameter, the first element of the array is returned.
	 * If the parameter does not exists or the value cannot be converted to an long, a MissingParameterException is thrown.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @return the value as a long
	 */
	long getLong(String name);

	/**
	 * Gets the value for the specified parametername as a long (primitive).
	 * When multiple value exists for the specified parameter, the first element of the array is returned.
	 * If the parameter does cannot be converted to an long, a MissingParameterException is thrown.
	 * When the parameter does not exist, the specified default value is returned.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @param df, the default value to be returned, when the specified parameter does not exist.
	 * @return the value as a long
	 */
	long getLong(String name, long df);

	/**
	 * Gets the value for the specified parametername as a boolean (primitive).
	 * When multiple value exists for the specified parameter, the first element of the array is returned.
	 * If the parameter does not exists or the value cannot be converted to an boolean, a MissingParameterException is thrown.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @return the value as a long
	 */
	boolean getBoolean(String name);

	/**
	 * Gets the value for the specified parametername as a boolean (primitive).
	 * When multiple value exists for the specified parameter, the first element of the array is returned.
	 * If the parameter does cannot be converted to an boolean, a MissingParameterException is thrown.
	 * When the parameter does not exist, the specified default value is returned.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @param df, the default value to be returned, when the specified parameter does not exist.
	 * @return the value as a boolean
	 */
	boolean getBoolean(String name, boolean df);

	/**
	 * Gets the value for the specified parametername as a Long object.
	 * When multiple value exists for the specified parameter, the first element of the array is returned.
	 * If the parameter does not exists or the value cannot be converted to an int, a MissingParameterException is thrown.
	 * This method uses decode() so hexadecimal and octal strings can be used as parameter values.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @return the value as a Long
	 */
	Long getLongW(String name);

	/**
	 * Gets the value for the specified parametername as a Long object.
	 * When multiple value exists for the specified parameter, the first element of the array is returned.
	 * If the parameter does cannot be converted to an int, a MissingParameterException is thrown.
	 * When the parameter does not exist, the specified default value is returned.
	 * This method uses decode() so hexadecimal and octal strings can be used as parameter values.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @param df, the default value to be returned, when the specified parameter does not exist.
	 * @return the value as a Long
	 */
	Long getLongW(String name, long df);

	/**
	 * Gets the value for the specified parametername as a Long object.
	 * When multiple value exists for the specified parameter, the first element of the array is returned.
	 * If the parameter does cannot be converted to an int, a MissingParameterException is thrown.
	 * When the parameter does not exist, the specified default value is returned.
	 * This method uses decode() so hexadecimal and octal strings can be used as parameter values.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @param df, the default value to be returned, when the specified parameter does not exist.
	 * @return the value as a Long
	 */
	Long getLongW(String name, Long df);

	/**
	 * Gets the value for the specified parametername as a String object.
	 * When multiple value exists for the specified parameter, a MultipleParameterException is thrown.
	 * When the parameter does not exist, a MissingParameterException is thrown.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @return the value as a String
	 */
	@Nonnull String getString(String name);

	/**
	 * Gets the value for the specified parametername as a String object.
	 * When multiple value exists for the specified parameter, a MultipleParameterException is thrown.
	 * When the parameter does not exist, the specified default value is returned.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @param df, the default value to be returned, when the specified parameter does not exist.
	 * @return the value as a String
	 */
	@Nullable String getString(String name, String df);

	/**
	 * Gets the value for the specified parametername as a String array.
	 * When the parameter does not exist, a MissingParameterException is thrown.
	 * This method is provided for legacy reasons only.
	 * The domui framework discourages uses of parameter arrays.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @return the value as a String
	 */
	@Nonnull String[] getStringArray(@Nonnull String name);

	@Nullable String[] getStringArray(@Nonnull String name, @Nullable String[] deflt);

	/**
	 * Gets all the names of the parameters this object is holding
	 * @return the parameter names in an array
	 */
	@Nonnull String[] getParameterNames();

	/**
	 * Compare this with another instance. Used to see that a new request has different parameters
	 * than an earlier request.
	 * <h2>remark</h2>
	 * <p>We check the size of the maps; if they are equal we ONLY have to check that each key-value
	 * pair in SOURCE exists in TARGET AND is the same. We don't need to check for "thingies in SRC
	 * that do not occur in TGT" because that cannot happen if the map sizes are equal.</p>
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override boolean equals(Object obj);

	@Override int hashCode();

	/**
	 * EXPENSIVE Hash all parameter values into an MD5 hash. This must be repeatable so same parameters get the same hash code.
	 * @return
	 */
	@Nonnull String calculateHashString();

	/**
	 * Return the number of characters that this would take on an url.
	 * @return
	 */
	int getDataLength();

	boolean isReadOnly();

	int size();
}
