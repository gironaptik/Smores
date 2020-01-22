package smartspace.com;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.CreateCollectionRequest;
import com.amazonaws.services.rekognition.model.CreateCollectionResult;
import com.amazonaws.services.rekognition.model.DeleteFacesRequest;
import com.amazonaws.services.rekognition.model.DeleteFacesResult;
import com.amazonaws.services.rekognition.model.FaceMatch;
import com.amazonaws.services.rekognition.model.FaceRecord;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.IndexFacesRequest;
import com.amazonaws.services.rekognition.model.IndexFacesResult;
import com.amazonaws.services.rekognition.model.QualityFilter;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.rekognition.model.SearchFacesByImageRequest;
import com.amazonaws.services.rekognition.model.SearchFacesByImageResult;
import com.amazonaws.services.rekognition.model.UnindexedFace;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;

import smartspace.data.UserEntity;

import java.util.List;

public class AwsRekognition {

	final String collectionId1 = "smores-clients";
	final String collectionId2 = "smores-logedIn";
	final String bucket = "smores-users";

	BasicAWSCredentials awsCredentials = new BasicAWSCredentials(System.getenv("AWS_KEY"), System.getenv("AWS_SECRET"));
	AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard()
			.withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).withRegion(Regions.US_WEST_2).build();

	public void AddFacesToCollection(UserEntity user, int status) {

		String collectionId;
		collectionId = (status == 1) ? collectionId1 : collectionId2;
		String photo = user.getUserEmail();
		photo = photo.replace("@", "_");
		Image image = new Image().withS3Object(new S3Object().withBucket(bucket).withName(photo + ".png"));
		IndexFacesRequest indexFacesRequest = new IndexFacesRequest().withImage(image)
				.withQualityFilter(QualityFilter.AUTO).withMaxFaces(1).withCollectionId(collectionId)
				.withExternalImageId(photo + ".png").withDetectionAttributes("DEFAULT");

		rekognitionClient.indexFaces(indexFacesRequest);

	}

	public void DeleteFacesFromCollection(UserEntity user) {
		String photo = user.getUserEmail();
		photo = photo.replace("@", "_");

		Image image = new Image().withS3Object(new S3Object().withBucket(bucket).withName(photo + ".png"));

		SearchFacesByImageRequest searchFacesByImageRequest = new SearchFacesByImageRequest()
				.withCollectionId(collectionId2).withImage(image).withFaceMatchThreshold(90F).withMaxFaces(1);

		SearchFacesByImageResult searchFacesByImageResult = rekognitionClient
				.searchFacesByImage(searchFacesByImageRequest);

		DeleteFacesRequest deleteFacesRequest = new DeleteFacesRequest().withCollectionId(collectionId2)
				.withFaceIds(searchFacesByImageResult.getFaceMatches().get(0).getFace().getFaceId());

		rekognitionClient.deleteFaces(deleteFacesRequest);

	}
}
