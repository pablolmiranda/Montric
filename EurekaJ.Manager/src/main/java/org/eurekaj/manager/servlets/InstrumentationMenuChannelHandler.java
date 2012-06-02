/**
    EurekaJ Profiler - http://eurekaj.haagen.name
    
    Copyright (C) 2010-2011 Joachim Haagen Skeie

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package org.eurekaj.manager.servlets;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.eurekaj.api.datatypes.TreeMenuNode;
import org.eurekaj.manager.json.BuildJsonObjectsUtil;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class InstrumentationMenuChannelHandler extends EurekaJGenericChannelHandler {
	private static final Logger log = Logger.getLogger(InstrumentationMenuChannelHandler.class);
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		String jsonResponse = "";
        
        try {
            JSONObject jsonObject = BuildJsonObjectsUtil.extractJsonContents(getHttpMessageContent(e));
            log.debug("Accepted JSON: \n" + jsonObject);

            jsonResponse = buildInstrumentationMenu(jsonResponse, jsonObject);
            jsonResponse = buildInstrumentationMenuNode(jsonResponse,jsonObject);
            deleteInstrumentationMenuNode(jsonObject);
        } catch (JSONException jsonException) {
            throw new IOException("Unable to process JSON Request", jsonException);
        }

        if (jsonResponse.length() <= 2) {
            jsonResponse = "{}";
        }
        
        writeContentsToBuffer(ctx, jsonResponse);
	}
	
	private String buildInstrumentationMenuNode(String jsonResponse, JSONObject jsonObject) throws JSONException {
		if (jsonObject.has("getInstrumentationMenuNode")) {
		    String nodeId = jsonObject.getString("getInstrumentationMenuNode");
		    TreeMenuNode node = getBerkeleyTreeMenuService().getTreeMenu(nodeId);
		    jsonResponse = BuildJsonObjectsUtil.buildInstrumentationNode(node).toString();
		    log.debug("Got Node: \n" + jsonResponse);
		}
		return jsonResponse;
	}

	private String buildInstrumentationMenu(String jsonResponse, JSONObject jsonObject) throws JSONException {
		if (jsonObject.has("getInstrumentationMenu")) {
		    String menuId = jsonObject.getString("getInstrumentationMenu");
		    boolean includeCharts = jsonObject.has("includeCharts") && jsonObject.getBoolean("includeCharts");

		    String includeChartType = null;
		    if (jsonObject.has("nodeType")) {
		        includeChartType = jsonObject.getString("nodeType");
		    }
		    jsonResponse = BuildJsonObjectsUtil.buildTreeTypeMenuJsonObject(menuId,
		            getBerkeleyTreeMenuService().getTreeMenu(),
		            getBerkeleyTreeMenuService().getAlerts(),
		            getBerkeleyTreeMenuService().getGroupedStatistics(),
		            0, 15, includeCharts, includeChartType).toString();

		    log.debug("Got Tree Type Menu:\n" + jsonResponse);
		}
		return jsonResponse;
	}

	private void deleteInstrumentationMenuNode(JSONObject jsonObject) throws JSONException {
		if (jsonObject.has("deleteInstrumentationMenuNodes")) {
			JSONArray nodes = jsonObject.getJSONArray("deleteInstrumentationMenuNodes");
			for (int i = 0; i < nodes.length(); i++) {
				String guiPath = nodes.getString(i);
				getBerkeleyTreeMenuService().deleteTreeMenuNode(guiPath);
			}
			
		}
	}
}