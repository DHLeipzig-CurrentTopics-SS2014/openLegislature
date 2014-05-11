package org.openlegislature.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openlegislature.util.Logger;

/**
 *
 * @author riddlore
 * @version 0.0.1
 */
public class DirectoryScanner {
	private int count = 0;
	private int filecount = 0;
	private int dircount = 0;
	File[] list;
	List<File> dirList = new ArrayList<>();
	List<File> fileList = new ArrayList<>();

	public int getCount() {
		return this.count;
	}

	public int getFilecount() {
		return this.filecount;
	}

	public int getDircount() {
		return this.dircount;
	}

	public File[] getList() {
		return this.list;
	}

	public List<File> getDirList() {
		return this.dirList;
	}

	public List<File> getFileList() {
		return this.fileList;
	}

	public void scan(String path) {
		File dir = new File(path);
		this.list = dir.listFiles();
		if ( list != null ) {
			for ( File f : this.list ) {
				Logger.getInstance().debug(DirectoryScanner.class, f.getName());
		    if (f.isDirectory() ) { this.dircount++; this.dirList.add(f); }else{ this.fileList.add(f); }
			}

			this.count = this.list.length;
			this.filecount = this.count - this.dircount;
		}
	}
}