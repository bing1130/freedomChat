package org.freedom.moniter.chat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;

import java.util.Set;


import org.freedom.moniter.bean.Endport;
import org.red5.logging.Red5LoggerFactory;
import org.red5.net.websocket.Constants;
import org.red5.net.websocket.WebSocketConnection;
import org.red5.net.websocket.listener.WebSocketDataListener;
import org.red5.net.websocket.model.MessageType;
import org.red5.net.websocket.model.WSMessage;
import org.red5.server.api.IConnection;
import org.red5.server.api.Red5;
import org.red5.server.api.scope.IScope;
import org.red5.server.stream.ClientBroadcastStream;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Handler / router for chat data.
 * 
 * @author Paul Gregoire
 */
public class WebSocketChatDataListener extends WebSocketDataListener {

	private static final Logger log = Red5LoggerFactory.getLogger(WebSocketChatDataListener.class, "chat");

	{
		setProtocol("chat");
	}

	private Router router;
	private final static HashMap< Long, Endport> endportMap = new HashMap<>();

	 
	 
	private Set<WebSocketConnection> connections = new HashSet<WebSocketConnection>();
	
	@Override
	public void onWSConnect(WebSocketConnection conn) {
		log.info("Connect: {}", conn);
	
		if (conn.getHeaders().containsKey(Constants.WS_HEADER_PROTOCOL)) {
			String protocol = (String) conn.getHeaders().get(Constants.WS_HEADER_PROTOCOL);
			if (protocol.indexOf("chat") != -1) {
				log.debug("Chat enabled");
			} else {
				log.info("Chat is not in the connections protocol list");
			}
		}
		log.info("send connId to client , connid{}", conn.getId() );
		// send its unique ID to the client (JSON)
		connections.add(conn);
		String msg = String.format("{\"msg\": \"connId\", \"connId\": \"%s\"}",
        		conn.getId());
		log.info("begin send connId to client , connid{} . msg {}", conn.getId() , msg);

        //sendClient(conn.getId(), msg);
      
		
	}

	@Override
	public void onWSDisconnect(WebSocketConnection conn) {
		log.info("Disconnect: {}", conn);
		connections.remove(conn);
		endportMap.remove(String.valueOf(conn.getId()));
		log.info("Remove connection from shareObject! ");
		router.route(conn.getPath(), endportMap);
	}

	@Override
	public void onWSMessage(WSMessage message) {
		// if its protocol doesn't match then skip the message
		if (!protocol.equals(message.getConnection().getProtocol())) {
			log.debug("Skipping message due to protocol mismatch");
			return;
		}
		// ignore ping and pong
		if (message.getMessageType() == MessageType.PING || message.getMessageType() == MessageType.PONG) {
			return;
		}
		// close if we get a close
		if (message.getMessageType() == MessageType.CLOSE) {
			message.getConnection().close();
			return;
		}
		// get the connection path for routing
		String path = message.getConnection().getPath();
		log.info("WebSocket connection path: {} ", path);
		// assume we have text
		String msg = new String(message.getPayload().array()).trim();
		log.info("onWSMessage: {}\n{}", msg, message.getConnection());
		// do a quick hacky json check
		if (msg.indexOf('{') != -1 && msg.indexOf(':') != -1) {
			log.info("JSON encoded text message");
			// channelName == roomid in most cases
			
			try {

                //TDODO handle {\"endportId\": \"cjId\", \"endportDesc\": \cjdescription\", "connId":connId}
	
				ObjectMapper objectMapper = new ObjectMapper();
				Endport endport = objectMapper.readValue(msg, Endport.class);
				endportMap.put(endport.getConnId(), endport);
				router.route(path, endportMap);
		
				sendClient(endport.getConnId(), msg);
				Application app = new Application();
				app.recordShow(endport.getEndportId());
				
	
				
				
				
				
				// send to all websocket connections matching this connections path
				//sendToAll(path, msg);
				// send to the shared object matching this connections path
				//router.route(path, msg);
			}catch (JsonParseException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				log.info("JsonParseException did not parse for message {}", msg);
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				log.info("JsonMappingException did not map for message {}", msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				log.info("IOException  for message {}", msg);
			}
		} else {
			log.info("Standard text message");
			// send to all websocket connections matching this connections path
			sendToAll(path, msg);
			// send to the shared object matching this connections path
			
			if (router == null){
				msg = msg + "router is null!";
			}else {
				msg = msg + "router is start";
				router.route(path, msg);
			}
			
		}
	}

	/**
	 * Send message to all connected connections.
	 * 
	 * @param path
	 * @param msg
	 */
	public void sendToAll(String path, String message) {			
		for (WebSocketConnection conn : connections) {
			if (path.equals(conn.getPath())) {
				try {
					conn.send(message);
				} catch (UnsupportedEncodingException e) {
				}
			} else {
				log.trace("Path did not match for message {} != {}", path, conn.getPath());
			}
		}
	}

	public void setRouter(Router router) {
		this.router = router;
		this.router.setWsListener(this);
	}
   
	
	private void sendClient(long connId, String message) {
		for (WebSocketConnection conn : connections) {
			if (connId ==conn.getId()) {
				try {
					conn.send(message);
				} catch (UnsupportedEncodingException e) {
				}
			} else {
				log.info("connection {} did not exist for message {}", connId, message);
			}
		}
		
    }
	
	
	
	
}
