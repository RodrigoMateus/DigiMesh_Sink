package com.maykot.mainApp;

import com.digi.xbee.api.listeners.IExplicitDataReceiveListener;
import com.digi.xbee.api.models.ExplicitXBeeMessage;

public class ExplicitDataReceiveListener implements IExplicitDataReceiveListener {

	TreatRequest treatRequest = new TreatRequest();

	@Override
	public void explicitDataReceived(ExplicitXBeeMessage explicitXBeeMessage) {
		treatRequest.process(explicitXBeeMessage);
	}
	
}
