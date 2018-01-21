package com.project.documentclustering.lucene;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.apache.tomcat.util.http.fileupload.FileUtils;

import com.project.documentclustering.datamodel.DataBase;

import document_analyzer.ResultClass;

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
		deleteIndexDirectory(path);
		directory = new SimpleFSDirectory(path);
		indexWriter = new IndexWriter(directory, indexWriterConfig);

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
				} else {
					System.err.println("Document " + i + " had a null terms vector for body");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return documentVectors;
	}

	public void close() throws CorruptIndexException, IOException {
		indexWriter.close();
	}

	public void indexDirectory(String directory) throws IOException {

		DataBase dataBase = DataBase.getInstance();

		for (String fileContent : dataBase.getFileContents()) {

			// System.out.println("Indexing " + file.getCanonicalPath());
			Document document = getDocument(fileContent,
					dataBase.getFileNames().get(dataBase.getFileEntities().indexOf(fileContent)));
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
//		Field contentField = new Field("Contents", fileOperator.readFiles(file.getAbsolutePath()), type);
		Field fileNameField = new TextField("FileName", file.getName(), Store.YES);

		// // index file path
		Field filePathField = new StringField("Path", file.getAbsolutePath(), Field.Store.YES);
//		document.add(contentField);
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

	public ArrayList<ResultClass> getCopyDocuments(ArrayList<ArrayList<Double>> similarityMatrix, int maxValue) {

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

	public ArrayList<String> getDocumentClusters(ArrayList<ArrayList<Double>> similarityMatrix) {

		ArrayList<String> documentsCluster = new ArrayList<String>();

		boolean subString;

		for (ArrayList<Double> row : similarityMatrix) {

			subString = false;
			String cluster;
			cluster = String.valueOf(similarityMatrix.indexOf(row) + 1);

			for (int column = similarityMatrix.indexOf(row) + 1; column < row.size(); column++) {
				if (row.get(column) >= Double.valueOf(0.2) && row.get(column) < Double.valueOf(0.8)) {
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

	private boolean deleteIndexDirectory(Path path) {
		try {
			FileUtils.deleteDirectory(new File(path.toString()));
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return false;
		}
	}
}
