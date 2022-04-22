package de.snx.psf.util;

import java.io.IOException;
import java.util.ArrayList;

import de.snx.psf.PSFFileIO;

public class ObjectRoom {

	private String name;

	private ObjectRoom parent;
	private ArrayList<ObjectRoom> childs;
	private ArrayList<DataObject> dataObjects;

	public ObjectRoom(String name, ObjectRoom parent) {
		this.name = name;
		this.parent = parent == null ? this : parent; // make topRoom's parent itself
		childs = new ArrayList<>();
		dataObjects = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public ObjectRoom getParent() {
		return parent;
	}

	public ArrayList<ObjectRoom> getChilds() {
		return childs;
	}

	public ArrayList<DataObject> getDataObjects() {
		return dataObjects;
	}

	public DataObject getDataObject(String key) {
		for (DataObject dataObject : dataObjects) {
			if (dataObject.getKey().equals(key))
				return dataObject;
		}
		if (PSFFileIO.showSoftErrors)
			new NullPointerException("There is no DataObject with the key \"" + getPath() + key + "\"")
					.printStackTrace();
		return null;
	}

	public void write(String key, DataType type, String data) {
		for (DataObject dataObject : dataObjects) {
			if (dataObject.getKey() == key) {
				dataObject.setType(type);
				dataObject.setData(data);
				// Info for overriding
				System.err.println("overritten \"" + getPath() + key + "\"");
				return;
			}
		}
		dataObjects.add(new DataObject(key, type, data));
	}

	public void readData(DataBuilder builder) throws NumberFormatException, IOException {
		int dataObjectsSize = Integer.parseInt(builder.getDataText());
		for (int i = 0; i < dataObjectsSize; i++) {
			dataObjects.add(builder.getDataObject());
		}
		int objectRoomSize = Integer.parseInt(builder.getDataText());
		for (int i = 0; i < objectRoomSize; i++) {
			String roomName = builder.getDataText();
			ObjectRoom newRoom = new ObjectRoom(roomName.substring(1, roomName.length() - 3), this);
			newRoom.readData(builder);
			childs.add(newRoom);
			builder.getDataText(); // clear the closing } symbol
		}
	}

	public void writeData(DataBuilder builder) throws IOException {
		builder.buildDataText(Integer.toString(dataObjects.size()));
		for (int i = 0; i < dataObjects.size(); i++) {
			DataObject dO = dataObjects.get(i);
			builder.buildDataSet(dO);
		}
		builder.buildDataText(Integer.toString(childs.size()));
		for (int i = 0; i < childs.size(); i++) {
			ObjectRoom child = childs.get(i);
			builder.buildDataText("[" + child.getName() + "]:{");
			child.writeData(builder);
			builder.buildDataText("}");
		}
	}

	public void printRooms() {
		if (name != "topRoom") {
			String roomPath = getPath();
			System.out.println(roomPath.substring(0, roomPath.length() - 1));
		}
		for (ObjectRoom objectRoom : childs)
			objectRoom.printRooms();
	}

	public void printDataObjects() {
		for (DataObject dO : dataObjects)
			System.out.println(getPath() + dO.getKey() + " [" + dO.getType() + "]");
		for (ObjectRoom objectRoom : childs)
			objectRoom.printDataObjects();
	}

	private String getPath() {
		if (name == "topRoom")
			return new String();
		String path = name + "/";
		ObjectRoom checkRoom = this;
		boolean hasParent = checkRoom.getParent().getName() != "topRoom";
		while (hasParent) {
			path = checkRoom.getParent().getName() + "/" + path;
			checkRoom = checkRoom.getParent();
			hasParent = checkRoom.getParent().getName() != "topRoom";
		}
		return path;
	}

	@Override
	public String toString() {
		return "ObjectRoom[name: " + name + "]";
	}
}
