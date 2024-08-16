package util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class StringLoadUtil {

	public static String load(String file) throws IOException {
		int len;
		final char[] chr = new char[4096];
		final StringBuffer buffer = new StringBuffer();
		final FileReader reader = new FileReader(file);
		try {
			while ((len = reader.read(chr)) > 0) {
				buffer.append(chr, 0, len);
			}
		} finally {
			reader.close();
		}
		return buffer.toString();
	}

	public static String loadResource(String filename) {
		InputStream x = Util.class.getClassLoader().getResourceAsStream(filename);
		Scanner in=new Scanner(x,"UTF-8");
		in.useDelimiter("\\A");
		String inputStreamString = in.next();
		in.close();
		return inputStreamString;
	}

	
	public static InputStream loadResource2InputStream(String filename) {
		return Util.class.getClassLoader().getResourceAsStream(filename);
		
	}
	
	public static List<Path> getFilePathList(String folderName,String extension) throws IOException {
		
		List<Path> result= new LinkedList<Path>();
		File folder = new File(folderName);
		File[] listOfFiles = folder.listFiles();

		for (File file : listOfFiles) {
		    if(file.isFile() && file.getCanonicalPath().endsWith(extension)) {
		        result.add(Paths.get(file.toURI()));
		    }
		}
		return result;
	}
	
	public static List<Path> getFilePathList(String folderName) throws IOException {
		return getFilePathList(folderName, "");
	}
	
	public static List<String> getFileContentsInFolder(String folderName, String ending) throws IOException {
		
		List<String> contents=new LinkedList<String>();
		List<Path> ps=getFilePathList(folderName, ending);
		for(Path p:ps) {
			contents.add(new String(Files.readAllBytes(p)));
		}
		
		return contents;
		
	}

	
	public static List<String> getFileContentsInFolder(String folderName) throws IOException {
	    return getFileContentsInFolder(folderName, "");
	}
}
