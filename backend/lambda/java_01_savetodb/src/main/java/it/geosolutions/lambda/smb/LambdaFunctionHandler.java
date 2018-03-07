package it.geosolutions.lambda.smb;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.S3Object;

public class LambdaFunctionHandler implements RequestHandler<SNSEvent, String> {
	static {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			System.err.println("PostgreSQL DataSource unable to load PostgreSQL JDBC Driver");			
		}
	}
	
    @Override
    public String handleRequest(SNSEvent event, Context context) {
        // context.getLogger().log("Received event: " + event);
        String message = event.getRecords().get(0).getSNS().getMessage();
        // context.getLogger().log("From SNS: " + message);
        S3EventNotification parsed = S3EventNotification.parseJson(message);
        String s3_bucket_name = parsed.getRecords().get(0).getS3().getBucket().getName();
        String s3_object_key = parsed.getRecords().get(0).getS3().getObject().getKey();
        
        context.getLogger().log(s3_bucket_name);
        context.getLogger().log(s3_object_key);

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.US_WEST_2)
                .build();
        
        S3Object obj = s3Client.getObject(s3_bucket_name, s3_object_key);
        
        context.getLogger().log("Size " + obj.getObjectMetadata().getContentLength());
        
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(obj.getObjectContent());
        try {
	        ZipEntry entry = zis.getNextEntry();
	
	        while(entry != null) {
	        	
	        	// TODO zip entry sanitizing checks
	        	
	            String fileName = entry.getName();
	            System.out.println("Extracting " + fileName + ", compressed: " + entry.getCompressedSize() + " bytes, extracted: " + entry.getSize() + " bytes");
	            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	            int len;
	            while ((len = zis.read(buffer)) > 0) {
	                outputStream.write(buffer, 0, len);
	            }
	            InputStream is = new ByteArrayInputStream(outputStream.toByteArray());
	            
	            BufferedReader in = new BufferedReader(new InputStreamReader(is));
	            String line;
	
	            QueryBuilder queries = new QueryBuilder(context);
	            while ((line = in.readLine()) != null) {
	            	context.getLogger().log(line);
	            	
	            	queries.parseLine(line);
	            }
	            
	            try {
	                Properties props = new Properties();
	            	props.setProperty("user",System.getenv("PGUSER"));
	            	props.setProperty("password",System.getenv("PGPASSWORD"));
	            	        	
	            	String url = "jdbc:postgresql://"+System.getenv("PGHOST")+":"+System.getenv("PGPORT")+"/"+System.getenv("PGDATABASE");

	                Connection conn = DriverManager.getConnection(url, props);
	                Statement stmt = conn.createStatement();
	                
	                Iterator<String> qI = queries.queries.iterator();
	                
	                while( qI.hasNext()) {
	                	stmt.executeUpdate(qI.next());
		                context.getLogger().log("Successfully executed query.\n");
	                }

	    		} catch (Exception e) {
	    			e.printStackTrace();
	    			context.getLogger().log("Caught exception: " + e.getMessage());
	    		}

	            
	            is.close();
	            outputStream.close();
	            entry = zis.getNextEntry();
	        }
	        zis.closeEntry();
			zis.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
		}
        
        try {
			obj.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        
        
        return s3_object_key;
    }
}
