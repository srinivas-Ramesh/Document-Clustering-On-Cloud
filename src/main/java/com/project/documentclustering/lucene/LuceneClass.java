package com.project.documentclustering.lucene;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import com.project.documentclustering.datamodel.DataBase;

public class LuceneClass {

	private IndexWriter indexWriter;
	private IndexReader indexReader;
	private Directory directory;
	private Map vector;
	private ArrayList<Map> documentVectors;

	public LuceneClass(String indexDirectoryPath) throws IOException {

		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		Path path = FileSystems.getDefault().getPath(indexDirectoryPath, "index");

		// close the previous directory to gain access to delete it
		if (directory != null)
			directory.close();
		deleteIndexDirectory(path);
		directory = new SimpleFSDirectory(path);
		indexWriter = new IndexWriter(directory, indexWriterConfig);
	}

	public IndexWriter getIndexWriter() {
		return indexWriter;
	}

	public IndexReader getIndexReader() {
		return indexReader;
	}

	public Directory getDirectory() {
		return directory;
	}

	public ArrayList<Map> getTermVectors() {

		try {
			indexReader = DirectoryReader.open(directory);
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			documentVectors = new ArrayList<Map>();
			for (int i = 0; i < indexReader.maxDoc(); ++i) {
				vector = new HashMap<String, Long>();
				final Terms terms = indexReader.getTermVector(i, "Contents");
				if (terms != null) {
					int numTerms = 0;
					// record term occurrences for corpus terms above threshold
					TermsEnum term = terms.iterator();
					while (term.next() != null) {
						vector.put(term.term().utf8ToString(), term.totalTermFreq());
						++numTerms;
					}
					documentVectors.add(vector);
					System.out.println("Document Vector " + (i + 1) + " :  " + vector.toString() + "\n");
				} else {
					System.err.println("Document " + i + " had a null terms vector for body");
				}
			}
			indexReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return documentVectors;
	}

	public void close() throws CorruptIndexException, IOException {
		indexWriter.close();
	}

	public void indexDirectory() throws IOException {

		DataBase dataBase = DataBase.getInstance();

		for (String fileName : dataBase.getFileNames()) {

			// System.out.println("Indexing " + file.getCanonicalPath());
			Document document = getDocument(dataBase.getFileContents().get(dataBase.getFileNames().indexOf(fileName)),
					fileName);
			indexWriter.addDocument(document);
		}
		indexWriter.flush();
	}

	private Document getDocument(File file) throws IOException {

		Document document = new Document();

		FieldType type = new FieldType();
		type.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
		type.setTokenized(true);
		type.setStored(true);
		type.setStoreTermVectors(true);

		// index file contents
		// Field contentField = new Field("Contents",
		// fileOperator.readFiles(file.getAbsolutePath()), type);
		Field fileNameField = new TextField("FileName", file.getName(), Store.YES);

		// // index file path
		Field filePathField = new StringField("Path", file.getAbsolutePath(), Field.Store.YES);
		// document.add(contentField);
		document.add(fileNameField);
		document.add(filePathField);
		return document;
	}

	private Document getDocument(String fileContent, String fileName) {
		Document document = new Document();

		FieldType type = new FieldType();
		type.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
		type.setTokenized(true);
		type.setStored(true);
		type.setStoreTermVectors(true);

		Field contentField = new Field("Contents", fileContent, type);
		Field fileNameField = new TextField("FileName", fileName, Store.YES);

		document.add(contentField);
		document.add(fileNameField);
		return document;
	}

	public ArrayList<ArrayList<Double>> getSimilarityMatrix(ArrayList<Map> vectors) {

		CosineSimilarity cosineSimilarity = new CosineSimilarity();

		ArrayList<ArrayList<Double>> similarityMatrix = new ArrayList<ArrayList<Double>>();

		for (Map vector1 : vectors) {

			ArrayList<Double> rowSimilarity = new ArrayList<Double>();

			for (Map vector2 : vectors) {
				rowSimilarity.add(cosineSimilarity.calculateCosineSimilarity(vector1, vector2));
			}
			similarityMatrix.add(rowSimilarity);
		}
		return similarityMatrix;

	}

	public ArrayList<ResultClass> getSimilarDocuments(ArrayList<ArrayList<Double>> similarityMatrix, double minValue,
			double maxValue) {

		ArrayList<ResultClass> similarDocuments = new ArrayList<ResultClass>();

		for (ArrayList<Double> row : similarityMatrix) {
			for (int column = 0; column < similarityMatrix.indexOf(row); column++) {

				if (row.get(column) >= Double.valueOf(minValue) && row.get(column) < Double.valueOf(maxValue)) {
					ResultClass result = new ResultClass();
					result.setDocument1(similarityMatrix.indexOf(row));
					result.setDocument2(column);
					similarDocuments.add(result);
				}
			}
		}

		return similarDocuments;
	}

	public ArrayList<ResultClass> getCopyDocuments(ArrayList<ArrayList<Double>> similarityMatrix, Double maxValue) {

		ArrayList<ResultClass> copyDocuments = new ArrayList<ResultClass>();

		for (ArrayList<Double> row : similarityMatrix) {
			for (int column = 0; column < similarityMatrix.indexOf(row); column++) {

				if (row.get(column) >= Double.valueOf(maxValue)) {
					ResultClass result = new ResultClass();
					result.setDocument1(similarityMatrix.indexOf(row));
					result.setDocument2(column);
					copyDocuments.add(result);
				}
			}
		}
		return copyDocuments;
	}

	public ArrayList<ResultClass> getOutlierDocuments(ArrayList<ArrayList<Double>> similarityMatrix, double minValue,
			double maxValue) {
		ArrayList<ResultClass> similarDocuments = this.getSimilarDocuments(similarityMatrix, minValue, maxValue);
		ArrayList<ResultClass> outliers = new ArrayList<ResultClass>();
		boolean isOutlier;
		for (int i = 0; i < similarityMatrix.size(); i++) {
			isOutlier = true;
			for (ResultClass result : similarDocuments) {
				if (i == result.getDocument1() || i == result.getDocument2()) {
					isOutlier = false;
					break;
				}
			}
			if (isOutlier) {
				ResultClass result = new ResultClass();
				result.setDocument1(i);
				outliers.add(result);
			}
		}
		return outliers;
	}

	public ArrayList<String> getDocumentClusters(ArrayList<ArrayList<Double>> similarityMatrix, Double minValue,
			Double maxValue) {

		ArrayList<String> documentsCluster = new ArrayList<String>();
		boolean subString;

		for (ArrayList<Double> row : similarityMatrix) {

			subString = false;
			String cluster;
			cluster = String.valueOf(similarityMatrix.indexOf(row) + 1);

			for (int column = similarityMatrix.indexOf(row) + 1; column < row.size(); column++) {

				if (row.get(column) >= Double.valueOf(minValue) && row.get(column) < Double.valueOf(maxValue)) {
					cluster = cluster + "," + String.valueOf(column + 1);
				}
			}
			for (String string : documentsCluster) {
				if (string.contains(cluster)) {
					subString = true;
					break;
				}
			}

			if (!subString) {
				documentsCluster.add(cluster);
				subString = false;
			}

		}

		return documentsCluster;
	}

	private void deleteIndexDirectory(Path path) {
		// File file = new File(path.toString());
		// // to end the recursive loop
		// if (!file.exists())
		// return;
		//
		// // if directory, go inside and call recursively
		// if (file.isDirectory()) {
		// for (File f : file.listFiles()) {
		// // call recursively
		// deleteIndexDirectory(f.toPath());
		// }
		// }
		// // call delete to delete files and empty directory
		// file.delete();
		try {
			MoreFiles.deleteRecursively(path, RecursiveDeleteOption.ALLOW_INSECURE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<ArrayList<Integer>> getDocumentClusters(ArrayList<ResultClass> similarDocuments) {

		ArrayList<ArrayList<Integer>> finalArray = new ArrayList<>();
		for (ResultClass result : similarDocuments) {

			ArrayList<Integer> cluster = new ArrayList<Integer>();
			cluster.add(result.getDocument1());
			cluster.add(result.getDocument2());

			for (ResultClass result1 : similarDocuments) {

				if (cluster.contains(result1.getDocument1())
						&& similarDocuments.indexOf(result1) != similarDocuments.indexOf(result)) {
					cluster.add(result1.getDocument2());
				}

				else if (cluster.contains(result1.getDocument2())
						&& similarDocuments.indexOf(result1) != similarDocuments.indexOf(result)) {
					cluster.add(result1.getDocument1());
				}
			}

			// check for ducplicate clusters
			boolean notDuplicate = true;
			for (ArrayList<Integer> cluster1 : finalArray) {
				if (equalLists(cluster, cluster1)) {
					notDuplicate = false;
				}
			}
			if (notDuplicate) {
				finalArray.add(cluster);
			}
		}
		
		return finalArray;
	}

	public boolean equalLists(List<Integer> one, List<Integer> two) {
		if (one == null && two == null) {
			return true;
		}

		if ((one == null && two != null) || one != null && two == null || one.size() != two.size()) {
			return false;
		}

		// to avoid messing the order of the lists we will use a copy
		// as noted in comments by A. R. S.
		one = new ArrayList<Integer>(one);
		two = new ArrayList<Integer>(two);

		Collections.sort(one);
		Collections.sort(two);
		return one.equals(two);
	}

}
