package com.ez.plupload.listeners;

import java.io.File;
import java.io.Serializable;

import com.ez.plupload.FileRef;

public interface FileUploadedListener extends Serializable {

    void onFileUploaded(FileRef ref, File file);
}