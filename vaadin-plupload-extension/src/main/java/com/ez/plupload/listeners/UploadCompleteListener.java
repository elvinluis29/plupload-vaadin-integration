package com.ez.plupload.listeners;

import java.io.Serializable;

public interface UploadCompleteListener extends Serializable {

    void onUploadComplete();
}