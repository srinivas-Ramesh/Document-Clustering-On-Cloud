package com.project.documentclustering.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import document_analyzer.LuceneClass;
import document_analyzer.ResultClass;

public class Processor {

	private LuceneClass lucene;
	private static String indexDirectoryPath = "C:\\Users\\Srinivas Rao\\Desktop";
	private static String dataDirectoryPath = "C:\\Users\\Srinivas Rao\\Desktop\\documents";

	private ArrayList<Map> termVectors;
	private ArrayList<ArrayList<Double>> similarityMatrix;
	private ArrayList<ResultClass> similarDocuments;
	private ArrayList<ResultClass> copyDocuments;
	private ArrayList<ResultClass> outlierDocuments;
	private ArrayList<ResultClass> documentsClusters;

	private Processor() {
	}

	public Processor(String indexPath, String dataPath) {
		this.indexDirectoryPath = indexPath;
		this.dataDirectoryPath = dataPath;
	}

	public void clusterDocuments(int minValue, int maxValue) {

		try {

			lucene = new LuceneClass(indexDirectoryPath);

			lucene.indexDirectory(dataDirectoryPath);

			lucene.close();

			// --------------------DOCUMENT VECTORS--------------------

			termVectors = lucene.getTermVectors();

			// --------------------SIMILARITY MATRIX--------------------

			similarityMatrix = lucene.getSimilarityMatrix(termVectors);

			// --------------------SIMILAR DOCUMENTS--------------------

			similarDocuments = lucene.getSimilarDocuments(similarityMatrix, minValue, maxValue);

			// --------------------COPY DOCUMENTS--------------------

			copyDocuments = lucene.getCopyDocuments(similarityMatrix, maxValue);

			// --------------------OUTLIER DOCUMENTS--------------------

			outlierDocuments = lucene.getOutlierDocuments(similarityMatrix, minValue, maxValue);

			// --------------------DOCUMENTS CLUSTER--------------------

			// documentsClusters = lucene.getDocumentClusters(similarityMatrix);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
