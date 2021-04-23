package de.snx.fileIO;

import java.util.ArrayList;

/**
 * 
 * The DataTypeList contains all data information and the associated
 * DataType.<br>
 * To write in some new data, use
 * {@link DataTypeList#add(String, Type, String)}.<br>
 * If you want to get some data use {@link DataTypeList#get(String, SNXFileIO)}.
 * 
 * @version 1.0
 * 
 * @author Sunnix
 *
 */
public class DataTypeList {

	private SNXFileIO fileIO;

	/**
	 * 
	 * Contains: <br>
	 * <b> STRING, INTEGER, BYTE, LONG, SHORT, FLOAT, DOUBLE, BOOLEAN, CHARACTER.
	 * 
	 * @author Sunnix
	 *
	 */
	public static enum Type {
		STRING, INTEGER, BYTE, LONG, SHORT, FLOAT, DOUBLE, BOOLEAN, CHARACTER
	};

	// ArrayList with all key-Words
	private ArrayList<String> keys;
	// ArrayList with primitive type and data as String
	private ArrayList<Pair<Type, String>> data;

	public DataTypeList(SNXFileIO fileIO) {
		this.fileIO = fileIO;
		keys = new ArrayList<>();
		data = new ArrayList<>();
	}

	/**
	 * Add new data into the DataTypeList.<br>
	 * The key should be unique and not appear twice
	 * 
	 * @param key  key-Word
	 * @param type primitive Type or String from the {@link Type} enum.
	 * @param data the data to save as String
	 * @throws Exception throws Exception, if the key already exist.
	 */
	public void add(String key, Type type, String data) throws Exception {
		if (this.keys.contains(key))
			throw new Exception("the key " + key + " is already taken");
		this.keys.add(key);
		this.data.add(new Pair<DataTypeList.Type, String>(type, data));
		checkForCorruption();
	}

	/**
	 * Read out a data object with key-Word
	 * 
	 * @param key      key-Word
	 * @param fileData the SNXFIleIO
	 * @return a Pair with the DataType and data as String
	 * @throws Exception if the DataTypeList got corrupted
	 */
	public Pair<Type, String> get(String key) throws Exception {
		if (checkForCorruption())
			// This should not happen
			throw new Exception("The DataTypeList got currupted");
		int index = this.keys.indexOf(key);
		if (index == -1) {
			if (fileIO.showMissingErrors)
				new Exception("There's no data with the key \"" + key + "\"").printStackTrace();
			return null;
		}
		return data.get(index);
	}

	public ArrayList<String> getKeys() {
		return keys;
	}

	/**
	 * @return size of data objects, or -1 if the list is corrupted
	 */
	public int size() {
		if (checkForCorruption())
			return -1;
		return keys.size();
	}

	/**
	 * Check if something messed up.<br>
	 * If the key ArrayList has a difference in size then the data ArrayList,
	 * something went wrong.
	 * 
	 * @return true if the sizes of the lists aren't equal
	 */
	public boolean checkForCorruption() {
		if (keys.size() == data.size())
			return false;
		System.err.println("DataTypeList get corrupted");
		return true;
	}

	/**
	 * Read out the DataType from {@link Type} with id.
	 * 
	 * @param id DataTypeID
	 * @return DataType
	 * @throws TypeException if the DataTypeID is outside of {@link Type}
	 */
	public static Type getTypeFromID(int id) throws TypeException {
		if (id < 0 && id >= Type.values().length)
			return Type.values()[id];
		throw new TypeException("The data type with this id does not exist");
	}

	/**
	 * TypeException is used, for the case, if a dataType is undefined or wrong
	 * 
	 * @author Sunnix
	 *
	 */
	static class TypeException extends Exception {
		private static final long serialVersionUID = -5813616298139309262L;

		public TypeException(Type isType, Type expectedType) {
			super("The type " + isType.toString() + " don't equals expected type " + expectedType.toString());
		}

		public TypeException(String message) {
			super(message);
		}
	}
}
