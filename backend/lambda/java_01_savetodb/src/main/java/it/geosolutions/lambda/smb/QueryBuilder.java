package it.geosolutions.lambda.smb;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.amazonaws.services.lambda.runtime.Context;

public class QueryBuilder {

	public Set<String> queries;

	public Map<String, String> currentQuery;
	protected Context context;

	public String[] headers;

	public QueryBuilder(Context context) {
		super();
		this.context = context;
		this.queries = new HashSet<String>();
		this.currentQuery = new HashMap<String, String>();
	}

	static public String cropLine(String line) {
		if (line != null) {
			if (line.lastIndexOf(",") == line.length() - 1) {
				return line.substring(0, line.length() - 1);
			}
			return line;
		}
		return "";
	}
	public StringBuilder sb = new StringBuilder();
			
	public QueryBuilder parseLine(String line) {

		if (line == null || line.isEmpty()) {
			context.getLogger().log("Got empty line, skipping it.");
			return this;
		}

		if (line.contains("sessionId")) {
			headers = cropLine(line).split(",");
			context.getLogger().log("Header: " + headers);
		} else {

			String[] values = cropLine(line).split(",");
			
			// Number of values must match number of headers
			if (values.length != headers.length) {
				context.getLogger().log("Line <-> Header");
				return this;
			}
			
			//Clear the buffer
			sb.setLength(0);
			
			sb.append("INSERT INTO testing (");
			for (int i = 0; i < headers.length; i++) {
				
				context.getLogger().log(headers[i] + " : "+ values[i]);
				currentQuery.put(headers[i], values[i]);
				if(!headers[i].equalsIgnoreCase("latitude") && !headers[i].equalsIgnoreCase("longitude")) {
					sb.append(headers[i]).append(",");
				}
				
			}
			sb.append("color,the_geom) VALUES (");
			
			for (int i = 0; i < headers.length; i++) {
				
				if(!headers[i].equalsIgnoreCase("latitude") && !headers[i].equalsIgnoreCase("longitude")) {
					sb.append(currentQuery.get(headers[i])).append(",");
				}
			}
			
			// TODO compute the color
			sb.append("128,st_setsrid(st_point(").append(currentQuery.get("longitude")).append(",").append(currentQuery.get("latitude")).append("), 4326));");
			
			queries.add(sb.toString());
			context.getLogger().log(sb.toString());
		}
		return this;
	}

}
