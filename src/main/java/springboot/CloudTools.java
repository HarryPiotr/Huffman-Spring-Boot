package springboot;

import com.google.cloud.storage.*;
import com.google.common.io.Files;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class CloudTools {

    public static void uploadFile(ByteArrayOutputStream arg, String filename) {

        Storage storage = StorageOptions.getDefaultInstance().getService();
        BlobId blobId = BlobId.of("eu.artifacts.secret-walker-295314.appspot.com", "tree_graphs/" + filename);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/png").build();
        Blob b = storage.create(blobInfo, arg.toByteArray());

    }
}
