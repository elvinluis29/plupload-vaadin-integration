package com.ez.plupload.utils;

import java.util.ArrayList;
import java.util.List;

import com.ez.plupload.FileRef;
import com.ez.plupload.Filter;
import com.ez.plupload.ImageResize;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

public class JsonUtils {
	private JsonUtils() {
	}
	
	public static FileRef toFileRef(JsonObject json) {
		FileRef ref = new FileRef();
		
		ref.setId(json.getString("id"));
		ref.setType(json.getString("type"));
		ref.setName(json.getString("name"));
		ref.setSize((long) json.getNumber("size"));
		ref.setStatus((int) json.getNumber("status"));
		ref.setLoaded((long) json.getNumber("loaded"));
		ref.setPercent((int) json.getNumber("percent"));
		ref.setOrigSize((long) json.getNumber("origSize"));
		ref.setRelativePath(json.getString("relativePath"));
		ref.setLastModifiedDate(json.getString("lastModifiedDate"));
		
		return ref;
	}
	
	public static List<FileRef> toFileList(JsonArray a){
		List<FileRef> files = new ArrayList<>();
		
		for (int i = 0; i < a.length(); i++) {
			files.add(toFileRef(a.getObject(i)));
		}
		
		return files;
	}
	
	public static JsonObject toJsonImageResize(ImageResize resize) {
		JsonObject json = Json.createObject();
		
		json.put("crop", resize.isCrop());
		json.put("width", resize.getWidth());
		json.put("height", resize.getHeight());
		json.put("enabled", resize.isEnabled());
		json.put("quality", resize.getQuality());
		json.put("preserve_headers", resize.isPreserveHeaders());
		
		return json;
	}
	
	public static ImageResize toImageResize(JsonObject json) {
		ImageResize r = new ImageResize();
		
		r.setCrop(json.getBoolean("crop"));
		r.setEnabled(json.getBoolean("enabled"));
		r.setWidth((int) json.getNumber("width"));
		r.setHeight((int) json.getNumber("height"));
		r.setQuality((int) json.getNumber("quality"));
		r.setPreserveHeaders(json.getBoolean("preserve_headers"));
		
		return r;
	}

	public static JsonObject toJsonFilter(Filter filter) {
		JsonObject json = Json.createObject();
		
		json.put("title", filter.getTitle());
		json.put("extensions", filter.getExtensions());
		
		return json;
	}
	
	public static Filter toFilter(JsonObject json) {
		Filter f = new Filter();
		
		f.setTitle(json.getString("title"));
		f.setExtensions(json.getString("extensions"));
		
		return f;
	}
}
