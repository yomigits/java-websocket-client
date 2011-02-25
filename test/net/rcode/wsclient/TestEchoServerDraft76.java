package net.rcode.wsclient;

import java.util.ArrayList;
import java.util.List;

import net.rcode.wsclient.WebSocket.Event;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestEchoServerDraft76 {

	@Test
	public void testForSmoke() throws Exception {
		final List<String> messages=new ArrayList<String>();
		
		WebSocket ws=new WebSocket("ws://echo.websocket.org");
		ws.addRequestHeader("Origin", "http://websocket.org");
		ws.addListener(new WebSocket.EventListener() {
			@Override
			public void handleEvent(Event event) {
				if (event.getType()==WebSocket.EVENT_MESSAGE) {
					Message msg=event.getMessage();
					if (msg.isText()) {
						synchronized (messages) {
							messages.add(msg.getMessageText());
							messages.notify();
						}
					}
				} else {
					System.out.println("EVENT: " + event);
				}
			}
		});
		ws.start();
		
		ws.send("Message 1");
		ws.send("Message 2");
		
		synchronized (messages) {
			while (messages.size()<2 && ws.getReadyState()!=WebSocket.CLOSED) {
				messages.wait();
			}
		}
		
		// Should still be open at this point
		assertEquals(WebSocket.OPEN, ws.getReadyState());
		assertEquals("Message 1", messages.get(0));
		assertEquals("Message 2", messages.get(1));
		
		// Start close
		ws.close();
		
		ws.waitForReadyState(WebSocket.CLOSED);
		Thread.sleep(1000);
	}
}