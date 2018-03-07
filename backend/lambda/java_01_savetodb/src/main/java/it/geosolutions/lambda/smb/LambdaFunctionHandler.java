package it.geosolutions.lambda.smb;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.s3.event.S3EventNotification;

public class LambdaFunctionHandler implements RequestHandler<SNSEvent, String> {

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
        
        return s3_object_key;
    }
}
