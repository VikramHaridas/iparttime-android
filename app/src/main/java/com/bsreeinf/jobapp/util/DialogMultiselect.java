package com.bsreeinf.jobapp.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.bsreeinf.jobapp.JobsActivity;
import com.bsreeinf.jobapp.R;
import com.bsreeinf.jobapp.RegisterUser;

import java.util.ArrayList;
import java.util.List;

public class DialogMultiselect extends DialogFragment {
    Context context;

    private List<SimpleContainer.SimpleBlock> listElemnts;
    private int tag;
    private LinearLayout listItems;
    private AlertDialog.Builder builder1;
    private AlertDialog dialog;
    private ArrayList<String> ids;

    public DialogMultiselect(final Context c, final int tag, final List<SimpleContainer.SimpleBlock> listElemnts,
                             final ArrayList<String> ids) {
        this.context = c;
        this.ids = ids;
        this.listElemnts = listElemnts;
        this.tag = tag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        builder1 = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(
                R.layout.dialog_multiselect, null);
        listItems = (LinearLayout) view.findViewById(R.id.listItems);
        fillSpinnerData();
        switch (tag) {
            case SimpleContainer.CONTAINER_TYPE_EDUCATION:
            case SimpleContainer.CONTAINER_TYPE_EDUCATION2:
                builder1.setTitle("Select Education");
                break;
            case SimpleContainer.CONTAINER_TYPE_SKILLS:
            case SimpleContainer.CONTAINER_TYPE_SKILLS2:
                builder1.setTitle("Select Skills");
                break;
            case SimpleContainer.CONTAINER_TYPE_LANGUAGES:
            case SimpleContainer.CONTAINER_TYPE_LANGUAGES2:
                builder1.setTitle("Select Languages");
                break;
            case SimpleContainer.CONTAINER_TYPE_LOCATIONS:
                builder1.setTitle("Select Locations");
                break;
            case SimpleContainer.CONTAINER_TYPE_COMPANIES:
                builder1.setTitle("Select Companies");
                break;
            case SimpleContainer.CONTAINER_TYPE_INDUSTRIES:
                builder1.setTitle("Select Industries");
                break;
            case SimpleContainer.CONTAINER_TYPE_SALARY_RANGES:
                builder1.setTitle("Select Salary Ranges");
                break;
        }

        builder1.setView(view);
        builder1.setCancelable(true);

        builder1.setPositiveButton("DONE",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (tag == SimpleContainer.CONTAINER_TYPE_EDUCATION ||
                                tag == SimpleContainer.CONTAINER_TYPE_EDUCATION2) {
                            int count = 0;
                            for (int i = 0; i < listItems.getChildCount(); i++) {
                                View v = listItems.getChildAt(i);
                                if (v instanceof LinearLayout) {
                                    Switch temp = (Switch) ((LinearLayout) v)
                                            .getChildAt(0);
                                    if (temp.isChecked()) {
                                        count++;
                                    }
                                }
                            }
                            if (count > 1) {
                                Toast.makeText(context, "Select only one", Toast.LENGTH_SHORT).show();
                                return;
                            } else if (count == 0) {
                                Toast.makeText(context, "Select at least one", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        ids = new ArrayList<>();
                        for (int i = 0; i < listItems.getChildCount(); i++) {
                            View v = listItems.getChildAt(i);
                            if (v instanceof LinearLayout) {
                                Switch temp = (Switch) ((LinearLayout) v)
                                        .getChildAt(0);
                                if (temp.isChecked()) {
                                    ids.add(listElemnts.get(i).getId());
                                    System.out.println("id " + i + ": " + listElemnts.get(i).getId());
                                }
                            }
                        }
                        switch (tag) {
                            case SimpleContainer.CONTAINER_TYPE_EDUCATION:
                            case SimpleContainer.CONTAINER_TYPE_SKILLS:
                            case SimpleContainer.CONTAINER_TYPE_LANGUAGES:
                                ((RegisterUser) context).onMultiselectCompleted(tag, ids);
                                break;
                            case SimpleContainer.CONTAINER_TYPE_LOCATIONS:
                            case SimpleContainer.CONTAINER_TYPE_COMPANIES:
                            case SimpleContainer.CONTAINER_TYPE_INDUSTRIES:
                            case SimpleContainer.CONTAINER_TYPE_SALARY_RANGES:
                            case SimpleContainer.CONTAINER_TYPE_EDUCATION2:
                            case SimpleContainer.CONTAINER_TYPE_SKILLS2:
                            case SimpleContainer.CONTAINER_TYPE_LANGUAGES2:
                                ((JobsActivity) context).onMultiselectCompleted(tag, ids);
                                break;
                        }

                        dialog.cancel();
                    }
                });

        dialog = builder1.create();
        return dialog;

    }

    private void fillSpinnerData() {
        for (int i = 0; i < listElemnts.size(); i++) {
            LinearLayout switchLayout = (LinearLayout) LayoutInflater.from(
                    context).inflate(R.layout.row_switch, null);
            Switch viewSwitch = (Switch) switchLayout.findViewById(R.id.viewSwitch);
            viewSwitch.setText(listElemnts.get(i).getTitle());
            if (ids != null) {
                viewSwitch.setChecked(false);
                for (int j = 0; j < ids.size(); j++) {
                    if (ids.get(j).equals(listElemnts.get(i).getId())) {
                        viewSwitch.setChecked(true);
                        break;
                    }
                }
            }
            listItems.addView(switchLayout);
        }
    }
}
