package br.com.naoligo;

import java.io.IOException;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

public class BaseActivity extends Activity {

	private ProgressDialog dialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dialog = new ProgressDialog(this);
		dialog.setTitle(getApplicationContext().getString(R.string.app_name));
		dialog.setMessage("Aguarde...");
	}

	class CallWebServiceTask extends AsyncTask<String[], Void, String> {

		public CallWebServiceTask(final Activity activity, final String soapAction) {
			this.soapAction = soapAction;
			this.activity = activity;
		}

		private final String soapAction;

		private final Activity activity;

		private SoapObject request;

		@Override
		protected void onPreExecute() {
			dialog.show();
		}

		@Override
		protected String doInBackground(final String[]... params) {
			request = new SoapObject(Contants.NAMESPACE, this.soapAction);

			for (final String[] p : params) {
				request.addProperty(p[0], p[1]);
			}

			final SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.setOutputSoapObject(request);
			final HttpTransportSE androidHttpTransport = new HttpTransportSE(Contants.URL);

			try {
				androidHttpTransport.call(this.soapAction, envelope);

				return (String) envelope.getResponse();
			} catch (final IOException e) {
				return "Serviço temporariamente indisponível";
			} catch (final Exception e) {
				return e.getMessage();
			}
		}

		@Override
		protected void onPostExecute(final String result) {
			if (soapAction == Contants.METHOD_NAME_LOGIN) {
				if ("ok".equals(result)) {
					final SharedPreferences settings = getApplicationContext().getSharedPreferences(
							Contants.PREFS_NAME, 0);
					final SharedPreferences.Editor editor = settings.edit();
					editor.putString("email", request.getPropertyAsString("email"));
					editor.putString("password", request.getPropertyAsString("senha"));
					editor.commit();
					setResult(RESULT_OK);
					finish();
				} else {
					showDialog(activity, result);
				}
			} else if (soapAction == Contants.METHOD_NAME_SEND_SMS) {
				final ContentValues values = new ContentValues();
				values.put("address", request.getPropertyAsString("ddd") + request.getPropertyAsString("telefone"));
				values.put("body", request.getPropertyAsString("texto") + " via NaoLigo");
				if (result.indexOf("Erro") >= 0) {
					getApplicationContext().getContentResolver().insert(Uri.parse("content://sms/failed"), values);
					showDialog(activity, result);
				} else {
					getApplicationContext().getContentResolver().insert(Uri.parse("content://sms/sent"), values);
					showDialog(activity, "SMS enviado com sucesso!");
				}
			}
			dialog.cancel();
		}
	}

	public void showDialog(final Activity activity, final String message) {
		final AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
		alertDialog.setTitle(getApplicationContext().getString(R.string.app_name));
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				alertDialog.hide();
			}
		});
		alertDialog.setIcon(R.drawable.icon);

		alertDialog.setMessage(message);
		alertDialog.show();
	}
}