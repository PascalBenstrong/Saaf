package com.bitec.saafs.ui.fragment;

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bitec.saafs.R;
import com.bitec.saafs.models.Comment;
import com.bitec.saafs.models.Video;
import com.bitec.saafs.ui.activities.post.PostImage;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static com.bitec.saafs.adapters.UploadListAdapter.uploadedImagesUrl;
import static com.bitec.saafs.ui.activities.post.PostImage.canUpload;

public class VideoUploader extends Fragment {

    private View mView;

    private Button mSelectFileToUpload;
    private Button mPauseUpload;
    private Button mCancelUpload;
    private Switch mSwitch;

    private TextView mFilenameLabel;
    private TextView mSizeLabel;
    private TextView mPercentage;
    private ProgressBar mProgressBar;

    private static int PICK_VIDEO = 400;

    private StorageReference mStorageReference;
    private StorageTask mStoragetask;
    public FirebaseUser mCurrentUser;
    private FirebaseFirestore mFirestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_video_upload, container, false);

        mSelectFileToUpload = mView.findViewById(R.id.select_file_to_upload);
        mPauseUpload = mView.findViewById(R.id.pause_upload);
        mCancelUpload = mView.findViewById(R.id.cancel_upload);
        mSwitch = mView.findViewById(R.id.switch1);

        mFilenameLabel = mView.findViewById(R.id.filename_label);
        mProgressBar = mView.findViewById(R.id.upload_progress);
        mSizeLabel = mView.findViewById(R.id.data);
        mPercentage = mView.findViewById(R.id.progress_label);
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mFirestore = FirebaseFirestore.getInstance();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mStorageReference = FirebaseStorage.getInstance().getReference();

        mSelectFileToUpload.setOnClickListener(vw -> onSelectFileToUpload());
        mPauseUpload.setOnClickListener(vw -> onPauseUpload());
        mCancelUpload.setOnClickListener(vw -> onCancelUpload());

        mPauseUpload.setVisibility(View.GONE);
        mCancelUpload.setVisibility(View.GONE);

        mSwitch.setChecked(true);
        mSwitch.setOnClickListener(vw -> mSwitch.setText((mSwitch.isChecked() ? "Streams" : "Testimonial")));
    }

    private void onSelectFileToUpload() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"), PICK_VIDEO);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(mView.getContext(), "Please install a File Manager", Toast.LENGTH_SHORT).show();
        }
    }

    private void onPauseUpload() {
        String buttonText = mPauseUpload.getText().toString();
        if (buttonText.equals("Pause Upload")) {
            mStoragetask.pause();
            mPauseUpload.setText("Resume Upload");
        } else {
            mStoragetask.resume();
            mPauseUpload.setText("Pause Upload");
        }
    }

    private void onCancelUpload() {
        mStoragetask.cancel();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_VIDEO) {
            if (resultCode == RESULT_OK) {
                Uri fileUri = data.getData();

                String filename = getFilename(fileUri);

                Toast.makeText(mView.getContext(), "You have selected a file: ", Toast.LENGTH_SHORT).show();

                //Progress Naming //
                mFilenameLabel.setText(filename);

                final StorageReference videoRef = mStorageReference.child("videos/" + filename);
                mProgressBar.setProgress(0);
                mStoragetask = videoRef.putFile(fileUri);

                mPauseUpload.setVisibility(View.VISIBLE);
                mCancelUpload.setVisibility(View.VISIBLE);

                mStoragetask.addOnSuccessListener((OnSuccessListener<UploadTask.TaskSnapshot>) taskSnapshot -> {
                    // Get a URL to the uploaded content


                });

                mStoragetask.addOnCompleteListener((OnCompleteListener<UploadTask.TaskSnapshot>) taskSnapshot -> {

                    if (taskSnapshot.isSuccessful()) {
                        if (!mStoragetask.isCanceled() && mStoragetask.isComplete()) {

                            videoRef.getDownloadUrl().addOnSuccessListener(uri -> {

                                //upload;
                                mFirestore.collection("Users").document(mCurrentUser.getUid()).get().addOnSuccessListener(documentSnapshot -> {

                                    Map<String, Object> videoMap = new HashMap<>();
                                    videoMap.put("userId", Objects.requireNonNull(documentSnapshot.getString("id")));
                                    videoMap.put("name", Objects.requireNonNull(documentSnapshot.getString("name")));
                                    videoMap.put("timestamp", String.valueOf(System.currentTimeMillis()));
                                    videoMap.put("url", uri.toString());
                                    videoMap.put("type", "video");
                                    videoMap.put("description", filename);
                                    videoMap.put("postId", "");

                                    CollectionReference mCollectionRef = mFirestore.collection(mSwitch.getText().toString());

                                    mCollectionRef.add(videoMap).addOnSuccessListener(documentReference -> {
                                        {
                                            //Toast.makeText(getActivity(), "File Uploaded: " + documentReference.getId(), Toast.LENGTH_SHORT).show();

                                            documentReference.update("postId",documentReference.getId());

                                            Toast.makeText(getActivity(), "File Uploaded", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(e -> {
                                        Toast.makeText(getActivity(), "Error uploading video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.e("Error upload video", e.getMessage());
                                    });

                                }).addOnFailureListener(e -> Log.e("Error getuser", e.getMessage()));

                            });
                        }
                    } else {
                        Log.e("Error", "listen", taskSnapshot.getException());
                    }
                    mPauseUpload.setVisibility(View.GONE);
                    mCancelUpload.setVisibility(View.GONE);
                });

                mStoragetask.addOnFailureListener(exception -> {
                    // Handle unsuccessful uploads
                    Toast.makeText(mView.getContext(), "There is an error", Toast.LENGTH_SHORT).show();
                });

                mStoragetask.addOnProgressListener((OnProgressListener<UploadTask.TaskSnapshot>) taskSnapshot ->
                {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());

                    animateProgress(mProgressBar, (int) progress, mProgressBar.getProgress());

                    double transferred = taskSnapshot.getTotalByteCount() * progress / (100 * 1024 * 1024);
                    double total = taskSnapshot.getTotalByteCount() / (1024f * 1024);

                    String progressText = String.format(Locale.getDefault(), "%.1f/%.1f mb", transferred, total);
                    mPercentage.setText(String.format(Locale.getDefault(), "%.0f ", progress).concat("%"));

                    mSizeLabel.setText(progressText);
                });

                mStoragetask.addOnCanceledListener(() -> Toast.makeText(mView.getContext(), "Uploaded Canceled", Toast.LENGTH_SHORT).show());
            }
        }

    }

    private String getFilename(Uri fileUri) {
        String filename = "";

        String uriString = fileUri.toString();

        if (uriString.startsWith("content://")) {
            Cursor cursor = null;
            try {
                cursor = getActivity().getContentResolver().query(fileUri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    filename = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        } else if (uriString.startsWith("file://")) {
            filename = new File(uriString).getName();
        }

        return filename;

    }

    private void animateProgress(ProgressBar progressBar, int progress, int currentProgress) {
        ObjectAnimator animator = ObjectAnimator.ofInt(progressBar, "progress", currentProgress, progress);
        animator.setDuration((progress - currentProgress) * 1000 / (progressBar.getMax()));
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

}
