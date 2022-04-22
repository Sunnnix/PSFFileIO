package de.snx.psf.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class PSFFileFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		return f.getName().endsWith(".psf") || f.isDirectory();
	}

	@Override
	public String getDescription() {
		return "PSFFileIO fomrat (.psf)";
	}

}
