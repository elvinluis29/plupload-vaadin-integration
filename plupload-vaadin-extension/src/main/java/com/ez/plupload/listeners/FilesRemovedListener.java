package com.ez.plupload.listeners;

import java.io.Serializable;
import java.util.List;

import com.ez.plupload.FileRef;

public interface FilesRemovedListener extends Serializable {
    void onFilesRemoved(List<FileRef> files);
}