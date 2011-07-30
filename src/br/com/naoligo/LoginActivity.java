package br.com.naoligo;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login);

		final Button btLogon = (Button) this.findViewById(R.id.btnLogin);
		final Button btClose = (Button) this.findViewById(R.id.btnClose);
		final EditText editemail = (EditText) this.findViewById(R.id.editEmail);
		final EditText editpassword = (EditText) this
				.findViewById(R.id.editPassword);

		SharedPreferences settings = getSharedPreferences(Contants.PREFS_NAME,
				0);
		final String email = settings.getString("email", "");
		final String password = settings.getString("password", "");
		if (email != null && !"".equals(email) && password != null
				&& !"".equals(password)) {
			Intent intent = new Intent(this, NaoLigoActivity.class);
			startActivity(intent);
		}

		btClose.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences settings = getSharedPreferences(
						Contants.PREFS_NAME, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("email", "");
				editor.putString("password", "");
				editor.commit();
				setResult(RESULT_OK);
				finish();
			}
		});

		btLogon.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				btLogon.setText(getString(R.string.logining));
				btLogon.setEnabled(false);

				SoapObject request = new SoapObject(Contants.NAMESPACE,
						Contants.METHOD_NAME_LOGIN);

				request.addProperty("email", editemail.getText().toString());
				request.addProperty("senha", editpassword.getText().toString());

				SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
						SoapEnvelope.VER11);

				envelope.setOutputSoapObject(request);
				HttpTransportSE androidHttpTransport = new HttpTransportSE(
						Contants.URL);

				try {
					androidHttpTransport.call(Contants.SOAP_ACTION_LOGIN,
							envelope);

					String resultsRequestSOAP = (String) envelope.getResponse();

					if ("ok".equals(resultsRequestSOAP)) {
						SharedPreferences settings = getSharedPreferences(
								Contants.PREFS_NAME, 0);
						SharedPreferences.Editor editor = settings.edit();
						editor.putString("email", editemail.getText()
								.toString());
						editor.putString("password", editpassword.getText()
								.toString());
						editor.commit();
						Intent intent = new Intent(v.getContext(),
								NaoLigoActivity.class);
						startActivity(intent);
					} else {
						showDialog(resultsRequestSOAP);
					}
				} catch (Exception e) {
					showDialog(e.getMessage());
				} finally {
					btLogon.setText(getString(R.string.login));
					btLogon.setEnabled(true);
				}
			}
		});
	}

	private void showDialog(String message) {
		final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(getString(R.string.app_name));
		alertDialog.setMessage(message);
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				alertDialog.hide();
			}
		});
		alertDialog.setIcon(R.drawable.icon);
		alertDialog.show();
	}
}
