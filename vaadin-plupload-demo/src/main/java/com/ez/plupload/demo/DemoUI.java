package com.ez.plupload.demo;

import javax.servlet.annotation.WebServlet;

import com.ez.plupload.Filter;
import com.ez.plupload.ImageResize;
import com.ez.plupload.Plupload;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("demo")
@Title("Vaadin Plupload Extension Demo")
@SuppressWarnings("serial")
public class DemoUI extends UI
{

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = DemoUI.class)
    public static class Servlet extends VaadinServlet {
    }

    @Override
    protected void init(VaadinRequest request) {
    	VerticalLayout layout = new VerticalLayout();

        setContent(layout);
        
        layout.setMargin(true);
        layout.setWidth("100%");
        layout.setSpacing(true);
        
        Button upload = new Button("Upload");
        layout.addComponent(upload);
        
        ProgressBar bar = new ProgressBar();
        layout.addComponent(bar);
        bar.setWidth("100%");
        
        VerticalLayout dropZone = new VerticalLayout();
        layout.addComponent(dropZone);
        
        dropZone.setWidth("100px");
        dropZone.setHeight("100px");
        dropZone.addStyleName("orange");
        
        ImageResize resize = new ImageResize();
        
        resize.setEnabled(true);
        resize.setWidth(200);
        resize.setHeight(200);
        resize.setCrop(false);
        
        Plupload ex = new Plupload(upload);
        
        ex.addDropZone(dropZone)
        	.setImageResize(resize)
        	
        	.addFilesAddedListener(files -> {
        		ex.start(); 
        		ex.disableBrowse(true);
        	})
        	
        	.addFileUploadedListener((r, f) -> {
        		System.out.println(f.getAbsolutePath());
        		Notification.show(r.getName() + " Uploaded");
        	})
        	
        	.addFilter(new Filter("Images", "jpg,jpeg,png,gif"))
        	.addUploadCompleteListener(() -> ex.disableBrowse(false))
        	.addDropZoneEnterListener(() -> dropZone.addStyleName("green"))
        	.addDropZoneDropListener(() -> dropZone.removeStyleName("green"))
        	.addDropZoneLeaveListener(() -> dropZone.removeStyleName("green"))
        	.addUploadProgressListener(f -> bar.setValue(f.getPercent()/100F))
        	
        	.init();
    }
}
