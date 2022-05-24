package de.snx.psf.util;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import de.snx.psf.PSFFileIO;

/**
 * This class is used to write the data into the file or read the data out
 * 
 * @author Sunnix
 *
 */
public class DataBuilder {

	private PSFFileIO fileIO;
	private FileWriter writer;
	private FileReader reader;
	private InputStream stream;

	public DataBuilder(PSFFileIO fileIO) {
		this.fileIO = fileIO;
	}

	public DataBuilder(PSFFileIO fileIO, FileWriter writer) {
		this(fileIO);
		this.writer = writer;
	}

	public DataBuilder(PSFFileIO fileIO, FileReader reader) {
		this(fileIO);
		this.reader = reader;
	}

	public DataBuilder(PSFFileIO fileIO, InputStream stream) {
		this(fileIO);
		this.stream = stream;
	}

	/**
	 * For Rooms or basic information
	 */
	public void buildDataText(String text) throws IOException {
		if (writer == null)
			throw new NullPointerException("no writer is defined");
		writer.write(text.length());
		writer.write(text);
	}

	/**
	 * For Data like Integer, Double, Boolean, String. <br>
	 * it is very likely that strings have more than 127 characters, so another
	 * number is read here, which can get a String with a size of
	 * {@value Integer#MAX_VALUE} characters
	 */
	public void buildDataSet(DataObject object) throws IOException {
		if (writer == null)
			throw new NullPointerException("no writer is defined");
		buildDataText(object.getKey());
		DataType type = object.getType();
		buildDataText(type.name());
		if (type.equals(DataType.STRING)) {
			if (object.getData() == null)
				object.setData("");
			String temp = Integer.toString(object.getData().length());
			writer.write(temp.length());
			if (!temp.isEmpty())
				writer.write(temp);
			writer.write(object.getData());
		} else {
			buildDataText(object.getData());
		}
	}

	/**
	 * For Rooms or basic information
	 */
	public String getDataText() throws IOException {
		if (reader == null && stream == null)
			throw new NullPointerException("no reader/stream is defined");
		String data = new String();
		int lenght;
		lenght = reader == null ? stream.read() : reader.read();
		if (lenght <= 0)
			data = "";
		else
			for (int i = 0; i < lenght; i++)
				data += (char) (reader == null ? stream.read() : reader.read());
		return data;
	}

	/**
	 * For Data like Integer, Double, Boolean, String. <br>
	 * it is very likely that strings have more than 127 characters, so another
	 * number is stored here, which sets the maximum length to
	 * {@value Integer#MAX_VALUE}
	 */
	public DataObject getDataObject() throws IOException {
		String key, data = new String();
		DataType type;
		int lenght;
		if (reader == null && stream == null)
			throw new NullPointerException("no reader/stream is defined");
		key = getDataText();
		type = DataType.valueOf(getDataText());
		if (fileIO.getFileVersion().equals("3.0") || type.equals(DataType.STRING) == false) {
			data = getDataText();
		} else {
			lenght = Integer.parseInt(getDataText());
			for (int i = 0; i < lenght; i++) {
				data += (char) (reader == null ? stream.read() : reader.read());
			}
		}
		return new DataObject(key, type, data);
	}

}
