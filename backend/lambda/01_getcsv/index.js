'use strict';

const AWS = require('aws-sdk');
const S3 = new AWS.S3();

console.log('Loading function');

exports.handler = (event, context, callback) => {
    console.log('Received event:', JSON.stringify(event, null, 2));

    if (event && event.requestContext){
        
        const sns = new AWS.SNS();

        sns.publish({
            Message: JSON.stringify(event.requestContext, null, 2) ,
            TopicArn: 'arn:aws:sns:us-west-2:227022658256:lambda_match_found'
        }, function(err, data) {
            if (err) {
                console.log(err.stack);
                return;
            }
            console.log('push sent');
            console.log(data);
        });
        
    }
    /*
    var params = {
      Bucket: "examplebucket", 
      Key: "HappyFace.jpg"
     };
    s3.getObject(params, function(err, data) {
   if (err) console.log(err, err.stack); // an error occurred
   else     console.log(data);           // successful response

   data = {
    AcceptRanges: "bytes", 
    ContentLength: 3191, 
    ContentType: "image/jpeg", 
    ETag: "\"6805f2cfc46c0f04559748bb039d69ae\"", 
    LastModified: <Date Representation>, 
    Metadata: {
    }, 
    TagCount: 2, 
    VersionId: "null"
   }
   });
   */   
   
   /*
	const { Client } = require('pg')
	const client = new Client()
	
	//console.log("Connecting to DB")
	client.connect()

    //console.log("Querying...")
	client.query('SELECT $1::text as message', ['Hello world!'], (err, res) => {
	  console.log(err ? err.stack : res.rows[0].message) // Hello World!
	  client.end()
	})
	*/
    
    callback(null, event);
};
