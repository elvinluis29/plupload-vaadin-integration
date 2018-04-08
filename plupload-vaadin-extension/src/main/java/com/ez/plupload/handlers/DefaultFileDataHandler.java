package com.ez.plupload.handlers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DefaultFileDataHandler implements FileDataHandler {

	private int bufferSize;
	private File uploadPath;
	
	public DefaultFileDataHandler() {
		bufferSize = 1024;
		
		String path = System.getProperty("java.io.tmpdir");
		
		uploadPath = new File(path, "plupload");
		
		if(!uploadPath.exists()) {
			uploadPath.mkdirs();
		}
	}
	
	private void sleep(int milli) {
		try {
			Thread.sleep(milli);
		} catch (InterruptedException e) {
			//ignore
		}
	}

	protected void write(InputStream input, OutputStream output, int delaySimulation) throws IOException {
		int n;

		byte[] buffer = new byte[bufferSize];

		while (-1 != (n = input.read(buffer))) {
			sleep(delaySimulation);
			output.write(buffer, 0, n);
		}
	}
	
	protected void write(InputStream input, OutputStream output) throws IOException {
		write(input, output, 0);
	}
	
	@Override
	public void handleDataChunk(String fileId, InputStream is) throws IOException {
		File file = getFile(fileId);

		try (FileOutputStream o = new FileOutputStream(file, file.exists())) {
			write(is, o);
		}
	}

	public File getUploadPath() {
		return uploadPath;
	}

	public void setUploadPath(File uploadPath) {
		this.uploadPath = uploadPath;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}
	
	public File getFile(String id) {
		return new File(uploadPath, String.format("file%s", id.hashCode()));
	}
}
