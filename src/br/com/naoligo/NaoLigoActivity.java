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

				try {
					String tmp = formatPhone(editphone.getText().toString());
					editphone.setText(tmp);
					String ddd = tmp.substring(0, 2);
					String phone = tmp.substring(2, tmp.length());

					request.addProperty("email", email);
					request.addProperty("senha", password);
					request.addProperty("ddd", ddd);
					request.addProperty("telefone", phone);
					request.addProperty("texto", editmessage.getText()
							.toString());

					SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
							SoapEnvelope.VER11);

					envelope.setOutputSoapObject(request);
					HttpTransportSE androidHttpTransport = new HttpTransportSE(
							Contants.URL);

					try {
						androidHttpTransport.call(
								Contants.SOAP_ACTION_SEND_SMS, envelope);

						String resultsRequestSOAP = (String) envelope
								.getResponse();

						showDialog(resultsRequestSOAP);
					} catch (Exception e) {
						showDialog(e.getMessage());
					} finally {
						btSend.setText(getString(R.string.send));
						btSend.setEnabled(true);
					}
				} catch (Exception e) {
					showDialog("O número do telefone deve estar no formato: ddd+telefone [1199990000]");
				}
			}
		});

		btSelectContact.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
						ContactsContract.Contacts.CONTENT_URI);
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
					String contactId = cursor.getString(cursor
							.getColumnIndex(ContactsContract.Contacts._ID));

					String hasPhone = cursor
							.getString(cursor
									.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

					if (hasPhone.equalsIgnoreCase("1")) {
						Cursor phones = getContentResolver()
								.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
										null,
										ContactsContract.CommonDataKinds.Phone.CONTACT_ID
												+ " = " + contactId, null, null);
						if (phones.moveToNext()) {
							String phoneNumber = phones
									.getString(phones
											.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

							EditText editphone = (EditText) findViewById(R.id.editPhone);
							editphone.setText(formatPhone(phoneNumber));
						}
						phones.close();
					}
				}
				break;
			}

		} else {
			// gracefully handle failure
			Log.w("DEBUG_TAG", "Warning: activity result not ok");
		}
	}

	private String formatPhone(String phoneNumber) {
		if (phoneNumber != null) {
			phoneNumber = phoneNumber.replaceAll("\\+55", "");
			if (phoneNumber.startsWith("0")) {
				phoneNumber = phoneNumber.replaceFirst("0", "");
			}
			if (phoneNumber.length() < 10) {
				phoneNumber = "11" + phoneNumber;
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