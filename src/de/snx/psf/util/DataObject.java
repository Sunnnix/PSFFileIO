package de.snx.psf.util;

import java.io.IOException;

public class DataObject {

	private String key;
	private DataType type;
	private String data;

	public DataObject(String key, DataType type, String data) {
		this.key = key;
		this.type = type;
		this.data = data;
	}

	public String getKey() {
		return key;
	}

	public DataType getType() {
		return type;
	}

	public void setType(DataType type) {
		this.type = type;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getDataAsString() {
		return getData();
	}

	public char getDataAsCharacter() {
		if (data.isEmpty())
			throw new NullPointerException("The data is empty");
		return data.charAt(0);
	}

	public byte getDataAsByte() {
		if (checkType(2, 5))
			return Byte.parseByte(getData());
		if (checkType(6, 7))
			throw new NumberFormatException("The data \"" + getData() + "\" is a decimal number");
		throw new NumberFormatException("The data \"" + getData() + "\" is no number");
	}

	public short getDataAsShort() {
		if (checkType(2, 5))
			return Short.parseShort(getData());
		if (checkType(6, 7))
			throw new NumberFormatException("The data \"" + getData() + "\" is a decimal number");
		throw new NumberFormatException("The data \"" + getData() + "\" is no number");
	}

	public int getDataAsInteger() {
		if (checkType(2, 5))
			return Integer.parseInt(getData());
		if (checkType(6, 7))
			throw new NumberFormatException("The data \"" + getData() + "\" is a decimal number");
		throw new NumberFormatException("The data \"" + getData() + "\" is no number");
	}

	public long getDataAsLong() {
		if (checkType(2, 5))
			return Long.parseLong(getData());
		if (checkType(6, 7))
			throw new NumberFormatException("The data \"" + getData() + "\" is a decimal number");
		throw new NumberFormatException("The data \"" + getData() + "\" is no number");
	}

	public float getDataAsFloat() {
		if (checkType(2, 7))
			return Float.parseFloat(getData());
		throw new NumberFormatException("The data \"" + getData() + "\" is no number");
	}

	public double getDataAsDouble() {
		if (checkType(2, 7))
			return Double.parseDouble(getData());
		throw new NumberFormatException("The data \"" + getData() + "\" is no number");
	}

	public boolean getDataAsBoolean() {
		if (checkType(8, 8))
			return Boolean.parseBoolean(getData());
		throw new NumberFormatException("The data \"" + getData() + "\" is no boolean value");
	}

	/**
	 * Check if the type is in this index
	 * 
	 * @param index_beginn include start
	 * @param index_end    include end
	 * @return <b>true</b> if the type is in the index
	 */
	private boolean checkType(int index_beginn, int index_end) {
		int ordinal = type.ordinal();
		return ordinal >= index_beginn && ordinal <= index_end;
	}

	public void writeData(DataBuilder builder) throws IOException {
		builder.buildDataSet(this);
	}

	@Override
	public String toString() {
		return "DataObject[key: " + key + ", type: " + type.name() + ", data: " + data + "]";
	}
}
