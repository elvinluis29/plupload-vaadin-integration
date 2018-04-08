window.com_ez_plupload_Plupload = function(){
	var self = this;

	self.trigger = this.getElement(
		this.getParentId(
			this.getConnectorId()
		)
	);

	self.uploader = self.uploader || {};
	
	self.init = function(settings){
		settings.browse_button = self.trigger;
		
		self.uploader = new plupload.Uploader(settings);
		
		self.uploader.bind('FilesAdded', function(up, files) {
	        self.filesAdded(files);
		});
		
		self.uploader.bind('FilesRemoved', function(up, files) {
		    self.filesRemoved(files);
		});
		
		self.uploader.bind('FileFiltered', function(up, file) {
		    self.fileFiltered(file);
		});
		
		self.uploader.bind('FileUploaded', function(up, file) {
		    self.fileUploaded(file);
		});
		
		self.uploader.bind('UploadProgress', function(up, file) {
		    self.uploadProgress(file);
		});
		
		self.uploader.bind('UploadComplete', function() {
		    self.uploadComplete();
		});
		
		self.uploader.bind('Error', function(up, error) {
		    self.error(error);
		});
		 
		self.uploader.bind('Destroy', function() {
		    self.destroy();
		});
		 
		self.uploader.bind('Init', function() {
		    self.initiated();
		});
		
		self.uploader.bind('BeforeUpload', function (up, file) {
		    up.settings.multipart_params = {fileId: file.id}
		});   
		
		self.uploader.init();
	}
	
	
	self.start = function(){
		self.uploader.start();
	}
	
	self.stop = function(){
		self.uploader.stop();
	}
	
	self.refresh = function(){
		self.uploader.refresh();
	}
	
	self.destroy = function(){
		self.uploader.destroy();
	}
	
	self.disableBrowse = function(disable){
		self.uploader.disableBrowse(disable);
	}
	
	self.remove = function(fileId){
		var file = self.uploader.getFile(fileId);
		
        if(typeof file !=='undefined') {
        	self.uploader.removeFile(file);
        }
	}
	
	self.setOption = function(name, value){
		self.uploader.setOption(name, value);
	}
	
	self.addDropZone = function(elementId){
		var element = document.getElementById(elementId);
        if(typeof element !== 'undefined') {
        	
        	var dropzone = new window.moxie.file.FileDrop({
                drop_zone: element
            });
        	
            dropzone.ondrop = function( event ) {
            	self.dragdrop();
            	self.uploader.addFile( dropzone.files );
            };
            
            dropzone.onerror = function( event ) {
            	self.error(event);
            }
            
            dropzone.ondragleave = function( event ) {
            	self.dragleave();
            }
            
            dropzone.ondragenter = function( event ) {
            	self.dragenter();
            }
            
            dropzone.init(); 
            
            self.uploader.dropZones = self.uploader.dropZones || [];
            
            self.uploader.dropZones.push(dropzone);
        }
	}
}