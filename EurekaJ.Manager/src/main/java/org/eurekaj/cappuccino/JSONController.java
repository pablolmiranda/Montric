package org.eurekaj.cappuccino;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eurekaj.manager.berkeley.treemenu.TreeMenuNode;
import org.eurekaj.manager.json.BuildJsonObjectsUtil;
import org.eurekaj.manager.service.TreeMenuService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class JSONController {
	private TreeMenuService berkeleyTreeMenuService;

	public TreeMenuService getBerkeleyTreeMenuService() {
		return berkeleyTreeMenuService;
	}

	public void setBerkeleyTreeMenuService(TreeMenuService berkeleyTreeMenuService) {
		this.berkeleyTreeMenuService = berkeleyTreeMenuService;
	}

	@RequestMapping(value = "/jsonController.capp", method = RequestMethod.POST)
	public void getJsonData(HttpServletRequest request, HttpServletResponse response) throws IOException, JSONException {
		JSONObject jsonResponse = new JSONObject();

		JSONObject jsonObject;
		jsonObject = extractRequestJSONContents(request);
		System.out.println("Accepted JSON: \n" + jsonObject);
		if (jsonObject.has("getInstrumentationMenu")) {
			String menuId = jsonObject.getString("getInstrumentationMenu");
			jsonResponse = BuildJsonObjectsUtil.buildTreeTypeMenuJsonObject(menuId, berkeleyTreeMenuService.getTreeMenu(), 0, 15);
			//jsonResponse = BuildJsonObjectsUtil.buildTreeMenuJsonObject(berkeleyTreeMenuService.getTreeMenu());
			System.out.println("Got Tree Type Menu:\n" + jsonResponse.toString(3));
		}
		
		if (jsonObject.has("getInstrumentationTree")) {
			JSONObject keyObject = jsonObject.getJSONObject("getInstrumentationTree");
			String menuId = keyObject.getString("id");
			String path = keyObject.getString("path");
			
			List<TreeMenuNode> menuList = new ArrayList<TreeMenuNode>();
			for (TreeMenuNode node : berkeleyTreeMenuService.getTreeMenu()) {
				if (node.getGuiPath().startsWith(path)) {
					menuList.add(new TreeMenuNode(node.getGuiPath().substring(path.length()+1), node.getNodeLive()));
				}
			}
			jsonResponse = BuildJsonObjectsUtil.buildTreeTypeMenuJsonObject(menuId, menuList, 0, 15);
			//jsonResponse = BuildJsonObjectsUtil.buildTreeMenuJsonObject(berkeleyTreeMenuService.getTreeMenu());
			System.out.println("Got Tree Menu:\n" + jsonResponse.toString(3));
		}
		
		if (jsonObject.has("getInstrumentationChartList")) {
			JSONObject keyObject = jsonObject.getJSONObject("getInstrumentationChartList");
			String listId = keyObject.getString("id");
			String path = keyObject.getString("path");
			
			List<TreeMenuNode> leafList = new ArrayList<TreeMenuNode>();
			for (TreeMenuNode node : berkeleyTreeMenuService.getTreeMenu()) {
				if (node.getGuiPath().startsWith(path)) {
					leafList.add(new TreeMenuNode(node.getGuiPath(), node.getNodeLive()));
				}
			}
			
			jsonResponse = BuildJsonObjectsUtil.buildLeafNodeList(listId, path, leafList);
			System.out.println("Got Chart List:\n" + jsonResponse.toString(3));
			
		}
		
		PrintWriter writer = response.getWriter();
		writer.write(jsonResponse.toString());
		response.flushBuffer();
	}

	private JSONObject extractRequestJSONContents(HttpServletRequest request) throws IOException, JSONException {
		JSONObject jsonRequestObject = new JSONObject();

		InputStream in = request.getInputStream();

		BufferedReader r = new BufferedReader(new InputStreamReader(in));

		int numChars = 0;
		String contents = "";
		char[] buffer = new char[25];
		while ((numChars = r.read(buffer)) > 0) {
			contents += new String(buffer);
			buffer = new char[25];
		}

		if (contents.length() > 2) {
			jsonRequestObject = new JSONObject(contents);
		}

		return jsonRequestObject;
	}

}
