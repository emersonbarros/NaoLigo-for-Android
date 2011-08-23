package br.com.naoligo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class NaoLigoActivity extends BaseActivity {

	private static final int CONTACT_PICKER_RESULT = 1001;


	private NaoLigoActivity naoLigoActivity = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		naoLigoActivity = this;
		
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		final Button btSend = (Button) findViewById(R.id.btnSend);
		final ImageButton btSelectContact = (ImageButton) findViewById(R.id.btnSelectContact);

		SharedPreferences settings = getSharedPreferences(Contants.PREFS_NAME, 0);
		final String email = settings.getString("email", "");
		final String password = settings.getString("password", "");

		if ("".equals(email) || "".equals(password)) {
			Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
			startActivity(intent);
		}

		btSend.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				EditText editphone = (EditText) findViewById(R.id.editPhone);
				EditText editmessage = (EditText) findViewById(R.id.editMessage);

				String tmp = unformatPhone(editphone.getText().toString());
				String ddd = "11";
				String phone = tmp;
				if (tmp.length() > 2) {
					ddd = tmp.substring(0, 2);
					phone = tmp.substring(2, tmp.length());
				}
				String[][] params = { { "email", email }, { "senha", password }, { "ddd", ddd }, { "telefone", phone },
						{ "texto", editmessage.getText().toString() } };

				CallWebServiceTask task = new CallWebServiceTask(naoLigoActivity, Contants.METHOD_NAME_SEND_SMS);
				task.execute(params);

			}
		});

		btSelectContact.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
				contactPickerIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
				startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
			}
		});
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case CONTACT_PICKER_RESULT:
				try {
					Cursor cursor = managedQuery(intent.getData(), null, null, null, null);
					while (cursor.moveToNext()) {
						String phoneNumber = cursor.getString(cursor
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

						EditText editphone = (EditText) findViewById(R.id.editPhone);
						editphone.setText(formatPhone(phoneNumber));
						break;
					}
					cursor.close();
				} catch (Exception e) {
				}
			}
		} else {
			// gracefully handle failure
			Log.w("DEBUG_TAG", "Warning: activity result not ok");
		}
	}

	private String unformatPhone(String phoneNumber) {
		if (phoneNumber != null) {
			while (phoneNumber.indexOf("+") > -1) {
				phoneNumber = phoneNumber.replace("+55", "");
			}
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
					phoneNumber = phoneNumber.substring(4, phoneNumber.length());
				} else if (phoneNumber.length() == 12) {
					ddd = phoneNumber.substring(2, 4);
					phoneNumber = phoneNumber.substring(4, phoneNumber.length());
				} else if (phoneNumber.length() == 10 || phoneNumber.length() == 11) {
					ddd = phoneNumber.substring(0, 2);
					phoneNumber = phoneNumber.substring(2, phoneNumber.length());
				}

				phoneNumber = phoneNumber.substring(0, phoneNumber.length() - 4) + "-"
						+ phoneNumber.substring(phoneNumber.length() - 4, phoneNumber.length());
				phoneNumber = "(" + ddd + ") " + phoneNumber;
			} catch (Exception e) {
			}
		}
		return phoneNumber;
	}

	private final int INFORMATION = 0;
	private final int PREFERENCES = 1;
	private final int LOGOUT = 2;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// Create and add new menu items.
		//MenuItem itemInformation = menu.add(0, INFORMATION, Menu.NONE, R.string.information);
		//MenuItem itemPreferences = menu.add(0, PREFERENCES, Menu.NONE, R.string.preferences);
		MenuItem itemLogout = menu.add(0, LOGOUT, Menu.NONE, R.string.logout);

		// Assign icons
		//itemInformation.setIcon(R.drawable.ic_menu_info_details);
		//itemPreferences.setIcon(R.drawable.ic_menu_preferences);
		itemLogout.setIcon(R.drawable.ic_menu_logout);

		// Allocate shortcuts to each of them.
		//itemInformation.setShortcut('0', 'a');
		//itemPreferences.setShortcut('1', 'r');
		itemLogout.setShortcut('2', 'r');

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case INFORMATION:
			return true;
		case PREFERENCES:
			Intent intent = new Intent(this, Preferences.class);
			startActivity(intent);
			return true;
		case LOGOUT:
			setResult(RESULT_OK);
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}