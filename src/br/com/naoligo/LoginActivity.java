package br.com.naoligo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends BaseActivity {

	private LoginActivity loginActivity = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		loginActivity = this;
		
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login);

		final Button btLogon = (Button) this.findViewById(R.id.btnLogin);
		final Button btClose = (Button) this.findViewById(R.id.btnClose);
		final EditText editemail = (EditText) this.findViewById(R.id.editEmail);
		final EditText editpassword = (EditText) this.findViewById(R.id.editPassword);

		btClose.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences settings = getSharedPreferences(Contants.PREFS_NAME, 0);
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
				String email = editemail.getText().toString();
				String password = editpassword.getText().toString();

				if (email.trim().equals("")) {
					showDialog(loginActivity, "Erro! email não informado");
					return;
				}

				if (password.trim().equals("")) {
					showDialog(loginActivity, "Erro! senha não informada");
					return;
				}

				CallWebServiceTask task = new CallWebServiceTask(loginActivity, Contants.METHOD_NAME_LOGIN);

				String[][] params = { { "email", email }, { "senha", password } };

				task.execute(params);
			}

		});
	}

	public void showMain() {
		Intent intent = new Intent(this, NaoLigoActivity.class);
		startActivity(intent);
	}
}
