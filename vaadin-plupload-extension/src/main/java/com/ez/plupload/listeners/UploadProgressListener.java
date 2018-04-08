package com.ez.plupload.listeners;

import java.io.Serializable;

import com.ez.plupload.FileRef;

public interface UploadProgressListener extends Serializable {

    void onUploadProgress(FileRef file);
}