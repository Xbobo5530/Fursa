package com.nyayozangu.labs.fursa.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.models.Post;
import com.nyayozangu.labs.fursa.helpers.CoMeth;
import com.nyayozangu.labs.fursa.helpers.Notify;
import com.nyayozangu.labs.fursa.models.User;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import id.zelory.compressor.Compressor;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.ACTION;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.CATEGORIES;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.CREDIT;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.DESC;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.FAIL;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.FOLLOWERS_VAL;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.FOLLOWER_POST;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.MESSAGE;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.MY_POSTS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.MY_POSTS_DOC;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.NEW_POST_UPDATES;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.NOTIFY;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.POSTS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.SUBSCRIPTIONS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.SUCCESS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.TAGS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.TAGS_VAL;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.TIMESTAMP;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.TITLE;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.USERS;
import static com.nyayozangu.labs.fursa.helpers.CoMeth.USER_ID_VAL;

public class CreatePostActivity extends AppCompatActivity implements View.OnClickListener {

    // TODO: 6/14/18 add multiple images upload

    private static final String TAG = "CreatePostActivity";
    private static final String EDIT_POST = "editPost";
    private static final String PRICE = "price";
    private static final String EVENT_DATE = "event_date";
    private static final String EVENT_END_DATE = "event_end_date";
    private static final String START_DATE = "start_date";
    private static final String END_DATE = "end_date";
    private static final String LOCATION = "location";
    private static final int DAILY_POSTS_LIMIT = 3;
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private int POST_COST_CREDIT = 1;
    //for file compression
    private Bitmap compressedImageFile;
    private CoMeth coMeth = new CoMeth();

    private ImageView createPostImageView;
    private ProgressDialog progressDialog;
    private Button descButton, categoriesButton, contactButton, locationButton,
            eventDateButton, eventEndDateButton, paymentButton;
    private Uri postImageUri;
    private String desc, postId,
//            imageText = "", imageLabels = "",
            downloadUri, downloadThumbUri,
            contactName,contactPhone, contactEmail,
            currentUserId, price;
    private Place postPlace = null;
    private View contactDialogView;
    private ArrayList<String> contactDetails, catsStringsArray, locationArray, cats, tags;
    private Date eventDate, eventEndDate;
    private ArrayList<Integer> mSelectedCats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.createPostToolbar);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setTitle(getString(R.string.create_post_text));
        actionBar.setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        descButton = findViewById(R.id.createPostDescButton);
        categoriesButton = findViewById(R.id.createPostCategoriesButton);
        contactButton = findViewById(R.id.createPostContactButton);
        locationButton = findViewById(R.id.createPostLocationButton);
        eventDateButton = findViewById(R.id.createPostEventDateButton);
        eventEndDateButton = findViewById(R.id.createPostEventEndDateButton);
        paymentButton = findViewById(R.id.createPostPriceButton);
        createPostImageView = findViewById(R.id.createPostImageView);
        Button submitButton = findViewById(R.id.createPostSubmitButton);
        FloatingActionButton editImageFAB = findViewById(R.id.createPostEditImageFab);

        mSelectedCats = new ArrayList<>();
        catsStringsArray = new ArrayList<>();
        locationArray = new ArrayList<>();
        contactDetails = new ArrayList<>();
        tags = new ArrayList<>();

        if (coMeth.isLoggedIn()) {
            currentUserId = coMeth.getUid();
        } else {
            goToMain(getResources().getString(R.string.not_logged_in_text));
        }
        handleIntent();

        //handle clicks
        submitButton.setOnClickListener(this);
        editImageFAB.setOnClickListener(this);
        createPostImageView.setOnClickListener(this);
        descButton.setOnClickListener(this);
        categoriesButton.setOnClickListener(this);
        contactButton.setOnClickListener(this);
        locationButton.setOnClickListener(this);
        eventDateButton.setOnClickListener(this);
        eventEndDateButton.setOnClickListener(this);
        paymentButton.setOnClickListener(this);
    }

    private void openPostImage() {
        if (postImageUri != null) {
            String imageUrl = postImageUri.toString();
            Intent openImageIntent = new Intent(
                    CreatePostActivity.this, ViewImageActivity.class);
            openImageIntent.putExtra(
                    getResources().getString(R.string.view_image_intent_name), imageUrl);
            startActivity(openImageIntent);
        }
    }

    private void handleAddPostImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                /*.setAspectRatio(16, 9)*/
                .setMinCropResultSize(512, 512)
                .start(CreatePostActivity.this);
    }

    private void handleSubmitPostClick() {
        if (coMeth.isConnected(this)) {
            //start submitting
            desc = descButton.getText().toString().trim();
            //check if description field is empty
            if (!TextUtils.isEmpty(desc)) {
                showProgress(getString(R.string.submitting));
                handleSubmitPost();
            } else {
                handleSubmitImageWithoutDesc();
            }
        } else {
            //notify user is not connected and cant post
            showSnack(getString(R.string.failed_to_connect_text));
        }
    }

    private void handleSubmitImageWithoutDesc() {
        if (postImageUri != null || !desc.isEmpty()) {
            //post has image
            //alert user post has no details
            AlertDialog.Builder noDetailsBuilder =
                    new AlertDialog.Builder(CreatePostActivity.this);
            noDetailsBuilder.setTitle(getResources().getString(R.string.warning_text))
                    .setIcon(getResources().getDrawable(R.drawable.ic_action_red_alert))
                    .setMessage(getString(R.string.submit_no_details_text) + "\n" +
                            getString(R.string.are_u_sure_submit_post_text))
                    .setPositiveButton(getString(R.string.edit_post_text),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                    .setNegativeButton(getString(R.string.proceed_text),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    handleSubmitPost();
                                }
                            })
                    .setCancelable(false)
                    .show();
        }else{
            showSnack(getString(R.string.enter_post_details_text));
        }
    }

    private void showPriceDialog() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(CreatePostActivity.this);
        builder.setTitle(getString(R.string.price_text)).setIcon(R.drawable.ic_action_payment);
        final EditText input = new EditText(CreatePostActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        builder.setView(input);
        if (!paymentButton.getText().toString().isEmpty()) {
            input.setText(paymentButton.getText().toString());
        }
        builder.setPositiveButton(getResources().getString(R.string.done_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        price = input.getText().toString().trim();
                        paymentButton.setText(price);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                .setCancelable(false);
        builder.show();
    }

    private void showEventDateDialog(final Button dateButton, final String dateType) {
        Calendar calendar = Calendar.getInstance();
        int YEAR = calendar.get(Calendar.YEAR);
        int MONTH = calendar.get(Calendar.MONTH);
        int DAY = calendar.get(Calendar.DAY_OF_MONTH);

        final DatePickerDialog eventDatePickerDialog =
                new DatePickerDialog(CreatePostActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                switch (dateType){
                                    case START_DATE:
                                        eventDate = new Date(year, month, dayOfMonth);
                                        String dateDetails = DateFormat
                                                .format("EEE, MMM d, 20yy", eventDate).toString();
                                        dateButton.setText(dateDetails);
                                        break;
                                    case END_DATE:
                                        eventEndDate = new Date(year, month, dayOfMonth);
                                        String endDateDetails = DateFormat
                                                .format("EEE, MMM d, 20yy", eventEndDate).toString();
                                        dateButton.setText(endDateDetails);
                                        break;
                                    default:
                                        Log.d(TAG, "onDateSet: at default");
                                }
                            }
                        }, YEAR, MONTH, DAY);
        eventDatePickerDialog.show();
    }

    private void openLocationView() {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(CreatePostActivity.this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException |
                GooglePlayServicesNotAvailableException e) {

            Log.e(TAG, "onClick: " + e.getMessage());
            AlertDialog.Builder locationErrorBuilder =
                    new AlertDialog.Builder(CreatePostActivity.this);
            locationErrorBuilder.setTitle("Error")
                    .setIcon(getResources().getDrawable(R.drawable.ic_action_red_alert))
                    .setMessage(R.string.failed_to_load_locations_text)
                    .setPositiveButton(getString(R.string.ok_text), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //dismiss the dialog
                            dialog.dismiss();
                        }
                    })
                    //show the dialog
                    .show();
        }
    }

    private void showCategoriesDialog() {
        AlertDialog.Builder catPickerBuilder =
                new AlertDialog.Builder(CreatePostActivity.this);
        catPickerBuilder.setTitle(getString(R.string.categories_text))
                .setIcon(getResources().getDrawable(R.drawable.ic_action_cat_light))
                .setMultiChoiceItems(coMeth.getCategories(this), null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                        if (isChecked) {
                            mSelectedCats.add(which);
                        } else if (mSelectedCats.contains(which)) {
                            mSelectedCats.remove(Integer.valueOf(which));
                        }
                    }
                })
                .setPositiveButton(getString(R.string.done_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String catsString = "";
                        for (int i = 0; i < mSelectedCats.size(); i++) {
                            catsString = catsString.concat(coMeth.getCategories(
                                    CreatePostActivity.this)[mSelectedCats.get(i)] + "\n");
                            catsStringsArray.add(coMeth.getCatKey(coMeth.getCategories(
                                    CreatePostActivity.this)[mSelectedCats.get(i)]));
                        }
                        categoriesButton.setText(catsString.trim());
                    }
                })
                .setNegativeButton(getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        mSelectedCats.clear();
        catsStringsArray.clear();
        categoriesButton.setText(getResources().getString(R.string.select_categories_create_post_hint_text));
        catPickerBuilder.show();
    }

    // TODO: 7/24/18 replace the desc dialog box with a new vew
    private void showContactDialog() {
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        contactDialogView = inflater.inflate(R.layout.contact_alert_dialog_content_layout, null);
        final AlertDialog.Builder contactDialogBuilder =
                new AlertDialog.Builder(CreatePostActivity.this);
        contactDialogBuilder.setTitle(R.string.contact_details_text)
                .setIcon(R.drawable.ic_action_contact)
                .setView(contactDialogView)
                .setPositiveButton(getString(R.string.done_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //user clicks done
                        EditText contactNameField =
                                contactDialogView.findViewById(R.id.contactNameDialogEditText);
                        EditText contactPhoneField =
                                contactDialogView.findViewById(R.id.contactPhoneDialogEditText);
                        EditText contactEmailField =
                                contactDialogView.findViewById(R.id.contactEmailDialogEditText);

                        //get values
                        contactName = contactNameField.getText().toString().trim();
                        contactPhone = contactPhoneField.getText().toString().trim();
                        contactEmail = contactEmailField.getText().toString().trim();

                        processContactDetails(dialog);
                    }
                })
                .setNegativeButton(
                        getResources().getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                .setCancelable(false);

        if (contactDialogView.getParent() != null) {
            ((ViewGroup) contactDialogView.getParent()).removeView(contactDialogView);
        }
        contactDialogBuilder.show();
    }

    private void showDescDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View descDialogView = inflater.inflate(R.layout.desc_dialog_content_layout, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(CreatePostActivity.this);
        final EditText descField = descDialogView.findViewById(R.id.descDialogEditText);
        if (desc != null && !desc.isEmpty()) {
            descField.setText(desc);
        }
        builder.setTitle(R.string.post_desc_text)
                .setIcon(R.drawable.ic_action_descritption)
                .setView(descDialogView)
                .setPositiveButton(getString(R.string.done_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                desc = descField.getText().toString().trim();
                descButton.setText(desc);
            }
                })
                .setNegativeButton(getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent();
    }

    private void handleSubmitPost() {
        if (!isEditPost()) {
            //is new post
            checkUserPostDailyQuota();
        } else {
            Intent intent = getIntent();
            postId = intent.getStringExtra(EDIT_POST);
            updatePostDetails();
        }
    }

    private void updatePostDetails() {
        coMeth.getDb().collection(POSTS).document(postId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        //check if task is successful
                        if (task.isSuccessful()) {

                            if (task.getResult().exists()) {

                                Post post = task.getResult().toObject(Post.class);
                                assert post != null;
                                if (post.getImage_url() != null)
                                    downloadUri = post.getImage_url();
                                if (post.getThumb_url() != null)
                                    downloadThumbUri = post.getThumb_url();
                                if (post.getCategories() != null)
                                    cats = post.getCategories();
                                if (post.getLocation() != null)
                                    locationArray = post.getLocation();
                                if (post.getContact_details() != null)
                                    contactDetails = post.getContact_details();
                                if (post.getEvent_date() != null)
                                    eventDate = post.getEvent_date().toDate();
                                if (post.getEvent_end_date() != null){
                                    eventEndDate = post.getEvent_end_date().toDate();
                                }
                                if (post.getPrice() != null)
                                    price = post.getPrice();

                                new SubmitPostTask().execute();
                                coMeth.stopLoading(progressDialog);
                                goToMain(getString(R.string.post_will_be_available_text));
                            }
                        } else {
                            //task failed
                            Log.d(TAG, "onComplete: task failed" + task.getException());
                        }
                    }
                });
    }

    private void checkUserPostDailyQuota() {
        Log.d(TAG, "checkUserPostDailyQuota: userId is " + coMeth.getUid());

        // today
        Calendar date = new GregorianCalendar();
        // reset hour, minutes, seconds and millis
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        Log.d(TAG, "onClick: date is " + date + "\ndate in millis is: " + date.getTimeInMillis());
        CollectionReference myPostsRef = coMeth.getDb().collection(USERS + "/" +
                currentUserId + "/" + SUBSCRIPTIONS + "/" + MY_POSTS_DOC + "/" + MY_POSTS);
        myPostsRef.whereGreaterThan(TIMESTAMP, date.getTime()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Log.d(TAG, "onSuccess: got documents");
                        if (!queryDocumentSnapshots.isEmpty()) {
                            int todayPostCount = queryDocumentSnapshots.size();
                            Log.d(TAG, "onSuccess: today's posts are: " + todayPostCount);
                            coMeth.stopLoading(progressDialog);
                            if (todayPostCount > DAILY_POSTS_LIMIT) {
                                AlertDialog.Builder quotaAlertBuilder =
                                        new AlertDialog.Builder(CreatePostActivity.this);
                                quotaAlertBuilder.setTitle(R.string.daily_limited_text)
                                        .setIcon(getResources().getDrawable(R.drawable.ic_action_quota))
                                        .setMessage(getResources().getString(R.string.you_have_reached_daily_limit_text) +
                                                "\nWould you like use your credit to get more posts?")
                                        .setPositiveButton(getResources().getString(R.string.ok_text),
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        //go to payments
                                                        // TODO: 6/8/18 handle opening payment page
                                                        //// TODO: 6/8/18 replace replacement code
                                                        dialog.dismiss();
                                                        goToMain();
                                                        // check if user has credit in account,
                                                        // if has apoints,
                                                        // check if credit are enough for new post,
                                                        // if enough, post,
                                                        // if user does not have enough point prompt payments,
                                                        // TODO: 8/6/18 check the daily quota when at main before entering create post
//                                                        startActivity(new Intent(CreatePostActivity.this, PaymentsActivity.class));

                                                        handleQuotaExceeded();

                                                    }
                                                })
                                        .show();
                            } else {
                                Log.d(TAG, "onSuccess: has not reached cap");
                                new SubmitPostTask().execute();
                                coMeth.stopLoading(progressDialog);
                                goToMain(getString(R.string.post_will_be_available_text));
                            }
                        } else {
                            Log.d(TAG, "onSuccess: has not reached cap, query is empty");
                            //has not reached cap
                            new SubmitPostTask().execute();
                            coMeth.stopLoading(progressDialog);
                            goToMain(getString(R.string.post_will_be_available_text));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to get user's posts from today\n" +
                                e.getMessage());
                    }
                });
    }

    private void handleQuotaExceeded() {
        //using credit to pay for more posts
        //get users current credit
        showProgress(getString(R.string.loading_text));
        final DocumentReference currentUserRef =  coMeth.getDb().collection(USERS).document(currentUserId);
        currentUserRef.get().addOnSuccessListener(
                new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    int userCredit = user.getCredit();
                    if (userCredit >= POST_COST_CREDIT){
                        //deduct credit
                        final int newCreditAmount = userCredit - POST_COST_CREDIT;
                        Map<String, Object> creditMap = new HashMap<>();
                        creditMap.put(CREDIT, newCreditAmount);
                        currentUserRef.update(creditMap).addOnSuccessListener(
                                new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                coMeth.stopLoading(progressDialog);
                                showCreditDeductedDialog(newCreditAmount);
                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "onFailure: failed to update credit\n" +
                                                e.getMessage(), e);
                                        coMeth.stopLoading(progressDialog);
                                        showSnack(getString(R.string.error_text) + ": " + e.getMessage());
                                    }
                                });

                    }else{
                        //insufficient credit
                        AlertDialog.Builder insufficientCreditBuilder =
                                new AlertDialog.Builder(CreatePostActivity.this);
                        /*String message = getString(R.string.insufficient_credit_message) + "\n"
                                + getString(R.string.buy_more_credit_text);*/
                        String message = getString(R.string.insufficient_credit_message_for_post) + "\n" +
                                "Try again tomorrow";
                        insufficientCreditBuilder.setTitle(R.string.insufficient_credit_text)
                                .setMessage(message)
                                .setIcon(getResources().getDrawable(R.drawable.ic_action_payment))
                                .setPositiveButton(getString(R.string.ok_text),
                                        new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                })

                                /*

                                .setPositiveButton(getString(R.string.buy_credit_text),
                                        new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // TODO: 8/9/18 goto to payments page
                                    }
                                })
                                .setNegativeButton(getString(R.string.cancel_text),
                                        new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })*/
                                .show();
                    }
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "onFailure: failed to get current user for handle quota exceeded\n" +
                                e.getMessage(), e);
                        coMeth.stopLoading(progressDialog);
                        showSnack(getString(R.string.error_text) + ": " + e.getMessage());
                    }
                });
    }

    private void showCreditDeductedDialog(int creditAmount) {
        AlertDialog.Builder creditDeductedBuilder = new AlertDialog.Builder(this);
        String message = POST_COST_CREDIT + getString(R.string.credit_deducted_message) +
                "\n" + getString(R.string.new_credit_text) + " " + creditAmount + "credit(s)";
        creditDeductedBuilder.setTitle(R.string.credit_deducted_text)
                .setMessage(message)
                .setIcon(getResources().getDrawable(R.drawable.ic_action_info_grey))
                .setPositiveButton(getString(R.string.ok_text),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        new SubmitPostTask().execute();
                        goToMain(getString(R.string.post_will_be_available_text));
                    }
                })
                .setCancelable(false)
                .show();
    }


    private void goToMain() {
        startActivity(new Intent(CreatePostActivity.this, MainActivity.class));
        finish();
    }

    private void processContactDetails(DialogInterface dialog) {

        String contactDetails;
        if (contactName.isEmpty() && contactPhone.isEmpty() && contactEmail.isEmpty()) {
            //all fields are empty
            dialog.cancel();

        } else {
            if (!contactName.isEmpty()) {
                if (!contactPhone.isEmpty()) {
                    if (!contactEmail.isEmpty()) {
                        contactDetails = contactName + "\n" +
                                contactPhone + "\n" +
                                contactEmail;
                        contactButton.setText(contactDetails);
                    } else {
                        contactDetails = contactName + "\n" + contactPhone;
                        contactButton.setText(contactDetails);
                    }
                } else {
                    if (!contactEmail.isEmpty()) {
                        contactDetails = contactName + "\n" + contactEmail;
                        contactButton.setText(contactDetails);
                    } else {
                        contactButton.setText("");
                    }
                }
            } else {
                if (!contactPhone.isEmpty()) {
                    if (!contactEmail.isEmpty()) {
                        contactDetails = contactPhone + "\n" + contactEmail;
                        contactButton.setText(contactDetails);
                    } else {
                        contactDetails = contactPhone;
                        contactButton.setText(contactDetails);
                    }
                } else {
                    contactDetails = contactEmail;
                    contactButton.setText(contactDetails);
                }
            }
        }
    }

    public void submitPost() {
        if (postImageUri != null) {
            handlePostWithImage();
        } else {
            handlePostWithoutImage();
        }
    }

    private void handlePostWithoutImage() {
        final Map<String, Object> postMap = handleMap(downloadThumbUri, downloadUri);
        if (!isEditPost()) {
            coMeth.getDb().collection(POSTS).add(postMap)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                notifyNewPostCatsUpdates(catsStringsArray);
                                String currentPostId = task.getResult().getId();
                                FirebaseMessaging.getInstance().subscribeToTopic(currentPostId);
                                updateMyPosts(currentPostId);
                                final String newPostId = task.getResult().getId();
                                updateTagsOnDB(newPostId, postMap);
                                updateCatsOnDB(newPostId, postMap);
                                newPostNotif(true, newPostId);
                                notifyFollowers(newPostId);
                            } else {

                                Log.d(TAG, "onComplete: " + task.getException());
//                                String errorMassage = Objects.requireNonNull(task.getException()).getMessage();
//                                newPostNotif(false, errorMassage);
                            }
                        }
                    });

        } else {

            //for edit post
            coMeth.getDb().collection(POSTS).document(postId).update(postMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                notifyNewPostCatsUpdates(catsStringsArray);
                                Log.d(TAG, "onComplete: posted post without image");
                                FirebaseMessaging.getInstance().subscribeToTopic(postId);
                                //update tags on db
                                updateTagsOnDB(postId, postMap);
                                updateCatsOnDB(postId, postMap);
                                newPostNotif(true, postId);
                                notifyFollowers(postId);
                            }
                        }
                    });
        }
    }

    private void notifyFollowers(String newPostId) {
        String mTopic = currentUserId + FOLLOWERS_VAL;
        new Notify().execute(FOLLOWER_POST, mTopic, newPostId);
    }

    private void newPostNotif(boolean isSuccess, String metaData) {
        String postReadyTopic = NEW_POST_UPDATES + currentUserId;
        FirebaseMessaging.getInstance().subscribeToTopic(postReadyTopic);
        if (isSuccess) {
            //notify user
            new Notify().execute(NEW_POST_UPDATES, postReadyTopic, SUCCESS, metaData);
        } else {
            //failed to post
            // TODO: 7/14/18 remove fails on posting
            new Notify().execute(NEW_POST_UPDATES, postReadyTopic, FAIL, metaData);
        }
    }

    private void updateTagsOnDB(final String newPostId, Map<String, Object> postMap) {
        //update tags on db
        if (postMap.get(TAGS_VAL) != null) {
            //cycle through the tags and add them to the tags list
            for (int i = 0; i < tags.size(); i++) {
                final String title = tags.get(i);
                //create tagsMap
                final Map<String, Object> tagsMap = new HashMap<>();
                tagsMap.put(TITLE, title);
                final DocumentReference tagDoc = coMeth.getDb().collection(TAGS).document(title);
                tagDoc.update(tagsMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                updatePostsOnTags(title, newPostId);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: failed to add new post to Tags collection\n" +
                                        e.getMessage());
                                tagDoc.set(tagsMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "onSuccess: created new tag on DB");
                                                updatePostsOnTags(title, newPostId);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "onFailure: failed to create new tag on DB\n" +
                                                        e.getMessage());
                                            }
                                        });
                            }
                        });
            }
        }
    }

    private void updatePostsOnTags(final String title, final String newPostId) {
        final DocumentReference newPostDoc = coMeth.getDb()
                .collection(TAGS + "/" + title + "/" + POSTS).document(newPostId);
        final Map<String, Object> tagsPostMap = new HashMap<>();
        tagsPostMap.put(TIMESTAMP, FieldValue.serverTimestamp());
        newPostDoc.update(tagsPostMap)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to update tags posts on DB\n" +
                                e.getMessage());
                        newPostDoc.set(tagsPostMap)
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: final check, " +
                                                "failed to update posts on tags\n" +
                                                e.getMessage());
                                    }
                                });
                    }
                });
    }


    private void handlePostWithImage() {
        final String randomName = UUID.randomUUID().toString();
        //define path to upload image
        StorageReference filePath =
                coMeth.getStorageRef().child("post_images").child(randomName + ".jpg");
        //upload the image
        filePath.putFile(postImageUri).addOnCompleteListener(
                new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                        //get download url
                        downloadUri = task.getResult().getDownloadUrl().toString();
//                        downloadUri = task.getResult().getUploadSessionUri().toString();
                        //handle results after attempting to upload
                        if (task.isSuccessful()) {
                            //upload complete
                            Log.d(TAG, "upload successful");
                            File newImageFile = new File(postImageUri.getPath());

                            try {
                                compressedImageFile = new Compressor(CreatePostActivity.this)
                                        .setMaxWidth(100)
                                        .setMaxHeight(100)
                                        .setQuality(3)
                                        .compressToBitmap(newImageFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            //handle Bitmap
                            uploadImage(randomName);

                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    private void uploadImage(String imageName) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] thumbData = baos.toByteArray();
        //uploading the thumbnail
        UploadTask uploadTask = coMeth.getStorageRef().child("post_images/thumbs")
                .child(imageName + ".jpg")
                .putBytes(thumbData);

        //on success listener
        uploadTask.addOnSuccessListener(
                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        downloadThumbUri = taskSnapshot.getDownloadUrl().toString();
//                        downloadThumbUri = taskSnapshot.getUploadSessionUri().toString();
                        Map<String, Object> postMap = handleMap(downloadThumbUri, downloadUri);
                        if (!isEditPost()) {
                            createNewPost(postMap);
                        } else {
                            updatePostDetails(postMap);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Db Update failed: " + e.getMessage());
            }
        });
    }


    private void updatePostDetails(final Map<String, Object> postMap) {
        coMeth.getDb().collection(POSTS).document(postId).update(postMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        //check the result
                        if (task.isSuccessful()) {
                            //notify users subscribed to cats
                            notifyNewPostCatsUpdates(catsStringsArray);
                            Log.d(TAG, "onComplete: about to upload " +
                                    "\ncategories are: " + catsStringsArray);
                            FirebaseMessaging.getInstance().subscribeToTopic(postId);
                            updateTagsOnDB(postId, postMap);
                            updateCatsOnDB(postId, postMap);
                            newPostNotif(true, postId);
                            notifyFollowers(postId);

                        } else {
                            //upload failed
                            Log.d(TAG, "Db Update failed: " +
                                    Objects.requireNonNull(task.getException()).getMessage());
                        }
                    }
                });
    }

    private void createNewPost(final Map<String, Object> postMap) {
        coMeth.getDb().collection(POSTS).add(postMap)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {

                        //check the result
                        if (task.isSuccessful()) {
                            //notify users subscribed to cats
                            notifyNewPostCatsUpdates(catsStringsArray);
                            //subscribe current user to post comments
                            String currentPostId = task.getResult().getId();
                            FirebaseMessaging.getInstance().subscribeToTopic(currentPostId);
                            //update users posts list
                            updateMyPosts(currentPostId);
                            String newPostId = task.getResult().getId();
                            updateTagsOnDB(newPostId, postMap);
                            updateCatsOnDB(newPostId, postMap);
                            newPostNotif(true, newPostId);
                            notifyFollowers(newPostId);

                        } else {

                            String errorMessage =
                                    Objects.requireNonNull(task.getException()).getMessage();
                            Log.d(TAG, "Db Update failed: " + errorMessage);
//                            newPostNotif(false, errorMessage);
                        }
                    }
                });
    }

    private void updateCatsOnDB(final String newPostId, Map<String, Object> postMap) {
        //check if post has cats
        if (postMap.get(CoMeth.CATEGORIES_VAL) != null) {
            //cycle through the cats
            for (final String cat : catsStringsArray) {
                //create a map
                final DocumentReference catDoc = coMeth.getDb().collection(CATEGORIES).document(cat);
                final Map<String, Object> catMap = new HashMap<>();
                catMap.put(TITLE, cat);
                catDoc.update(catMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "onSuccess: cats on db have been updated");
                                updatePostsOnCats(cat, newPostId);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: failed to update cats on db\n" +
                                        e.getMessage());
                                catDoc.set(catMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "onSuccess: cats on db have been updated");
                                                updatePostsOnCats(cat, newPostId);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "onFailure: failed to set new cat on db\n" +
                                                        e.getMessage());
                                            }
                                        });
                            }
                        });
            }
        }
    }

    private void updatePostsOnCats(final String cat, final String newPostId) {
        //create map
        final DocumentReference newPostDoc = coMeth.getDb()
                .collection(CATEGORIES + "/" + cat + "/" + POSTS).document(newPostId);
        final Map<String, Object> catsPostMap = new HashMap<>();
        catsPostMap.put(CoMeth.TIMESTAMP, FieldValue.serverTimestamp());
        newPostDoc.update(catsPostMap).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to update post on cat on db\n" +
                                e.getMessage());
                        //attempt to set map
                        newPostDoc.set(catsPostMap).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: failed to set post to cat on db\n"
                                                + e.getMessage());
                                    }
                                });
                    }
                });
    }

    private void goToMain(String message) {
        Intent goToMainIntent = new Intent(CreatePostActivity.this, MainActivity.class);
        goToMainIntent.putExtra(ACTION, NOTIFY);
        goToMainIntent.putExtra(MESSAGE, message);
        goToMainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(goToMainIntent);
        finish();
    }

    private void updateMyPosts(String currentPostId) {
        //create map with timestamp
        Map<String, Object> myPostMap = new HashMap<>();
        DocumentReference currentPost = coMeth.getDb()
                .collection(USERS + "/" + currentUserId + "/" + SUBSCRIPTIONS)
                .document(MY_POSTS_DOC).collection(MY_POSTS).document(currentPostId);
        myPostMap.put(TIMESTAMP, FieldValue.serverTimestamp());
        //update current user's subscriptions
        currentPost.set(myPostMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: myPosts has been updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to update my posts\n" + e);
                    }
                });
    }

    private void notifyNewPostCatsUpdates(ArrayList<String> catsStringsArray) {
        //send notifications to all users subscribed to cats in catStringArray
        for (int i = 0; i < catsStringsArray.size(); i++) {

            String notifType = "categories_updates";
            Log.d(TAG, "notifyNewPostCatsUpdates: cat is " + catsStringsArray.get(i));
            new Notify().execute(notifType, catsStringsArray.get(i));
        }
    }

    @NonNull
    private Map<String, Object> handleMap(String downloadThumbUri, String downloadUri) {

        //store the user info associated with post
        Map<String, Object> postMap = new HashMap<>();
        if (downloadUri != null) {
            postMap.put("image_url", downloadUri);
        }
        if (downloadThumbUri != null) {
            postMap.put("thumb_url", downloadThumbUri);
        }
//        if (!imageLabels.isEmpty()) {
//            postMap.put("image_labels", imageLabels);
//        }
//        if (!imageText.isEmpty()) {
//            postMap.put("image_text", imageText);
//        }

        postMap.put(DESC, desc);
        postMap.put(USER_ID_VAL , currentUserId);

        //get the current time
        if (!isEditPost()) {
            postMap.put(TIMESTAMP, FieldValue.serverTimestamp());
        }
        //handle contact details
        if (contactDetails != null) {
            processContactDetails();
        }
        //handle location
        if (postPlace != null) {
            //set up an array for location
            locationArray.clear();
            locationArray.add(postPlace.getName().toString());
            locationArray.add(postPlace.getAddress().toString());
        }
        //location
        if (locationArray != null) {
            postMap.put(LOCATION, locationArray);
        }
        //event date
        if (eventDate != null) {
            postMap.put(EVENT_DATE, eventDate);
        }
        if (eventEndDate != null){
            postMap.put(EVENT_END_DATE, eventEndDate);
        }
        //contact details
        if (contactDetails != null && contactDetails.size() > 0) {
            postMap.put("contact_details", contactDetails);
        }
        //price
        if (price != null) {
            postMap.put("price", price);
        }
        //categories
        if (!catsStringsArray.isEmpty()) {
            postMap.put(CoMeth.CATEGORIES_VAL, catsStringsArray);
        }

        // TODO: 6/1/18 account for then # is in word (not begging or end)
        //create tags
        String titleDesc = (desc).replaceAll("\\s+", " ");
        if (titleDesc.contains("#")) {
            Log.d(TAG, "handleMap: has #");
            int hashPos = titleDesc.indexOf("#");
            while (hashPos < titleDesc.length() &&
                    titleDesc.indexOf("#", hashPos) != -1 &&
                    hashPos != -1) {
                if (titleDesc.indexOf(" ", hashPos) != -1) {
                    String tag = titleDesc.substring(hashPos + 1, titleDesc.indexOf(" ", hashPos));
                    tags.add(tag);
                } else {
                    String tag = titleDesc.substring(hashPos + 1);
                    tags.add(tag);
                }
                hashPos = titleDesc.indexOf("#", hashPos + 1);
            }
            postMap.put(CoMeth.TAGS_VAL, tags);
        }
        return postMap;
    }

    private void processContactDetails() {

        if (contactName != null) {
            if (contactPhone != null) {
                if (contactEmail != null && !contactEmail.isEmpty()) {
                    contactDetails.add(contactName);
                    contactDetails.add(contactPhone);
                    contactDetails.add(contactEmail);
                } else {
                    contactDetails.add(contactName);
                    contactDetails.add(contactPhone);
                }
            } else {
                if (contactEmail != null && !contactEmail.isEmpty()) {
                    contactDetails.add(contactName);
                    contactDetails.add(contactEmail);
                } else {
                    contactDetails.add(contactName);
                }
            }
        } else {
            if (contactPhone != null && !contactPhone.isEmpty()) {
                if (contactEmail != null && !contactEmail.isEmpty()) {
                    contactDetails.add(contactPhone);
                    contactDetails.add(contactEmail);
                } else {
                    contactDetails.add(contactPhone);
                }
            } else {
                if (contactEmail != null && !contactEmail.isEmpty()) {
                    contactDetails.add(contactEmail);
                } else {
                    contactDetails = null;
                }
            }
        }
    }

    private boolean isEditPost() {
        return getIntent().hasExtra(EDIT_POST);
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            String type = intent.getType();
            if (Intent.ACTION_SEND.equals(action) && type != null) {
                if (coMeth.isLoggedIn()) {
                    if ("text/plain".equals(type)) {
                        handleSendText(intent);
                    } else if (type.startsWith("image/")) {
                        handleSendImage(intent);
                    }
                } else {
                    goToLogin(getResources().getString(R.string.login_to_post_text));
                }
            }
            if (isEditPost()) {
                //get the sent intent
                Intent getEditPostIdIntent = getIntent();
                String postId = getEditPostIdIntent.getStringExtra("editPost");
                Log.d(TAG, "postId is: " + postId);
                //populate data
                populateEditPostData(postId);

            }

        }
    }

    private void goToLogin(String message) {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.putExtra(CoMeth.MESSAGE, message);
        startActivity(loginIntent);
        finish();
    }

    void handleSendText(Intent intent) {

        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            //set shared text to desc field
            descButton.setText(sharedText);
        }
    }

    void handleSendImage(Intent intent) {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {

            //check if user is logged in
            if (coMeth.isLoggedIn()) {
                // Update image Uri
                postImageUri = imageUri;
                Log.d(TAG, "handleSendImage: image uri is " + imageUri);
                //open the image cropper activity and pass in the uri
                CropImage.activity(postImageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        /*.setAspectRatio(16, 9)*/
                        .setMinCropResultSize(512, 512)
                        .start(CreatePostActivity.this);
            } else {
                //user is not logged in
                showLoginAlertDialog(getString(R.string.login_to_create_post));
            }
        }
    }


    private void populateEditPostData(String postId) {

        showProgress(getString(R.string.loading_text));
        //access db to set items
        coMeth.getDb().collection(POSTS).document(postId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                //check if result is successful
                if (task.isSuccessful() && task.getResult().exists()) {

                    //set items
                    Post post = task.getResult().toObject(Post.class);
                    if  (post != null) {
                        String title = post.getTitle();
                        String desc = post.getDesc();
                        String description = title + "\n" + desc;
                        descButton.setText(description);

                        if (post.getImage_url() != null) {
                            String imageUrl = post.getImage_url();
                            String thumbUrl = post.getThumb_url();
                            try {
                                coMeth.setImage(R.drawable.appiconshadow, imageUrl, thumbUrl,
                                        createPostImageView, Glide.with(CreatePostActivity.this));
                            } catch (Exception e) {
                                Log.d(TAG, "onComplete: failed to set create image");
                            }
                            //update the imageUrl
                            downloadUri = imageUrl;
                            downloadThumbUri = thumbUrl;
                        }

                        //set categories
                        if (post.getCategories() != null) {

                            ArrayList catsArray = (ArrayList) task.getResult().get("categories");
                            Log.d(TAG, "onComplete: \n catsArray on edit is: " + catsArray);
                            String catsString = "";
                            for (int i = 0; i < catsArray.size(); i++) {
                                catsString = catsString.concat(
                                        coMeth.getCatValue(catsArray.get(i).toString(),
                                                CreatePostActivity.this) + "\n");
                            }
                            //set cat string
                            categoriesButton.setText(catsString.trim());
                            Log.d(TAG, "onComplete: \n catString is: " + catsString);
                        }
                        //set contact details
                        if (post.getContact_details() != null) {
                            ArrayList contactArray = (ArrayList) task.getResult().get("contact_details");
                            if (contactArray != null) {
                                String contactString = "";
                                for (int i = 0; i < contactArray.size(); i++) {
                                    contactString = contactString.concat(contactArray.get(i).toString() + "\n");
                                }
                                contactButton.setText(contactString.trim());
                            }
                        }
                        //set location
                        if (post.getLocation() != null) {
                            ArrayList locationArray = (ArrayList) task.getResult().get("location");
                            if (locationArray != null) {
                                String locationString = "";
                                for (int i = 0; i < locationArray.size(); i++) {
                                    locationString = locationString
                                            .concat(locationArray.get(i).toString() + "\n");
                                }
                                locationButton.setText(locationString.trim());
                            }
                        }

                        //set event date
                        if (post.getEvent_date() != null) {
                            long eventDate = post.getEvent_date().toDate().getTime();
                            String eventDateString =
                                    DateFormat.format("EEE, MMM d, 20yy", new Date(eventDate)).toString();
                            eventDateButton.setText(eventDateString);
                        }
                        if (post.getEvent_end_date() != null) {
                            long eventEndDate = post.getEvent_date().toDate().getTime();
                            String eventEndDateString =
                                    DateFormat.format("EEE, MMM d, 20yy", new Date(eventEndDate)).toString();
                            eventEndDateButton.setText(eventEndDateString);
                        }
                        //set price
                        String price = post.getPrice();
                        if (price != null) {
                            paymentButton.setText(price);
                        }
                    }
                    coMeth.stopLoading(progressDialog);
                } else {
                    //post does not exist
                    Log.d(TAG, "onComplete: post does not exist" + task.getException());
                    //go to main with error message
                    Intent postNotFountIntent =
                            new Intent(CreatePostActivity.this, MainActivity.class);
                    postNotFountIntent.putExtra(ACTION,NOTIFY);
                    postNotFountIntent.putExtra(MESSAGE, getString(R.string.post_not_found_text));
                    startActivity(postNotFountIntent);
                    finish();
                }
            }
        });
    }

    //when the image is selected by user
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        //for selecting post image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                //image is selected
                //get image uri
                postImageUri = result.getUri();
                createPostImageView.setImageURI(postImageUri);
                //process image tags
//                processMLImage(postImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                //handle errors
                Log.d(TAG, "onActivityResult: error " + result.getError().getMessage());
            }
        }

        //for google places (location)
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(CreatePostActivity.this, data);
                String placeText = place.getName() + "\n" + place.getAddress();
                locationButton.setText(placeText);
                //save the location
                postPlace = place;
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error when the creating post process is incomplete.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "onActivityResult: location canceled");
            }
        }
    }

    private void showProgress(String message) {
        progressDialog = new ProgressDialog(CreatePostActivity.this);
        progressDialog.setMessage(message);
        // TODO: 8/6/18 remember the submit option
//        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void showSnack(String message) {
        Snackbar.make(findViewById(R.id.createPostActivityLayout),
                message, Snackbar.LENGTH_LONG).show();
    }

    private void showLoginAlertDialog(String message) {
        //Prompt user to log in
        AlertDialog.Builder loginAlertBuilder = new AlertDialog.Builder(CreatePostActivity.this);
        loginAlertBuilder.setTitle("Login")
                .setIcon(getResources().getDrawable(R.drawable.ic_action_red_alert))
                .setMessage("You are not logged in\n" + message)
                .setPositiveButton(getResources().getString(R.string.login_text),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goToLogin();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }
    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.createPostDescButton:
                showDescDialog();
                break;
            case R.id.createPostCategoriesButton:
                showCategoriesDialog();
                break;
            case R.id.createPostContactButton:
                showContactDialog();
                break;
            case R.id.createPostLocationButton:
                openLocationView();
                break;
            case R.id.createPostEventDateButton:
                showEventDateDialog(eventDateButton, START_DATE);
                break;
            case R.id.createPostEventEndDateButton:
                showEventDateDialog(eventEndDateButton, END_DATE);
                break;
            case R.id.createPostPriceButton:
            showPriceDialog();
            break;
            case R.id.createPostSubmitButton:
                handleSubmitPostClick();
                break;
            case R.id.createPostImageView:
                openPostImage();
                break;
            case R.id.createPostEditImageFab:
                handleAddPostImage();
                break;
            default:
                Log.d(TAG, "onClick: create post at default click");
        }
    }
    //submit post in background
    // TODO: 7/5/18 fix leak
    public class SubmitPostTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            submitPost();
            return null;
        }
    }
}