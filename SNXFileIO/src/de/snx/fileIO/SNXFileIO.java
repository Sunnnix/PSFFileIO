package de.snx.fileIO;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import de.snx.fileIO.DataTypeList.Type;
import de.snx.fileIO.DataTypeList.TypeException;

/**
 * 
 * Advanced File -writer and -reader with key search system.<br>
 * <br>
 * 
 * SNXFileIO is the main class for reading and writing files with SNXFileIO.<br>
 * <br>
 * 
 * If a file to read from is defined, you have to use
 * {@link SNXFileIO#readInData()}, to get the data from the file.<br>
 * 
 * {@link SNXFileIO#readInData()} should be used before writing data, as it
 * creates a new data list and the previously written data will be lost.<br>
 * <br>
 * 
 * When writing data, each keyword can only be used once. if a keyword that
 * already exists is used you will get an exception
 * 
 * @version 2.2
 * 
 * @author Sunnix
 *
 */
public class SNXFileIO implements AutoCloseable {

	private FileWriter writer;
	private FileReader reader;

	private InputStream stream;

	private int caeasar = -20; // encryption
	private boolean data_read; // checks whether data has been read (for error help)
	boolean showMissingErrors = true; // control, if the SNXFileIO print Stacktrace on missing objects

	/**
	 * List of all data of file
	 */
	private DataTypeList data_list;

	/**
	 * Create a Stream to read/write from.<br>
	 * For Files in Jar's use {@link SNXFileIO#SNXFileIO(String)} or
	 * {@link SNXFileIO#SNXFileIO(String, File)}
	 * 
	 * @param file the File to read or/and write from/in
	 * @param mode mode like: "r" for "read mode" or "w" for "write mode"<br>
	 *             you can use "rw" aswell
	 * @throws IllegalArgumentException if something else then "r", "w" or "rw" is
	 *                                  used.
	 * @throws IOException              if File can't created or something went
	 *                                  wrong with read in data.
	 */
	public SNXFileIO(File file, String mode) throws Exception {
		data_list = new DataTypeList(this);
		if (mode == "r")
			prepareReader(file);
		else if (mode == "w") {
			prepareWriter(file);
		} else if (mode == "rw") {
			prepareReader(file);
			prepareWriter(file);
		} else
			throw new IllegalArgumentException(mode + " is no valid Argument");
	}

	/**
	 * Create two Stream's to read from and write in.<br>
	 * For Files in Jar's use {@link SNXFileIO#SNXFileIO(String)} or
	 * {@link SNXFileIO#SNXFileIO(String, File)}
	 * 
	 * @param input  file to read from
	 * @param output file to write in
	 * @throws IOException if File can't created or something went wrong with read
	 *                     in data.
	 */
	public SNXFileIO(File input, File output) throws Exception {
		data_list = new DataTypeList(this);
		prepareReader(input);
		prepareWriter(output);
	}

	/**
	 * Opens a Stream to read from a file in Jar's. <br>
	 * With this you can only use the read functions.<br>
	 * 
	 * @param path path
	 * @throws IOException if something went wrong with read in data.
	 */
	public SNXFileIO(String path) throws Exception {
		data_list = new DataTypeList(this);
		stream = getClass().getResourceAsStream("/" + path);
		if (stream == null)
			throw new NullPointerException("Can't load file!");
	}

	/**
	 * Opens a Stream to read from a file in Jar's. <br>
	 * Creates a Stream to write into file<br>
	 * 
	 * @param path   path
	 * @param output the file to write in
	 * @throws IOException if File can't created or something went wrong with read
	 *                     in data.
	 */
	public SNXFileIO(String path, File output) throws Exception {
		this(path);
		prepareWriter(output);
	}

	private void prepareReader(File file) throws Exception {
		reader = new FileReader(file);
	}

	private void prepareWriter(File file) throws IOException {
		// Catch error, if the file have no parent folder
		try {
			file.getParentFile().mkdirs();
		} catch (Exception e) {
			// just jump to next operation
		}
		file.createNewFile();
		writer = new FileWriter(file);
	}

	/*
	 * #############################################################################
	 * # # # Write # # #
	 * #############################################################################
	 */

	private void write(String key, Type type, String data) throws Exception {
		if (writer == null)
			throw new NullPointerException("No writer is defined");
		data_list.add(key, type, data);
	}

	public void write(String key, String string) throws Exception {
		write(key, Type.STRING, string);
	}

	public void write(String key, int i) throws Exception {
		write(key, Type.INTEGER, Integer.toString(i));
	}

	public void write(String key, byte b) throws Exception {
		write(key, Type.BYTE, Byte.toString(b));
	}

	public void write(String key, long l) throws Exception {
		write(key, Type.LONG, Long.toString(l));
	}

	public void write(String key, short s) throws Exception {
		write(key, Type.SHORT, Short.toString(s));
	}

	public void write(String key, float f) throws Exception {
		write(key, Type.FLOAT, Float.toString(f));
	}

	public void write(String key, double d) throws Exception {
		write(key, Type.DOUBLE, Double.toString(d));
	}

	public void write(String key, boolean b) throws Exception {
		write(key, Type.BOOLEAN, b ? "1" : "0");
	}

	public void write(String key, char c) throws Exception {
		write(key, Type.CHARACTER, Character.toString(c));
	}

	/**
	 * only primitive types and String can be applied to array
	 * 
	 * @param key   key-word (this key-word will be applied as array_"key"_(id in
	 *              array)
	 * @param array array of primitive type
	 */
	public void write(String key, Object[] array) throws Exception {
		int typeID = checkType(array[0]);
		if (typeID == -1)
			throw new Exception("The type of the array is not permitted");
		write("array_" + key, typeID);
		write("array_" + key + "_length", array.length);
		switch (Type.values()[typeID]) {
		case STRING:
			for (int i = 0; i < array.length; i++) {
				write("array_" + key + "_" + i, (String) array[i]);
			}
			break;
		case INTEGER:
			for (int i = 0; i < array.length; i++) {
				write("array_" + key + "_" + i, (int) array[i]);
			}
			break;
		case BYTE:
			for (int i = 0; i < array.length; i++) {
				write("array_" + key + "_" + i, (byte) array[i]);
			}
			break;
		case LONG:
			for (int i = 0; i < array.length; i++) {
				write("array_" + key + "_" + i, (long) array[i]);
			}
			break;
		case SHORT:
			for (int i = 0; i < array.length; i++) {
				write("array_" + key + "_" + i, (short) array[i]);
			}
			break;
		case FLOAT:
			for (int i = 0; i < array.length; i++) {
				write("array_" + key + "_" + i, (float) array[i]);
			}
			break;
		case DOUBLE:
			for (int i = 0; i < array.length; i++) {
				write("array_" + key + "_" + i, (double) array[i]);
			}
			break;
		case BOOLEAN:
			for (int i = 0; i < array.length; i++) {
				write("array_" + key + "_" + i, (boolean) array[i]);
			}
			break;
		case CHARACTER:
			for (int i = 0; i < array.length; i++) {
				write("array_" + key + "_" + i, (char) array[i]);
			}
			break;
		default:
			throw new Exception("The type of the Array is no valid type!");
		}
	}

	/**
	 * only primitive types and String can be applied to ArrayList
	 * 
	 * @param key  key-word (this key-word will be applied as arrayList_"key"_(id in
	 *             ArrayList)
	 * @param list ArrayList of primitive type
	 */
	public void write(String key, ArrayList<?> list) throws Exception {
		int typeID = checkType(list.get(0));
		if (typeID == -1)
			throw new Exception("The type of the generic is not permitted");
		write("arrayList_" + key, typeID);
		write("arrayList_" + key + "_size", list.size());
		switch (Type.values()[typeID]) {
		case STRING:
			for (int i = 0; i < list.size(); i++) {
				write("arrayList_" + key + "_" + i, (String) list.get(i));
			}
			break;
		case INTEGER:
			for (int i = 0; i < list.size(); i++) {
				write("arrayList_" + key + "_" + i, (int) list.get(i));
			}
			break;
		case BYTE:
			for (int i = 0; i < list.size(); i++) {
				write("arrayList_" + key + "_" + i, (byte) list.get(i));
			}
			break;
		case LONG:
			for (int i = 0; i < list.size(); i++) {
				write("arrayList_" + key + "_" + i, (long) list.get(i));
			}
			break;
		case SHORT:
			for (int i = 0; i < list.size(); i++) {
				write("arrayList_" + key + "_" + i, (short) list.get(i));
			}
			break;
		case FLOAT:
			for (int i = 0; i < list.size(); i++) {
				write("arrayList_" + key + "_" + i, (float) list.get(i));
			}
			break;
		case DOUBLE:
			for (int i = 0; i < list.size(); i++) {
				write("arrayList_" + key + "_" + i, (double) list.get(i));
			}
			break;
		case BOOLEAN:
			for (int i = 0; i < list.size(); i++) {
				write("arrayList_" + key + "_" + i, (boolean) list.get(i));
			}
			break;
		case CHARACTER:
			for (int i = 0; i < list.size(); i++) {
				write("arrayList_" + key + "_" + i, (char) list.get(i));
			}
			break;
		default:
			throw new Exception("The type of the Array is no valid type!");
		}
	}

	private int checkType(Object o) {
		if (o instanceof String)
			return 0;
		else if (o instanceof Integer)
			return 1;
		else if (o instanceof Byte)
			return 2;
		else if (o instanceof Long)
			return 3;
		else if (o instanceof Short)
			return 4;
		else if (o instanceof Float)
			return 5;
		else if (o instanceof Double)
			return 6;
		else if (o instanceof Boolean)
			return 7;
		else if (o instanceof Character)
			return 8;
		else
			return -1;
	}

	/**
	 * Writes data from the data list into File.
	 */
	private void writeData(String key, Type type, String data) throws IOException {
		// write key
		writer.write(key.length());
		writer.write(caesar(key, true));
		// write type
		writer.write(type.ordinal());
		// write data
		writer.write(data.length());
		writer.write(caesar(data, true));
	}

	/*
	 * #############################################################################
	 * # # # Read # # #
	 * #############################################################################
	 */

	/**
	 * read all Data from File/Stream into dataList.<br>
	 * this will create a new dataList and all previous written data will be lost.
	 */
	public void readInData() throws Exception {
		if (reader == null && stream == null) {
			System.err.println("Write mode only!");
			return;
		}
		data_read = true;
		data_list = new DataTypeList(this);
		boolean r = reader != null; // make sure to use the right Stream (FileWriter/InputStream)
		int readLenght;
		Type type;
		String key, data;
		// Setup start (key lenght or -1 if File/Stream is empty)
		readLenght = (int) (r ? reader.read() : stream.read());
		while (readLenght != -1) {
			// read in key
			key = "";
			for (int i = 0; i < readLenght; i++) {
				key += caesar(Character.toString((char) (r ? reader.read() : stream.read())), false);
			}
			// read in dataType
			readLenght = (int) (r ? reader.read() : stream.read());
			type = Type.values()[readLenght];
			// read in data length and generate a for loop to read in data
			readLenght = (int) (r ? reader.read() : stream.read());
			data = "";
			for (int i = 0; i < readLenght; i++) {
				data += caesar(Character.toString((char) (r ? reader.read() : stream.read())), false);
			}
			// create dataType
			data_list.add(key, type, data);
			// prepare next key (-1 if end of file reached)
			readLenght = (int) (r ? reader.read() : stream.read());
		}
	}

	/**
	 * get a String from the key.<br>
	 * a Character can be read out with this method, but will be converted to a
	 * String.
	 * 
	 * @param key key-Word
	 * @return String from key-Word
	 * @throws Exception
	 */
	public String read(String key) throws Exception {
		if (data_read == false) {
			throw new NullPointerException(
					"No file has been read yet\n" + "Use \"readInData\" from SNXFileIO before read data!");
		}
		Pair<Type, String> data = data_list.get(key);
		if (data == null) {
			return null;
		}
		if (data.key != Type.STRING && data.key != Type.CHARACTER)
			throw new TypeException(data.key, Type.STRING);
		return data.value;
	}

	public String readString(String key) throws Exception {
		return read(key);
	}

	public int readInt(String key) throws Exception {
		Pair<Type, String> data = data_list.get(key);
		if (data == null) {
			return -1;
		}
		if (data.key != Type.INTEGER)
			throw new TypeException(data.key, Type.INTEGER);
		return Integer.parseInt(data.value);
	}

	public byte readByte(String key) throws Exception {
		Pair<Type, String> data = data_list.get(key);
		if (data == null) {
			return -1;
		}
		if (data.key != Type.BYTE)
			throw new TypeException(data.key, Type.BYTE);
		return Byte.parseByte(data.value);
	}

	public long readLong(String key) throws Exception {
		Pair<Type, String> data = data_list.get(key);
		if (data == null) {
			return -1;
		}
		if (data.key != Type.LONG)
			throw new TypeException(data.key, Type.LONG);
		return Long.parseLong(data.value);
	}

	public short readShort(String key) throws Exception {
		Pair<Type, String> data = data_list.get(key);
		if (data == null) {
			return -1;
		}
		if (data.key != Type.SHORT)
			throw new TypeException(data.key, Type.SHORT);
		return Short.parseShort(data.value);
	}

	public float readFloat(String key) throws Exception {
		Pair<Type, String> data = data_list.get(key);
		if (data == null) {
			return -1;
		}
		if (data.key != Type.FLOAT)
			throw new TypeException(data.key, Type.FLOAT);
		return Float.parseFloat(data.value);
	}

	public double readDouble(String key) throws Exception {
		Pair<Type, String> data = data_list.get(key);
		if (data == null) {
			return -1;
		}
		if (data.key != Type.DOUBLE)
			throw new TypeException(data.key, Type.DOUBLE);
		return Double.parseDouble(data.value);
	}

	public boolean readBoolean(String key) throws Exception {
		Pair<Type, String> data = data_list.get(key);
		if (data == null) {
			return false;
		}
		if (data.key != Type.BOOLEAN)
			throw new TypeException(data.key, Type.BOOLEAN);
		return Integer.parseInt(data.value) == 1;
	}

	public char readCharacter(String key) throws Exception {
		Pair<Type, String> data = data_list.get(key);
		if (data == null) {
			return '?';
		}
		if (data.key != Type.CHARACTER)
			throw new TypeException(data.key, Type.CHARACTER);
		return data.value.charAt(0);
	}

	/**
	 * only primitive types and String can be applied to array.
	 * 
	 * @param key key-word (this key-word will be applied as array_"key"_(id in
	 *            array)
	 */
	public Object[] readArray(String key) throws Exception {
		Object[] array;
		int typeID = readInt("array_" + key);
		if (typeID == -1) {
			if (showMissingErrors)
				new NullPointerException("There is no Array with the key " + key).printStackTrace();
			return null;
		}
		Type type = Type.values()[typeID];
		int lenght = readInt("array_" + key + "_length");
		switch (type) {
		case STRING:
			array = new String[lenght];
			for (int i = 0; i < array.length; i++) {
				array[i] = read("array_" + key + "_" + i);
			}
			return array;
		case INTEGER:
			array = new Integer[lenght];
			for (int i = 0; i < array.length; i++) {
				array[i] = readInt("array_" + key + "_" + i);
			}
			return array;
		case BYTE:
			array = new Byte[lenght];
			for (int i = 0; i < array.length; i++) {
				array[i] = readByte("array_" + key + "_" + i);
			}
			return array;
		case LONG:
			array = new Long[lenght];
			for (int i = 0; i < array.length; i++) {
				array[i] = readLong("array_" + key + "_" + i);
			}
			return array;
		case SHORT:
			array = new Short[lenght];
			for (int i = 0; i < array.length; i++) {
				array[i] = readShort("array_" + key + "_" + i);
			}
			return array;
		case FLOAT:
			array = new Float[lenght];
			for (int i = 0; i < array.length; i++) {
				array[i] = readFloat("array_" + key + "_" + i);
			}
			return array;
		case DOUBLE:
			array = new Double[lenght];
			for (int i = 0; i < array.length; i++) {
				array[i] = readDouble("array_" + key + "_" + i);
			}
			return array;
		case BOOLEAN:
			array = new Boolean[lenght];
			for (int i = 0; i < array.length; i++) {
				array[i] = readBoolean("array_" + key + "_" + i);
			}
			return array;
		case CHARACTER:
			array = new Character[lenght];
			for (int i = 0; i < array.length; i++) {
				array[i] = readCharacter("array_" + key + "_" + i);
			}
			return array;
		default:
			throw new TypeException(type + " is no valid type!");
		}
	}

	/**
	 * only primitive types and String can be applied to ArrayList
	 * 
	 * @param key key-word (this key-word will be applied as ArrayList_"key"_(id in
	 *            array)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList<?> readArrayList(String key) throws Exception {
		ArrayList list;
		int typeID = readInt("arrayList_" + key);
		if (typeID == -1) {
			if (showMissingErrors)
				new NullPointerException("There is no ArrayList with the key " + key).printStackTrace();
			return null;
		}
		Type type = Type.values()[typeID];
		int size = readInt("arrayList_" + key + "_size");
		switch (type) {
		case STRING:
			list = new ArrayList<String>();
			for (int i = 0; i < size; i++) {
				list.add(read("arrayList_" + key + "_" + i));
			}
			return list;
		case INTEGER:
			list = new ArrayList<Integer>();
			for (int i = 0; i < size; i++) {
				list.add(readInt("arrayList_" + key + "_" + i));
			}
			return list;
		case BYTE:
			list = new ArrayList<Byte>();
			for (int i = 0; i < size; i++) {
				list.add(readByte("arrayList_" + key + "_" + i));
			}
			return list;
		case LONG:
			list = new ArrayList<Long>();
			for (int i = 0; i < size; i++) {
				list.add(readLong("arrayList_" + key + "_" + i));
			}
			return list;
		case SHORT:
			list = new ArrayList<Short>();
			for (int i = 0; i < size; i++) {
				list.add(readShort("arrayList_" + key + "_" + i));
			}
			return list;
		case FLOAT:
			list = new ArrayList<Float>();
			for (int i = 0; i < size; i++) {
				list.add(readFloat("arrayList_" + key + "_" + i));
			}
			return list;
		case DOUBLE:
			list = new ArrayList<Double>();
			for (int i = 0; i < size; i++) {
				list.add(readDouble("arrayList_" + key + "_" + i));
			}
			return list;
		case BOOLEAN:
			list = new ArrayList<Boolean>();
			for (int i = 0; i < size; i++) {
				list.add(readBoolean("arrayList_" + key + "_" + i));
			}
			return list;
		case CHARACTER:
			list = new ArrayList<Character>();
			for (int i = 0; i < size; i++) {
				list.add(readCharacter("arrayList_" + key + "_" + i));
			}
			return list;
		default:
			throw new TypeException(type + " is no valid type!");
		}
	}

	private String caesar(String string, boolean krypt) {
		char[] chars = string.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (krypt)
				chars[i] += caeasar;
			else
				chars[i] -= caeasar;
		}
		return new String(chars);
	}

	/**
	 * This should only be used at the beginning
	 * 
	 * @param shift
	 */
	public void setCAESAR(int shift) {
		caeasar = shift;
	}

	/**
	 * If an key isn't in the File, a Stacktrace will be printed.<br>
	 * This error message can be deactivated with this by setting this on false
	 * 
	 * @param show show error
	 */
	public void showMissingErrors(boolean show) {
		this.showMissingErrors = show;
	}

	@Override
	public void close() throws Exception {
		if (writer != null) {
			// In the end of the SNXFileIO, write all data into file and close the writer
			for (String key : data_list.getKeys()) {
				Pair<Type, String> pair = data_list.get(key);
				writeData(key, pair.key, pair.value);
			}
			writer.close();
		}
		if (reader != null) {
			reader.close();
		}
		if (stream != null) {
			stream.close();
		}
	}
}
