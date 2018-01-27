package com.project.documentclustering.lucene;

import java.io.IOException;

import com.project.documentclustering.datamodel.DataBase;

public class Processor {

	private LuceneClass lucene;
	private String indexDirectoryPath;
	private DataBase dataBase;

	public Processor(String indexPath) {
		this.indexDirectoryPath = indexPath;
	}

	public void clusterDocuments(Double minValue, Double maxValue) {

		try {

			lucene = new LuceneClass(indexDirectoryPath);

			lucene.indexDirectory();

			lucene.close();
			
			dataBase = DataBase.getInstance();

			// --------------------DOCUMENT VECTORS--------------------

			dataBase.setTermVectors(lucene.getTermVectors());

			// --------------------SIMILARITY MATRIX--------------------

			dataBase.setSimilarityMatrix(lucene.getSimilarityMatrix(dataBase.getTermVectors()));

			// --------------------SIMILAR DOCUMENTS--------------------

			dataBase.setSimilarDocuments(lucene.getSimilarDocuments(dataBase.getSimilarityMatrix(), minValue, maxValue));

			// --------------------COPY DOCUMENTS--------------------

			dataBase.setCopyDocuments(lucene.getCopyDocuments(dataBase.getSimilarityMatrix(), maxValue));

			// --------------------OUTLIER DOCUMENTS--------------------

			dataBase.setOutlierDocuments(lucene.getOutlierDocuments(dataBase.getSimilarityMatrix(), minValue, maxValue));

			// --------------------DOCUMENTS CLUSTER--------------------

			// documentsClusters = lucene.getDocumentClusters(similarityMatrix);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
