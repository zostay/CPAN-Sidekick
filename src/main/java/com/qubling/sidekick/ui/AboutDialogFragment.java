package com.qubling.sidekick.ui;

import com.qubling.sidekick.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class AboutDialogFragment extends DialogFragment {
    public static final String LINK_REPORT_A_BUG = "https://github.com/zostay/CPAN-Sidekick/issues/new";
    public static final String LINK_ABOUT_METACPAN = "https://metacpan.org/about";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflator = getActivity().getLayoutInflater();
        
        View dialogView = inflator.inflate(R.layout.about_cpan_sidekick, null);
        
        TextView reportBug = (TextView) dialogView.findViewById(R.id.link_report_a_bug);
        reportBug.setText(Html.fromHtml(getResources().getString(R.string.about_report_a_bug)));
        reportBug.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(LINK_REPORT_A_BUG));
                startActivity(intent);
            }
        });
        
        TextView aboutMetaCpan = (TextView) dialogView.findViewById(R.id.link_about_metacpan);
        aboutMetaCpan.setText(Html.fromHtml(getResources().getString(R.string.about_metacpan)));
        aboutMetaCpan.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(LINK_ABOUT_METACPAN));
                startActivity(intent);
            }
        });
        
        builder.setView(dialogView)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AboutDialogFragment.this.getDialog().dismiss();
                }
            });
        
        return builder.create();
    }

}
