package org.tensorflow.lite.examples.detection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import org.tensorflow.lite.examples.detection.tflite.Classifier;
import org.tensorflow.lite.examples.detection.tflite.TFLiteObjectDetectionAPIModel;
import org.tensorflow.lite.examples.detection.env.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;


public class DirectoryProcess extends AppCompatActivity {

    private static final Logger LOGGER = new Logger();
    private Classifier detector;
    private static final int TF_OD_API_INPUT_SIZE = 500;
    private static final int INPUT_SIZE = 500;
    private static final boolean TF_OD_API_IS_QUANTIZED = true;
    private static final String TF_OD_API_MODEL_FILE = "file:///android_asset/frozen_inference_graph.pb";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labels.txt";
    int cropSize = TF_OD_API_INPUT_SIZE;
    private String dirPath = "";
    Handler mHandler;
    ProgressBar progressBar;
    File[] allFiles;
    //String imagesList[] = new String[]{};
    //String resultsList[] = new String[]{};
    //final ArrayList<String> imagesList = new ArrayList<String>();
    //final ArrayList<String> resultsList = new ArrayList<String>();
    String[][] results;



    TextView totalImagesCount, processProgress, processEta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory_process);
        totalImagesCount = findViewById(R.id.totalImagesCount);
        //processProgress = findViewById(R.id.processProgress);
        progressBar = findViewById(R.id.progressBar);
        mHandler = new Handler();
        processEta = findViewById(R.id.processETA);
        String res1 = getIntent().getExtras().getString("imageType");


        if(res1.equals("directory")){
            try{
                Intent intent = getIntent();
                dirPath = intent.getStringExtra("directoryPath");
                File directory = new File(dirPath);
                allFiles = directory.listFiles();
                Log.d("Files", "Size: "+ allFiles.length);
                int counter = 0;
                for (int i = 0; i < allFiles.length; i++) {
                    String[] index = allFiles[i].getName().split(Pattern.quote("."));

                    String ext = index[index.length - 1].toLowerCase();
                    if (ext.equals("jpg") || ext.equals("png") || ext.equals("jpeg")) {
                        counter += 1;
                    }
                }

                results = new String[counter][2];

                totalImagesCount.setText(counter +" total images");




            }catch (Exception ex){
                Toast.makeText(this, "Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
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

    public int DetectImage(Bitmap toProcessImg){
        try {
            Bitmap mLog = Bitmap.createScaledBitmap(toProcessImg, 500, 500, true);
            //Toast.makeText(this, "height" + toProcessImg.getHeight(), Toast.LENGTH_SHORT).show();
            final List<Classifier.Recognition> results = detector.recognizeImage(mLog);
            //Toast.makeText(this, "Results:" + results.size(), Toast.LENGTH_SHORT).show();
            Bitmap mutableBitmap = mLog.copy(Bitmap.Config.ARGB_8888, true);
            float minimumConfidence = 0.5f;
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
                    canvas.drawRect(location, paint);


                    result.setLocation(location);
                    mappedRecognitions.add(result);
                }
            }
            canvas.drawText("Detected: " + i, 10, 10, paint);
            Log.d("d", String.valueOf(i));
            //Toast.makeText(this, i + ":pp", Toast.LENGTH_SHORT).show();
            return i;
        }catch (Exception ex){
            //Toast.makeText(this, ex.getMessage() + "error", Toast.LENGTH_SHORT).show();
            Log.d("Error:", ex.getMessage());
            return -1;
        }

    }

    public void processDirectoryImages(View v) {
        //LoadModel();
        new downloadASynTask().execute();
        /*
        String filePath = dirPath + File.separator + "output.csv";
        File f = new File(filePath);
        CSVWriter writer;
        FileWriter mFileWriter;
        try {
            if (f.exists()) {
                if (f.delete()) {
                    writer = new CSVWriter(new FileWriter(filePath));
                } else {
                    return;
                }
            } else {
                writer = new CSVWriter(new FileWriter(filePath));
            }
            for (int i = 0; i < allFiles.length; i++) {

                //float output = (i / allFiles.length) * 100;
                //final int progressBarVal1 = Math.round(output);
                //progressBar.setProgress(progressBarVal);
                //processProgress.setText(i + "/" + allFiles.length);
                */
        /*
                String[] index = allFiles[i].getName().split(Pattern.quote("."));

                String ext = index[index.length - 1].toLowerCase();
                Log.d("Extension", ext);
                //Toast.makeText(this, "Ext:" + ext, Toast.LENGTH_SHORT).show();
                //ext == "jpg" || ext == "png" || ext == "jpeg"
                if (ext.equals("jpg") || ext.equals("png") || ext.equals("jpeg")) {
                    Log.d("Files", "FileName:" + allFiles[i].getName());
                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    Bitmap toProcessImg = BitmapFactory.decodeFile(allFiles[i].getAbsolutePath(), bmOptions);
                    Log.d("ImgProp", "Height:" + toProcessImg.getHeight() + " - Width:" + toProcessImg.getWidth());
                    int res = DetectImage(toProcessImg);
                    Log.d("Result", "Res:" + res);
                    if (res != -1) {
                        //progressBar.setProgress(progressBarVal1);
                        processProgress.setText("working");
                        //Toast.makeText(this, allFiles[i].getName() + " : " + res, Toast.LENGTH_SHORT).show();
                        String[] data = {allFiles[i].getName(), Integer.toString(res)};
                        writer.writeNext(data);
                    }
                }

            }
            writer.close();
            processProgress.setText("Done");
        } catch (Exception ex) {
            //processProgress.setText(ex.getMessage());
            Toast.makeText(this, "Err:" + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        */
    }


    class  downloadASynTask extends AsyncTask<Void, String, String[][]>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    processEta.setText("INFO: Starting Processing");
                }

            });


        }
        @Override
        protected String[][] doInBackground(Void... arg0) {
            //String[][] results = new String[8][8];
            publishProgress("LOADING_MODEL");
            onProgressUpdate("LOADING_MODEL");

            LoadModel();
            publishProgress("MODEL_LOADED");
            onProgressUpdate("MODEL_LOADED");
            int g = 0;
            for (int i = 0; i < allFiles.length; i++) {
                int k = i;
                k += 1;
                float output = (k * 100)  / allFiles.length;

                int progressBarVal1 = Math.round(output);

                //Log.d("Progress 1:", String.valueOf(progressBarVal1));
                String progressResult = progressBarVal1 + "-" + k;
                publishProgress(progressResult);
                onProgressUpdate(progressResult);
                String[] index = allFiles[i].getName().split(Pattern.quote("."));

                String ext = index[index.length - 1].toLowerCase();
                if (ext.equals("jpg") || ext.equals("png") || ext.equals("jpeg")) {
                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    Bitmap toProcessImg = BitmapFactory.decodeFile(allFiles[i].getAbsolutePath(), bmOptions);
                    Log.d("ImgProp", "Height:" + toProcessImg.getHeight() + " - Width:" + toProcessImg.getWidth());
                    int res = DetectImage(toProcessImg);
                    Log.d("Res: ", String.valueOf(res));
                    if(res != -1){
                        results[g][0] = allFiles[i].getName();
                        results[g][1] = Integer.toString(res);

                        //Log.d("ND", String.valueOf(results.length));
                        g += 1;
                        //Log.d("REs 1", results[g][0]);
                    }
                }
                //int res = DetectImage(toProcessImg);
            }
            //Log.d("REs 1", results[0][0]);
            return results;
        }

        private void onProgressUpdate(String progressResult) {
            //Log.d("Progress: ", String.valueOf(i));
            if(progressResult == "LOADING_MODEL"){
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        processEta.setText("INFO: Loading Model");
                    }
                });
            }

            else if(progressResult == "MODEL_LOADED"){
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        processEta.setText("INFO: Detecting");
                    }
                });
            }else {
                String[] index = progressResult.split(Pattern.quote("-"));
                Log.d("Progress", progressResult);
                progressBar.setProgress(Integer.parseInt(index[0]));
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        TextView processProgress1 = findViewById(R.id.processProgress);
                        processProgress1.setText(index[0] + "%");
                        TextView progressImgs = findViewById(R.id.processImgsView);
                        progressImgs.setText(index[1] + "/" + allFiles.length);

                    }
                });
            }

        }

        @Override
        protected void onPostExecute(String[][] results)
        {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        processEta.setText("INFO: Saving Excel File");
                    }
                });

            String filePath = dirPath + File.separator + "output.csv";
            File f = new File(filePath);
            CSVWriter writer;
            FileWriter mFileWriter;
            try {
                if (f.exists()) {
                    if (f.delete()) {
                        writer = new CSVWriter(new FileWriter(filePath));
                    } else {
                        return;
                    }
                } else {
                    writer = new CSVWriter(new FileWriter(filePath));
                }
                //Log.d("Results length:", String.valueOf(results.length));

                for (int i = 0; i < results.length; i++) {
                    //Log.d("val", results[i][0]);
                    String[] data = {results[i][0], results[i][1]};
                    writer.writeNext(data);
                }
                writer.close();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        processEta.setText("INFO: Saving Excel Done\nSaved to Current Photos directory");
                        processEta.setTextColor(getResources().getColor(R.color.color_one));
                    }
                });
            }catch (Exception ex){

            }
        }
    }

}
