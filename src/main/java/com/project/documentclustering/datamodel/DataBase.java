package com.project.documentclustering.datamodel;

import java.util.ArrayList;
import java.util.List;

import org.glassfish.jersey.media.multipart.BodyPartEntity;


public class DataBase {
	
	private List<BodyPartEntity> fileEntities;
	private List<String> fileNames;
	private static DataBase instance;
	
	private DataBase() {
		fileEntities = new ArrayList<BodyPartEntity>();
		fileNames = new ArrayList<String>();
	}

	public static DataBase getInstance() {
		if( instance == null) {
			instance = new DataBase();
		}
		return instance;
	}
	public List<String> getFileNames() {
		return fileNames;
	}

	public void setFileName(String fileName) {
		this.fileNames.add(fileName);
	}

	public List<BodyPartEntity> getFileEntities() {
		return fileEntities;
	}

	public void setFileEntity(BodyPartEntity fileEntity) {
		this.fileEntities.add(fileEntity);
	}

	public void clearDataBase() {
		if(instance != null) {
			instance.getFileEntities().clear();
			instance.getFileNames().clear();
		}
	}
}
