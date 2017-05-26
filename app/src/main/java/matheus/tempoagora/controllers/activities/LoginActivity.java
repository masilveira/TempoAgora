package matheus.tempoagora.controllers.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONObject;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import matheus.tempoagora.R;
import matheus.tempoagora.controllers.services.ConnectionStatusService;

public class LoginActivity extends Activity {
    CallbackManager callbackManager;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        callbackManager = CallbackManager.Factory.create();
        final AccessToken accessToken = AccessToken.getCurrentAccessToken();
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_container);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                R.color.colorAccent,
                R.color.colorPrimaryDark,
                R.color.colorPrimaryDark);
        if(accessToken !=null && ConnectionStatusService.IsOnline(this)){
            getFacebookData(accessToken);
            loginButton.setVisibility(View.GONE);
            swipeRefreshLayout.setEnabled(false);
        }
        else {
            if (accessToken != null) {
                Crouton.makeText(LoginActivity.this, R.string.error_no_connection, Style.ALERT).show();
                swipeRefreshLayout.setEnabled(true);
                swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        getFacebookData(accessToken);
                    }
                });
                loginButton.setVisibility(View.GONE);
            } else {
                loginButton.setVisibility(View.VISIBLE);
                loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        getFacebookData(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onError(FacebookException error) {
                    }
                });
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
    private void getFacebookData(final AccessToken accessToken)
    {
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try {
                            Intent intent = new Intent(LoginActivity.this, WeatherActivity.class);
                            intent.putExtra("firstName", response.getJSONObject().getString("first_name"));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                            swipeRefreshLayout.setRefreshing(false);
                            Crouton.makeText(LoginActivity.this, R.string.error_no_connection, Style.ALERT).show();

                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,email,first_name,last_name,gender");
        request.setParameters(parameters);
        request.executeAsync();
    }
}
