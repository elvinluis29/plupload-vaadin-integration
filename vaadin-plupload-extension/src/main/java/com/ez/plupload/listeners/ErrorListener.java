package com.ez.plupload.listeners;

import java.io.Serializable;

import elemental.json.JsonObject;

public interface ErrorListener extends Serializable {

    void onError(JsonObject error);
}