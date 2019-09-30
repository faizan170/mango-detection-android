package org.tensorflow.lite.examples.detection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.tensorflow.lite.examples.detection.env.Logger;
import org.tensorflow.lite.examples.detection.tflite.Classifier;
import org.tensorflow.lite.examples.detection.tflite.TFLiteObjectDetectionAPIModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class ActivitySelectImage extends AppCompatActivity {

    private static final Logger LOGGER = new Logger();
    TextView progressBarHolderText, detectionCount;
    ImageView imgProcess;
    private Classifier detector;
    public int totalCount = 0;
    private FrameLayout progressBarHolder;
    public AlphaAnimation inAnimation;
    public AlphaAnimation outAnimation;
    private static final int TF_OD_API_INPUT_SIZE = 500;
    private static final int INPUT_SIZE = 500;
    private static final boolean TF_OD_API_IS_QUANTIZED = true;
    private static final String TF_OD_API_MODEL_FILE = "file:///android_asset/frozen_inference_graph.pb";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labels.txt";
    private Spinner spinner;
    int cropSize = TF_OD_API_INPUT_SIZE;
    double weights[] = new double[]{0.220, 0.265, 0.250, 0.550, 0.490, 0.290, 0.178, 0.530};
    Bitmap toProcessImg;
    Bitmap processedImg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_image);

        detectionCount = findViewById(R.id.detectionCount);
        imgProcess = findViewById(R.id.imgView);
        progressBarHolder = findViewById(R.id.progressBarHolder);
        progressBarHolderText = findViewById(R.id.progressBarHolderText);
        String res1 = getIntent().getExtras().getString("imageType");
        spinner = findViewById(R.id.spinner1);
        if(res1.equals("gallery")) {
            try {
                Bundle bd = getIntent().getExtras();
                Uri uri = bd.getParcelable("bitmapImage");
                Log.e("URI", uri.toString());
                try {
                    toProcessImg = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    imgProcess.setImageBitmap(toProcessImg);
                } catch (FileNotFoundException e) {
                    Toast.makeText(this, "File Not found exception", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                } catch (IOException e) {
                    Toast.makeText(this, "IO exception", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            } catch (Exception EX) {
                Toast.makeText(this, EX.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        else if(res1.equals("pCamera")) {
            try {
                Intent intent = getIntent();
                String imgPath = intent.getStringExtra("imgPath");
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                toProcessImg = BitmapFactory.decodeFile(imgPath,bmOptions);
                //imgProcess.setImageURI(Uri.parse(imgPath));
                imgProcess.setImageBitmap(toProcessImg);
                //setPic(currentPhotoPath);
            } catch (Exception EX) {
            }

        }


    }

    public void LoadModel(){
        try {
            detector =
                    TFLiteObjectDetectionAPIModel.create(
                            getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
            cropSize = TF_OD_API_INPUT_SIZE;
        } catch (final IOException e) {
            e.printStackTrace();
            LOGGER.e(e, "Exception initializing classifier!");
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }
    }

    public void startDetectionImage(View v) {
        //Toast.makeText(this, "" + spinner.getSelectedItemPosition(), Toast.LENGTH_SHORT).show();
        new downloadASynTask().execute();
        //detectionCount.setText("Detected: " + totalCount + " mangoes");

    }
    public int DetectImage(){
        //Toast.makeText(this, toProcessImg.getHeight() + ":" + toProcessImg.getWidth(), Toast.LENGTH_SHORT).show();
        try {
            //Toast.makeText(this, "Started Detection", Toast.LENGTH_SHORT).show();
            Bitmap croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888);
            Bitmap mLog = Bitmap.createScaledBitmap(toProcessImg, 500, 500, true);
            final List<Classifier.Recognition> results = detector.recognizeImage(mLog);
            //Toast.makeText(this, "Results:" + results.size(), Toast.LENGTH_SHORT).show();
            Bitmap mutableBitmap = mLog.copy(Bitmap.Config.ARGB_8888, true);
            float minimumConfidence = 0.35f;
            Bitmap cropCopyBitmap = Bitmap.createBitmap(mutableBitmap);

            final Canvas canvas = new Canvas(cropCopyBitmap);

            final Paint paint = new Paint();
            paint.setColor(Color.GREEN);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2.0f);

            final Paint paint2 = new Paint();
            paint2.setColor(Color.GREEN);
            paint2.setStyle(Paint.Style.STROKE);
            paint2.setStrokeWidth(4.0f);
            final List<Classifier.Recognition> mappedRecognitions =
                    new LinkedList<Classifier.Recognition>();
            int i = 0;
            for (final Classifier.Recognition result : results) {
                //i += 1;
                //Toast.makeText(this, i + ":P", Toast.LENGTH_SHORT).show();
                final RectF location = result.getLocation();

                if (location != null && result.getConfidence() >= minimumConfidence) {
                    i += 1;
                    //canvas.drawRect(location, paint);


                    result.setLocation(location);
                    mappedRecognitions.add(result);
                }
            }
            //canvas.drawText("Detected: " + i * 3, 10, 10, paint);
            //Log.d("Detected", String.valueOf(i));
            i = i * 3;
            totalCount = i;
            Log.d("Total Count:", String.valueOf(totalCount));
            //Toast.makeText(this, i + ":pp", Toast.LENGTH_SHORT).show();
            processedImg = cropCopyBitmap;
            return i;
        }catch (Exception ex){
            Log.d("Detection Exception:", ex.getMessage());
            //Toast.makeText(this, ex.getMessage() + "error", Toast.LENGTH_SHORT).show();
            return 0;
        }

    }




    class  downloadASynTask extends AsyncTask<Void, String, Integer>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            inAnimation = new AlphaAnimation(0f, 1f);
            inAnimation.setDuration(200);
            progressBarHolder.setAnimation(inAnimation);
            //progressBarHolderText.setText("Loading Model");
            progressBarHolder.setVisibility(View.VISIBLE);
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    progressBarHolderText.setText("Loading Model");
                    //detectionCount.setText("INFO: Starting Processing");
                }

            });


        }
        @Override
        protected Integer doInBackground(Void... arg0) {
            //String[][] results = new String[8][8];
            publishProgress("INFO: Loading Model");
            onProgressUpdate("INFO: Loading Model");
            LoadModel();

            publishProgress("INFO: Processing");
            onProgressUpdate("INFO: Processing");
            Integer res = DetectImage();


            Log.d("REs 1", String.valueOf(res));
            return res;
        }

        private void onProgressUpdate(String progressResult) {
            //Log.d("Progress: ", String.valueOf(i));

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        progressBarHolderText.setText(progressResult);

                        //detectionCount.setText(progressResult);
                    }
                });



        }

        @Override
        protected void onPostExecute(Integer results)
        {
            outAnimation = new AlphaAnimation(1f, 0f);
            outAnimation.setDuration(200);
            progressBarHolder.setAnimation(outAnimation);
            progressBarHolder.setVisibility(View.GONE);
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    imgProcess.setImageBitmap(processedImg);
                    int index = (int)spinner.getSelectedItemId();
                    detectionCount.setText("INFO: Detected " + totalCount + " Mangoes\n INFO: Total Weight: " + totalCount * weights[index] + " KGs");
                }
            });

        }
    }





}
