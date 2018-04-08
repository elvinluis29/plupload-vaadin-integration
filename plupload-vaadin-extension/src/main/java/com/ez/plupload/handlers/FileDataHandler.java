package com.ez.plupload.handlers;

import java.io.IOException;
import java.io.InputStream;

public interface FileDataHandler {
	void handleDataChunk(String fileId, InputStream is) throws IOException;
}
