package de.snx.psf;

import java.awt.Component;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.function.Consumer;

import javax.swing.JFileChooser;

import de.snx.psf.util.DataBuilder;
import de.snx.psf.util.DataObject;
import de.snx.psf.util.DataType;
import de.snx.psf.util.FileFormatException;
import de.snx.psf.util.ObjectRoom;
import de.snx.psf.util.PSFFileFilter;

/**
 * <b>Pair Sorted Format File Input Output:</b><br>
 * <br>
 * There are many ways to use the writer / reader.<br>
 * After the PSFFileIO has been initialized, the write methods like
 * {@link PSFFileIO#write(String, String)} and read methods like
 * {@link PSFFileIO#readInt(String)} methods can be used.<br>
 * on the console.<br>
 * First all rooms and then all data objects are listed.<br>
 * <br>
 * Rooms can be used, like a profile.<br>
 * You can {@link PSFFileIO#enterRoom(String)} to enter (or create a new room,
 * if the room dosn't exist's) and can put the data into the room.<br>
 * a room can contain further rooms by using the
 * {@link PSFFileIO#enterRoom(String)} method one more time in a room.
 * Attention: A room with the name RoomB is not the same room, which is also
 * called RoomB, but was created in RoomA. This is helpful, if a variable like
 * name is used for every profile.<br>
 * Since the use of a key word of a variable can only be used once, or this will
 * be overwritten with repeated use, a room with e.g. the profile "profile0" can
 * be created / opened and in this the variable with the key word name can be
 * used.<br>
 * if you are in a room, you can only write or read variables for this room.<br>
 * In order to be able to access the other variables, a room must be changed by
 * using {@link PSFFileIO#exitRoom()} or move further into the room with
 * {@link PSFFileIO#enterRoom(String)} and selecting the next room. You can go
 * to the top level room directly via {@link PSFFileIO#exitAllRooms()}.<br>
 * <br>
 * a room with the name ending _array should not be created or called as this
 * could cause problems, by reading in/out data.<br>
 * <br>
 * {@link PSFFileIO#printData()} can be used to output all data in the
 * PSFFileIO.<br>
 * This method can be used to better understand the system.<br>
 * <br>
 * <br>
 * <b> UPDATE:</b> {@value PSFFileIO#UPDATED}<br>
 * -<br>
 * Added new room access feature to work with rooms safely<br>
 * {@link PSFFileIO#room(String, Consumer)}<br>
 * 
 * @version {@value PSFFileIO#IDENTIFIER}<br>
 *          {@value PSFFileIO#VERSION}<br>
 *          Updated: {@value PSFFileIO#UPDATED}<br>
 * @author Sunnix
 *
 */
public class PSFFileIO implements Closeable {

	public static final String IDENTIFIER = "PSFFileIO V3";
	public static final String OLD_IDENTIFIER = "SNXFileIO V3";
	public static final String VERSION = "3.1.3";
	public static final String UPDATED = "22w18";
	public static final String CREATOR = "Sunnix";

	private String fileCreator = "n/a";

	private FileWriter writer;
	private FileReader reader;
	private InputStream stream;

	/**
	 * 
	 */
	private DataBuilder builder;

	/**
	 * topRoom is the basic top Tier room and is created automaticly
	 */
	private ObjectRoom topRoom;
	private ObjectRoom currentRoom;

	/**
	 * this variables are used to get information about the version format of the
	 * file
	 */
	private static String f_version, f_updated, f_creator;

	/**
	 * show softerrors like missing fields
	 */
	public static boolean showSoftErrors;

	private PSFFileIO() {
		showSoftErrors = true;
		topRoom = new ObjectRoom("topRoom", null);
		currentRoom = topRoom;
	}

	/**
	 * Create a new PSFFileIO for writing and/or reading from a file The files.<br>
	 * the file should end with .psf as the PSFFileIO format.<br>
	 * if not, it is automatically attached when a file is created
	 * 
	 * @param file file to read and/or write
	 * @param mode "w" - write<br>
	 *             "r" - read<br>
	 *             "wr" - write and read
	 * @throws NullPointerException
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @throws FileFormatException
	 */
	public PSFFileIO(File file, String mode)
			throws NullPointerException, IOException, IllegalArgumentException, FileFormatException {
		this();
		if (file == null)
			throw new NullPointerException("File is null");
		if (file.isDirectory())
			throw new IOException("The file is a Directory");
		switch (mode) {
		// write
		case "w":
			file = prepareWritingPath(file);
			writer = new FileWriter(file);
			break;
		// read
		case "r":
			reader = new FileReader(file);
			readData();
			break;
		// write and read
		case "wr":
			writer = new FileWriter(file);
			reader = new FileReader(file);
			readData();
			break;
		default:
			throw new IllegalArgumentException("\"" + mode + "\" is no valible mode");
		}
	}

	/**
	 * Create a new PSFFileIO for writing and/or reading from a file * the file
	 * should end with .psf as the PSFFileIO format.<br>
	 * if not, it is automatically attached when a file is created
	 * 
	 * @param file path of file to read and/or write
	 * @param mode "w" - write<br>
	 *             "r" - read<br>
	 *             "wr" - write and read
	 * @throws NullPointerException
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @throws FileFormatException
	 */
	public PSFFileIO(String pathname, String mode)
			throws NullPointerException, IOException, IllegalArgumentException, FileFormatException {
		this();
		checkFilePath(pathname);
		File file = new File(pathname);
		if (file.isDirectory())
			throw new IOException("The file is a Directory");
		switch (mode) {
		// write
		case "w":
			file = prepareWritingPath(file);
			writer = new FileWriter(file);
			break;
		// read
		case "r":
			reader = new FileReader(file);
			readData();
			break;
		// write and read
		case "wr":
			writer = new FileWriter(file);
			reader = new FileReader(file);
			readData();
			break;
		default:
			throw new IllegalArgumentException("\"" + mode + "\" is no valible mode");
		}
	}

	/**
	 * Create a new PSFFileIO for writing and reading from two files * the file
	 * should end with .psf as the PSFFileIO format.<br>
	 * if not, it is automatically attached when a file is created
	 * 
	 * @param in  File to read from
	 * @param out File to write in
	 * @throws NullPointerException
	 * @throws IOException
	 * @throws FileFormatException
	 */
	public PSFFileIO(File in, File out) throws NullPointerException, IOException, FileFormatException {
		this();
		if (in == null)
			throw new NullPointerException("File in is null");
		if (out == null)
			throw new NullPointerException("File out is null");
		if (in.isDirectory())
			throw new IOException("File in is a Directory");
		if (out.isDirectory())
			throw new IOException("File out file is a Directory");
		out = prepareWritingPath(out);
		writer = new FileWriter(out);
		reader = new FileReader(in);
		readData();
	}

	/**
	 * Create a new PSFFileIO for reading from a stream * the file should end with
	 * .psf as the PSFFileIO format.<br>
	 * if not, it is automatically attached when a file is created
	 * 
	 * @param pathname pathname to a file in a jar
	 * @throws IOException
	 * @throws FileFormatException
	 */
	public PSFFileIO(String pathname) throws IOException, FileFormatException {
		this();
		checkFilePath(pathname);
		if (!pathname.contains("."))
			stream = getClass().getResourceAsStream("/" + pathname);
		else if (pathname.endsWith("."))
			throw new FileFormatException("The File can't end with '.'");
		readData();
	}

	/**
	 * Create a new PSFFileIO for reading from a stream and writing into a file.<br>
	 * 
	 * @param pathname pathname to a file in a jar
	 * @param out      file to write in
	 * @throws IOException
	 * @throws FileFormatException
	 */
	public PSFFileIO(String pathname, File out) throws NullPointerException, IOException, FileFormatException {
		this(pathname);
		if (out == null)
			throw new NullPointerException("File out is null");
		if (out.isDirectory())
			throw new IOException("File out file is a Directory");
		out = prepareWritingPath(out);
		writer = new FileWriter(out);
		readData();
	}

	/**
	 * 
	 * @param path Filepath
	 * @throws FileFormatException if there are illegal characters in the path
	 */
	private void checkFilePath(String path) throws FileFormatException {
		int point = path.lastIndexOf('.');
		if (point == -1)
			if (path.endsWith("/"))
				throw new FileFormatException("Illegal Filepath ends with '/'");
			else
				path += ".psf";
		else if (path.lastIndexOf('/') > point)
			path += ".psf";
		String characters = "#%&{}\\<>*?$!'\":+-´`|=";
		for (int i = 0; i < characters.length(); i++)
			if (path.contains(Character.toString(characters.charAt(i))))
				throw new FileFormatException("Pathname contains Illegal character");
	}

	/**
	 * Creates the file and filepath, if its missing
	 * 
	 * @param file the file to write
	 * @throws IOException
	 */
	private File prepareWritingPath(File file) throws IOException {
		if (file.exists() == false) {
			File path = file.getParentFile();
			if (path != null)
				path.mkdirs();
			file.createNewFile();
		}
		return file;
	}

	/**
	 * get into room<br>
	 * if the room dos'nt exists, the room will be created
	 * 
	 * @param name room name
	 * @deprecated use {@link PSFFileIO#room(String, Consumer)}
	 */
	@Deprecated
	public void enterRoom(String name) {
		if (name == "topRoom") {
			new Exception("topRoom is no valid room").printStackTrace();
			return;
		}
		ArrayList<ObjectRoom> childs = currentRoom.getChilds();
		for (ObjectRoom room : childs) {
			if (room.getName().equals(name)) {
				currentRoom = room;
				return;
			}
		}
		ObjectRoom newRoom = new ObjectRoom(name, currentRoom);
		childs.add(newRoom);
		currentRoom = newRoom;
	}

	/**
	 * go out one step of the current room
	 * @deprecated use {@link PSFFileIO#room(String, Consumer)}
	 */
	@Deprecated
	public void exitRoom() {
		if (currentRoom == topRoom)
			return;
		currentRoom = currentRoom.getParent();
	}

	/**
	 * exit all rooms to the top level
	 */
	public void exitAllRooms() {
		currentRoom = topRoom;
	}
	
	public void room(String name, Consumer<String> function) {
		enterRoom(name);
		function.accept(name);
		exitRoom();
	}

	public void write(String key, String s) {
		currentRoom.write(key, DataType.STRING, s);
	}

	public void write(String key, char c) {
		currentRoom.write(key, DataType.CHARACTER, Character.toString(c));
	}

	public void write(String key, byte b) {
		currentRoom.write(key, DataType.BYTE, Byte.toString(b));
	}

	public void write(String key, short s) {
		currentRoom.write(key, DataType.SHORT, Short.toString(s));
	}

	public void write(String key, int i) {
		currentRoom.write(key, DataType.INTEGER, Integer.toString(i));
	}

	public void write(String key, long l) {
		currentRoom.write(key, DataType.LONG, Long.toString(l));
	}

	public void write(String key, float f) {
		currentRoom.write(key, DataType.FLOAT, Float.toString(f));
	}

	public void write(String key, double d) {
		currentRoom.write(key, DataType.DOUBLE, Double.toString(d));
	}

	public void write(String key, boolean b) {
		currentRoom.write(key, DataType.BOOLEAN, Boolean.toString(b));
	}

	/**
	 * 
	 * @param key  keyWord of dataObject
	 * @param list ArrayList of primitive types, and String
	 * @throws Exception
	 */
	public void write(String key, ArrayList<?> list) throws Exception {
		enterRoom(key + "_array");
		currentRoom.write("array_size", DataType.INTEGER, Integer.toString(list.size()));
		if (list.size() > 0) {
			DataType arrayType = checkType(list.get(0));
			for (int i = 0; i < list.size(); i++) {
				switch (arrayType) {
				case STRING:
					currentRoom.write("element_" + i, DataType.STRING, list.get(i).toString());
					break;
				case CHARACTER:
					currentRoom.write("element_" + i, DataType.CHARACTER, list.get(i).toString());
					break;
				case BYTE:
					currentRoom.write("element_" + i, DataType.BYTE, list.get(i).toString());
					break;
				case SHORT:
					currentRoom.write("element_" + i, DataType.SHORT, list.get(i).toString());
					break;
				case INTEGER:
					currentRoom.write("element_" + i, DataType.INTEGER, list.get(i).toString());
					break;
				case LONG:
					currentRoom.write("element_" + i, DataType.LONG, list.get(i).toString());
					break;
				case FLOAT:
					currentRoom.write("element_" + i, DataType.FLOAT, list.get(i).toString());
					break;
				case DOUBLE:
					currentRoom.write("element_" + i, DataType.DOUBLE, list.get(i).toString());
					break;
				case BOOLEAN:
					currentRoom.write("element_" + i, DataType.BOOLEAN, list.get(i).toString());
					break;
				default:
					throw new Exception("Something unexpected happened");// this should never happen
				}
			}
		}
		exitRoom();
	}

	public void write(String key, String[] array) throws Exception {
		ArrayList<String> list = new ArrayList<>();
		for (String e : array)
			list.add(e);
		write(key, list);
	}

	public void write(String key, char[] array) throws Exception {
		ArrayList<Character> list = new ArrayList<>();
		for (char e : array)
			list.add(e);
		write(key, list);
	}

	public void write(String key, byte[] array) throws Exception {
		ArrayList<Byte> list = new ArrayList<>();
		for (byte e : array)
			list.add(e);
		write(key, list);
	}

	public void write(String key, short[] array) throws Exception {
		ArrayList<Short> list = new ArrayList<>();
		for (short e : array)
			list.add(e);
		write(key, list);
	}

	public void write(String key, int[] array) throws Exception {
		ArrayList<Integer> list = new ArrayList<>();
		for (int e : array)
			list.add(e);
		write(key, list);
	}

	public void write(String key, long[] array) throws Exception {
		ArrayList<Long> list = new ArrayList<>();
		for (long e : array)
			list.add(e);
		write(key, list);
	}

	public void write(String key, float[] array) throws Exception {
		ArrayList<Float> list = new ArrayList<>();
		for (float e : array)
			list.add(e);
		write(key, list);
	}

	public void write(String key, double[] array) throws Exception {
		ArrayList<Double> list = new ArrayList<>();
		for (double e : array)
			list.add(e);
		write(key, list);
	}

	public void write(String key, boolean[] array) throws Exception {
		ArrayList<Boolean> list = new ArrayList<>();
		for (boolean e : array)
			list.add(e);
		write(key, list);
	}

	public String readString(String key) {
		DataObject dO = currentRoom.getDataObject(key);
		if (dO == null)
			return "null";
		else
			return dO.getDataAsString();
	}

	public char readChar(String key) {
		DataObject dO = currentRoom.getDataObject(key);
		if (dO == null)
			return ' ';
		else
			return dO.getDataAsCharacter();
	}

	public byte readByte(String key) {
		DataObject dO = currentRoom.getDataObject(key);
		if (dO == null)
			return -1;
		else
			return dO.getDataAsByte();
	}

	public short readShort(String key) {
		DataObject dO = currentRoom.getDataObject(key);
		if (dO == null)
			return -1;
		else
			return dO.getDataAsShort();
	}

	public int readInt(String key) {
		DataObject dO = currentRoom.getDataObject(key);
		if (dO == null)
			return -1;
		else
			return dO.getDataAsInteger();
	}

	public long readLong(String key) {
		DataObject dO = currentRoom.getDataObject(key);
		if (dO == null)
			return -1;
		else
			return dO.getDataAsLong();
	}

	public float readFloat(String key) {
		DataObject dO = currentRoom.getDataObject(key);
		if (dO == null)
			return -1;
		else
			return dO.getDataAsFloat();
	}

	public double readDouble(String key) {
		DataObject dO = currentRoom.getDataObject(key);
		if (dO == null)
			return -1;
		else
			return dO.getDataAsDouble();
	}

	public boolean readBoolean(String key) {
		DataObject dO = currentRoom.getDataObject(key);
		if (dO == null)
			return false;
		else
			return dO.getDataAsBoolean();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ArrayList<?> readArrayList(String key) throws Exception {
		ArrayList list;
		enterRoom(key + "_array");
		int arraySize = currentRoom.getDataObject("array_size").getDataAsInteger();
		if (arraySize > 0) {
			DataType type = currentRoom.getDataObject("element_0").getType();
			switch (type) {
			case STRING:
				list = new ArrayList<String>();
				for (int i = 0; i < arraySize; i++)
					list.add(readString("element_" + i));
				break;
			case CHARACTER:
				list = new ArrayList<Character>();
				for (int i = 0; i < arraySize; i++)
					list.add(readChar("element_" + i));
				break;
			case BYTE:
				list = new ArrayList<Byte>();
				for (int i = 0; i < arraySize; i++)
					list.add(readByte("element_" + i));
				break;
			case SHORT:
				list = new ArrayList<Short>();
				for (int i = 0; i < arraySize; i++)
					list.add(readShort("element_" + i));
				break;
			case INTEGER:
				list = new ArrayList<Integer>();
				for (int i = 0; i < arraySize; i++)
					list.add(readInt("element_" + i));
				break;
			case LONG:
				list = new ArrayList<Long>();
				for (int i = 0; i < arraySize; i++)
					list.add(readLong("element_" + i));
				break;
			case FLOAT:
				list = new ArrayList<Float>();
				for (int i = 0; i < arraySize; i++)
					list.add(readFloat("element_" + i));
				break;
			case DOUBLE:
				list = new ArrayList<Double>();
				for (int i = 0; i < arraySize; i++)
					list.add(readDouble("element_" + i));
				break;
			case BOOLEAN:
				list = new ArrayList<Boolean>();
				for (int i = 0; i < arraySize; i++)
					list.add(readBoolean("element_" + i));
				break;
			default:
				throw new Exception("Something unexpected happened");// this should never happen
			}
		} else
			list = new ArrayList<>();
		exitRoom();
		return list;
	}

	public String[] readStringArray(String key) throws Exception {
		Object[] raw = readArrayList(key).toArray();
		if (raw.length > 0)
			if (checkType(raw[0]).equals(DataType.STRING) == false)
				throw new IllegalArgumentException(
						"the array is a " + checkType(raw[0] + " and not compatible with " + DataType.STRING));
		return (String[]) raw;
	}

	public char[] readCharArray(String key) throws Exception {
		Object[] raw = readArrayList(key).toArray();
		if (raw.length > 0)
			if (checkType(raw[0]).equals(DataType.CHARACTER) == false)
				throw new IllegalArgumentException(
						"the array is a " + checkType(raw[0] + " and not compatible with " + DataType.CHARACTER));
		char[] array = new char[raw.length];
		for (int i = 0; i < array.length; i++) {
			array[i] = (char) raw[i];
		}
		return array;
	}

	public byte[] readByteArray(String key) throws Exception {
		Object[] raw = readArrayList(key).toArray();
		if (raw.length > 0)
			if (checkType(raw[0]).equals(DataType.BYTE) == false)
				throw new IllegalArgumentException(
						"the array is a " + checkType(raw[0] + " and not compatible with " + DataType.BYTE));
		byte[] array = new byte[raw.length];
		for (int i = 0; i < array.length; i++) {
			array[i] = (byte) raw[i];
		}
		return array;
	}

	public short[] readShortArray(String key) throws Exception {
		Object[] raw = readArrayList(key).toArray();
		if (raw.length > 0)
			if (checkType(raw[0]).equals(DataType.SHORT) == false)
				throw new IllegalArgumentException(
						"the array is a " + checkType(raw[0] + " and not compatible with " + DataType.SHORT));
		short[] array = new short[raw.length];
		for (int i = 0; i < array.length; i++) {
			array[i] = (short) raw[i];
		}
		return array;
	}

	public int[] readIntArray(String key) throws Exception {
		Object[] raw = readArrayList(key).toArray();
		if (raw.length > 0)
			if (checkType(raw[0]).equals(DataType.INTEGER) == false)
				throw new IllegalArgumentException(
						"the array is a " + checkType(raw[0] + " and not compatible with " + DataType.INTEGER));
		int[] array = new int[raw.length];
		for (int i = 0; i < array.length; i++) {
			array[i] = (int) raw[i];
		}
		return array;
	}

	public long[] readLongArray(String key) throws Exception {
		Object[] raw = readArrayList(key).toArray();
		if (raw.length > 0)
			if (checkType(raw[0]).equals(DataType.LONG) == false)
				throw new IllegalArgumentException(
						"the array is a " + checkType(raw[0] + " and not compatible with " + DataType.LONG));
		long[] array = new long[raw.length];
		for (int i = 0; i < array.length; i++) {
			array[i] = (long) raw[i];
		}
		return array;
	}

	public float[] readFloatArray(String key) throws Exception {
		Object[] raw = readArrayList(key).toArray();
		if (raw.length > 0)
			if (checkType(raw[0]).equals(DataType.FLOAT) == false)
				throw new IllegalArgumentException(
						"the array is a " + checkType(raw[0] + " and not compatible with " + DataType.FLOAT));
		float[] array = new float[raw.length];
		for (int i = 0; i < array.length; i++) {
			array[i] = (float) raw[i];
		}
		return array;
	}

	public double[] readDoubleArray(String key) throws Exception {
		Object[] raw = readArrayList(key).toArray();
		if (raw.length > 0)
			if (checkType(raw[0]).equals(DataType.DOUBLE) == false)
				throw new IllegalArgumentException(
						"the array is a " + checkType(raw[0] + " and not compatible with " + DataType.DOUBLE));
		double[] array = new double[raw.length];
		for (int i = 0; i < array.length; i++) {
			array[i] = (double) raw[i];
		}
		return array;
	}

	public boolean[] readBooleanArray(String key) throws Exception {
		Object[] raw = readArrayList(key).toArray();
		if (raw.length > 0)
			if (checkType(raw[0]).equals(DataType.BOOLEAN) == false)
				throw new IllegalArgumentException(
						"the array is a " + checkType(raw[0] + " and not compatible with " + DataType.BOOLEAN));
		boolean[] array = new boolean[raw.length];
		for (int i = 0; i < array.length; i++) {
			array[i] = (boolean) raw[i];
		}
		return array;
	}

	private DataType checkType(Object o) {
		if (o.getClass().getSimpleName().equals("String"))
			return DataType.STRING;
		else if (o.getClass().getSimpleName().equals("Character"))
			return DataType.CHARACTER;
		else if (o.getClass().getSimpleName().equals("Byte"))
			return DataType.BYTE;
		else if (o.getClass().getSimpleName().equals("Short"))
			return DataType.SHORT;
		else if (o.getClass().getSimpleName().equals("Integer"))
			return DataType.INTEGER;
		else if (o.getClass().getSimpleName().equals("Long"))
			return DataType.LONG;
		else if (o.getClass().getSimpleName().equals("Float"))
			return DataType.FLOAT;
		else if (o.getClass().getSimpleName().equals("Double"))
			return DataType.DOUBLE;
		else if (o.getClass().getSimpleName().equals("Boolean"))
			return DataType.BOOLEAN;
		throw new IllegalArgumentException("the elements of the array aren't a valid dataType ("
				+ o.getClass().getSimpleName() + ") - valid are " + DataType.values());
	}

	private void readData() throws IOException, FileFormatException {
		if (stream != null)
			builder = new DataBuilder(this, stream);
		else if (reader != null)
			builder = new DataBuilder(this, reader);
		else
			throw new NullPointerException("No reader or stream is defined");
		// prevent reading other files format with errors
		String datatext = builder.getDataText();
		if (!datatext.equals(IDENTIFIER) && !datatext.equals(OLD_IDENTIFIER))
			throw new FileFormatException("The file does not correspond to the PSFFileIO format");
		f_version = checkVersion(builder.getDataText().substring("Version: ".length()));
		f_updated = builder.getDataText().substring("Updated: ".length());
		f_creator = builder.getDataText().substring("Creator: ".length());
		fileCreator = builder.getDataText().substring("File Creator: ".length());
		builder.getDataText();// clear topRoom dataText cause it already exists
		topRoom.readData(builder);
		builder.getDataText();// clear topRoom's end }
	}

	private String checkVersion(String version) throws FileFormatException {
		String[] thisV, fileV;
		thisV = VERSION.split(".");
		fileV = version.split(".");
		for (int i = 0; i < 2 && i < thisV.length && i < fileV.length; i++)
			if (Integer.parseInt(thisV[i]) < Integer.parseInt(fileV[i]))
				throw new FileFormatException(
						"The version of the file is more recent than the version of the api used");
		return version;
	}

	private void writeData() throws IOException {
		// Write Version
		builder.buildDataText(IDENTIFIER); // to filter out, if the file is from the right format
		builder.buildDataText("Version: " + VERSION);
		builder.buildDataText("Updated: " + UPDATED);
		builder.buildDataText("Creator: " + CREATOR);
		builder.buildDataText("File Creator: " + fileCreator);
		builder.buildDataText("[" + topRoom.getName() + "]:{");
		topRoom.writeData(builder);
		builder.buildDataText("}");
	}

	/**
	 * Print a List of all data in the File
	 */
	public void printData() {
		System.out.println("File info:");
		System.out.println(f_version);
		System.out.println(f_updated);
		System.out.println(f_creator);
		System.out.println(fileCreator);
		System.out.println();
		System.out.println("Rooms:");
		topRoom.printRooms();
		System.out.println();
		System.out.println("Data:");
		topRoom.printDataObjects();
	}

	/**
	 * get the PSFFileIO version of the file
	 */
	public String getFileVersion() {
		return f_version;
	}

	/**
	 * get the PSFFileIO update version date of the file
	 */
	public String getFileUpdatedVersion() {
		return f_updated;
	}

	public String getFileIOCreator() {
		return f_creator;
	}

	/**
	 * set the creatorname of the file
	 * 
	 * @param creator name of creator
	 * @return this
	 */
	public PSFFileIO setFileCreator(String creator) {
		this.fileCreator = creator;
		return this;
	}

	/**
	 * get the creatorname from the file
	 */
	public String getFileCreator() {
		return fileCreator;
	}

	/**
	 * List all object names on slot 0 and "room" or "data" on slot 2
	 */
	public String[][] listCurrentRoom() {
		ArrayList<DataObject> data = currentRoom.getDataObjects();
		ArrayList<ObjectRoom> childs = currentRoom.getChilds();
		String[][] list = new String[childs.size() + data.size()][2];
		for (int i = 0; i < data.size(); i++) {
			list[i][0] = data.get(i).getKey();
			list[i][1] = "data";
		}
		for (int i = 0; i < childs.size(); i++) {
			list[i + data.size()][0] = childs.get(i).getName();
			list[i + data.size()][1] = "room";
		}
		return list;
	}

	/**
	 * Get a DataObject from key
	 * 
	 * @param key DataObject's key
	 * @return DataObject from key or null if there is no DataObject with this key
	 */
	public DataObject getDataObject(String key) {
		for (DataObject dO : currentRoom.getDataObjects())
			if (dO.getKey().equals(key))
				return dO;
		return null;
	}

	public void addDataObject(DataObject object) {
		ArrayList<DataObject> dOs = currentRoom.getDataObjects();
		DataObject existing = null;
		for (DataObject dO : dOs) {
			if (dO.getKey().equals(object.getKey())) {
				existing = dO;
				break;
			}
		}
		if (existing != null) {
			int pos = dOs.indexOf(existing);
			dOs.set(pos, object);
		} else
			currentRoom.getDataObjects().add(object);
	}

	@Override
	public void close() throws IOException {
		if (writer != null) {
			builder = new DataBuilder(this, writer);
			writeData();
			writer.close();
		}
		if (reader != null)
			reader.close();
		if (stream != null)
			stream.close();
	}

	public static File chooseFile(String dictonary, Component parent, boolean open) {
		JFileChooser chooser = new JFileChooser(dictonary);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileFilter(new PSFFileFilter());
		if (open)
			chooser.showOpenDialog(parent);
		else
			chooser.showSaveDialog(parent);
		return chooser.getSelectedFile();
	}

	public static File chooseFile(Component parent, boolean open) {
		return chooseFile(null, parent, open);
	}
}
