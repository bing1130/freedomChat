package org.freedom.moniter.chat;

import java.util.HashSet;
import java.util.Set;

import org.red5.logging.Red5LoggerFactory;
import org.red5.net.websocket.WebSocketPlugin;
import org.red5.net.websocket.WebSocketScopeManager;
import org.red5.server.adapter.MultiThreadedApplicationAdapter;
import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.scope.IScope;
import org.red5.server.plugin.PluginRegistry;
import org.red5.server.stream.ClientBroadcastStream;
import org.red5.server.util.ScopeUtils;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Main application entry point for the chat application.
 * 
 * @author Paul Gregoire
 */
public class Application extends MultiThreadedApplicationAdapter implements ApplicationContextAware {

	private static Logger log = Red5LoggerFactory.getLogger(Application.class, "chat");
	
	private static Set<IConnection> Iconnections = new HashSet<IConnection>();

	@SuppressWarnings("unused")
	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public boolean appStart(IScope scope) {
		log.info("Chat starting scope path {}", scope.getPath());
		// add our application to enable websocket support
		WebSocketScopeManager manager = ((WebSocketPlugin) PluginRegistry.getPlugin("WebSocketPlugin")).getManager();
		manager.addApplication(scope);
		return super.appStart(scope);
	}

	@Override
	public void appStop(IScope scope) {
		log.info("Chat stopping");
		// remove our app
		WebSocketScopeManager manager = ((WebSocketPlugin) PluginRegistry.getPlugin("WebSocketPlugin")).getManager();
		manager.removeApplication(scope);
		super.appStop(scope);
	}
	
	@Override
	public boolean roomConnect(org.red5.server.api.IConnection conn,
            Object[] params){
		
		log.info("override roomConnect!");
		return super.roomConnect(conn,params);
	}
  
	@Override
	public boolean appConnect(org.red5.server.api.IConnection conn,
            Object[] params){
	    
		log.info("override appConnect! params size{} :" , params.length);
		for (int i=0; i<  params.length ; i++){
			log.info("override appConnect! params:" , params[i]);
			
		}		
		Iconnections.add(conn);
		return super.appConnect(conn,params);
		
	}
	@Override
	public boolean appJoin(IClient client, IScope app){ 
		log.info("override appJoin!");
		 Set<String>  streamnams = this.getBroadcastStreamNames(app);
		  log.info(" appJoin show streams : " + streamnams.toString());
	       return true; 
	} 

	@Override
	public void appDisconnect(org.red5.server.api.IConnection conn){
		log.info("override appDisconnect remove conn from Map!");
		Iconnections.remove(conn);
		super.appDisconnect(conn);
	}
	
	
    /**
     * Start recording the publishing stream for the specified
     * IConnection.
     * 
     * @param conn
     */
    public void recordShow(String streamname) {
    	log.info("Recording show Iconnections size(): " + Iconnections.size());
   	
    	for (IConnection conn : Iconnections) {
    		log.info("Recording show for: " + conn.getPath());
    		
    		log.info("Recording show for scope Name{} , scopepath{}: " + conn.getScope().getName(), conn.getScope().getPath());
    		if (conn.getScope().equals(scope)){
    			
    			log.info("Recording show we are in same scope! ");
    			
    		}else {
    			log.info("Recording show we are not in same scope! ");
    			
    		}
    		
    		String filename = String.valueOf(System.currentTimeMillis());
    		
    		    		
    		Set<String>  streamnams = this.getBroadcastStreamNames(conn.getScope());
    		  log.info("Recording show streams : " + streamnams.toString());
    		 if (streamnams.contains(streamname)){
    			 // Get a reference to the current broadcast stream.
     	        ClientBroadcastStream stream = (ClientBroadcastStream) this.getBroadcastStream(
     	                conn.getScope(), streamname);
     	        
     	        
     	        try {
     	        	
     	        	if (stream == null){
     	        		log.info("stream is null for: " + conn.getPath());
     	        	} else {
     	        		 // Save the stream to disk.
     	                stream.saveAs(filename, false);
     	        	}
     	           
     	        } catch (Exception e) {
     	            log.error("Error while saving stream: " + filename, e);
     	        }  
    			 
    		 }    	         		
    	}
        
    }
 
    /**
     * Stops recording the publishing stream for the specified
     * IConnection.
     * 
     * @param conn
     */
    public void stopRecordingShow(String streamname) {
    	
    	for (IConnection conn : Iconnections) {
    		log.info("stopRecordingShow  for: " + conn.getPath());
    		
    		log.info("stopRecordingShow  for scope Name{} , scopepath{}: " + conn.getScope().getName(), conn.getScope().getPath());
    		if (conn.getScope().equals(scope)){
    			
    			log.info("stopRecordingShow  we are in same scope! ");
    			
    		}    		    		
    		Set<String>  streamnams = this.getBroadcastStreamNames(conn.getScope());
    		  log.info("stopRecordingShow streams : " + streamnams.toString());
    		 if (streamnams.contains(streamname)){
    			 log.info("Stop recording show for: " + conn.getScope().getContextPath());
    		        // Get a reference to the current broadcast stream.
    		        ClientBroadcastStream stream = (ClientBroadcastStream) this.getBroadcastStream(
    		                conn.getScope(), streamname);
    		        // Stop recording.
    		        stream.stopRecording();
    			 
    		 }
        }       
    }
}
