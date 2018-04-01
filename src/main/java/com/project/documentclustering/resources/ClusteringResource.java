package com.project.documentclustering.resources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.project.documentclustering.datamodel.DataBase;
import com.project.documentclustering.lucene.Processor;
import com.project.documentclustering.lucene.ResultClass;

@Path("/clustering")
public class ClusteringResource {

	private DataBase dataBase;
	private Scanner scanner;

	@GET
	public Response getFiles(@HeaderParam("dataPath") String dataPath, @HeaderParam("indexPath") String indexPath,
			@HeaderParam("minValue") Double minValue, @HeaderParam("maxValue") Double maxValue) {
		dataBase = DataBase.getInstance();
		if (indexPath == null || minValue == null || maxValue == null) {
			return Response.status(Status.BAD_REQUEST)
					.entity("pass dataPath, indexPath, minValue, maxValue as header parameters").build();
		}

		else if (!dataBase.getFileNames().isEmpty()) {

			Processor processor = new Processor(indexPath);
			processor.clusterDocuments(minValue, maxValue);
			JsonObject result = prepareClusterResponse(dataBase);
			return Response.ok(result.toString(), MediaType.APPLICATION_JSON).build();
		}

		else {
			return Response.status(Status.NOT_ACCEPTABLE).entity("No files have been saved in the system").build();
		}

	}

	private JsonObject prepareClusterResponse(DataBase dataBase) {

		JsonObject CopyDocumentsJsonObject = new JsonObject();
		ArrayList<ResultClass> copyDocuments = dataBase.getCopyDocuments();
		int counter = 1;

		for (ResultClass copyDocument : copyDocuments) {
			JsonArray documentArray = new JsonArray();
			documentArray.add(getFileName(copyDocument.getDocument1()));
			documentArray.add(getFileName(copyDocument.getDocument2()));
			CopyDocumentsJsonObject.add(String.valueOf(counter++), documentArray);
		}

		JsonObject OutliersJsonObject = new JsonObject();
		ArrayList<ResultClass> outlierDocuments = dataBase.getOutlierDocuments();
		counter = 1;

		for (ResultClass outlierDocument : outlierDocuments) {
			OutliersJsonObject.addProperty(String.valueOf(counter++), getFileName(outlierDocument.getDocument1()));
		}

		JsonObject similarDocumentsJsonObject = new JsonObject();
		ArrayList<ResultClass> similarDocuments = dataBase.getSimilarDocuments();
		counter = 1;
		for (ResultClass similarDocument : similarDocuments) {

			JsonArray documentArray = new JsonArray();
			documentArray.add(getFileName(similarDocument.getDocument1()));
			documentArray.add(getFileName(similarDocument.getDocument2()));
			similarDocumentsJsonObject.add(String.valueOf(counter++), documentArray);
		}

		JsonObject responseObject = new JsonObject();
		responseObject.add("similarDocuments", similarDocumentsJsonObject);
		responseObject.add("oulierDocuments", OutliersJsonObject);
		responseObject.add("copyDocuments", CopyDocumentsJsonObject);

		return responseObject;
	}

	private String getFileName(int document1) {
		return dataBase.getFileNames().get(document1);
	}

	@DELETE
	public Response deleteAllFiles() {
		dataBase = DataBase.getInstance();
		if (!dataBase.getFileNames().isEmpty()) {
			dataBase.clearDataBase();
		}
		return Response.ok().build();
	}

	@Path("/upload")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFiles(final FormDataMultiPart multiPart) {

		dataBase = DataBase.getInstance();
		List<FormDataBodyPart> bodyParts = multiPart.getFields("files");

		if (bodyParts == null) {
			return Response.status(Status.BAD_REQUEST).entity("No files sent/ key for files must be 'files'").build();
		}

		/* Save multiple files */
		for (int i = 0; i < bodyParts.size(); i++) {
			BodyPartEntity bodyPartEntity = (BodyPartEntity) bodyParts.get(i).getEntity();
			String fileName = bodyParts.get(i).getContentDisposition().getFileName();
			if (fileName.contains(".txt") || fileName.contains(".pdf")) {
				if (!dataBase.getFileNames().contains(fileName)) {
					saveFile(bodyPartEntity, fileName);
				}
			} else {
				return Response.status(Status.BAD_REQUEST).entity("only txt files accepted").build();
			}
		}

		return Response.ok("Files saved for Document Clustering").build();
	}

	private void saveFile(BodyPartEntity fileEntity, String fileName) {
		dataBase = DataBase.getInstance();
		dataBase.setFileEntity(fileEntity);
		dataBase.setFileName(fileName);
		try {
			dataBase.setFileContent(getText(fileEntity, fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getText(BodyPartEntity entity, String fileName) throws IOException {

		if (fileName.toLowerCase().endsWith("pdf")) {
			
			InputStream stream = entity.getInputStream();
			File file = new File("pdf");
			OutputStream outputStream = new FileOutputStream(file);
			IOUtils.copy(stream, outputStream);
			outputStream.close();
			PDDocument document = PDDocument.load(file);
			PDFTextStripper pdfStripper = new PDFTextStripper();
			String text = pdfStripper.getText(document);
			return text;
		} else {
			InputStream stream = entity.getInputStream();
			scanner = new Scanner(stream);
			Scanner s = scanner.useDelimiter("\\A");
			String result = s.hasNext() ? s.next() : "";
			return result;
		}
	}
}
