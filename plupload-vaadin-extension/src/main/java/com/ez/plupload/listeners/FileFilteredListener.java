package com.ez.plupload.listeners;

import java.io.Serializable;

import com.ez.plupload.FileRef;

public interface FileFilteredListener extends Serializable {

    void onFileFiltered(FileRef file);
}