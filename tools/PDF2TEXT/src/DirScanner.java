


import java.io.File;
import java.util.ArrayList;

public class DirScanner {

	int count=0;
	int filecount=0;
	int dircount=0;
	File[] list; 
	ArrayList<File> dirList = new ArrayList<File>();
	ArrayList<File> fileList = new ArrayList<File>();
	
	public void scan(String path)
	{
		File dir = new File(path);
		list = dir.listFiles();
		if(list!=null){
			for(File f : list) 
		
			{
		    if(f.isDirectory()){dircount++;dirList.add(f);}else{fileList.add(f);}
		    //System.out.println(f.getName());
			}
			count=list.length;
			filecount=count-dircount;
		}
	}

	public int getCount() {
		return count;
	}

	public int getFilecount() {
		return filecount;
	}

	public int getDircount() {
		return dircount;
	}

	public File[] getList() {
		return list;
	}

	public ArrayList<File> getDirList() {
		return dirList;
	}

	public ArrayList<File> getFileList() {
		return fileList;
	}
}
