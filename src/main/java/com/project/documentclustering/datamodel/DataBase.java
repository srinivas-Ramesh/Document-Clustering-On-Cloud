package com.project.documentclustering.datamodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.glassfish.jersey.media.multipart.BodyPartEntity;

import com.project.documentclustering.lucene.ResultClass;

public class DataBase {

	private List<BodyPartEntity> fileEntities;
	private List<String> fileNames;
	private static DataBase instance;
	private List<String> fileContents;

	private ArrayList<Map> termVectors;
	private ArrayList<ArrayList<Double>> similarityMatrix;
	private ArrayList<ResultClass> similarDocuments;
	private ArrayList<ResultClass> copyDocuments;
	private ArrayList<ResultClass> outlierDocuments;
	private ArrayList<ResultClass> documentsClusters;

	private DataBase() {
		fileEntities = new ArrayList<BodyPartEntity>();
		fileNames = new ArrayList<String>();
		fileContents = new ArrayList<String>();
		termVectors = new ArrayList<>();
		similarityMatrix = new ArrayList<>() ;
		similarDocuments = new ArrayList<>();
		copyDocuments = new ArrayList<>();
		outlierDocuments = new ArrayList<>();
		documentsClusters = new ArrayList<>();
	}

	public static DataBase getInstance() {
		if (instance == null) {
			instance = new DataBase();
		}
		return instance;
	}

	public List<String> getFileContents() {
		return fileContents;
	}

	public void setFileContent(String fileContent) {
		this.fileContents.add(fileContent);
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

	public ArrayList<Map> getTermVectors() {
		return termVectors;
	}

	public void setTermVectors(ArrayList<Map> termVectors) {
		this.termVectors= termVectors;
	}

	public ArrayList<ArrayList<Double>> getSimilarityMatrix() {
		return similarityMatrix;
	}

	public void setSimilarityMatrix(ArrayList<ArrayList<Double>> similarityMatrix) {
		this.similarityMatrix = similarityMatrix;
	}

	public ArrayList<ResultClass> getSimilarDocuments() {
		return similarDocuments;
	}

	public void setSimilarDocuments(ArrayList<ResultClass> similarDocuments) {
		this.similarDocuments = similarDocuments;
	}

	public ArrayList<ResultClass> getCopyDocuments() {
		return copyDocuments;
	}

	public void setCopyDocuments(ArrayList<ResultClass> copyDocuments) {
		this.copyDocuments = copyDocuments;
	}

	public ArrayList<ResultClass> getOutlierDocuments() {
		return outlierDocuments;
	}

	public void setOutlierDocuments(ArrayList<ResultClass> outlierDocuments) {
		this.outlierDocuments = outlierDocuments;
	}

	public ArrayList<ResultClass> getDocumentsClusters() {
		return documentsClusters;
	}

	public void setDocumentsClusters(ArrayList<ResultClass> documentsClusters) {
		this.documentsClusters = documentsClusters;
	}

	public void clearDataBase() {
		if (instance != null) {
			instance.getFileEntities().clear();
			instance.getFileNames().clear();
			instance.getFileContents().clear();
			instance.getCopyDocuments().clear();
			instance.getDocumentsClusters().clear();
			instance.getOutlierDocuments().clear();
			instance.getSimilarDocuments().clear();
			instance.getSimilarityMatrix().clear();
			instance.getTermVectors().clear();
		}
	}

}
