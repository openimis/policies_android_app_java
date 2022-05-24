//Copyright (c) 2016-%CurrentYear% Swiss Agency for Development and Cooperation (SDC)
//
//The program users must agree to the following terms:
//
//Copyright notices
//This program is free software: you can redistribute it and/or modify it under the terms of the GNU AGPL v3 License as published by the 
//Free Software Foundation, version 3 of the License.
//This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU AGPL v3 License for more details www.gnu.org.
//
//Disclaimer of Warranty
//There is no warranty for the program, to the extent permitted by applicable law; except when otherwise stated in writing the copyright 
//holders and/or other parties provide the program "as is" without warranty of any kind, either expressed or implied, including, but not 
//limited to, the implied warranties of merchantability and fitness for a particular purpose. The entire risk as to the quality and 
//performance of the program is with you. Should the program prove defective, you assume the cost of all necessary servicing, repair or correction.
//
//Limitation of Liability 
//In no event unless required by applicable law or agreed to in writing will any copyright holder, or any other party who modifies and/or 
//conveys the program as permitted above, be liable to you for damages, including any general, special, incidental or consequential damages 
//arising out of the use or inability to use the program (including but not limited to loss of data or data being rendered inaccurate or losses 
//sustained by you or third parties or a failure of the program to operate with any other programs), even if such holder or other party has been 
//advised of the possibility of such damages.
//
//In case of dispute arising out or in relation to the use of the program, it is subject to the public law of Switzerland. The place of jurisdiction is Berne.

package org.openimis.imispolicies;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Xml;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imispolicies.tools.Log;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Feedback extends AppCompatActivity {
    private static final String LOG_TAG = "FEEDBACK";
    private Global global;

    private EditText etOfficer;
    private EditText etClaimCode;
    private EditText etCHFID;
    private RadioGroup rg1;
    private RadioGroup rg2;
    private RadioGroup rg3;
    private RadioGroup rg4;
    private RadioButton rbYes1;
    private RadioButton rbYes2;
    private RadioButton rbYes3;
    private RadioButton rbYes4;
    private RadioButton rbNo1;
    private RadioButton rbNo2;
    private RadioButton rbNo3;
    private RadioButton rbNo4;
    private RatingBar rb1;
    private ProgressDialog pd;
    private Button btnSubmit;

    private String ClaimUUID;
    private File FeedbackXML;
    private File FeedbackJSON;
    private String FileName;
    private String OfficerCode;
    private int msgType;
    private ClientAndroidInterface ca;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        global = (Global) getApplicationContext();
        setContentView(R.layout.feedback);

        ca = new ClientAndroidInterface(this);
        etOfficer = findViewById(R.id.etOfficer);
        etClaimCode = findViewById(R.id.etClaimCode);
        etCHFID = findViewById(R.id.etCHFID);
        rbYes1 = findViewById(R.id.rYes1);
        rbYes2 = findViewById(R.id.rYes2);
        rbYes3 = findViewById(R.id.rYes3);
        rbYes4 = findViewById(R.id.rYes4);
        rbNo1 = findViewById(R.id.rNo1);
        rbNo2 = findViewById(R.id.rNo2);
        rbNo3 = findViewById(R.id.rNo3);
        rbNo4 = findViewById(R.id.rNo4);
        btnSubmit = findViewById(R.id.btnSubmit);
        OfficerCode = getIntent().getStringExtra("OfficerCode");
        etOfficer.setText(OfficerCode);
        ClaimUUID = getIntent().getStringExtra("ClaimUUID");
        etClaimCode.setText(getIntent().getStringExtra("ClaimCode"));
        etCHFID.setText(getIntent().getStringExtra("CHFID"));

        btnSubmit.setOnClickListener(v -> {

            if (!isValidate()) return;
            pd = ProgressDialog.show(Feedback.this, "", getResources().getString(R.string.UploadingFeedback));
            final String[] feed = {null};

            new Thread(() -> {
                String Answers = Answers();
                try {
                    feed[0] = WriteJSON(String.valueOf(etOfficer.getText()), ClaimUUID, etCHFID.getText().toString(), Answers);
                    WriteXML(String.valueOf(etOfficer.getText()), ClaimUUID, etCHFID.getText().toString(), Answers);
                } catch (IllegalArgumentException | IllegalStateException | IOException e) {
                    e.printStackTrace();
                    return;
                }

                msgType = 3;

                runOnUiThread(() -> {
                    switch (msgType) {
                        case 1:
                            DeleteRow(ClaimUUID);
                            //ca.ShowDialog(getResources().getString(R.string.UploadedSuccessfully));
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.UploadedSuccessfully), Toast.LENGTH_LONG).show();
                            break;
                        case 2:
                            DeleteRow(ClaimUUID);
                            //ca. ShowDialog(getResources().getString(R.string.ServerRejected));
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.ServerRejected), Toast.LENGTH_LONG).show();
                            break;
                        case 3:
                            UpdateRow(ClaimUUID);
                            //ca. ShowDialog(getResources().getString(R.string.SavedOnSDCard));
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.SavedOnSDCard), Toast.LENGTH_LONG).show();
                            break;
                        case -1:
                            //ca. ShowDialog(getResources().getString(R.string.FeedBackNotUploaded));
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.FeedBackNotUploaded), Toast.LENGTH_LONG).show();
                            break;
                    }
                    finish();
                });
                pd.dismiss();
            }).start();
        });
    }

    private void DeleteRow(String ClaimUUID) {
        ca.CleanFeedBackTable(ClaimUUID);
    }

    private void UpdateRow(String ClaimUUID) {
        ca.UpdateFeedBack(ClaimUUID);
    }

    private void WriteXML(String Officer, String ClaimUUID, String CHFID, String Answers) throws IllegalArgumentException, IllegalStateException, IOException {
        File MyDir = new File(global.getSubdirectory("Feedback"));
        FileName = "Feedback_" + etClaimCode.getText() + ".xml";
        FeedbackXML = new File(MyDir, FileName);

        FileOutputStream fos = new FileOutputStream(FeedbackXML);
        XmlSerializer serializer = Xml.newSerializer();

        serializer.setOutput(fos, "UTF-8");
        serializer.startDocument(null, Boolean.TRUE);
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
        serializer.startTag(null, "feedback");

        serializer.startTag(null, "Officer");
        serializer.text(Officer);
        serializer.endTag(null, "Officer");

        serializer.startTag(null, "ClaimUUID");
        serializer.text(ClaimUUID);
        serializer.endTag(null, "ClaimUUID");

        serializer.startTag(null, "CHFID");
        serializer.text(CHFID);
        serializer.endTag(null, "CHFID");

        serializer.startTag(null, "Answers");
        serializer.text(Answers);
        serializer.endTag(null, "Answers");

        serializer.startTag(null, "Date");
        SimpleDateFormat formatter = AppInformation.DateTimeInfo.getDefaultDateFormatter();
        Calendar cal = Calendar.getInstance();
        String d = formatter.format(cal.getTime());
        serializer.text(d);
        serializer.endTag(null, "Date");

        serializer.endTag(null, "feedback");
        serializer.endDocument();
        serializer.flush();
        fos.close();
    }

    private String WriteJSON(String Officer, String ClaimUUID, String CHFID, String Answers) {
        File MyDir = new File(global.getSubdirectory("Feedback"));
        SimpleDateFormat format = AppInformation.DateTimeInfo.getDefaultDateFormatter();
        Calendar cal = Calendar.getInstance();
        String d = format.format(cal.getTime());
        FileName = "FeedbackJSON_" + etClaimCode.getText() + ".json";
        FeedbackJSON = new File(MyDir, FileName);

        JSONObject FullObject = new JSONObject();

        try {
            JSONObject FeedbackObject = new JSONObject();

            FeedbackObject.put("Officer", Officer);
            FeedbackObject.put("ClaimUUID", ClaimUUID);
            FeedbackObject.put("CHFID", CHFID);
            FeedbackObject.put("Answers", Answers);
            FeedbackObject.put("Date", d);

            FullObject.put("feedback", FeedbackObject);

            try {
                FileOutputStream fOut = new FileOutputStream(FeedbackJSON);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.append(FullObject.toString());
                myOutWriter.close();
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IllegalStateException | JSONException e) {
            Log.e(LOG_TAG, "Error while writing feedback file", e);
        }

        return FullObject.toString();
    }

    private String Answers() {
        String Ans = "";
        rg1 = findViewById(R.id.radioGroup1);
        int Ans1 = rg1.getCheckedRadioButtonId();
        rg2 = findViewById(R.id.radioGroup2);
        int Ans2 = rg2.getCheckedRadioButtonId();
        rg3 = findViewById(R.id.radioGroup3);
        int Ans3 = rg3.getCheckedRadioButtonId();
        rg4 = findViewById(R.id.radioGroup4);
        int Ans4 = rg4.getCheckedRadioButtonId();

        if (Ans1 == R.id.rYes1) Ans = "1";
        else Ans = "0";
        if (Ans2 == R.id.rYes2) Ans = Ans + "1";
        else Ans = Ans + "0";
        if (Ans3 == R.id.rYes3) Ans = Ans + "1";
        else Ans = Ans + "0";
        if (Ans4 == R.id.rYes4) Ans = Ans + "1";
        else Ans = Ans + "0";

        rb1 = findViewById(R.id.ratingBar1);
        Ans = Ans + (int) rb1.getRating();
        return Ans;
    }

    private boolean isValidate() {

        if (etOfficer.getText().length() == 0) {
            ca.ShowDialog(getResources().getString(R.string.MissingOfficer));
            etOfficer.requestFocus();
            return false;
        }
        if (etClaimCode.getText().length() == 0) {
            ca.ShowDialog(getResources().getString(R.string.MissingClaimID));
            etClaimCode.requestFocus();
            return false;
        }
        if (etCHFID.getText().length() == 0) {
            ca.ShowDialog(getResources().getString(R.string.MissingCHFID));
            etCHFID.requestFocus();
            return false;
        }
        if ((!rbYes1.isChecked() && !rbNo1.isChecked()) || (!rbYes2.isChecked() && !rbNo2.isChecked()) || (!rbYes3.isChecked() && !rbNo3.isChecked()) || (!rbYes4.isChecked() && !rbNo4.isChecked())) {
            ca.ShowDialog(getResources().getString(R.string.MissingAnswers));
            return false;
        }

        return true;
    }
}
