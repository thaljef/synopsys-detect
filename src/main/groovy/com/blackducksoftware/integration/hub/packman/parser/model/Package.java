package com.blackducksoftware.integration.hub.packman.parser.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

public class Package {
	public String name;
	public String version;
	public List<Package> dependencies = new ArrayList<Package>();

	public Package() {
	}

	public Package(String packageName, String packageVersion) {
		this.name = packageName;
		this.version = packageVersion;
	}

	@Override
	public String toString() {
		Gson gson = new Gson();
		String json = gson.toJson(this);
		return json;
	}
}
