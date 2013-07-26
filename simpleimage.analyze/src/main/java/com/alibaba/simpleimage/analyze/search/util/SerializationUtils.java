package com.alibaba.simpleimage.analyze.search.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializationUtils {
	public static Object loadObject(String location){
		File file = new File(location);
		if (file != null && file.exists()) {
			try {
				ObjectInputStream str = new ObjectInputStream(
						new FileInputStream(file));
				return str.readObject();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static void saveObject(Object obj,File file){
		try {
			ObjectOutputStream stream = new ObjectOutputStream(
					new FileOutputStream(file));
			stream.writeObject(obj);
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}