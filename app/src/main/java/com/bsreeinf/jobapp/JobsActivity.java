package com.bsreeinf.jobapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.bsreeinf.jobapp.util.AnimatedLinearLayout;
import com.bsreeinf.jobapp.util.Commons;
import com.bsreeinf.jobapp.util.CompanyContainer;
import com.bsreeinf.jobapp.util.DialogMultiselect;
import com.bsreeinf.jobapp.util.JobsAdapter;
import com.bsreeinf.jobapp.util.JobsContainer;
import com.bsreeinf.jobapp.util.SavedAppliedJobsContainer;
import com.bsreeinf.jobapp.util.SimpleContainer;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.Builders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class JobsActivity extends Activity {

    private static final String TAG = "JobsActivity";
    public static JobsContainer jobs;
    public static String user_id = null;
    private TextView txtTabHome, txtTabSaved, txtTabProfile;
    private ImageView imgTabHome, imgTabFilter, imgTabSaved, imgTabProfile;
    private ViewFlipper viewFlipper;
    private Context context;
    private int viewCount;
    private ListView layoutJobsContainer;
    private LinearLayout layoutSavedJobsContainer, layoutAppliedJobsContainer;
    private AnimatedLinearLayout layoutFilter;
    private String strFilterLocation, strFilterCompany, strFilterIndustry, strFilterSalary;
    private ArrayList<String> arrFilterLocationIDs, arrFilterCompanyIDs, arrFilterIndustryIDs, arrFilterSalaryIDs;
    private LinearLayout layoutFilterLocation, layoutFilterCompany, layoutFilterIndustry, layoutFilterSalary;
    private TextView txtFilterLocation, txtFilterCompany, txtFilterIndustry, txtFilterSalary;
    private Button btnExecFilter;
    private String strHighestEducatioIDs, strSkillsIDs, strLanguagesIDs;
    private ArrayList<String> arrHighestEducatioIDs, arrSkillsIDs, arrLanguagesIDs;
    private EditText txtName;
    private EditText txtEmail;
    private EditText txtPhone;
    private EditText txtPassword;
    private EditText txtConfirmPassword;
    private LinearLayout layoutHighestEducation, layoutSkills, layoutLanguages;
    private TextView txtHighestEducation, txtSkills, txtLanguages;
    private Button btnUpdateProfile;

    private Typeface font, fontBold;

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_jobs);
        context = this;
        font = Typeface.createFromAsset(context.getAssets(), "fonts/Raleway-Light.ttf");
        fontBold = Typeface.createFromAsset(context.getAssets(), "fonts/Raleway-Bold.ttf");
        user_id = Commons.getSharedPreferences(context).getString("user_id", "");
        findViewById(R.id.imgLogout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Logout?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Commons.clearCache(context);
                        finish();
                        startActivity(new Intent(context, SplashActivity.class));
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.create().show();
            }
        });
        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        layoutJobsContainer = (ListView) findViewById(R.id.layoutJobsContainer);
        layoutSavedJobsContainer = (LinearLayout) findViewById(R.id.layoutSavedJobsContainer);
        layoutAppliedJobsContainer = (LinearLayout) findViewById(R.id.layoutAppliedJobsContainer);

        txtTabHome = (TextView) findViewById(R.id.txtTabHome);
        txtTabSaved = (TextView) findViewById(R.id.txtTabSaved);
        txtTabProfile = (TextView) findViewById(R.id.txtTabProfile);

        txtTabHome.setTypeface(font);
        txtTabSaved.setTypeface(font);
        txtTabProfile.setTypeface(font);

        imgTabFilter = (ImageView) findViewById(R.id.imgTabFilter);
        imgTabHome = (ImageView) findViewById(R.id.imgTabHome);
        imgTabSaved = (ImageView) findViewById(R.id.imgTabSaved);
        imgTabProfile = (ImageView) findViewById(R.id.imgTabProfile);

        layoutFilter = (AnimatedLinearLayout) findViewById(R.id.layoutFilter);
        Animation inAnimation = AnimationUtils.loadAnimation(context, R.anim.abc_fade_in);
//        Animation outAnimation = AnimationUtils.loadAnimation(context, R.anim.abc_fade_out);
        layoutFilter.setInAnimation(inAnimation);
//        layoutFilter.setOutAnimation(outAnimation);

        viewCount = viewFlipper.getChildCount();
        refreshJobsList();

        findViewById(R.id.tabHome).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPage(0);
                imgTabFilter.setVisibility(View.VISIBLE);
                refreshJobsList();
                txtTabHome.setTextColor(getResources().getColor(R.color.pallet_coral_red_light));
                txtTabSaved.setTextColor(getResources().getColor(R.color.white));
                txtTabProfile.setTextColor(getResources().getColor(R.color.white));

                // hide filter first
                if (layoutFilter.isShown()) {
                    layoutFilter.setVisibility(View.GONE);
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        imgTabFilter.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_filter_white));
                    } else {
                        imgTabFilter.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_filter_white, null));
                    }
                    return;
                }

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    imgTabHome.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_home));
                    imgTabSaved.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_saved_white));
                    imgTabProfile.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_profile_white));
                } else {
                    imgTabHome.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_home, null));
                    imgTabSaved.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_saved_white, null));
                    imgTabProfile.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_profile_white, null));
                }
            }

        });

        findViewById(R.id.tabFilter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewFlipper.getDisplayedChild() != 0) {
                    if (Commons.SHOW_DEBUG_MSGS)
                        Log.d(TAG, "View jobs to filter them");
                    return;
                }
                if (layoutFilter.isShown()) {
                    layoutFilter.setVisibility(View.GONE);
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        imgTabFilter.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_filter_white));
                    } else {
                        imgTabFilter.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_filter_white, null));
                    }

                } else {
                    layoutFilter.setVisibility(View.VISIBLE);
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        imgTabFilter.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_filter));
                    } else {
                        imgTabFilter.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_filter, null));
                    }
                }
            }

        });

        findViewById(R.id.btnCloseFilter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutFilter.setVisibility(View.GONE);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    imgTabFilter.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_filter_white));
                } else {
                    imgTabFilter.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_filter_white, null));
                }
            }
        });

        findViewById(R.id.tabSaved).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPage(1);
                imgTabFilter.setVisibility(View.INVISIBLE);
                loadSavedAndAppliedJobs();
                txtTabSaved.setTextColor(getResources().getColor(R.color.pallet_coral_red_light));
                txtTabHome.setTextColor(getResources().getColor(R.color.white));
                txtTabProfile.setTextColor(getResources().getColor(R.color.white));

                // hide filter first
                if (layoutFilter.isShown()) {
                    layoutFilter.setVisibility(View.GONE);
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        imgTabFilter.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_filter_white));
                    } else {
                        imgTabFilter.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_filter_white, null));
                    }
                    return;
                }

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    imgTabSaved.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_saved));
                    imgTabHome.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_home_white));
                    imgTabProfile.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_profile_white));
                } else {
                    imgTabSaved.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_saved, null));
                    imgTabHome.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_home_white, null));
                    imgTabProfile.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_profile_white, null));
                }
                return;
            }

        });

        findViewById(R.id.tabProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPage(2);
                imgTabFilter.setVisibility(View.GONE);

                txtTabProfile.setTextColor(getResources().getColor(R.color.pallet_coral_red_light));
                txtTabSaved.setTextColor(getResources().getColor(R.color.white));
                txtTabHome.setTextColor(getResources().getColor(R.color.white));

                // hide filter first
                if (layoutFilter.isShown()) {
                    layoutFilter.setVisibility(View.GONE);
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        imgTabFilter.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_filter_white));
                    } else {
                        imgTabFilter.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_filter_white, null));
                    }
                    return;
                }

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    imgTabProfile.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_profile));
                    imgTabSaved.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_saved_white));
                    imgTabHome.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_home_white));
                } else {
                    imgTabProfile.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_profile, null));
                    imgTabSaved.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_saved_white, null));
                    imgTabHome.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_home_white, null));
                }

                loadProfileData();
                return;
            }

        });

        layoutFilterLocation = (LinearLayout) findViewById(R.id.layoutFilterLocation);
        layoutFilterCompany = (LinearLayout) findViewById(R.id.layoutFilterCompany);
        layoutFilterIndustry = (LinearLayout) findViewById(R.id.layoutFilterIndustry);
        layoutFilterSalary = (LinearLayout) findViewById(R.id.layoutFilterSalary);

        txtFilterLocation = (TextView) findViewById(R.id.txtFilterLocation);
        txtFilterCompany = (TextView) findViewById(R.id.txtFilterCompany);
        txtFilterIndustry = (TextView) findViewById(R.id.txtFilterIndustry);
        txtFilterSalary = (TextView) findViewById(R.id.txtFilterSalary);

        txtFilterLocation.setTypeface(font);
        txtFilterCompany.setTypeface(font);
        txtFilterIndustry.setTypeface(font);
        txtFilterSalary.setTypeface(font);

        btnExecFilter = (Button) findViewById(R.id.btnExecFilter);
        btnExecFilter.setTypeface(fontBold);

        layoutFilterLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DialogMultiselect(
                        context,
                        SimpleContainer.CONTAINER_TYPE_LOCATIONS,
                        Commons.locationList.getElementList(),
                        arrFilterLocationIDs
                )
                        .show(getFragmentManager(), "");
            }
        });

        layoutFilterCompany.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DialogMultiselect(
                        context,
                        SimpleContainer.CONTAINER_TYPE_COMPANIES,
                        Commons.companyList.getElementList(),
                        arrFilterCompanyIDs
                ).show(getFragmentManager(), "");
            }
        });

        layoutFilterIndustry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DialogMultiselect(
                        context,
                        SimpleContainer.CONTAINER_TYPE_INDUSTRIES,
                        Commons.industryList.getElementList(),
                        arrFilterIndustryIDs
                ).show(getFragmentManager(), "");
            }
        });

        layoutFilterSalary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DialogMultiselect(
                        context,
                        SimpleContainer.CONTAINER_TYPE_SALARY_RANGES,
                        Commons.salaryRangeList.getElementList(),
                        arrFilterSalaryIDs
                ).show(getFragmentManager(), "");
            }
        });

        btnExecFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Commons.IS_NETWORK_TEST_EMULATED) {
//            callbackGetJobsRequest(TestNetwork.request(
//                            TestNetwork.getSampleGetJobsRequest(TestNetwork.TYPE_DEFAULT))
//            );
                } else {
                    final ProgressDialog progress = Commons.getCustomProgressDialog(context);

                    Builders.Any.B a = Ion.with(getApplicationContext())
                            .load(Commons.HTTP_GET, Commons.URL_JOBS)
                            .setLogging("Ion Request", Log.DEBUG)
                                    // location, company, industry, salary

                            .followRedirect(true);


                    Builders.Any.U b = null;
                    if (strFilterLocation != null) if (!strFilterLocation.isEmpty())
                        b = a.setBodyParameter("filter_location", strFilterLocation);
                    if (strFilterCompany != null) if (!strFilterCompany.isEmpty())
                        b = (b == null ? a : b).setBodyParameter("filter_company", strFilterCompany);
                    if (strFilterIndustry != null) if (!strFilterIndustry.isEmpty())
                        b = (b == null ? a : b).setBodyParameter("filter_industry", strFilterIndustry);
                    if (strFilterSalary != null) if (!strFilterSalary.isEmpty())
                        b = (b == null ? a : b).setBodyParameter("filter_salary", strFilterSalary);
                    (b == null ? a : b).asJsonArray()
                            .setCallback(new FutureCallback<JsonArray>() {
                                @Override
                                public void onCompleted(Exception e, JsonArray result) {
                                    progress.dismiss();
                                    callbackGetJobsRequest(result);
                                }
                            });
                    //TODO Actual network request here; Callbacks: callbackInitRequest()
                }
                layoutFilter.setVisibility(View.GONE);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    imgTabFilter.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_filter_white));
                } else {
                    imgTabFilter.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_filter_white, null));
                }
            }
        });

        txtName = (EditText) findViewById(R.id.txtName);
        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtPhone = (EditText) findViewById(R.id.txtPhone);

        txtHighestEducation = (TextView) findViewById(R.id.txtHighestEducation);
        txtSkills = (TextView) findViewById(R.id.txtSkills);
        txtLanguages = (TextView) findViewById(R.id.txtLanguages);

        txtName.setTypeface(font);
        txtEmail.setTypeface(font);
        txtPhone.setTypeface(font);
        txtHighestEducation.setTypeface(font);
        txtSkills.setTypeface(font);
        txtLanguages.setTypeface(font);

        layoutHighestEducation = (LinearLayout) findViewById(R.id.layoutHighestEducation);
        layoutSkills = (LinearLayout) findViewById(R.id.layoutSkills);
        layoutLanguages = (LinearLayout) findViewById(R.id.layoutLanguages);

        layoutHighestEducation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DialogMultiselect(
                        context,
                        SimpleContainer.CONTAINER_TYPE_EDUCATION2,
                        Commons.educationList.getElementList(),
                        arrHighestEducatioIDs
                )
                        .show(getFragmentManager(), "");
            }
        });

        layoutSkills.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DialogMultiselect(
                        context,
                        SimpleContainer.CONTAINER_TYPE_SKILLS2,
                        Commons.skillsList.getElementList(),
                        arrSkillsIDs
                ).show(getFragmentManager(), "");
            }
        });

        layoutLanguages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DialogMultiselect(
                        context,
                        SimpleContainer.CONTAINER_TYPE_LANGUAGES2,
                        Commons.languageList.getElementList(),
                        arrLanguagesIDs
                ).show(getFragmentManager(), "");
            }
        });

        btnUpdateProfile = (Button) findViewById(R.id.btnUpdateProfile);
        btnUpdateProfile.setTypeface(fontBold);
        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progress = Commons.getCustomProgressDialog(context);
                JsonObject requestJson = new JsonObject();
                requestJson.addProperty("name", txtName.getText().toString().trim());
                requestJson.addProperty("email", txtEmail.getText().toString().trim());
                requestJson.addProperty("phone", txtPhone.getText().toString().trim());
                requestJson.addProperty("highest_education", strHighestEducatioIDs);
                requestJson.addProperty("skills", strSkillsIDs);
                requestJson.addProperty("language", strLanguagesIDs);
                Log.d("Ion Request", "Request Json is : " + requestJson.toString());
                Ion.with(getApplicationContext())
                        .load(Commons.HTTP_PUT, Commons.URL_USERS + "/" + Commons.getSharedPreferences(context).getString("user_id", ""))
                        .setLogging("Ion Request", Log.DEBUG)
                        .followRedirect(true)
                        .setJsonObjectBody(requestJson)
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                Log.d("Ion Request", "Completed");
                                progress.dismiss();
                                if (e != null) {
                                    e.printStackTrace();
                                    return;
                                }
                                if (Commons.SHOW_DEBUG_MSGS)
                                    Log.d(TAG, "Ion Request " + result.toString());
                                if (Commons.SHOW_TOAST_MSGS)
                                    Toast.makeText(context, "Ion Request " + result.toString(), Toast.LENGTH_LONG).show();

                                callbackProfileUpdateRequest(result);
                            }
                        });
            }
        });

    }

    private void callbackProfileUpdateRequest(JsonObject responseObject) {
        if (responseObject.has("id")) {
            if (Commons.SHOW_DEBUG_MSGS)
                Log.d(TAG, "Update succeeded");
            if (Commons.SHOW_TOAST_MSGS)
                Toast.makeText(context, "Update succeeded", Toast.LENGTH_LONG).show();
        } else {
            if (Commons.SHOW_DEBUG_MSGS)
                Log.d(TAG, "Update failed");
            if (Commons.SHOW_TOAST_MSGS)
                Toast.makeText(context, "Update failed", Toast.LENGTH_LONG).show();
        }
    }

    private void loadSavedAndAppliedJobs() {
        if (Commons.IS_NETWORK_TEST_EMULATED) {
//            callbackGetSavedAppliedJobsRequest(TestNetwork.request(
//                            TestNetwork.getSampleGetJSavedAppliedobsRequest(TestNetwork.TYPE_DEFAULT))
//            );
        } else {
            //TODO Actual network request here; Callbacks: callbackInitRequest()
            final ProgressDialog progress = Commons.getCustomProgressDialog(context);
            Ion.with(getApplicationContext())
                    .load(Commons.HTTP_GET, Commons.URL_SAVED_APPLIED_JOBS)
                    .setLogging("Ion Request", Log.DEBUG)
                    .followRedirect(true)
                    .asJsonArray()
                    .setCallback(new FutureCallback<JsonArray>() {
                        @Override
                        public void onCompleted(Exception e, JsonArray result) {
                            progress.dismiss();
                            callbackGetSavedAppliedJobsRequest(result);
                        }
                    });
        }
    }

    private void refreshJobsList() {
        if (Commons.IS_NETWORK_TEST_EMULATED) {
//            callbackGetJobsRequest(TestNetwork.request(
//                            TestNetwork.getSampleGetJobsRequest(TestNetwork.TYPE_DEFAULT))
//            );
        } else {
            final ProgressDialog progress = Commons.getCustomProgressDialog(context);
            Ion.with(getApplicationContext())
                    .load(Commons.HTTP_GET, Commons.URL_JOBS)
                    .setLogging("Ion Request", Log.DEBUG)
                    .followRedirect(true)
                    .asJsonArray()
                    .setCallback(new FutureCallback<JsonArray>() {
                        @Override
                        public void onCompleted(Exception e, JsonArray result) {
                            progress.dismiss();
                            callbackGetJobsRequest(result);
                        }
                    });
            //TODO Actual network request here; Callbacks: callbackInitRequest()
        }
    }

    private void loadProfileData() {
        SharedPreferences prefs = Commons.getSharedPreferences(context);
        txtName.setText(prefs.getString("name", ""));
        txtEmail.setText(prefs.getString("email", ""));
        txtPhone.setText(prefs.getString("phone", ""));

        strHighestEducatioIDs = prefs.getString("highest_education", "");
        strSkillsIDs = prefs.getString("skills", "");
        strLanguagesIDs = prefs.getString("language", "");

        arrHighestEducatioIDs = new ArrayList<>(Arrays.asList(strHighestEducatioIDs.split(",")));
        txtHighestEducation.setText("");
        if (arrHighestEducatioIDs.size() == 0)
            txtHighestEducation.setText("None Selected");
        for (int i = 0; i < arrHighestEducatioIDs.size(); i++) {
            txtHighestEducation
                    .append(Commons.educationList.getElementList().get(i).getTitle());
            if (i != arrHighestEducatioIDs.size() - 1)
                txtHighestEducation.append(", ");
        }

        arrSkillsIDs = new ArrayList<>(Arrays.asList(strSkillsIDs.split(",")));
        txtSkills.setText("");
        if (arrSkillsIDs.size() == 0)
            txtSkills.setText("None Selected");
        for (int i = 0; i < arrSkillsIDs.size(); i++) {
            txtSkills
                    .append(Commons.skillsList.getElementList().get(i).getTitle());
            if (i != arrSkillsIDs.size() - 1)
                txtSkills.append(", ");
        }

        arrLanguagesIDs = new ArrayList<>(Arrays.asList(strLanguagesIDs.split(",")));
        txtLanguages.setText("");
        if (arrLanguagesIDs.size() == 0)
            txtLanguages.setText("None Selected");
        for (int i = 0; i < arrLanguagesIDs.size(); i++) {
            txtLanguages
                    .append(Commons.languageList.getElementList().get(i).getTitle());
            if (i != arrLanguagesIDs.size() - 1)
                txtLanguages.append(", ");
        }
    }

    private void callbackGetJobsRequest(JsonArray responseObject) {
        System.out.println(responseObject.toString());
        jobs = new JobsContainer(responseObject);
        if (jobs.getListJobs().size() != 0) {
            JobsAdapter jobsAdapter = new JobsAdapter(context, jobs.getListJobs());
            layoutJobsContainer.setAdapter(jobsAdapter);
            layoutJobsContainer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    Intent intent = new Intent(context, JobActivity.class);
                    intent.putExtra("position", position);
                    startActivity(intent);
                }
            });
        } else {
            if (Commons.SHOW_DEBUG_MSGS)
                Log.d(TAG, "No Jobs to show");
            if (Commons.SHOW_TOAST_MSGS)
                Toast.makeText(context, "No Jobs to show", Toast.LENGTH_LONG).show();
        }

    }

    private void callbackGetSavedAppliedJobsRequest(JsonArray responseObject) {
        System.out.println(responseObject.toString());
        final List<JobsContainer.Job> savedJobs = new ArrayList<>();
        final List<JobsContainer.Job> appliedJobs = new ArrayList<>();
        final SavedAppliedJobsContainer objSavedAppliedJobs = new SavedAppliedJobsContainer(responseObject);
        for (int i = 0; i < jobs.getListJobs().size(); i++) {

            JobsContainer.Job job = jobs.getJob(i);
            if (Commons.SHOW_DEBUG_MSGS)
                Log.d(TAG, "Matching job " + i + " with job_id(" + job.getJob_id() + ") and user_id(" + user_id + ")");
            SavedAppliedJobsContainer.SavedAppliedStub match = objSavedAppliedJobs.getMatchingSavedAppliedStub(job.getJob_id(), user_id);
            if (match != null) {
                if (Commons.SHOW_DEBUG_MSGS)
                    Log.d(TAG, "Match found: Match status is " + match.getStatus());
                if (match.getStatus().equals(SavedAppliedJobsContainer.JOB_STATUS_SAVED)) {
                    savedJobs.add(job);
                    if (Commons.SHOW_DEBUG_MSGS)
                        Log.d(TAG, "Added saved job");
                }
                if (match.getStatus().equals(SavedAppliedJobsContainer.JOB_STATUS_APPLIED)) {
                    appliedJobs.add(job);
                    if (Commons.SHOW_DEBUG_MSGS)
                        Log.d(TAG, "Added applied job");
                }
            }
        }

        if (jobs.getListJobs().size() != 0) {
            if (Commons.SHOW_DEBUG_MSGS)
                Log.d(TAG, "Rendering jobs");
            if (Commons.SHOW_DEBUG_MSGS)
                Log.d(TAG, "Get Saved/Applied Jobs request succeeded");
            if (Commons.SHOW_TOAST_MSGS)
                Toast.makeText(context, "Get Saved/Applied Jobs request succeeded", Toast.LENGTH_LONG).show();

            layoutSavedJobsContainer.removeAllViews();
            layoutAppliedJobsContainer.removeAllViews();
            TextView lblJobTitle, lblJobLocation, lblPostDate;
            for (int i = 0; i < savedJobs.size(); i++) {
                if (Commons.SHOW_DEBUG_MSGS)
                    Log.d(TAG, "Rendered saved job");
                View rowView = LayoutInflater.from(context).inflate(R.layout.layout_job_concrete, null);

                lblJobTitle = (TextView) rowView.findViewById(R.id.lblJobTitle);
                lblJobLocation = (TextView) rowView.findViewById(R.id.lblJobLocation);
                lblPostDate = (TextView) rowView.findViewById(R.id.lblPostDate);

                lblJobTitle.setTypeface(fontBold);
                lblJobLocation.setTypeface(fontBold);
                lblPostDate.setTypeface(fontBold);

                JobsContainer.Job jobA = savedJobs.get(i);
                lblJobTitle.setText(jobA.getTitle());
                lblJobLocation.setText(Commons.locationList.getBlockByID(jobA.getLocation_city()).getTitle());
                lblPostDate.setText(jobA.getPosted_date());
                final int pos = i;
                layoutSavedJobsContainer.addView(rowView);
                rowView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, JobActivity.class);
                        intent.putExtra("position", jobs.getPosition(savedJobs.get(pos).getJob_id()));
                        startActivity(intent);
                    }
                });
            }
            for (int i = 0; i < appliedJobs.size(); i++) {
                if (Commons.SHOW_DEBUG_MSGS)
                    Log.d(TAG, "Rendered applied job");
                View rowView = LayoutInflater.from(context).inflate(R.layout.layout_job_concrete, null);

                lblJobTitle = (TextView) rowView.findViewById(R.id.lblJobTitle);
                lblJobLocation = (TextView) rowView.findViewById(R.id.lblJobLocation);
                lblPostDate = (TextView) rowView.findViewById(R.id.lblPostDate);

                lblJobTitle.setTypeface(fontBold);
                lblJobLocation.setTypeface(fontBold);
                lblPostDate.setTypeface(fontBold);

                JobsContainer.Job jobA = appliedJobs.get(i);
                lblJobTitle.setText(jobA.getTitle());
                lblJobLocation.setText(Commons.locationList.getBlockByID(jobA.getLocation_city()).getTitle());
                lblPostDate.setText(jobA.getPosted_date());
                final int pos = i;
                layoutAppliedJobsContainer.addView(rowView);
                rowView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, JobActivity.class);
                        intent.putExtra("position", jobs.getPosition(appliedJobs.get(pos).getJob_id()));
                        startActivity(intent);
                    }
                });
            }
        } else {
            if (Commons.SHOW_DEBUG_MSGS)
                Log.d(TAG, "No jobs to render");
            if (Commons.SHOW_DEBUG_MSGS)
                Log.d(TAG, "Get Saved/Applied Jobs request succeeded");
            if (Commons.SHOW_TOAST_MSGS)
                Toast.makeText(context, "Get Saved/Applied Jobs request succeeded", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_jobs, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_clear_list) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setPage(int i) {
        System.out.println(i + " " + viewFlipper.getDisplayedChild());
        if (i > viewFlipper.getDisplayedChild()) {
            viewFlipper.setInAnimation(context, R.anim.slide_left_in);
            viewFlipper.setOutAnimation(context, R.anim.slide_left_out);
            while (i > viewFlipper.getDisplayedChild()) {
                viewFlipper.showNext();
            }
        } else if (i < viewFlipper.getDisplayedChild()) {
            viewFlipper.setInAnimation(context, R.anim.slide_right_in);
            viewFlipper.setOutAnimation(context, R.anim.slide_right_out);
            while (i < viewFlipper.getDisplayedChild()) {
                viewFlipper.showPrevious();
            }
        }
    }

    public void onMultiselectCompleted(int tag, ArrayList<String> ids) {
        String strIDs = "";
        for (int i = 0; i < ids.size(); i++) {
            strIDs += ids.get(i) + ((i != ids.size() - 1) ? "," : "");
        }
        System.out.println("Returned IDs" + strIDs);
        switch (tag) {
            case SimpleContainer.CONTAINER_TYPE_LOCATIONS:
                arrFilterLocationIDs = ids;
                strFilterLocation = strIDs;
                break;
            case SimpleContainer.CONTAINER_TYPE_COMPANIES:
                arrFilterCompanyIDs = ids;
                strFilterCompany = strIDs;
                break;
            case SimpleContainer.CONTAINER_TYPE_INDUSTRIES:
                arrFilterIndustryIDs = ids;
                strFilterIndustry = strIDs;
                break;
            case SimpleContainer.CONTAINER_TYPE_SALARY_RANGES:
                arrFilterSalaryIDs = ids;
                strFilterSalary = strIDs;
                break;
            case SimpleContainer.CONTAINER_TYPE_EDUCATION2:
                arrHighestEducatioIDs = ids;
                strHighestEducatioIDs = strIDs;
                break;
            case SimpleContainer.CONTAINER_TYPE_SKILLS2:
                arrSkillsIDs = ids;
                strSkillsIDs = strIDs;
                break;
            case SimpleContainer.CONTAINER_TYPE_LANGUAGES2:
                arrLanguagesIDs = ids;
                strLanguagesIDs = strIDs;
                break;
            default:
                return;
        }
        refreshStoresText(tag, ids);
    }

    private void refreshStoresText(int tag, ArrayList<String> ids) {
        TextView textViewToUpdate;
        SimpleContainer listElements = null;
        CompanyContainer companyList = null;
        switch (tag) {
            case SimpleContainer.CONTAINER_TYPE_LOCATIONS:
                textViewToUpdate = txtFilterLocation;
                listElements = Commons.locationList;
                break;
            case SimpleContainer.CONTAINER_TYPE_COMPANIES:
                textViewToUpdate = txtFilterCompany;
                companyList = Commons.companyList;
                break;
            case SimpleContainer.CONTAINER_TYPE_INDUSTRIES:
                listElements = Commons.industryList;
                textViewToUpdate = txtFilterIndustry;
                break;
            case SimpleContainer.CONTAINER_TYPE_SALARY_RANGES:
                listElements = Commons.salaryRangeList;
                textViewToUpdate = txtFilterSalary;
                break;
            case SimpleContainer.CONTAINER_TYPE_EDUCATION2:
                textViewToUpdate = txtHighestEducation;
                listElements = Commons.educationList;
                break;
            case SimpleContainer.CONTAINER_TYPE_SKILLS2:
                textViewToUpdate = txtSkills;
                listElements = Commons.skillsList;
                break;
            case SimpleContainer.CONTAINER_TYPE_LANGUAGES2:
                listElements = Commons.languageList;
                textViewToUpdate = txtLanguages;
                break;
            default:
                return;
        }
        if (ids == null) {
            textViewToUpdate.setText("None Selected");
            return;
        }
        textViewToUpdate.setText("");
        if (ids.size() == 0)
            textViewToUpdate.setText("None Selected");
        for (int i = 0; i < ids.size(); i++) {
            if (listElements != null)
                textViewToUpdate
                        .append(listElements.getBlockByID(ids.get(i)).getTitle());
            else if (companyList != null)
                textViewToUpdate
                        .append(companyList.getCompanyByID(ids.get(i)).getName());
            if (i != ids.size() - 1)
                textViewToUpdate.append(", ");
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        // hide filter first
        if (layoutFilter.isShown()) {
            layoutFilter.setVisibility(View.GONE);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                imgTabFilter.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_filter_white));
            } else {
                imgTabFilter.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_filter_white, null));
            }
            return;
        }

        doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT)
                .show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2500);
    }

}
