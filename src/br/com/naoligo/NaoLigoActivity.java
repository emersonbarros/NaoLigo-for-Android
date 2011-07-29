package br.com.naoligo;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class NaoLigoActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		SharedPreferences settings = getSharedPreferences(Contants.PREFS_NAME,
				0);
		final String email = settings.getString("email", "");
		final String password = settings.getString("password", "");

		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		final Button btSend = (Button) findViewById(R.id.btnSend);
		final Button btCancel = (Button) findViewById(R.id.btnCancel);

		btSend.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				btSend.setText(getString(R.string.sending));
				btSend.setEnabled(false);

				SoapObject request = new SoapObject(Contants.NAMESPACE,
						Contants.METHOD_NAME_SEND_SMS);

				EditText editddd = (EditText) findViewById(R.id.editDdd);
				EditText editphone = (EditText) findViewById(R.id.editPhone);
				EditText editmessage = (EditText) findViewById(R.id.editMessage);

				request.addProperty("email", email);
				request.addProperty("senha", password);
				request.addProperty("ddd", editddd.getText().toString());
				request.addProperty("telefone", editphone.getText().toString());
				request.addProperty("texto", editmessage.getText().toString());

				SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
						SoapEnvelope.VER11);

				envelope.setOutputSoapObject(request);
				HttpTransportSE androidHttpTransport = new HttpTransportSE(
						Contants.URL);

				try {
					androidHttpTransport.call(Contants.SOAP_ACTION_SEND_SMS,
							envelope);

					String resultsRequestSOAP = (String) envelope.getResponse();

					showDialog(resultsRequestSOAP);
				} catch (Exception e) {
					showDialog(e.getMessage());
				} finally {
					btSend.setText(getString(R.string.send));
					btSend.setEnabled(true);
				}
			}
		});

		btCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_OK);
				finish();
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