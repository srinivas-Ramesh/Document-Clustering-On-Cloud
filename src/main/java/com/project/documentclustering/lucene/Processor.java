package com.project.documentclustering.lucene;

import java.io.IOException;
import java.util.ArrayList;

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
			System.out.println("--------------------DOCUMENT VECTORS--------------------\n");

			dataBase.setTermVectors(lucene.getTermVectors());

			System.out.println("\n");

			// --------------------SIMILARITY MATRIX--------------------
			System.out.println("--------------------SIMILARITY MATRIX--------------------\n");

			dataBase.setSimilarityMatrix(lucene.getSimilarityMatrix(dataBase.getTermVectors()));

			for (ArrayList<Double> row : dataBase.getSimilarityMatrix()) {
				for (Double CosineSimilarity : row) {
					System.out.print(CosineSimilarity + "\t");
				}
				System.out.println("\n");
			}
			System.out.println("\n");

			// --------------------SIMILAR DOCUMENTS--------------------

			System.out.println("--------------------SIMILAR DOCUMENTS--------------------\n");

			dataBase.setSimilarDocuments(
					lucene.getSimilarDocuments(dataBase.getSimilarityMatrix(), minValue, maxValue));

			for (ResultClass result : dataBase.getSimilarDocuments()) {
				System.out.println(dataBase.getFileNames().get(result.getDocument1()) + " And "
						+ dataBase.getFileNames().get(result.getDocument2()) + "\n");
			}
			System.out.println("\n");
			// --------------------COPY DOCUMENTS--------------------
			System.out.println("--------------------COPY DOCUMENTS--------------------\n");
			
			dataBase.setCopyDocuments(lucene.getCopyDocuments(dataBase.getSimilarityMatrix(), maxValue));
			
			for (ResultClass result : dataBase.getCopyDocuments()) {
				System.out.println(dataBase.getFileNames().get(result.getDocument1()) + " And "
						+ dataBase.getFileNames().get(result.getDocument2()) + "\n");
			}
			System.out.println("\n");
			
			// --------------------OUTLIER DOCUMENTS--------------------
			System.out.println("--------------------OUTLIER DOCUMENTS--------------------\n");
			
			dataBase.setOutlierDocuments(
					lucene.getOutlierDocuments(dataBase.getSimilarityMatrix(), minValue, maxValue));

			for (ResultClass result : dataBase.getOutlierDocuments()) {
				System.out.println(dataBase.getFileNames().get(result.getDocument1()) + "\n");
			}
			
			System.out.println("\n");
			// --------------------DOCUMENTS CLUSTER--------------------

			// documentsClusters = lucene.getDocumentClusters(similarityMatrix);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
