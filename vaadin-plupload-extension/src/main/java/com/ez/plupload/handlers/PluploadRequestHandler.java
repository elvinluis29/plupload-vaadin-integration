package com.ez.plupload.handlers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import com.vaadin.server.RequestHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.server.VaadinSession;

public class PluploadRequestHandler implements RequestHandler {
	private static final long serialVersionUID = 1L;
	public static final String UPLOAD_ACTION_PATH = "plupload-extension";

	public static PluploadRequestHandler get() {
		VaadinSession session = VaadinSession.getCurrent();
		for (RequestHandler handler : session.getRequestHandlers()) {
			if (handler instanceof PluploadRequestHandler) {
				return (PluploadRequestHandler) handler;
			}
		}

		PluploadRequestHandler receiver = new PluploadRequestHandler();
		session.addRequestHandler(receiver);

		return receiver;
	}
	
	private FileDataHandler fileDataHanler;
	
	public PluploadRequestHandler() {
		fileDataHanler = new DefaultFileDataHandler();
	}
	
	@Override
	public boolean handleRequest(VaadinSession session, VaadinRequest request, VaadinResponse response)
			throws IOException {
		boolean canHandle = request.getPathInfo() != null && (request instanceof VaadinServletRequest)
				&& request.getPathInfo().endsWith(UPLOAD_ACTION_PATH);
		if (!canHandle) {
			return false;
		}

		VaadinServletRequest vsr = (VaadinServletRequest) request;
		HttpServletRequest req = vsr.getHttpServletRequest();

		if (ServletFileUpload.isMultipartContent(req)) {
			try {

				synchronized (this) {
					ServletFileUpload upload = new ServletFileUpload();
					FileItemIterator items = upload.getItemIterator(req);

					Map<String, String> fields = new HashMap<>();

					// Handle fields first
					while (items.hasNext()) {
						FileItemStream stream = items.next();

						if (stream.isFormField()) {
							fields.put(stream.getFieldName(), Streams.asString(stream.openStream()));
						} else if(fileDataHanler != null){
							fileDataHanler.handleDataChunk(fields.get("fileId"), stream.openStream());
						}
					}

					response.getWriter().write(String.format("Received chunk %s of %s for id %s", fields.get("chunk"),
							fields.get("chunks"), fields.get("fileId")));
				}
			} catch (Exception ex) {
				response.getWriter().append(
						"file upload unsuccessful, because of " + ex.getClass().getName() + ":" + ex.getMessage());
				throw new IOException(
						"There was a problem during processing of uploaded chunk. Nested exceptions may have more info.",
						ex);
			}

			return true;
		}

		return false;
	}

	public FileDataHandler getFileDataHandler() {
		return fileDataHanler;
	}

	public void setFileDataHanler(FileDataHandler fileDataHanler) {
		this.fileDataHanler = fileDataHanler;
	}
}
