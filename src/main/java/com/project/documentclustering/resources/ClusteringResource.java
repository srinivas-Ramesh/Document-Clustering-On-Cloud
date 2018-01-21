package com.project.documentclustering.resources;

import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.project.documentclustering.datamodel.DataBase;
import com.project.documentclustering.lucene.Processor;

@Path("/clustering")
public class ClusteringResource {

	private DataBase dataBase;
	private Scanner scanner;

	@GET
	public Response getFiles() {
		DataBase dataBase = DataBase.getInstance();
		if (!dataBase.getFileNames().isEmpty()) {
			
			Processor processor = new Processor();
			
//			List<String> fileNames = dataBase.getFileNames();
//			JsonArray namesArray = new JsonArray();
//			for (String name : fileNames) {
//				namesArray.add(name);
//			}
//
//			JsonObject fileNamesObject = new JsonObject();
//			fileNamesObject.add("files", namesArray);
//			return Response.ok(fileNamesObject.toString(), MediaType.APPLICATION_JSON).build();
		}

		else {
			return Response.noContent().build();
		}

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
			if (fileName.contains(".txt")) {
				if (!dataBase.getFileNames().contains(fileName)) {
					saveFile(bodyPartEntity, fileName);
				}
			} else {
				return Response.status(Status.BAD_REQUEST).entity("only txt files accepted").build();
			}
		}

		return Response.ok("Files saved for Document Clustering").build();
	}
	
	@DELETE
	public Response deleteAllFiles() {
		dataBase = DataBase.getInstance();
		if(!dataBase.getFileNames().isEmpty()) {
			dataBase.clearDataBase();
		}
		return Response.ok().build();
	}

	private void saveFile(BodyPartEntity fileEntity, String fileName) {
		dataBase = DataBase.getInstance();
		dataBase.setFileEntity(fileEntity);
		dataBase.setFileName(fileName);
		dataBase.setFileContent(getText(fileEntity));
	}
	
	public String getText(BodyPartEntity entity) {
		InputStream stream = entity.getInputStream();
		scanner = new Scanner(stream);
		Scanner s = scanner.useDelimiter("\\A");
		String result = s.hasNext() ? s.next() : "";
		return result;
	}
}
