package com.routegis.applications.datachoose;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import core.routegis.engine.layers.RenderableLayer;


public interface loaderface {
	
	public void fetchURI();
	
	public void analysedata(String uri, String cachefile);
	
	public void cleardata();
	
	public void streamCopy(InputStream in, OutputStream out) throws IOException;
	
	public RenderableLayer getRenderableLayer(RenderableLayer rLayer);

}
