package com.ez.plupload;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ez.plupload.handlers.DefaultFileDataHandler;
import com.ez.plupload.handlers.FileDataHandler;
import com.ez.plupload.handlers.PluploadRequestHandler;
import com.ez.plupload.listeners.DestroyListener;
import com.ez.plupload.listeners.DropZoneDropListener;
import com.ez.plupload.listeners.DropZoneEnterListener;
import com.ez.plupload.listeners.DropZoneLeaveListener;
import com.ez.plupload.listeners.ErrorListener;
import com.ez.plupload.listeners.FileFilteredListener;
import com.ez.plupload.listeners.FileUploadedListener;
import com.ez.plupload.listeners.FilesAddedListener;
import com.ez.plupload.listeners.FilesRemovedListener;
import com.ez.plupload.listeners.InitListener;
import com.ez.plupload.listeners.UploadCompleteListener;
import com.ez.plupload.listeners.UploadProgressListener;
import com.ez.plupload.listeners.UploadStartListener;
import com.ez.plupload.listeners.UploadStopListener;
import com.ez.plupload.utils.JsonUtils;
import com.vaadin.annotations.JavaScript;
import com.vaadin.server.AbstractJavaScriptExtension;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Button;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

@JavaScript({"plupload.full.min.js", "plupload-connector.js" })
public class Plupload extends AbstractJavaScriptExtension {
	private static final long serialVersionUID = 1L;

	private List<Filter> filterList;
	private boolean uploading, initiated;
	private JsonObject settings, filters;
	private PluploadRequestHandler requestHandler;
	
	private final Map<String, FileRef> fileMap = new HashMap<>();
	private final Set<InitListener> initListeners = new LinkedHashSet<>();
	private final Set<ErrorListener> errorListeners = new LinkedHashSet<>();
	private final Set<DestroyListener> destroyListeners = new LinkedHashSet<>();
	private final Set<UploadStopListener> uploadStopListeners = new LinkedHashSet<>();
	private final Set<FilesAddedListener> filesAddedListeners = new LinkedHashSet<>();
	private final Set<UploadStartListener> uploadStartListeners = new LinkedHashSet<>();
	private final Set<FilesRemovedListener> filesRemovedListeners = new LinkedHashSet<>();
	private final Set<FileFilteredListener> fileFilteredListeners = new LinkedHashSet<>();
	private final Set<FileUploadedListener> fileUploadedListeners = new LinkedHashSet<>();
	private final Set<DropZoneDropListener> dropZoneDropListeners = new LinkedHashSet<>();
	private final Set<DropZoneEnterListener> dropZoneEnterListeners = new LinkedHashSet<>();
	private final Set<DropZoneLeaveListener> dropZoneLeaveListeners = new LinkedHashSet<>();
	private final Set<UploadProgressListener> uploadProgressListeners = new LinkedHashSet<>();
	private final Set<UploadCompleteListener> uploadCompleteListeners = new LinkedHashSet<>();
		
	public Plupload(Button button) {
		extend(button);
		
		filterList = new ArrayList<>();
		
		settings = Json.createObject();
		requestHandler = PluploadRequestHandler.get();
				
		settings.put("max_retries", 3);
		settings.put("multipart", true);
		settings.put("chunk_size", "0mb");
		settings.put("multi_selection", true);
		settings.put("runtimes", "html5");
		settings.put("url", PluploadRequestHandler.UPLOAD_ACTION_PATH);
		settings.put("resize", JsonUtils.toJsonImageResize(new ImageResize()));
		
		filters = Json.createObject();
		settings.put("filters", filters);
		
		filters.put("max_file_size", "1024mb");
		filters.put("prevent_duplicates", false);
				
		addFunction("error", a -> error(a));
		addFunction("destroy", a -> destroy(a));
		addFunction("dragdrop", a -> dragDrop(a));
		addFunction("initiated", a -> initiated(a));
		addFunction("dragenter", a -> dragEnter(a));
		addFunction("dragleave", a -> dragLeave(a));
		addFunction("filesAdded", a -> filesAdded(a));
		addFunction("fileUploaded", a -> fileUploaded(a));
		addFunction("filesRemoved", a -> filesRemoved(a));
		addFunction("fileFiltered", a -> fileFiltered(a));
		addFunction("uploadProgress", a -> uploadProgress(a));
		addFunction("uploadComplete", a -> uploadComplete(a));
	}
	
	private void updateFilters() {
		JsonArray mimeTypes = Json.createArray();
		
		int i = 0;
		
		for (Filter f : filterList) {
			mimeTypes.set(i, JsonUtils.toJsonFilter(f));
			
			i += 1;
		}
		
		filters.put("mime_types", mimeTypes);
		
		if(initiated) {
    		setOption(Option.FILTERS, filters);
    	}
	}
	
	private void dragDrop(JsonArray a) {
		for (DropZoneDropListener l : dropZoneDropListeners) {
			l.onDrop();
		}
	}
	
	private void dragEnter(JsonArray a) {
		for (DropZoneEnterListener l : dropZoneEnterListeners) {
			l.onEnter();
		}
	}
	
	private void dragLeave(JsonArray a) {
		for (DropZoneLeaveListener l : dropZoneLeaveListeners) {
			l.onLeave();
		}
	}

	private void setFileMap(List<FileRef> files) {
		fileMap.clear();
		
		files.forEach(r -> fileMap.put(r.getId(), r));
	}
	
	private void fileUploaded(JsonArray a) {
		JsonObject file = a.getObject(0);
		
		FileDataHandler handler = requestHandler.getFileDataHandler();
		boolean isDefaultHandler = handler instanceof DefaultFileDataHandler;
		
		for (FileUploadedListener listener : fileUploadedListeners) {
			File f = null;
			FileRef r = JsonUtils.toFileRef(file);
			
			if(isDefaultHandler) {
				f = ((DefaultFileDataHandler)handler).getFile(r.id);
			}
			
            listener.onFileUploaded(r, f);
        }
	}

	private void uploadProgress(JsonArray a) {
		JsonObject file = a.getObject(0);
        for (UploadProgressListener listener : uploadProgressListeners) {
            listener.onUploadProgress(JsonUtils.toFileRef(file));
        }
	}

	private void uploadComplete(JsonArray a) {
		uploading = false;
		for (UploadCompleteListener listener : uploadCompleteListeners) {
            listener.onUploadComplete();
        }
	}

	private void error(JsonArray a) {
		uploading = false;
		
		JsonObject error = a.getObject(0);
		
		for (ErrorListener listener : errorListeners) {
            listener.onError(error);
        }
	}

	private void destroy(JsonArray a) {
		uploading = false;
		
		for (DestroyListener listener : destroyListeners) {
            listener.onDestroy();
        }
	}

	private void initiated(JsonArray a) {
		for (InitListener listener : initListeners) {
            listener.onInit();
        }
	}

	private void filesAdded(JsonArray a) {
		JsonArray files = a.getArray(0);
		List<FileRef> refs = JsonUtils.toFileList(files);
		
		setFileMap(refs);
		
		for (FilesAddedListener listener : filesAddedListeners) {
            listener.onFilesAdded(refs);
        }
	}

	private void filesRemoved(JsonArray a) {
		JsonArray files = a.getArray(0);
		for (FilesRemovedListener listener : filesRemovedListeners) {
            listener.onFilesRemoved(JsonUtils.toFileList(files));
        }
	}

	private void fileFiltered(JsonArray a) {
		JsonObject file = a.getObject(0);
		for (FileFilteredListener listener : fileFilteredListeners) {
            listener.onFileFiltered(JsonUtils.toFileRef(file));
        }
	}

	protected Plupload setOption(Option o, Object value) {
		callFunction("setOption", o.toString(), value);
		return this;
	}
	
	public Plupload start() {
		if(!uploading && !fileMap.isEmpty()) {
			for (UploadStartListener listener : uploadStartListeners) {
                listener.onUploadStart();
            }
			
			callFunction("start");
			
			uploading = true;
		}
		
		return this;
	}
	
	public Plupload stop() {
		if (uploading) {
			callFunction("stop");

            for (UploadStopListener listener : uploadStopListeners) {
                listener.onUploadStop();
            }
            
            uploading = false;
        }
		
		return this;
	}
	
	public Plupload refresh() {
		callFunction("refresh");
		
		return this;
	}
	
	public Plupload destroy() {
		callFunction("destroy");
		
		return this;
	}
	
	public Plupload disableBrowse(boolean disable) {
		callFunction("disableBrowse", disable);
		
		Button b = (Button) getParent();
		
		if(disable) {
			b.addStyleName("v-disabled");
		} else {
			b.removeStyleName("v-disabled");
		}
		
		return this;
	}
	
	public Plupload remove(FileRef ref) {
		callFunction("remove", ref.getId());
		return this;
	}
	
	public Plupload addDropZone(AbstractLayout layout) {
		if(layout.getId() == null || layout.getId().trim().isEmpty()) {
			layout.setId(String.format("plupload-%s", System.currentTimeMillis()));
		}
		
		callFunction("addDropZone", layout.getId());
		
		return this;
	}
	
	public Plupload addFilesAddedListener(FilesAddedListener listener) {
        this.filesAddedListeners.add(listener);
        return this;
    }

    public Plupload addFilesRemovedListener(FilesRemovedListener listener) {
        this.filesRemovedListeners.add(listener);
        return this;
    }

    public Plupload addFileFilteredListener(FileFilteredListener listener) {
        this.fileFilteredListeners.add(listener);
        return this;
    }

    public Plupload addFileUploadedListener(FileUploadedListener listener) {
        this.fileUploadedListeners.add(listener);
        return this;
    }

    public Plupload addUploadProgressListener(UploadProgressListener listener) {
        this.uploadProgressListeners.add(listener);
        return this;
    }

    public Plupload addUploadStartListener(UploadStartListener listener) {
        this.uploadStartListeners.add(listener);
        return this;
    }

    public Plupload addUploadStopListener(UploadStopListener listener) {
        this.uploadStopListeners.add(listener);
        return this;
    }

    public Plupload addUploadCompleteListener(UploadCompleteListener listener) {
        this.uploadCompleteListeners.add(listener);
        return this;
    }

    public Plupload addErrorListener(ErrorListener listener) {
        this.errorListeners.add(listener);
        return this;
    }

    public Plupload addDestroyListener(DestroyListener listener) {
        this.destroyListeners.add(listener);
        return this;
    }

    public Plupload addInitListener(InitListener listener) {
        this.initListeners.add(listener);
        return this;
    }
    
    public Plupload addDropZoneDropListener(DropZoneDropListener listener) {
        this.dropZoneDropListeners.add(listener);
        return this;
    }
    
    public Plupload addDropZoneEnterListener(DropZoneEnterListener listener) {
        this.dropZoneEnterListeners.add(listener);
        return this;
    }

    public Plupload addDropZoneLeaveListener(DropZoneLeaveListener listener) {
        this.dropZoneLeaveListeners.add(listener);
        return this;
    }

    public Plupload removeFilesAddedListener(FilesAddedListener listener) {
        this.filesAddedListeners.remove(listener);
        return this;
    }

    public Plupload removeFilesRemovedListener(FilesRemovedListener listener) {
        this.filesRemovedListeners.remove(listener);
        return this;
    }

    public Plupload removeFileFilteredListener(FileFilteredListener listener) {
        this.fileFilteredListeners.remove(listener);
        return this;
    }

    public Plupload removeFileUploadedListener(FileUploadedListener listener) {
        this.fileUploadedListeners.remove(listener);
        return this;
    }

    public Plupload removeUploadProgressListener(UploadProgressListener listener) {
        this.uploadProgressListeners.remove(listener);
        return this;
    }

    public Plupload removeUploadStartListener(UploadStartListener listener) {
        this.uploadStartListeners.remove(listener);
        return this;
    }

    public Plupload removeUploadStopListener(UploadStopListener listener) {
        this.uploadStopListeners.remove(listener);
        return this;
    }

    public Plupload removeUploadCompleteListener(UploadCompleteListener listener) {
        this.uploadCompleteListeners.remove(listener);
        return this;
    }

    public Plupload removeErrorListener(ErrorListener listener) {
        this.errorListeners.remove(listener);
        return this;
    }

    public Plupload removeInitListener(InitListener listener) {
        this.initListeners.remove(listener);
        return this;
    }

    public Plupload removeDestroyListener(DestroyListener listener) {
        this.destroyListeners.remove(listener);
        return this;
    }
    
    public Plupload removeDropZoneDropListener(DropZoneDropListener listener) {
        this.dropZoneDropListeners.remove(listener);
        return this;
    }
    
    public Plupload removeDropZoneEnterListener(DropZoneEnterListener listener) {
        this.dropZoneEnterListeners.remove(listener);
        return this;
    }

    public Plupload removeDropZoneLeaveListener(DropZoneLeaveListener listener) {
        this.dropZoneLeaveListeners.remove(listener);
        return this;
    }
    
    public Plupload setFileDataHandler(FileDataHandler handler) {
    	if(requestHandler != null) {
    		requestHandler.setFileDataHanler(handler);
    	}
    	
    	return this;
    }
    
    public FileDataHandler getFileDataHandler() {
    	return requestHandler == null ? null : requestHandler.getFileDataHandler();
    }
    
    public Plupload setUploadPath(File path) {
    	if(requestHandler != null && requestHandler.getFileDataHandler() instanceof DefaultFileDataHandler) {
    		((DefaultFileDataHandler)requestHandler.getFileDataHandler()).setUploadPath(path);
    	}
    	
    	return this;
    }
    
    public File getUploadPath() {
    	if(requestHandler != null && requestHandler.getFileDataHandler() instanceof DefaultFileDataHandler) {
    		return ((DefaultFileDataHandler)requestHandler.getFileDataHandler()).getUploadPath();
    	}
    	
    	return null;
    }
    
    public Plupload init() {
    	initiated = true;
    	callFunction("init", settings);
    	
    	return this;
    }
    
    public Plupload setMaxRetries(int retries) {
    	settings.put(Option.MAX_RETRIES.toString(), retries);
    	
    	if(initiated) {
    		setOption(Option.MAX_RETRIES, retries);
    	}
    	
    	return this;
    }
    
    public int getMaxRetries() {
    	return (int) settings.getNumber(Option.MAX_RETRIES.toString());
    }
    
    public Plupload setMultiPart(boolean multipart) {
    	settings.put(Option.MULTIPART.toString(), multipart);
    	
    	if(initiated) {
    		setOption(Option.MULTIPART, multipart);
    	}
    	
    	return this;
    }
    
    public boolean isMultiPart() {
    	return settings.getBoolean(Option.MULTIPART.toString());
    }
    
    public Plupload setRuntimes(String... runtimes) {
    	String runtimeString =
    	Arrays.asList(runtimes).stream().reduce((t, u) -> {
    		return t == null ? u : (t + "," + u) ;
		}).orElse("html5");
    	
    	settings.put(Option.RUNTIMES.toString(), runtimeString);
    	
    	if(initiated) {
    		setOption(Option.RUNTIMES, runtimeString);
    	}
    	
    	return this;
    }
    
    public String getRuntimes() {
    	return settings.getString(Option.RUNTIMES.toString());
    }
     
    public Plupload setChunkSize(String size) {
    	settings.put(Option.CHUNK_SIZE.toString(), size);
    	
    	if(initiated) {
    		setOption(Option.CHUNK_SIZE, size);
    	}
    	
    	return this;
    }
    
    public String getChuckSize() {
    	return settings.getString(Option.CHUNK_SIZE.toString());
    }
    
    public Plupload setMultiSelection(boolean multiSelect) {
    	settings.put(Option.MULTI_SELECTION.toString(), multiSelect);
    	
    	if(initiated) {
    		setOption(Option.MULTI_SELECTION, multiSelect);
    	}
    	
    	return this;
    }
    
    public boolean isMultiSelection() {
    	return settings.getBoolean(Option.MULTI_SELECTION.toString());
    }
    
    public Plupload setMaxFileSize(String size) {
    	filters.put(Option.MAX_FILE_SIZE.toString(), size);
    	
    	if(initiated) {
    		setOption(Option.FILTERS, filters);
    	}
    	
    	return this;
    }
    
    public String getMaxFileSize() {
    	return filters.getString(Option.MAX_FILE_SIZE.toString());
    }
    
    public Plupload setPreventDuplicates(boolean prevent) {
    	filters.put(Option.PREVENT_DUPLICATES.toString(), prevent);
    	
    	if(initiated) {
    		setOption(Option.FILTERS, filters);
    	}
    	
    	return this;
    }
    
    public boolean isPreventDuplicates() {
    	return filters.getBoolean(Option.PREVENT_DUPLICATES.toString());
    }
    
    public Plupload setImageResize(ImageResize resize) {
    	JsonObject jr = JsonUtils
    			.toJsonImageResize(resize);
    	
    	settings.put("resize", jr);
    	
    	if(initiated) {
    		setOption(Option.RESIZE, jr);
    	}
    	
    	return this;
    }
    
    public ImageResize getImageResize() {
    	return JsonUtils.toImageResize(settings.getObject("resize"));
    }
    
    public Plupload addFilter(Filter filter) {
    	filterList.add(filter);
    	
    	updateFilters();
    	
    	return this;
    }
    
    public Plupload removeFilter(Filter filter) {
    	filterList.remove(filter);
    	
    	updateFilters();
    	
    	return this;
    }
    
    public List<Filter> getFilters(){
    	return Collections.unmodifiableList(filterList);
    }
}
