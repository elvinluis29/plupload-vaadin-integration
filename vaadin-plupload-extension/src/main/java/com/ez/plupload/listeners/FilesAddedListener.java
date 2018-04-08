package com.ez.plupload.listeners;

import java.io.Serializable;
import java.util.List;

import com.ez.plupload.FileRef;

public interface FilesAddedListener extends Serializable {

    void onFilesAdded(List<FileRef> files);
}