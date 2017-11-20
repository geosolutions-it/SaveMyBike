'use strict';

const AWS = require('aws-sdk');
AWS.config.update({region: 'eu-west-1'});
const S3 = new AWS.S3();

console.log('Loading function');

exports.handler = (event, context, callback) => {
    console.log('Received event:', JSON.stringify(event, null, 2));
    const message = event.Records[0].Sns.Message;
    console.log('From SNS:', message);
    var msg;
    try {
        msg = JSON.parse(message);
    } catch(e) {
        callback(e, message);
    }
    
    if (msg && msg.Records){
        console.log('RECORDS: '+ msg.Records.length);
        const sns = new AWS.SNS();

        sns.publish({
            Message: 'Found '+msg.Records.length+ ' records' ,
            TopicArn: 'arn:aws:sns:eu-west-1:227022658256:lambda_match_found'
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
   
	const { Client } = require('pg')
	const client = new Client()

	client.connect()

	client.query('SELECT $1::text as message', ['Hello world!'], (err, res) => {
	  console.log(err ? err.stack : res.rows[0].message) // Hello World!
	  client.end()
	})
  

    
    callback(null, message);
};
