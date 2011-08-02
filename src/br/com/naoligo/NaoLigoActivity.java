package br.com.naoligo;

import java.io.IOException;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class NaoLigoActivity extends Activity {

	private static final int CONTACT_PICKER_RESULT = 1001;

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
		final ImageButton btSelectContact = (ImageButton) findViewById(R.id.btnSelectContact);

		btSend.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				btSend.setText(getString(R.string.sending));
				btSend.setEnabled(false);

				SoapObject request = new SoapObject(Contants.NAMESPACE,
						Contants.METHOD_NAME_SEND_SMS);

				EditText editphone = (EditText) findViewById(R.id.editPhone);
				EditText editmessage = (EditText) findViewById(R.id.editMessage);

				String tmp = unformatPhone(editphone.getText().toString());
				String ddd = "11";
				String phone = tmp;
				if (tmp.length() > 2) {
					ddd = tmp.substring(0, 2);
					phone = tmp.substring(2, tmp.length());
				}
				request.addProperty("email", email);
				request.addProperty("senha", password);
				request.addProperty("ddd", ddd);
				request.addProperty("telefone", phone);
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
				} catch (IOException e) {
					showDialog("Serviço temporariamente indisponível");
				} catch (Exception e) {
					showDialog(e.getMessage());
				} finally {
					btSend.setText(getString(R.string.send));
					btSend.setEnabled(true);
				}
			}
		});

		btSelectContact.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
						ContactsContract.Contacts.CONTENT_URI);
				contactPickerIntent
						.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
				startActivityForResult(contactPickerIntent,
						CONTACT_PICKER_RESULT);
			}
		});

		btCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_OK);
				finish();
			}
		});
	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case CONTACT_PICKER_RESULT:
				Cursor cursor = managedQuery(intent.getData(), null, null,
						null, null);
				while (cursor.moveToNext()) {
					String phoneNumber = cursor
							.getString(cursor
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

					EditText editphone = (EditText) findViewById(R.id.editPhone);
					editphone.setText(formatPhone(phoneNumber));
					break;
				}
				cursor.close();
			}
		} else {
			// gracefully handle failure
			Log.w("DEBUG_TAG", "Warning: activity result not ok");
		}
	}

	private String unformatPhone(String phoneNumber) {
		if (phoneNumber != null) {
			phoneNumber = phoneNumber.replaceAll("\\+55", "");
			if (phoneNumber.startsWith("0")) {
				phoneNumber = phoneNumber.replaceFirst("0", "");
			}
			phoneNumber = phoneNumber.replaceAll("-", "");
			phoneNumber = phoneNumber.replaceAll("\\(", "");
			phoneNumber = phoneNumber.replaceAll("\\)", "");
			phoneNumber = phoneNumber.replaceAll(" ", "");
			phoneNumber = phoneNumber.trim();
		}
		return phoneNumber;
	}

	private String formatPhone(String phoneNumber) {
		if (phoneNumber != null) {
			try {
				phoneNumber = unformatPhone(phoneNumber);

				String ddd = "11";
				if (phoneNumber.length() == 13) {
					ddd = phoneNumber.substring(2, 4);
					phoneNumber = phoneNumber
							.substring(4, phoneNumber.length());
				} else if (phoneNumber.length() == 12) {
					ddd = phoneNumber.substring(2, 4);
					phoneNumber = phoneNumber
							.substring(4, phoneNumber.length());
				} else if (phoneNumber.length() == 10
						|| phoneNumber.length() == 11) {
					ddd = phoneNumber.substring(0, 2);
					phoneNumber = phoneNumber
							.substring(2, phoneNumber.length());
				}

				phoneNumber = phoneNumber
						.substring(0, phoneNumber.length() - 4)
						+ "-"
						+ phoneNumber.substring(phoneNumber.length() - 4,
								phoneNumber.length());
				phoneNumber = "(" + ddd + ") " + phoneNumber;
			} catch (Exception e) {
			}
		}
		return phoneNumber;
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