package smartspace.com;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.DeleteFacesRequest;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.IndexFacesRequest;
import com.amazonaws.services.rekognition.model.QualityFilter;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.rekognition.model.SearchFacesByImageRequest;
import com.amazonaws.services.rekognition.model.SearchFacesByImageResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import smartspace.data.UserEntity;

public class AwsRekognition {

	final String collectionId1 = "smores-clients";
	final String collectionId2 = "smores-logedIn";
	final String usersBucket = "smores-users";
	final String recommendationBucketString = "smores-recommendation-engine";
	final String key_name = "trx_data.csv";

	BasicAWSCredentials awsCredentials = new BasicAWSCredentials(System.getenv("AWS_KEY"),
			System.getenv("AWS_SECRET"));

	AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard()
			.withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).withRegion(Regions.US_WEST_2).build();

	public void AddFacesToCollection(UserEntity user, int status) {
		String collectionId;
		collectionId = (status == 1) ? collectionId1 : collectionId2;
		String photo = user.getUserEmail();
		photo = photo.replace("@", "_");
		Image image = new Image().withS3Object(new S3Object().withBucket(usersBucket).withName(photo + ".png"));
		IndexFacesRequest indexFacesRequest = new IndexFacesRequest().withImage(image)
				.withQualityFilter(QualityFilter.AUTO).withMaxFaces(1).withCollectionId(collectionId)
				.withExternalImageId(photo + ".png").withDetectionAttributes("DEFAULT");
		rekognitionClient.indexFaces(indexFacesRequest);
	}

	public void DeleteFacesFromCollection(UserEntity user) {
		String photo = user.getUserEmail();
		photo = photo.replace("@", "_");
		Image image = new Image().withS3Object(new S3Object().withBucket(usersBucket).withName(photo + ".png"));
		SearchFacesByImageRequest searchFacesByImageRequest = new SearchFacesByImageRequest()
				.withCollectionId(collectionId2).withImage(image).withFaceMatchThreshold(90F).withMaxFaces(1);
		SearchFacesByImageResult searchFacesByImageResult = rekognitionClient
				.searchFacesByImage(searchFacesByImageRequest);
		DeleteFacesRequest deleteFacesRequest = new DeleteFacesRequest().withCollectionId(collectionId2)
				.withFaceIds(searchFacesByImageResult.getFaceMatches().get(0).getFace().getFaceId());
		rekognitionClient.deleteFaces(deleteFacesRequest);

	}
	
	public void downloadCSV(String file) {
		final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).withRegion(Regions.US_WEST_2).build();
		try {
			System.out.println("Downloading" + file + "CSV File....");
		    com.amazonaws.services.s3.model.S3Object o = s3.getObject(recommendationBucketString, file);
		    S3ObjectInputStream s3is = o.getObjectContent();
		    FileOutputStream fos = new FileOutputStream(new File(file));
		    byte[] read_buf = new byte[1024];
		    int read_len = 0;
		    while ((read_len = s3is.read(read_buf)) > 0) {
		        fos.write(read_buf, 0, read_len);
		    }
		    s3is.close();
		    fos.close();
		} catch (AmazonServiceException e) {
		    System.err.println("Failed to download! "+ e.getErrorMessage());
		    System.exit(1);
		} catch (FileNotFoundException e) {
		    System.err.println("Failed to download! "+ e.getMessage());
		    System.exit(1);
		} catch (IOException e) {
		    System.err.println("Failed to download! "+ e.getMessage());
		    System.exit(1);
		}
	}
	
	public void uploadCSV(String file) {
	     final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).withRegion(Regions.US_WEST_2).build();
	     try {
	         s3.putObject(recommendationBucketString, file, new File(file));
	     } catch (AmazonServiceException e) {
	         System.err.println(e.getErrorMessage());
	         System.exit(1);
	     }
	}
}
