package de.snx.psf.util;

/**
 * A FileFormatException is called if the file that is being read from does not
 * correspond to the format of the SNXFileIO.
 * 
 * @author Sunnix
 *
 */
public class FileFormatException extends Exception {

	public FileFormatException(String message) {
		super(message);
	}

	private static final long serialVersionUID = -2210910943058702681L;

}
