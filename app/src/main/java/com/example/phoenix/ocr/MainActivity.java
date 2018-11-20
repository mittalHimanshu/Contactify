package com.example.phoenix.ocr;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ContentProviderOperation;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.soundcloud.android.crop.Crop;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_CONTACTS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 0007;
    public static final int RequestPermissionCode = 7;
    EditText nameText, phoneText, emailText;
    ImageView imageView;
    private String pictureImagePath;
    Uri outputFileUri;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    AlertDialog.Builder builder;
    List<String> nameLabel, phoneLabel, emailLabel;
    String name, email, phone;
    ConstraintLayout constraintLayout;
    Bitmap profileBitmap;
    int choice = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        constraintLayout = findViewById(R.id.constraintLayout);
        imageView = findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.ic_center_focus_strong_black_24dp);

        if(CheckingPermissionIsEnabledOrNot())
            drawableListeners();

        else
            RequestMultiplePermission();

    }

    public boolean CheckingPermissionIsEnabledOrNot() {

        int FirstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        int ThirdPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int ForthPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_CONTACTS);

        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SecondPermissionResult == PackageManager.PERMISSION_GRANTED &&
                ThirdPermissionResult == PackageManager.PERMISSION_GRANTED &&
                ForthPermissionResult == PackageManager.PERMISSION_GRANTED ;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case RequestPermissionCode:

                if (grantResults.length > 0) {

                    boolean CameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean RecordAudioPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean SendSMSPermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean GetAccountsPermission = grantResults[3] == PackageManager.PERMISSION_GRANTED;

                    if (CameraPermission && RecordAudioPermission && SendSMSPermission && GetAccountsPermission) {

                        Snackbar.make(constraintLayout, "Thanks for granting Permissions", Snackbar.LENGTH_SHORT).show();
                    }
                    else {
                        Snackbar.make(constraintLayout, "Please grant all Permissions !!!", Snackbar.LENGTH_SHORT).show();

                    }
                }

                break;
        }
    }

    private void RequestMultiplePermission() {


        ActivityCompat.requestPermissions(MainActivity.this, new String[]
                {
                        CAMERA,
                        READ_EXTERNAL_STORAGE,
                        WRITE_EXTERNAL_STORAGE,
                        WRITE_CONTACTS
                }, RequestPermissionCode);

    }

    @SuppressLint("ClickableViewAccessibility")
    public void drawableListeners(){

        nameText = findViewById(R.id.nameText);
        phoneText = findViewById(R.id.phoneText);
        emailText = findViewById(R.id.emailText);
        builder = new AlertDialog.Builder(MainActivity.this);
        nameLabel = new ArrayList<>();
        phoneLabel = new ArrayList<>();
        emailLabel = new ArrayList<>();

        nameText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (nameText.getRight() - nameText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        choice = 1;
                        promptSpeechInput();
                        return true;
                    }
                }
                return false;
            }
        });

        phoneText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (phoneText.getRight() - phoneText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        choice = 2;
                        promptSpeechInput();
                        return true;
                    }
                }
                return false;
            }
        });

        emailText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (emailText.getRight() - emailText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        choice = 3;
                        promptSpeechInput();
                        return true;
                    }
                }
                return false;
            }
        });

    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say Something !!!");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Snackbar.make(constraintLayout, "Sorry your device doesn't support speech input", Snackbar.LENGTH_SHORT).show();
        }

    }

    public void add(String name, String mobile, String email, Bitmap mBitmap){

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        ops.add(ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());


        if (name != null) {
            ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(
                            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                            name).build());
        }

        if (mobile != null) {
            ops.add(ContentProviderOperation.
                    newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, mobile)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build());
        }

        if (email != null) {
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Email.DATA, email)
                    .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                    .build());
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if(mBitmap!=null){

            mBitmap.compress(Bitmap.CompressFormat.PNG , 75, stream);
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.IS_SUPER_PRIMARY, 1)
                    .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO,stream.toByteArray())
                    .build());

            try {
                stream.flush();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            Snackbar.make(constraintLayout, name + " added", Snackbar.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(constraintLayout, e.getMessage(), Snackbar.LENGTH_SHORT).show();
        }

    }

    public void addContact(View view) {

        String name = nameText.getText().toString();
        if(name.equals("")){
            Snackbar.make(constraintLayout, "Name is mandatory", Snackbar.LENGTH_SHORT).show();
        }
        else {
            String mobile = phoneText.getText().toString();
            String email = emailText.getText().toString();
            add(name, mobile, email, profileBitmap);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case CAMERA_REQUEST: {

                File imgFile = new File(pictureImagePath);
                if (imgFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    imageView.setImageBitmap(myBitmap);
                    performOcr(myBitmap);
                }

                break;

            }

            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    switch(choice){

                        case 1:
                            nameText.setText(result.get(0)); break;

                        case 2:
                            phoneText.setText(result.get(0)); break;

                        case 3:
                            emailText.setText(result.get(0)); break;

                    }

                }
                break;
            }

            case Crop.REQUEST_CROP: {

                imageView.setImageURI(outputFileUri);
                try {
                    profileBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), outputFileUri);
                    nameText.setText(name);
                    phoneText.setText(phone);
                    emailText.setText(email);
                    new File(pictureImagePath).delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            }

        }
    }

    private void performOcr(Bitmap bitmap) {

        TextRecognizer txtRecognizer = new TextRecognizer.Builder(MainActivity.this).build();
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray items = txtRecognizer.detect(frame);
        for (int i = 0; i < items.size(); i++) {
            TextBlock item = (TextBlock) items.valueAt(i);
            for (Text line : item.getComponents()) {
                nameLabel.add(line.getValue());
                String temp = line.getValue();

                String pattern = "[A-Z a-z0-9+_.-]+@(.+)";
                Pattern r = Pattern.compile(pattern);
                Matcher m = r.matcher(temp);
                if (m.find()) {
                    emailLabel.add(m.group(0));
                }

                pattern = "(?:(?:\\+?([1-9]|[0-9][0-9]|[0-9][0-9][0-9])\\s*(?:[.-]\\s*)?)?(?:\\(\\s*([2-9]1[02-9]|[2-9][02-8]1|[2-9][02-8][02-9])\\s*\\)|([0-9][1-9]|[0-9]1[02-9]|[2-9][02-8]1|[2-9][02-8][02-9]))\\s*(?:[.-]\\s*)?)?([2-9]1[02-9]|[2-9][02-9]1|[2-9][02-9]{2})\\s*(?:[.-]\\s*)?([0-9]{4})(?:\\s*(?:#|x\\.?|ext\\.?|extension)\\s*(\\d+))?";
                r = Pattern.compile(pattern);
                m = r.matcher(temp);
                if (m.find()) {
                    phoneLabel.add(m.group(0));
                }

            }
        }

        displayNameBuilder();

    }

    public void displayEmailBuilder(){

        builder.setTitle("Choose Email : ");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, emailLabel);

        builder.setAdapter(dataAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                email = emailLabel.get(which);
                dialog.dismiss();
                Crop.of(outputFileUri, outputFileUri).asSquare().start(MainActivity.this);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("Next", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Crop.of(outputFileUri, outputFileUri).asSquare().start(MainActivity.this);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void displayPhoneBuilder(){

        builder.setTitle("Choose Mobile No : ");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, phoneLabel);

        builder.setAdapter(dataAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                phone = phoneLabel.get(which);
                dialog.dismiss();
                displayEmailBuilder();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("Next", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                displayEmailBuilder();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void displayNameBuilder(){

        builder.setTitle("Choose Name : ");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, nameLabel);

        builder.setAdapter(dataAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                name = nameLabel.get(which);
                dialog.dismiss();
                displayPhoneBuilder();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("Next", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                displayPhoneBuilder();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void findImage(View view) {

        try{
            nameLabel.clear(); phoneLabel.clear(); emailLabel.clear();
        }catch(Exception e){}
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + ".jpg";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        pictureImagePath = storageDir.getAbsolutePath() + "/" + imageFileName;
        File file = new File(pictureImagePath);
        outputFileUri = Uri.fromFile(file);
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);

    }
}
