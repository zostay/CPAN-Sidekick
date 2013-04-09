package com.qubling.sidekick.ui

import com.qubling.sidekick.R

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.TextView

object AboutDialogFragment {
  val LinkReportABug    = "https://github.com/zostay/CPAN-Sidekick/issues/new"
  val LinkAboutMetaCPAN = "https://metacpan.org/about"
}

class AboutDialogFragment extends DialogFragment {

  override def onCreateDialog(state : Bundle) : Dialog = {
    val builder = new AlertDialog.Builder(getActivity())
    val inflator = getActivity().getLayoutInflater()

    val dialogView = inflator.inflate(R.layout.about_cpan_sidekick, null)

    val reportBug = dialogView.findViewById(R.id.link_report_a_bug).asInstanceOf[TextView]
        reportBug.setText(Html.fromHtml(getResources().getString(R.string.about_report_a_bug)))
        reportBug.setOnClickListener(new OnClickListener {

          override def onClick(v : View) {
            val intent = new Intent
            intent.setAction(Intent.ACTION_VIEW)
            intent.setData(Uri.parse(AboutDialogFragment.LinkReportABug))
            startActivity(intent)
          }

        })

    val aboutMetaCpan = dialogView.findViewById(R.id.link_about_metacpan).asInstanceOf[TextView]
        aboutMetaCpan.setText(Html.fromHtml(getResources().getString(R.string.about_metacpan)))
        aboutMetaCpan.setOnClickListener(new OnClickListener {

          override def onClick(v : View) {
            val intent = new Intent
            intent.setAction(Intent.ACTION_VIEW)
            intent.setData(Uri.parse(AboutDialogFragment.LinkAboutMetaCPAN))
            startActivity(intent)
          }
        })

    builder.setView(dialogView)
           .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener {
             override def onClick(dialog : DialogInterface, which : Int) {
               AboutDialogFragment.this.getDialog.dismiss()
            }
          })
        
    return builder.create()
  }
}

